// Returns the number of active blocks are in the planner buffer.
uint8_t plan_get_block_buffer_count()
{
  if (block_buffer_head >= block_buffer_tail) { return(block_buffer_head-block_buffer_tail); }
  return(BLOCK_BUFFER_SIZE - (block_buffer_tail-block_buffer_head));
}