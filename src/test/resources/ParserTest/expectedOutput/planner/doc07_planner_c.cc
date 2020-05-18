float plan_get_exec_block_exit_speed()
{
  uint8_t block_index = plan_next_block_index(block_buffer_tail);
  if (block_index == block_buffer_head) { return( 0.0 ); }
  return( sqrt( block_buffer[block_index].entry_speed_sqr ) );
}