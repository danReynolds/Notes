import java.util.*;
import java.math.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class WLPPParse {
  public static void main(String[] args) {
    new WLPPParse().run();
  }
  ArrayList<String> inputLinesEquiv =  new ArrayList<String>(); //like /,(),return,etc

  private void run() {
    Scanner in = new Scanner(System.in);
    List<Transition> table = new ArrayList<Transition>();
    ArrayList<String> productionRules =  new ArrayList<String>();
    ArrayList<String> printLines =  new ArrayList<String>();
    ArrayList<String> inputLines =  new ArrayList<String>();

    try {
      BufferedReader br = new BufferedReader(new FileReader("sample2.lr1"));
        // read the number of terminals and move to the next line
      int numTerm = Integer.parseInt(br.readLine());

      // skip the lines containing the terminals
      for (int i = 0; i < numTerm; i++) {
        String x = br.readLine();
        terms.add(x);
      }
      // read the number of non-terminals and move to the next line
      int numNonTerm = Integer.parseInt(br.readLine());

      // skip the lines containing the non-terminals
      for (int i = 0; i < numNonTerm; i++) {
        String x = br.readLine();
        nonterms.add(x);
      }      

      // skip the line containing the start symbol
        start = br.readLine();

      int ruleCount = Integer.parseInt(br.readLine());

      for (int x = 0; x < ruleCount; x++) {
        String rule = br.readLine();
        productionRules.add(rule);
        prods.add(rule);
      }
      int transitionCount = Integer.parseInt(br.readLine()); //skip state count
      transitionCount = Integer.parseInt(br.readLine());

      for (int x = 0; x < transitionCount; x++) {
        String currentLine = br.readLine();
        if (currentLine.indexOf("shift") != -1) {
          String lineTokens[] = currentLine.split(" ");
          table.add(new Transition(Integer.parseInt(lineTokens[0]), lineTokens[1],Integer.parseInt(lineTokens[3]), "SHIFT"));
        }
        else if (currentLine.indexOf("reduce") != -1) {
          String lineTokens[] = currentLine.split(" ");
          table.add(new Transition(Integer.parseInt(lineTokens[0]), lineTokens[1],Integer.parseInt(lineTokens[3]), "REDUCE"));
        }
      }
      br.close();

    } catch(Exception e) {
      System.out.println("broke");
    }
    inputLines.add("BOF");
    inputLinesEquiv.add("BOF");

    while (in.hasNextLine()) {
      String[] input = in.nextLine().split(" ");
      for (int q = 0; q < input.length; q+=2) {
        inputLines.add(input[q]);
        inputLinesEquiv.add(input[q+1]);
      }
    }
    inputLines.add("EOF");
    inputLinesEquiv.add("EOF");

    Stack<Integer> stateStack = new Stack<Integer>();
    Stack<String> symbolStack = new Stack<String>();  

    stateStack.push(0);

    for(int x = 0; x < inputLines.size(); x++) {
      Transition predicted;
      while (true) {
        predicted = findTransition(stateStack.peek(),inputLines.get(x),table);
        if (predicted == null) {
          System.err.println("ERROR at " + x);
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
          System.err.println("ERROR at " + x);
        else
          printLines.add(productionRules.get(predicted.toState));

        stateStack.push(goTo.toState);
      }
      //shift
      stateStack.push(predicted.toState);
      symbolStack.push(inputLines.get(x));
      
    }
    printLines.add(productionRules.get(stateStack.get(0)));
    // for (int z = 0; z < printLines.size(); z++)
    //   System.out.println(printLines.get(z));

    ////////////////////////////////////////////////
    // System.out.println(" ");
    Tree parsetree = lrdo(printLines); // read reverse rightmost derivation
        traverse(parsetree); // write forward leftmost derivation
    ////////////////////////////////////////////////

  }

  ////////////////////////////////////////////////////// BEGIN code to change from reverse right to forward left

  public class Tree {
    String rule;
    LinkedList<Tree> children = new LinkedList<Tree>();
  }
  Set<String> terms = new HashSet<String>(); // terminals
  Set<String> nonterms = new HashSet<String>(); // nonterminals
  Set<String> prods = new HashSet<String>(); // production rules
  String start; // start symbol

  //output leftmost derivation of tree t with indentation d
  public void traverse(Tree t) {
      // for(int i = 0; i < d; i++) System.out.print(" ");
      System.out.println(t.rule); // print root
      String[] ruleTokens = t.rule.split(" ");
      for (int x = 1; x < ruleTokens.length; x++) {
        if (terms.contains(ruleTokens[x])) {
          System.out.println(ruleTokens[x] + " " + inputLinesEquiv.get(0));
          inputLinesEquiv.remove(0);
        }
        else {
          traverse(t.children.getFirst());
          t.children.remove();
        }
      }
      // for(Tree c : t.children) {  // print all subtrees
      //     traverse(c);
      // }
  }

  // public void traverse(Tree t, int d) {
  //     for(int i = 0; i < d; i++) System.out.print(" ");
  //     System.out.println(t.rule); // print root
  //     for(Tree c : t.children) {  // print all subtrees
  //         traverse(c, d+1);
  //     }
  // }

  // Tree traverse(Tree t) {
  //     // for(int i = 0; i < d; i++) System.out.print(" ");
  //     System.out.println(t.rule); // print root
  //     String[] ruleTokens = t.rule.split(" ");
  //     Tree subtree = t.children.getFirst();
  //     for (int x = 1; x < ruleTokens.length; x++) {
  //       if (terms.contains(ruleTokens[x])) {
  //         System.out.println(ruleTokens[x]);
  //       }
  //       else {
  //         subtree = traverse(subtree);

  //       }
  //     }
  //     return subtree.children.getFirst();

  //     // for(Tree c : t.children) {  // print all subtrees
  //     //     traverse(c);
  //     // }
  // }

  // print elements of set h in .cfg file format
  public void dump(Set<String> h) {
      System.out.println(h.size());
      for(String s : h) {
          System.out.println(s);
      }
  }

  // pop rhs and accumulate subtress, push new tree node
  public void popper(Stack<Tree> stack, List<String> rhs, String rule) {
      Tree n = new Tree();
      n.rule = rule;
      for(String s : rhs) {
          n.children.addFirst(stack.pop());
      }
      stack.push(n);
  }

  // build tree from remaining input
  public Tree lrdo(ArrayList<String> printLines) {
      Stack<Tree> stack = new Stack<Tree>();
      String l; // lhs symbol
      int position = 0;
      do {
          String f = printLines.get(position);
          List<String> r = new ArrayList<String>(); // rhs symbols

          String[] tokens = f.split(" ");
          l = tokens[0]; // lhs symbol
          int x = 1;
          while(x < tokens.length) {
              String s = tokens[x++];
              if(nonterms.contains(s)) r.add(s); // only non-terminals
          }
          popper(stack, r, f); // reduce rule
          position++;
      } while(!start.equals(l));
      return stack.peek();
  }
  ////////////////////////////////////////////////////// END of code to change from reverse right to forward left


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
}
