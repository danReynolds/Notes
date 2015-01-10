#include <stdio.h>
#include <string.h>
#include <stdlib.h>

/**
 * Skip the grammar part of the input.
 *
 * @param in the file pointer for reading input
 */
void skipLine(FILE* in) {
  char buf[256];
  fgets(buf, 256, in);
}
 
void skipGrammar(FILE* in) {
  int i, numTerm, numNonTerm, numRules;

  // read the number of terminals and move to the next line
  fscanf(in, "%d", &numTerm);
  skipLine(in);
  
  // skip the lines containing the terminals
  for (i = 0; i < numTerm; i++) {
    skipLine(in);
  }
  
  // read the number of non-terminals and move to the next line
  fscanf(in, "%d", &numNonTerm);
  skipLine(in);
  
  // skip the lines containing the non-terminals
  for (i = 0; i < numNonTerm; i++) {
    skipLine(in);
  }
  
  // skip the line containing the start symbol
  skipLine(in);
  
  // read the number of rules and move to the next line
  fscanf(in, "%d", &numRules);
  skipLine(in);
  
  // skip the lines containing the production rules
  for (i = 0; i < numRules; i++) {
    skipLine(in);
  }
}

char* trim(char* str) {
  int i, len;
  char* result;

  len = strlen(str);
  if (str[len-1] == '\n'){
    str[--len] = '\0';
  }

  for (i=len-1; i>=0 && ' '==str[i]; --i)
    len--;
  str[i+1] = '\0';

  result = str;
  for (i=0; i<len && ' '==str[i]; ++i) {
    result = result+1;
  }
  
  return result;
}

/**
 * Prints the derivation with whitespace trimmed.
 *
 * @param in the file pointer for reading input
 */
void printDerivation(FILE *in) {
  char line[256];
  int lineSize;

  while (1) {
    fgets(line, 256, in);
    if (feof(in)) break;
    printf ("%s\n", trim(line));
  }

}

/**
 * Reads a .cfg file and prints the left-canonical
 * derivation without leading or trailing spaces.
 */
int main(int argc, char** argv) {
  skipGrammar(stdin);
  printDerivation(stdin);
  return 0;
}
