// Reset the planner position vectors. Called by the system abort/initialization routine.
void plan_sync_position()
{
  // TODO: For motor configurations not in the same coordinate frame as the machine position,
  // this function needs to be updated to accomodate the difference.
  uint8_t idx;
  for (idx=0; idx<N_AXIS; idx++) {
    #ifdef COREXY
     if (idx==X_AXIS) {
        pl.position[X_AXIS] = system_convert_corexy_to_x_axis_steps(sys.position);
      } else if (idx==Y_AXIS) {
        pl.position[Y_AXIS] = system_convert_corexy_to_y_axis_steps(sys.position);
      } else {
        pl.position[idx] = sys.position[idx];
      }
    #else
      pl.position[idx] = sys.position[idx];
    #endif
  }
}