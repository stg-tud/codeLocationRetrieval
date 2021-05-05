void parse_command(char* input) {
  axis_command = NULL;
  if (input[0] == 'G') {
    // mm or inches
    unit = parse_unit(input);
    axis_command =
      parse_axis_command(input);
    mode = 0;
    move_x(axis_command);
    move_y(axis_command);
  } else if (input[0] == 'H'){
    coolant();
  } else { FAIL(UNSUPPORTED_COMMAND);  }
  if (axis_command) {
    do_command(mode);
  }
  return;
}