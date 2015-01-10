import java.util.*;

/**
 * A simple class to read a .cfg file and print the left-canonical
 * derivation without leading or trailing spaces.
 *
 * @version 071024.0
 */
public class DerivationPrinter {
  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    skipGrammar(in);
    printDerivation(in);
  }

  /**
   * Skip the grammar part of the input.
   *
   * @param in the scanner for reading input
   */
  private static void skipGrammar(Scanner in) {
    assert(in.hasNextInt());

    // read the number of terminals and move to the next line
    int numTerm = in.nextInt();
    in.nextLine();

    // skip the lines containing the terminals
    for (int i = 0; i < numTerm; i++) {
      in.nextLine();
    }

    // read the number of non-terminals and move to the next line
    int numNonTerm = in.nextInt();
    in.nextLine();

    // skip the lines containing the non-terminals
    for (int i = 0; i < numNonTerm; i++) {
      in.nextLine();
    }

    // skip the line containing the start symbol
    in.nextLine();

    // read the number of rules and move to the next line
    int numRules = in.nextInt();
    in.nextLine();

    // skip the lines containing the production rules
    for (int i = 0; i < numRules; i++) {
      in.nextLine();
    }
  }

  /**
   * Prints the derivation with whitespace trimmed.
   *
   * @param in the scanner for reading input
   */
  private static void printDerivation(Scanner in) {
    while (in.hasNextLine()) {
      System.out.println(in.nextLine().trim());
    }
  }
}
