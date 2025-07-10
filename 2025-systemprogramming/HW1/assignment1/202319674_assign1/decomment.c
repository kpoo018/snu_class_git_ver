#include <stdio.h>
#include <assert.h>
#include <stdlib.h>

/* This is skeleton code for reading characters from 
standard input (e.g., a file or console input) one by one until 
the end of the file (EOF) is reached. It keeps track of the current 
line number and is designed to be extended with additional 
functionality, such as processing or transforming the input data. 
In this specific task, the goal is to implement logic that removes 
C-style comments from the input. */

enum state {SINGLE_QUOTE, SQ_BACK_SLASH, DOUBLE_QOUTE, DQ_BACK_SLASH, LINE_COMMENT, BLOCK_COMMENT, POTENTIAL_COMMENT, END_BLOCK_COMMENT, NORMAL};
//SQ_BACK_SLASH: back slash in single quote
//DQ_BACK_SLASH: back slash in double quote

int main(void)
{
  // ich: int type variable to store character input from getchar() (abbreviation of int character)
  int ich;
  // line_cur & line_com: current line number and comment line number (abbreviation of current line and comment line)
  int line_cur, line_com;
  // ch: character that comes from casting (char) on ich (abbreviation of character)
  char ch;
  // state: state of the program (abbreviation of state)
  enum state state = NORMAL;

  line_cur = 1;
  line_com = -1;

  // This while loop reads all characters from standard input one by one
  while (1) {

    ich = getchar();
    if (ich == EOF) 
      break;

    ch = (char)ich;
    // TODO: Implement the decommenting logic

    switch (state){

      case NORMAL:
        //detects single quote
        if (ch == '\''){  
          state = SINGLE_QUOTE;
          putchar(ch);
        }
        //detects double quote
        else if (ch == '\"'){
          state = DOUBLE_QOUTE;
          putchar(ch);
        }
        //detects '/' (potential comment)
        else if (ch == '/')
          state = POTENTIAL_COMMENT;
        else
          putchar(ch);
        break;

      case SINGLE_QUOTE:
        //if detects single quote, goes back to normal state
        if (ch == '\'')
          state = NORMAL;
        //if detects back slash, goes to sq back slash state
        else if (ch == '\\')
          state = SQ_BACK_SLASH;
        putchar(ch);
        break;

      case SQ_BACK_SLASH:
        //after the back slash, it is the normal character
        putchar(ch);
        state = SINGLE_QUOTE;
        break;

      case DOUBLE_QOUTE:
        //if detects double quote, goes back to normal state
        if (ch == '\"')
          state = NORMAL;
        //if detects back slash, goes to dq back slash state
        else if (ch == '\\')
          state = DQ_BACK_SLASH;
        putchar(ch);
        break;

      case DQ_BACK_SLASH:
        //after the back slash, it is the normal character
        putchar(ch);
        state = DOUBLE_QOUTE;
        break;

      case POTENTIAL_COMMENT:
        //if detects another '/', it is a line comment
        if (ch == '/'){
          state = LINE_COMMENT;
          putchar(' ');
        }
        //if detects '*', it is a block comment
        else if (ch == '*'){
          state = BLOCK_COMMENT;
          line_com = line_cur;    // if EOF is detected, before the end of the comment, line_com will be used to print the error message.
          putchar(' ');
        }
        else {
          state = NORMAL;
          putchar('/');
          putchar(ch);
        }
        break;

      case LINE_COMMENT:
        //if detects '\n', goes back to normal state
        if (ch == '\n'){
          state = NORMAL;
          putchar('\n');
        }
        break;

      case BLOCK_COMMENT:
        //if detects '*', goes to end block comment state
        if (ch == '*')
          state = END_BLOCK_COMMENT;
        else if (ch == '\n')
          putchar('\n');
        break; 

      case END_BLOCK_COMMENT:
        //if detects '/', goes back to normal state
        if (ch == '/')
          state = NORMAL;
        //if detects '*', stays in end block comment state
        else if (ch == '*')
          state = END_BLOCK_COMMENT;
        //if detects others, goes back to block comment state
        else if (ch == '\n'){
          putchar('\n');
          state = BLOCK_COMMENT;
        }
        else
          state = BLOCK_COMMENT;
        break;

      default:
        assert(0);
        break;
    }
  
    if (ch == '\n')
      line_cur++;

  }


  //if EOF is detected, check the state of the program
  if (ich == EOF) {
    switch (state){  

      case NORMAL:
      case SINGLE_QUOTE:
      case SQ_BACK_SLASH:
      case DOUBLE_QOUTE:
      case DQ_BACK_SLASH: 
      case LINE_COMMENT:
        break;

    //if EOF is detected, but the comment is not terminated, print the error message
      case BLOCK_COMMENT:
      case END_BLOCK_COMMENT:
        fprintf(stderr, "Error: line %d: unterminated comment\n", line_com);
        return(EXIT_FAILURE);
        break;

      case POTENTIAL_COMMENT:
        putchar('/');
        break;
    
      default:
        assert(0);
        break;
    }

    
    
  }
  
  return(EXIT_SUCCESS);
}
