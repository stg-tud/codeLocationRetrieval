void plan_reset()
{
  memset(&pl, 0, sizeof(planner_t)); // Clear planner struct
  block_buffer_tail = 0;
  block_buffer_head = 0; // Empty = tail
  next_buffer_head = 1; // plan_next_block_index(block_buffer_head)
  block_buffer_planned = 0; // = block_buffer_tail;
}