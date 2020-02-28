void plan_discard_current_block()
{
  if (block_buffer_head != block_buffer_tail) { // Discard non-empty buffer.
    uint8_t block_index = plan_next_block_index( block_buffer_tail );
    // Push block_buffer_planned pointer, if encountered.
    if (block_buffer_tail == block_buffer_planned) { block_buffer_planned = block_index; }
    block_buffer_tail = block_index;
  }
}