plan_block_t *plan_get_current_block()
{
  if (block_buffer_head == block_buffer_tail) { return(NULL); } // Buffer empty
  return(&block_buffer[block_buffer_tail]);
}