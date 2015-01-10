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
        public int offset;
        public int value;

        public SymbolPair(String type, String id, int offset, int value) {
            this.type = type;
            this.id = id;
            this.offset = offset;
            tableItem = id + " " + type;
            this.value = value;
        }
    }

    List<SymbolPair> symbols = new ArrayList<SymbolPair>();
    ArrayList<String> genCodeTypes = new ArrayList<String>();

    String type; 
    Boolean needValue = true;
    int globalOffset = 0;
    String globalId = "";
    int openedWhile = 0;
    int closedWhile = 0;
    int openedIf = 0;
    int closedIf = 0;
    Boolean whileType = false;

    // Data structure for storing the parse tree.
    public class Tree {
        List<String> rule;
        ArrayList<Tree> children = new ArrayList<Tree>();

        String typeChecktraverse() {
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
                expr.add(children.get(x).typeChecktraverse());
            }
            // System.out.println("ohohooh " + rule);
            // System.out.println("herrreee " + expr);

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

        void genCode() {
            if (rule.get(0).equals("statement") && rule.get(1).equals("lvalue")) {
                Tree lvalueChildren = children.get(0);

                children.get(0).genCode();
                String leftSideId = globalId;
                children.get(2).genCode();

                if (lvalueChildren.rule.toString().equals("[lvalue, LPAREN, lvalue, RPAREN]")) {
                    while (lvalueChildren.rule.toString().equals("[lvalue, LPAREN, lvalue, RPAREN]")) {
                        lvalueChildren = lvalueChildren.children.get(1);
                        if (lvalueChildren.rule.toString().equals("[lvalue, STAR, factor]")) {
                            setReferenceValue(leftSideId);
                            break;
                        }
                        else if(lvalueChildren.rule.toString().equals("[lvalue, ID]")) {
                            setValue(leftSideId);
                            break;
                        }
                    }
                }
                else {
                    if (lvalueChildren.rule.toString().equals("[lvalue, STAR, factor]")) {
                        setReferenceValue(leftSideId);
                    }
                    else if(lvalueChildren.rule.toString().equals("[lvalue, ID]")) {
                        setValue(leftSideId);
                    }
                }
                
                return;
            }
            else if (rule.get(0).equals("statement") && rule.get(1).equals("PRINTLN")) {
                children.get(2).genCode();
                System.out.println("lis $6");
                System.out.println(".word print");
                System.out.println("add $1, $3, $0");

                //save return address on stack since JALR puts LR in $31
                System.out.println("sw $31, -4($30)");
                System.out.println("sub $30, $30, $4");
                System.out.println("jalr $6");
                System.out.println("lw $31, 0($30)");
                System.out.println("add $30, $30, $4");

                return;

            }
            else if (rule.get(0).equals("statement") && rule.get(1).equals("IF")) {
                whileType = false;
                children.get(2).genCode();
                int depth = openedIf;
                children.get(5).genCode();
                System.out.println("beq $0, $0, closedIf" + depth);
                System.out.println("openedLabelElse" + depth + ":");
                children.get(9).genCode();
                System.out.println("closedIf" + depth + ":");

                return;
            }
            else if (rule.get(0).equals("statement") && rule.get(1).equals("WHILE")) {
                whileType = true;
                children.get(2).genCode();
                int depth = openedWhile;
                children.get(5).genCode();
                System.out.println("beq $0, $0, openedLabel" + depth);
                System.out.println("closedLabel" + depth + ":");

                return;
            }
            else if(rule.get(0).equals("test")) {
                if (whileType) {
                    System.out.println("openedLabel" + ++openedWhile + ":");
                    children.get(0).genCode();
                    pushStack();
                    children.get(2).genCode();
                    popStack();
                    if (rule.get(2).equals("LT")) {
                        System.out.println("slt $3, $5, $3");
                        System.out.println("beq $3, $0, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("GE")) {
                        System.out.println("slt $3, $5, $3");
                        System.out.println("bne $3, $0, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("LE")) {
                        System.out.println("sub $8, $5, $10");
                        System.out.println("slt $3, $8, $3");
                        System.out.println("beq $3, $0, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("GT")) {
                        System.out.println("slt $3, $3, $5");
                        System.out.println("beq $3, $0, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("EQ")) {
                        System.out.println("bne $5, $3, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("NE")) {
                        System.out.println("beq $5, $3, closedLabel" + openedWhile);
                    }
                }
                else {
                    System.out.println("openedLabelIf" + ++openedIf + ":");
                    children.get(0).genCode();
                    pushStack();
                    children.get(2).genCode();
                    popStack();
                    if (rule.get(2).equals("LT")) {
                        System.out.println("slt $3, $5, $3");
                        System.out.println("beq $3, $0, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("GE")) {
                        System.out.println("slt $3, $5, $3");
                        System.out.println("bne $3, $0, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("LE")) {
                        System.out.println("sub $8, $5, $10");
                        System.out.println("slt $3, $8, $3");
                        System.out.println("beq $3, $0, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("GT")) {
                        System.out.println("slt $3, $3, $5");
                        System.out.println("beq $3, $0, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("EQ")) {
                        System.out.println("bne $5, $3, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("NE")) {
                        System.out.println("beq $5, $3, openedLabelElse" + openedIf);
                    }                    
                }
                return;
            }
            else if(rule.toString().equals("[expr, expr, PLUS, term]")) { //this will be an expr -> expr + term
                children.get(0).genCode();
                pushStack(); //after calculating LHS, put in $3, push it to stack
                children.get(2).genCode(); //get RHS, put in $3
                popStack(); //get LHS off stack into $5

                //logic for types being added
                if(genCodeTypes.get(0).equals("[int, PLUS, int*]")) {
                    System.out.println("mult $5, $4");
                    System.out.println("mflo $5");
                }
                else if(genCodeTypes.get(0).equals("[int*, PLUS, int]")) {
                    System.out.println("mult $3, $4");
                    System.out.println("mflo $3");
                }
                System.out.println("add $3, $5, $3"); //now can add
                genCodeTypes.remove(0);
                return;
                
            }
            else if(rule.toString().equals("[expr, expr, MINUS, term]")) {
                children.get(0).genCode();
                pushStack();
                children.get(2).genCode();
                popStack();

                //logic for types being subtracted
                if (genCodeTypes.get(0).equals("[int*, MINUS, int*]")) {
                    System.out.println("sub $3, $5, $3");
                    System.out.println("div $3, $4");
                    System.out.println("mflo $3");
                }
                else if(genCodeTypes.get(0).equals("[int*, MINUS, int]")) {
                    System.out.println("mult $3, $4");
                    System.out.println("mflo $3");
                    System.out.println("sub $3, $5, $3");
                }
                else {
                    System.out.println("sub $3, $5, $3");
                }
                genCodeTypes.remove(0);
                return;
            }
            else if(rule.toString().equals("[term, term, STAR, factor]")) {
                children.get(0).genCode();
                pushStack();
                children.get(2).genCode();
                popStack();
                System.out.println("mult $5, $3");
                System.out.println("mflo $3");

                return;

            }
            else if(rule.toString().equals("[term, term, SLASH, factor]")) {
                children.get(0).genCode();
                pushStack();
                children.get(2).genCode();
                popStack();
                System.out.println("div $5, $3");
                System.out.println("mflo $3");

                return;

            }
            else if(rule.toString().equals("[term, term, PCT, factor]")) {
                children.get(0).genCode();
                pushStack();
                children.get(2).genCode();
                popStack();
                System.out.println("div $5, $3");
                System.out.println("mfhi $3");

                return;

            }
            else if (rule.get(0).equals("NUM")) {
                System.out.println("lis $3");
                System.out.println(".word " + rule.get(1));
                return;
            }
            else if (rule.get(0).equals("NULL")) {
                System.out.println("lis $3");
                System.out.println(".word 0");
            }
            else if (rule.get(0).equals("ID")) {
                getVal(rule.get(1));
                globalId = rule.get(1);
                return;
            }
            else if (rule.toString().equals("[factor, STAR, factor]") || rule.toString().equals("[lvalue, STAR, factor]")) {
                children.get(1).genCode();
                System.out.println("lw $3, 0($3)");
                return;

            }
            else if (rule.toString().equals("[statement, DELETE, LBRACK, RBRACK, expr, SEMI]")) {
                children.get(3).genCode();
                System.out.println("lis $6");
                System.out.println(".word delete");
                System.out.println("sw $31, -4($30)");
                System.out.println("sub $30, $30, $4");
                System.out.println("add $1, $3, $0");
                System.out.println("jalr $6");
                System.out.println("lw $31, 0($30)");
                System.out.println("add $30, $30, $4");

                return;
            }
            else if (rule.toString().equals("[factor, NEW, INT, LBRACK, expr, RBRACK]")) {

                children.get(3).genCode();

                System.out.println("lis $6");
                System.out.println(".word new");
                System.out.println("sw $31, -4($30)");
                System.out.println("sub $30, $30, $4");
                System.out.println("add $1, $3, $0");
                System.out.println("jalr $6");
                System.out.println("lw $31, 0($30)");
                System.out.println("add $30, $30, $4");

                return;

            }
            else if (rule.toString().equals("[factor, AMP, lvalue]")) {
                children.get(1).genCode();
                deReference(globalId);
                return;

            }

            else if (rule.get(0).equals("dcl") || rule.get(0).equals("dcls")) {
                return;
            }

            for (int x = 0; x < children.size(); x++) {
                children.get(x).genCode();
            }

            return;
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
        // System.out.println(tokens);
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
                symbols.add(new SymbolPair(type,id, (globalOffset)++ * -4, 0));
                needValue = true;
            }
        }
        else if (line.startsWith("ID")) {
            variableDeclaration(line.split(" ")[1]);
        }
        else if (line.startsWith("NUM") && needValue) {
            symbols.get(symbols.size()-1).value = Integer.parseInt(line.split(" ")[1]);
            needValue = false;
        }
        else if (line.startsWith("NULL") && needValue) {
            symbols.get(symbols.size()-1).value = 0;
            needValue = false;
        }
        if (!terminals.contains(lhs)) {
            Scanner sc = new Scanner(line);
            String left = sc.next(); // discard lhs
            while (sc.hasNext()) {
                String s = sc.next();
                ret.children.add(readParse(s, (left.equals("dcl"))));
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
            if (expr.toString().indexOf("PLUS") != -1 || expr.toString().indexOf("MINUS") != -1) {
                genCodeTypes.add(expr.toString());
            }
            return expressionToType.get(expr.toString());
        }
        else {
            System.err.println("ERROR invalid types for reducible expression " + expr);
            System.exit(0);
        }
        return "ERROR";
    }

    //Assignment 10 Functions

    public void genSymbols() {
        System.out.println("lis $4");
        System.out.println(".word 4");
        System.out.println("lis $10");
        System.out.println(".word 1");
        System.out.println("add $29, $30, $0");
        System.out.println("sw $1, -4($29)");
        System.out.println("sw $2, -8($29)");
        System.out.println("sub $29, $30, $4");
        System.out.println("sub $30, $30, $4");
        System.out.println("sub $30, $30, $4");

        for (int x = 2; x < symbols.size(); x++) {
            System.out.println("lis $3");
            System.out.println(".word " + symbols.get(x).value);
            System.out.println("sw $3, -4($30)");
            System.out.println("sub $30, $30, $4");
        }
    }

    void getVal(String id) {
        int offset = 0;
        for (int x = 0; x < symbols.size(); x++) {
            if (symbols.get(x).id.equals(id)) {
               offset = symbols.get(x).offset;
               break;
            }
        }
        System.out.println("lw $3, " + offset + "($29)");
        return;
    }

    void deReference(String id) {
        for (int x = 0; x < symbols.size(); x++) {
            if (symbols.get(x).id.equals(id)) {
               if (symbols.get(x).type.equals("int")) {
                    System.out.println("lis $3");
                    System.out.println(".word " + symbols.get(x).offset);
                    System.out.println("add $3, $3, $29");
               }
               else if (symbols.get(x).type.equals("int*")) {
                    System.out.println("lw $3, " + symbols.get(x).offset + "($29)");
               }
            }
        }

    }

    void setValue(String id) {
        for (int x = 0; x < symbols.size(); x++) {
            if (symbols.get(x).id.equals(id)) {
                System.out.println("sw $3, " + symbols.get(x).offset + "($29)");
                return;
            }
        }
        return;
    }
    void setReferenceValue(String id) {
        for (int x = 0; x < symbols.size(); x++) {
            if (symbols.get(x).id.equals(id)) {
                System.out.println("lw $5, " + symbols.get(x).offset + "($29)");
                System.out.println("sw $3, 0($5)");
                return;
            }
        }
        return;
    }

    void pushStack() {
        System.out.println("sw $3, -4($30)");
        System.out.println("sub $30, $30, $4");
    }
    void popStack() {
        System.out.println("lw $5, 0($30)");
        System.out.println("add $30, $30, $4");
    }

    // Main program
    public static final void main(String args[]) {
        new WLPPGen().go();
    }

    public void printSymbols() {
        for (int x = symbols.size()-1; x >= 0; x--) {
            System.out.println(symbols.get(x).tableItem + " with offset: " + symbols.get(x).offset + " value: " + symbols.get(x).value);
        }
    }

    public void initAllocation() {
        System.out.println("lis $6");
        System.out.println(".word init");
        System.out.println("sw $31, -4($30)");
        System.out.println("sub $30, $30, $4");
        if (symbols.get(0).type.equals("int")) {
            System.out.println("add $2, $0, $0");
        }
        System.out.println("jalr $6");
        System.out.println("lw $31, 0($30)");
        System.out.println("add $30, $30, $4");
    }

    public void go() {
        Tree parseTree = readParse("S",false);
        parseTree.typeChecktraverse();
        // System.out.println(".import print");
        // System.out.println(".import init");
        // System.out.println(".import new");
        // System.out.println(".import delete");
        genSymbols();
        // initAllocation();
        // printSymbols();
        parseTree.genCode();
        System.out.println("jr $31");

    }
}
