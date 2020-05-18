/*
  planner.c - buffers movement commands and manages the acceleration profile plan
  Part of Grbl

  Copyright (c) 2011-2015 Sungeun K. Jeon
  Copyright (c) 2009-2011 Simen Svale Skogsrud
  Copyright (c) 2011 Jens Geisler

  Grbl is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Grbl is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Grbl.  If not, see <http://www.gnu.org/licenses/>.
*/

#include "grbl.h"

#define SOME_LARGE_VALUE 1.0E+38 // Used by rapids and acceleration maximization calculations. Just needs
                                 // to be larger than any feasible (mm/min)^2 or mm/sec^2 value.

static plan_block_t block_buffer[BLOCK_BUFFER_SIZE];  // A ring buffer for motion instructions
static uint8_t block_buffer_tail;     // Index of the block to process now
static uint8_t block_buffer_head;     // Index of the next block to be pushed
static uint8_t next_buffer_head;      // Index of the next buffer head
static uint8_t block_buffer_planned;  // Index of the optimally planned block

// Define planner variables
typedef struct {
  int32_t position[N_AXIS];          // The planner position of the tool in absolute steps. Kept separate
                                     // from g-code position for movements requiring multiple line motions,
                                     // i.e. arcs, canned cycles, and backlash compensation.
  float previous_unit_vec[N_AXIS];   // Unit vector of previous path line segment
  float previous_nominal_speed_sqr;  // Nominal speed of previous path line segment
}