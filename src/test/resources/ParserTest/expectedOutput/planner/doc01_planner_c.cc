planner_t;
static planner_t pl;


// Returns the index of the next block in the ring buffer. Also called by stepper segment buffer.
uint8_t plan_next_block_index(uint8_t block_index)
{
  block_index++;
  if (block_index == BLOCK_BUFFER_SIZE) { block_index = 0; }
  return(block_index);
}