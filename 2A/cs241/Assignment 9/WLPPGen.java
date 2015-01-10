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
public class WLPPGen {
    Scanner in = new Scanner(System.in);

    // The set of terminal symbols in the WLPP grammar.
    Set<String> terminals = new HashSet<String>(Arrays.asList("BOF", "BECOMES", 
         "COMMA", "ELSE", "EOF", "EQ", "GE", "GT", "ID", "IF", "INT", "LBRACE", 
         "LE", "LPAREN", "LT", "MINUS", "NE", "NUM", "PCT", "PLUS", "PRINTLN",
         "RBRACE", "RETURN", "RPAREN", "SEMI", "SLASH", "STAR", "WAIN", "WHILE",
         "AMP", "LBRACK", "RBRACK", "NEW", "DELETE", "NULL"));

    ArrayList<String> reducibleExpressions =  new ArrayList<String>() {{
        add("[expr, expr, PLUS, term]");
        add("[term, term, PCT, factor]");
        add("[term, term, STAR, factor]");
        add("[term, term, SLASH, factor]");
        add("[expr, expr, MINUS, term]");
        add("[factor, LPAREN, expr, RPAREN]");
        add("[lvalue, LPAREN, lvalue, RPAREN]");
        add("[lvalue, STAR, factor]");
        add("[factor, AMP, lvalue]");
        add("[factor, STAR, factor]");
        add("[factor, NEW, INT, LBRACK, expr, RBRACK]");
        //p3
        add("[test, expr, EQ, expr]");
        add("[test, expr, NE, expr]");
        add("[test, expr, LE, expr]");
        add("[test, expr, LT, expr]");
        add("[test, expr, GE, expr]");
        add("[test, expr, GT, expr]");
        add("[statement, PRINTLN, LPAREN, expr, RPAREN, SEMI]");
        add("[statement, DELETE, LBRACK, RBRACK, expr, SEMI]");
        add("[statement, lvalue, BECOMES, expr, SEMI]");
        add("[dcl, type, ID]");
        add("[dcls, dcls, dcl, BECOMES, NULL, SEMI]");
        add("[dcls, dcls, dcl, BECOMES, NUM, SEMI]");
        add("[procedure, INT, WAIN, LPAREN, dcl, COMMA, dcl, RPAREN, LBRACE, dcls, statements, RETURN, expr, SEMI, RBRACE]");
    }};

    Hashtable<String, String> expressionToType = new Hashtable<String,String>() {{
        put("[int, PLUS, int]","int");
        put("[int*, PLUS, int]","int*");
        put("[int, PLUS, int*]","int*");
        put("[int, MINUS, int]","int");
        put("[int*, MINUS, int]","int*");
        put("[int*, MINUS, int*]","int");
        put("[int, SLASH, int]", "int");
        put("[int, PCT, int]", "int");
        put("[int, STAR, int]", "int");
        put("[LPAREN, int, RPAREN]","int");
        put("[LPAREN, int*, RPAREN]","int*");
        put("[AMP, int]","int*");
        put("[STAR, int*]","int");
        put("[NEW, INT, LBRACK, int, RBRACK]","int*");
        //p3
        put("[int, EQ, int]","equal");
        put("[int, NE, int]","equal");
        put("[int, GT, int]","equal");
        put("[int, GE, int]","equal");
        put("[int, LE, int]","equal");
        put("[int, LT, int]","equal");
        put("[int*, EQ, int*]","equal");
        put("[int*, NE, int*]","equal");
        put("[int*, GT, int*]","equal");
        put("[int*, GE, int*]","equal");
        put("[int*, LE, int*]","equal");
        put("[int*, LT, int*]","equal");
        put("[PRINTLN, LPAREN, int, RPAREN, SEMI]","printin");
        put("[DELETE, LBRACK, RBRACK, int*, SEMI]","deletinshit");
        put("[int, BECOMES, int, SEMI]","becominshit");
        put("[int*, BECOMES, int*, SEMI]","becominshit");
        put("[INT, int]","int");
        put("[INT, int*]","int*");
        put("[dcls, int, BECOMES, int, SEMI]","dcls");
        put("[dcls, int*, BECOMES, int*, SEMI]","dcls");
        put("[INT, WAIN, LPAREN, int, COMMA, int, RPAREN, LBRACE, dcls, statements, RETURN, int, SEMI, RBRACE]", "procedureRight");
        put("[INT, WAIN, LPAREN, int*, COMMA, int, RPAREN, LBRACE, dcls, statements, RETURN, int, SEMI, RBRACE]", "procedureRight");
    }};

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

        String traverse() {
            ArrayList<String> expr = new ArrayList<String>();

            // System.out.println("Printing Rule " + rule);

            if (rule.get(0).equals("ID")) {
                expr.add(variableType(rule.get(1)));
            }
            else if (rule.get(0).equals("NUM")) {
                expr.add("int");
            }
            else if (rule.get(0).equals("NULL")) {
                expr.add("int*");
            }

            for (int x = 0; x < children.size(); x++) {
                expr.add(children.get(x).traverse());
            }
            // // System.out.println("ohohooh " + rule);
            // // System.out.println("herrreee " + expr);

            for(int x = 0; x < reducibleExpressions.size(); x++) {
                if (reducibleExpressions.get(x).equals(rule.toString())) {
                    return evaluateType(expr, rule);
                }
            }

            if (expr.size() == 0) {
                // System.out.println("end");
                return rule.get(0);
            }
            // System.out.println("Returning " + expr.get(0) + " for rule " + rule + " and expr " + expr);

            return expr.get(0);
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
        else if (line.startsWith("ID")) {
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

    //return variable type
    String variableType(String id) {
        for (int x = 0; x < symbols.size(); x++) {
            if (symbols.get(x).id.equals(id))
                return symbols.get(x).type;
        }
        return "ERROR";
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

    //Evaluate type of expression
    String evaluateType(ArrayList<String> expr, List<String> rule) {
        // System.out.println("Evaluating Type for " + rule + " with bubbled up types: " + expr + " bubbling up: " + expressionToType.get(expr.toString()));
        if (expressionToType.get(expr.toString()) != null) {
            return expressionToType.get(expr.toString());
        }
        else {
            System.err.println("ERROR invalid types for reducible expression " + expr);
            System.exit(0);
        }
        return "ERROR";
    }

    // Main program
    public static final void main(String args[]) {
        new WLPPGen().go();
    }

    public void printSymbols() {
        for (int x = symbols.size()-1; x >= 0; x--) {
            // System.out.println(symbols.get(x).tableItem);
        }
    }

    public void go() {
        Tree parseTree = readParse("S",false);
        printSymbols();
        parseTree.traverse();
    }
}
