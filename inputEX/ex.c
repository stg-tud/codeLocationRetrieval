void parse_command(char* input) {
  axis_command = NULL;
  if (input[0] == 'G') {
    unit = parse_unit(input); // mm or inches
    axis_command = parse_axis_command(input);
    move_x(axis_command);
    move_y(axis_command);
    #ifdef Z_ENABLED
      move_z(axis_command);
    #endif
  } else if (input[0] == 'H'){
    home_axes();
  } else { FAIL(UNSUPPORTED_COMMAND)  }
  if (axis_command) {...}
}
