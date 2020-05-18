// Re-initialize buffer plan with a partially completed block, assumed to exist at the buffer tail.
// Called after a steppers have come to a complete stop for a feed hold and the cycle is stopped.
void plan_cycle_reinitialize()
{
  // Re-plan from a complete stop. Reset planner entry speeds and buffer planned pointer.
  st_update_plan_block_parameters();
  block_buffer_planned = block_buffer_tail;
  planner_recalculate();
}