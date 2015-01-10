import java.util.*;
import java.math.*;

public class Galaxy2 {
  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    skipGrammar(in);
    String nextLine[];
    String derivation = "";
    int total = 0;

    if(in.hasNextLine()) {
      derivation = in.nextLine().trim();
    }

    while(in.hasNextLine()) {
      String currentLine = in.nextLine().trim();
      nextLine = currentLine.split(" ", 2);
      derivation = derivation.replaceFirst(nextLine[0], nextLine[1]);
      // System.out.println(derivation);
    }
    derivation = derivation.replace("id","42");
    while (derivation.contains("(")) {
      total = calcSum(derivation);
      int results[] = bracketPositions(derivation);
      derivation = total + " " + derivation.substring(results[1]+1,derivation.length());
      // System.out.println("loop: " + derivation);
    }
    System.out.println(total);

  }

  private static int calcSum(String segment) {
    int total = 0;
    int sum = 0;
    if (segment.contains("(")) {
      int result[] = bracketPositions(segment);
      // System.out.println("before replace: " + segment);
      int subtotal = calcSum(segment.substring(result[0]+1,result[1]));
      // System.out.println("after replace: " + segment);
      segment = segment.replace(segment.substring(result[0],result[1]+1), "integer" + Integer.toString(subtotal));
      // System.out.println("after replace2: " + segment);

      String substringTokens[] = segment.split(" ");

      int sign = 1;
      for (int x = 0; x < substringTokens.length; x++) {
        if (substringTokens[x].equals("-"))
          sign *= -1;
        else if (substringTokens[x].equals("42")) {
          total += (42)*sign;
          sign = 1;
        }
        else if (substringTokens[x].startsWith("integer")) {
          int integer = Integer.parseInt(substringTokens[x].substring(7,substringTokens[x].length()));
          total += (integer)*sign;
          sign = 1;
        }
        else if (substringTokens[x].equals("(")) {
          return total;
        }
      }

    }
    else {
      // System.out.println(segment);

      String substringTokens[] = segment.split(" ");
      int sign = 1;
      for (int x = 0; x < substringTokens.length; x++) {
        if (substringTokens[x].equals("-"))
          sign *= -1;
        else if (substringTokens[x].equals("42")) {
          total += (42)*sign;
          sign = 1;
        }
        else if (substringTokens[x].startsWith("integer")) {
          int integer = Integer.parseInt(substringTokens[x].substring(7,substringTokens[x].length()));
          total += (integer)*sign;
          sign = 1;
        }
      }
    }
    return total;
  }

  private static int[] bracketPositions(String derivation) {
    int bracketCount = 1;
    int begin = derivation.indexOf("(");
    int end = begin;
    while(bracketCount > 0) {
      end++;
      if (derivation.charAt(end) == '(' ) {
        bracketCount++;
      }
      else if (derivation.charAt(end) == ')' ) {
        bracketCount--;
      }
    }
    int result[] = {begin,end};
    return result;
  }

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
}


