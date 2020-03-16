/* Add a new linear movement to the buffer. target[N_AXIS] is the signed, absolute target position
   in millimeters. Feed rate specifies the speed of the motion. If feed rate is inverted, the feed
   rate is taken to mean "frequency" and would complete the operation in 1/feed_rate minutes.
   All position data passed to the planner must be in terms of machine position to keep the planner
   independent of any coordinate system changes and offsets, which are handled by the g-code parser.
   NOTE: Assumes buffer is available. Buffer checks are handled at a higher level by motion_control.
   In other words, the buffer head is never equal to the buffer tail.  Also the feed rate input value
   is used in three ways: as a normal feed rate if invert_feed_rate is false, as inverse time if
   invert_feed_rate is true, or as seek/rapids rate if the feed_rate value is negative (and
   invert_feed_rate always false). */
#ifdef USE_LINE_NUMBERS
  void plan_buffer_line(float *target, float feed_rate, uint8_t invert_feed_rate, int32_t line_number)
#else
  void plan_buffer_line(float *target, float feed_rate, uint8_t invert_feed_rate)
#endif
{
  // Prepare and initialize new block
  plan_block_t *block = &block_buffer[block_buffer_head];
  block->step_event_count = 0;
  block->millimeters = 0;
  block->direction_bits = 0;
  block->acceleration = SOME_LARGE_VALUE; // Scaled down to maximum acceleration later
  #ifdef USE_LINE_NUMBERS
    block->line_number = line_number;
  #endif

  // Compute and store initial move distance data.
  // TODO: After this for-loop, we don't touch the stepper algorithm data. Might be a good idea
  // to try to keep these types of things completely separate from the planner for portability.
  int32_t target_steps[N_AXIS];
  float unit_vec[N_AXIS], delta_mm;
  uint8_t idx;
  #ifdef COREXY
    target_steps[A_MOTOR] = lround(target[A_MOTOR]*settings.steps_per_mm[A_MOTOR]);
    target_steps[B_MOTOR] = lround(target[B_MOTOR]*settings.steps_per_mm[B_MOTOR]);
    block->steps[A_MOTOR] = labs((target_steps[X_AXIS]-pl.position[X_AXIS]) + (target_steps[Y_AXIS]-pl.position[Y_AXIS]));
    block->steps[B_MOTOR] = labs((target_steps[X_AXIS]-pl.position[X_AXIS]) - (target_steps[Y_AXIS]-pl.position[Y_AXIS]));
  #endif

  for (idx=0; idx<N_AXIS; idx++) {
    // Calculate target position in absolute steps, number of steps for each axis, and determine max step events.
    // Also, compute individual axes distance for move and prep unit vector calculations.
    // NOTE: Computes true distance from converted step values.
    #ifdef COREXY
      if ( !(idx == A_MOTOR) && !(idx == B_MOTOR) ) {
        target_steps[idx] = lround(target[idx]*settings.steps_per_mm[idx]);
        block->steps[idx] = labs(target_steps[idx]-pl.position[idx]);
      }
      block->step_event_count = max(block->step_event_count, block->steps[idx]);
      if (idx == A_MOTOR) {
        delta_mm = (target_steps[X_AXIS]-pl.position[X_AXIS] + target_steps[Y_AXIS]-pl.position[Y_AXIS])/settings.steps_per_mm[idx];
      } else if (idx == B_MOTOR) {
        delta_mm = (target_steps[X_AXIS]-pl.position[X_AXIS] - target_steps[Y_AXIS]+pl.position[Y_AXIS])/settings.steps_per_mm[idx];
      } else {
        delta_mm = (target_steps[idx] - pl.position[idx])/settings.steps_per_mm[idx];
      }
    #else
      target_steps[idx] = lround(target[idx]*settings.steps_per_mm[idx]);
      block->steps[idx] = labs(target_steps[idx]-pl.position[idx]);
      block->step_event_count = max(block->step_event_count, block->steps[idx]);
      delta_mm = (target_steps[idx] - pl.position[idx])/settings.steps_per_mm[idx];
    #endif
    unit_vec[idx] = delta_mm; // Store unit vector numerator. Denominator computed later.

    // Set direction bits. Bit enabled always means direction is negative.
    if (delta_mm < 0 ) { block->direction_bits |= get_direction_pin_mask(idx); }

    // Incrementally compute total move distance by Euclidean norm. First add square of each term.
    block->millimeters += delta_mm*delta_mm;
  }
  block->millimeters = sqrt(block->millimeters); // Complete millimeters calculation with sqrt()

  // Bail if this is a zero-length block. Highly unlikely to occur.
  if (block->step_event_count == 0) { return; }

  // Adjust feed_rate value to mm/min depending on type of rate input (normal, inverse time, or rapids)
  // TODO: Need to distinguish a rapids vs feed move for overrides. Some flag of some sort.
  if (feed_rate < 0) { feed_rate = SOME_LARGE_VALUE; } // Scaled down to absolute max/rapids rate later
  else if (invert_feed_rate) { feed_rate *= block->millimeters; }
  if (feed_rate < MINIMUM_FEED_RATE) { feed_rate = MINIMUM_FEED_RATE; } // Prevents step generation round-off condition.

  // Calculate the unit vector of the line move and the block maximum feed rate and acceleration scaled
  // down such that no individual axes maximum values are exceeded with respect to the line direction.
  // NOTE: This calculation assumes all axes are orthogonal (Cartesian) and works with ABC-axes,
  // if they are also orthogonal/independent. Operates on the absolute value of the unit vector.
  float inverse_unit_vec_value;
  float inverse_millimeters = 1.0/block->millimeters;  // Inverse millimeters to remove multiple float divides
  float junction_cos_theta = 0;
  for (idx=0; idx<N_AXIS; idx++) {
    if (unit_vec[idx] != 0) {  // Avoid divide by zero.
      unit_vec[idx] *= inverse_millimeters;  // Complete unit vector calculation
      inverse_unit_vec_value = fabs(1.0/unit_vec[idx]); // Inverse to remove multiple float divides.

      // Check and limit feed rate against max individual axis velocities and accelerations
      feed_rate = min(feed_rate,settings.max_rate[idx]*inverse_unit_vec_value);
      block->acceleration = min(block->acceleration,settings.acceleration[idx]*inverse_unit_vec_value);

      // Incrementally compute cosine of angle between previous and current path. Cos(theta) of the junction
      // between the current move and the previous move is simply the dot product of the two unit vectors,
      // where prev_unit_vec is negative. Used later to compute maximum junction speed.
      junction_cos_theta -= pl.previous_unit_vec[idx] * unit_vec[idx];
    }
  }

  // TODO: Need to check this method handling zero junction speeds when starting from rest.
  if (block_buffer_head == block_buffer_tail) {

    // Initialize block entry speed as zero. Assume it will be starting from rest. Planner will correct this later.
    block->entry_speed_sqr = 0.0;
    block->max_junction_speed_sqr = 0.0; // Starting from rest. Enforce start from zero velocity.

  } else {
    /*
       Compute maximum allowable entry speed at junction by centripetal acceleration approximation.
       Let a circle be tangent to both previous and current path line segments, where the junction
       deviation is defined as the distance from the junction to the closest edge of the circle,
       colinear with the circle center. The circular segment joining the two paths represents the
       path of centripetal acceleration. Solve for max velocity based on max acceleration about the
       radius of the circle, defined indirectly by junction deviation. This may be also viewed as
       path width or max_jerk in the previous Grbl version. This approach does not actually deviate
       from path, but used as a robust way to compute cornering speeds, as it takes into account the
       nonlinearities of both the junction angle and junction velocity.

       NOTE: If the junction deviation value is finite, Grbl executes the motions in an exact path
       mode (G61). If the junction deviation value is zero, Grbl will execute the motion in an exact
       stop mode (G61.1) manner. In the future, if continuous mode (G64) is desired, the math here
       is exactly the same. Instead of motioning all the way to junction point, the machine will
       just follow the arc circle defined here. The Arduino doesn't have the CPU cycles to perform
       a continuous mode path, but ARM-based microcontrollers most certainly do.

       NOTE: The max junction speed is a fixed value, since machine acceleration limits cannot be
       changed dynamically during operation nor can the line move geometry. This must be kept in
       memory in the event of a feedrate override changing the nominal speeds of blocks, which can
       change the overall maximum entry speed conditions of all blocks.
    */
    // NOTE: Computed without any expensive trig, sin() or acos(), by trig half angle identity of cos(theta).
    if (junction_cos_theta > 0.999999) {
      //  For a 0 degree acute junction, just set minimum junction speed.
      block->max_junction_speed_sqr = MINIMUM_JUNCTION_SPEED*MINIMUM_JUNCTION_SPEED;
    } else {
      junction_cos_theta = max(junction_cos_theta,-0.999999); // Check for numerical round-off to avoid divide by zero.
      float sin_theta_d2 = sqrt(0.5*(1.0-junction_cos_theta)); // Trig half angle identity. Always positive.

      // TODO: Technically, the acceleration used in calculation needs to be limited by the minimum of the
      // two junctions. However, this shouldn't be a significant problem except in extreme circumstances.
      block->max_junction_speed_sqr = max( MINIMUM_JUNCTION_SPEED*MINIMUM_JUNCTION_SPEED,
                                   (block->acceleration * settings.junction_deviation * sin_theta_d2)/(1.0-sin_theta_d2) );

    }
  }

  // Store block nominal speed
  block->nominal_speed_sqr = feed_rate*feed_rate; // (mm/min). Always > 0

  // Compute the junction maximum entry based on the minimum of the junction speed and neighboring nominal speeds.
  block->max_entry_speed_sqr = min(block->max_junction_speed_sqr,
                                   min(block->nominal_speed_sqr,pl.previous_nominal_speed_sqr));

  // Update previous path unit_vector and nominal speed (squared)
  memcpy(pl.previous_unit_vec, unit_vec, sizeof(unit_vec)); // pl.previous_unit_vec[] = unit_vec[]
  pl.previous_nominal_speed_sqr = block->nominal_speed_sqr;

  // Update planner position
  memcpy(pl.position, target_steps, sizeof(target_steps)); // pl.position[] = target_steps[]

  // New block is all set. Update buffer head and next buffer head indices.
  block_buffer_head = next_buffer_head;
  next_buffer_head = plan_next_block_index(block_buffer_head);

  // Finish up by recalculating the plan with the new block.
  planner_recalculate();
}