int parse_unit(char*);
int unit;
void* axis_command;
void* parse_axis_command(char*);
void move_x(void*);ove_y(void*);
void move_z(void*);
void home_axes();
#define NULL 0
#define UNSUPPORTED_COMMAND ""
#define FAIL(error) "";
#define FOO ""

void parse_command(char* input) {
  axis_command = NULL;
  int x;
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
  x = 9;
  if (axis_command) {
    FOO;
    y = x;
  }
}



void garbage() {
    axis;
}



void garbage2() {
    axis;
}