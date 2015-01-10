import java.util.*;
import java.math.*;

public class LROriginal {
  public static void main(String[] args) {
    new LROriginal().run();
  }

  private void run() {
    Scanner in = new Scanner(System.in);
    skipGrammar(in);
    ArrayList<String> lines =  new ArrayList<String>();
    List<Transition> table = new ArrayList<Transition>();
    ArrayList<String> productionRules =  new ArrayList<String>();
    ArrayList<String> printLines =  new ArrayList<String>();
    ArrayList<String> inputLines =  new ArrayList<String>();



    int ruleCount = Integer.parseInt(in.nextLine());

    for (int x = 0; x < ruleCount; x++) {
      productionRules.add(in.nextLine());
    }
    int transitionCount = Integer.parseInt(in.nextLine()); //skip state count
    transitionCount = Integer.parseInt(in.nextLine());

    for (int x = 0; x < transitionCount; x++) {
      String currentLine = in.nextLine();
      lines.add(currentLine);
      if (currentLine.indexOf("shift") != -1) {
        String lineTokens[] = currentLine.split(" ");
        table.add(new Transition(Integer.parseInt(lineTokens[0]), lineTokens[1],Integer.parseInt(lineTokens[3]), "SHIFT"));
      }
      else if (currentLine.indexOf("reduce") != -1) {
        String lineTokens[] = currentLine.split(" ");
        table.add(new Transition(Integer.parseInt(lineTokens[0]), lineTokens[1],Integer.parseInt(lineTokens[3]), "REDUCE"));
      }
    }
    while (in.hasNextLine()) {
      String[] input = in.nextLine().split(" ");
      for (int q = 0; q < input.length; q++)
        inputLines.add(input[q]);
    }

    Stack<Integer> stateStack = new Stack<Integer>();
    Stack<String> symbolStack = new Stack<String>();  

    stateStack.push(0);

    for(int x = 0; x < inputLines.size(); x++) {
      Transition predicted;
      while (true) {
        predicted = findTransition(stateStack.peek(),inputLines.get(x),table); //this is fine but put the reduces in the table first, so that can tell if need to reduce or if want to wait and reduce later
        //the lookahead just needs a condtion in the findTransition that it should only grab the reduce based on the next  symbol, just need to use it to determine if should wait for next or reduce immediately
        if (predicted == null) {
          System.err.println("ERROR at " + (x+1));
          System.exit(0);
        }
        else if (predicted.type.equals("SHIFT"))
          break;

        //do the reduce
        String[] productionRule = productionRules.get(predicted.toState).split(" ");

        for (int y = 0; y < productionRule.length - 1; y++) {
          symbolStack.pop();
          stateStack.pop();
        }
        symbolStack.push(productionRule[0]);
        Transition goTo = findTransition(stateStack.peek(),symbolStack.peek(), table);
        if (goTo == null)
          System.err.println("ERROR at " + (x+1));
        else
          printLines.add(productionRules.get(predicted.toState));

        stateStack.push(goTo.toState);
      }
      //shift
      stateStack.push(predicted.toState);
      symbolStack.push(inputLines.get(x));
      
    }
    printLines.add(productionRules.get(stateStack.get(0)));
    for (int z = 0; z < printLines.size(); z++)
      System.out.println(printLines.get(z));
  }

  private static enum State {
    START;
  }

  static Transition findTransition(int state, String edge, List<Transition> table) {
      for( int j = 0; j < table.size(); j++ ) {
          Transition t = table.get(j);
          if(t.fromState == state && t.edge.equals(edge)) {
              return t;
          }
      }
      return null;
  }

  private class Transition {
    int fromState;
    String edge;
    int toState;
    String type;
    Transition(int fromState, String edge, int toState, String type) {
      this.fromState = fromState;
      this.edge = edge;
      this.toState = toState;
      this.type = type;
    }
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

  }
}
