import java.util.*;

/**
 * Starter code for CS241 assignments 9-11 for Spring 2011.
 * 
 * Based on Scheme code by Gord Cormack. Java translation by Ondrej Lhotak.
 * 
 * Version 20081105.1
 *
 * Modified June 30, 2011 by Brad Lushman
 */
public class WLPPGen2 {
    Scanner in = new Scanner(System.in);

    // The set of terminal symbols in the WLPP grammar.
    Set<String> terminals = new HashSet<String>(Arrays.asList("BOF", "BECOMES", 
         "COMMA", "ELSE", "EOF", "EQ", "GE", "GT", "ID", "IF", "INT", "LBRACE", 
         "LE", "LPAREN", "LT", "MINUS", "NE", "NUM", "PCT", "PLUS", "PRINTLN",
         "RBRACE", "RETURN", "RPAREN", "SEMI", "SLASH", "STAR", "WAIN", "WHILE",
         "AMP", "LBRACK", "RBRACK", "NEW", "DELETE", "NULL"));

    class SymbolPair {
        public String type;
        public String id;
        public String tableItem;

        public SymbolPair(String type, String id) {
            this.type = type;
            this.id = id;
            tableItem = id + " " + type;
        }
    }

    List<SymbolPair> symbols = new ArrayList<SymbolPair>();

    String type; 


    // Data structure for storing the parse tree.
    public class Tree {
        List<String> rule;

        ArrayList<Tree> children = new ArrayList<Tree>();

        // Does this node's rule match otherRule?
        boolean matches(String otherRule) {
            return tokenize(otherRule).equals(rule);
        }
    }

    // Divide a string into a list of tokens.
    List<String> tokenize(String line) {
        List<String> ret = new ArrayList<String>();
        Scanner sc = new Scanner(line);
        while (sc.hasNext()) {
            ret.add(sc.next());
        }
        return ret;
    }

    // Read and return wlppi parse tree
    Tree readParse(String lhs, Boolean addToTable) {
        String line = in.nextLine();
        List<String> tokens = tokenize(line);
        Tree ret = new Tree();
        ret.rule = tokens;
        if (addToTable) {
            if (line.indexOf("INT STAR") != -1) {
                type = "int*";
            }
            else if (line.indexOf("INT") != -1) {
                type = "int";
            }
            else if (line.indexOf("ID") != -1) {
                String id = line.split(" ")[1];
                for (int x = 0; x < symbols.size(); x++) {
                    if (symbols.get(x).id.equals(id)) {
                        System.err.println("ERROR duplicate id " + id);
                        System.exit(0);
                    }
                }
                symbols.add(new SymbolPair(type,id));
            }
        }
        if (line.startsWith("ID")) {
            variableDeclaration(line.split(" ")[1]);
        }
        if (!terminals.contains(lhs)) {
            Scanner sc = new Scanner(line);
            String left = sc.next(); // discard lhs
            while (sc.hasNext()) {
                String s = sc.next();
                ret.children.add(readParse(s, left.equals("dcl")));
            }
        }
        return ret;
    }

    // Compute symbols defined in t
    List<String> genSymbols(Tree t) {
        return null;
    }
 

    // Print an error message and exit the program.
    void bail(String msg) {
        System.err.println("ERROR: " + msg);
        System.exit(0);
    }

    // Generate the code for the parse tree t.
    String genCode(Tree t) {
        return null;
    }

    //check variable definition
    void variableDeclaration(String id) {
        Boolean declared = false;
        for (int x = 0; x < symbols.size(); x++) {
            if (symbols.get(x).id.equals(id))
                declared = true;
        }
        if (!declared) {
            System.err.println("ERROR undefined variable " + id);
            System.exit(0);
        }
    }

    // Main program
    public static final void main(String args[]) {
        new WLPPGen2().go();
    }

    public void printSymbols() {
        for (int x = symbols.size()-1; x >= 0; x--) {
            System.err.println(symbols.get(x).tableItem);
        }
    }

    public void go() {
        Tree parseTree = readParse("S",false);
        printSymbols();
    }
}
