import java.util.*;

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
        put("[int, PLUS, int]","int"); //normal addition
        put("[int*, PLUS, int]","int*"); //can add a number to a pointer in any order
        put("[int, PLUS, int*]","int*"); //can add a number to a pointer in any order
        put("[int, MINUS, int]","int"); //normal subtraction
        put("[int*, MINUS, int]","int*"); //can subtract an int from a pointer
        put("[int*, MINUS, int*]","int"); //can subtract a pointer from a pointer to get the difference between the addresses
        put("[int, SLASH, int]", "int"); //normal division
        put("[int, PCT, int]", "int"); //normal modulus, recall that quotient lo, remainder hi
        put("[int, STAR, int]", "int"); //normal multiplication
        put("[LPAREN, int, RPAREN]","int");//normal parentheses
        put("[LPAREN, int*, RPAREN]","int*");//normla parentheses
        put("[AMP, int]","int*");//can take the address of an int
        put("[STAR, int*]","int");//can dereference an address, get its value
        put("[NEW, INT, LBRACK, int, RBRACK]","int*");//can make a new pointer with new int[int] specifying size
        //p3
        put("[int, EQ, int]","equal");//can test equality of ints or pointers
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
        put("[PRINTLN, LPAREN, int, RPAREN, SEMI]","printin"); //can print an int
        put("[DELETE, LBRACK, RBRACK, int*, SEMI]","deletinshit");//can delete a pointer
        put("[int, BECOMES, int, SEMI]","becominshit");//can assign an int to an int
        put("[int*, BECOMES, int*, SEMI]","becominshit");//can assign a pointer to a pointer
        put("[INT, int]","int");
        put("[INT, int*]","int*");
        put("[dcls, int, BECOMES, int, SEMI]","dcls");//declared int must equal int
        put("[dcls, int*, BECOMES, int*, SEMI]","dcls");//declared pointer must equal a pointer
        put("[INT, WAIN, LPAREN, int, COMMA, int, RPAREN, LBRACE, dcls, statements, RETURN, int, SEMI, RBRACE]", "procedureRight");//can have a pointer as first param
        put("[INT, WAIN, LPAREN, int*, COMMA, int, RPAREN, LBRACE, dcls, statements, RETURN, int, SEMI, RBRACE]", "procedureRight");//can have an int as first param
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

    String type; 
    Boolean needValue = true;
    int globalOffset = 0;
    String globalId = "";
    int openedWhile = 0;
    int closedWhile = 0;
    int openedIf = 0;
    int closedIf = 0;
    Boolean whileType = false;
    Boolean isAmp = false;
    String jalrWord = "init";

    // Data structure for storing the parse tree.
    public class Tree {
        List<String> rule;
        ArrayList<Tree> children = new ArrayList<Tree>();

        String typeChecktraverse() {
            ArrayList<String> expr = new ArrayList<String>();

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

            for(int x = 0; x < reducibleExpressions.size(); x++) {
                if (reducibleExpressions.get(x).equals(rule.toString())) {
                    return evaluateType(expr, rule);
                }
            }

            if (expr.size() == 0) {
                return rule.get(0);
            }

            return expr.get(0);
        }

        void genCode() {
            if (rule.get(0).equals("statement") && rule.get(1).equals("lvalue")) {
                Tree lvalueChildren = children.get(0);

                while(lvalueChildren.rule.toString().equals("[lvalue, LPAREN, lvalue, RPAREN]")) {
                    lvalueChildren = lvalueChildren.children.get(1);
                }
                if (lvalueChildren.rule.toString().equals("[lvalue, STAR, factor]")) {
                    lvalueChildren.children.get(1).genCode();
                    System.out.println("add $11, $3, $0");
                    children.get(2).genCode();
                    System.out.println("sw $3, 0($11)");
                }
                else if(lvalueChildren.rule.toString().equals("[lvalue, ID]")) {
                    children.get(0).genCode();
                    String leftSideId = globalId;
                    children.get(2).genCode();
                    setValue(leftSideId);
                }
                else {
                    System.out.println("error");
                }
                
                return;
            }
            else if (rule.get(0).equals("statement") && rule.get(1).equals("PRINTLN")) {
                children.get(2).genCode();
                if (!jalrWord.equals("print")) {
                    System.out.println("lis $6");
                    System.out.println(".word print");
                    jalrWord = "print";
                }

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

                    String operand1 = children.get(0).typeChecktraverse();
                    String operand2 = children.get(2).typeChecktraverse();

                    Boolean isInt = false;
                    if (operand1.equals("int") && operand2.equals("int"))
                        isInt = true;

                    if (rule.get(2).equals("LT")) {
                        if (isInt)
                            System.out.println("slt $3, $5, $3");
                        else
                            System.out.println("sltu $3, $5, $3");
                        System.out.println("beq $3, $0, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("GE")) {
                        if (isInt)
                            System.out.println("slt $3, $5, $3");
                        else
                            System.out.println("sltu $3, $5, $3");
                        System.out.println("bne $3, $0, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("LE")) {
                        if (isInt)
                            System.out.println("slt $3, $3, $5");
                        else
                            System.out.println("sltu $3, $3, $5");
                        System.out.println("bne $3, $0, closedLabel" + openedWhile);
                    }
                    else if(rule.get(2).equals("GT")) {
                        if (isInt)
                            System.out.println("slt $3, $3, $5");
                        else
                            System.out.println("sltu $3, $3, $5");
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
                    ++openedIf;
                    children.get(0).genCode();
                    pushStack();
                    children.get(2).genCode();
                    popStack();

                    String operand1 = children.get(0).typeChecktraverse();
                    String operand2 = children.get(2).typeChecktraverse();

                    Boolean isInt = false;
                    if (operand1.equals("int") && operand2.equals("int"))
                        isInt = true;

                    if (rule.get(2).equals("LT")) {
                        if (isInt)
                            System.out.println("slt $3, $5, $3");
                        else
                            System.out.println("sltu $3, $5, $3");
                        System.out.println("beq $3, $0, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("GE")) {
                        if (isInt)
                            System.out.println("slt $3, $5, $3");
                        else
                            System.out.println("sltu $3, $5, $3");
                        System.out.println("bne $3, $0, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("LE")) {
                        if (isInt)
                            System.out.println("slt $3, $3, $5");
                        else
                            System.out.println("slt $3, $3, $5");
                        System.out.println("bne $3, $0, openedLabelElse" + openedIf);
                    }
                    else if(rule.get(2).equals("GT")) {
                        if (isInt)
                            System.out.println("slt $3, $3, $5");
                        else
                            System.out.println("sltu $3, $3, $5");
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

                String operand1 = children.get(0).typeChecktraverse();
                String operand2 = children.get(2).typeChecktraverse();

                //logic for types being added
                if(operand1.equals("int") && operand2.equals("int*")){
                    System.out.println("mult $5, $4");
                    System.out.println("mflo $5");
                }
                else if(operand1.equals("int*") && operand2.equals("int")) {
                    System.out.println("mult $3, $4");
                    System.out.println("mflo $3");
                }
                System.out.println("add $3, $5, $3"); //now can add
                return;
                
            }
            else if(rule.toString().equals("[expr, expr, MINUS, term]")) {
                children.get(0).genCode();
                pushStack();
                children.get(2).genCode();
                popStack();

                String operand1 = children.get(0).typeChecktraverse();
                String operand2 = children.get(2).typeChecktraverse();

                //logic for types being subtracted
                if (operand1.equals("int*") && operand2.equals("int*")) {
                    System.out.println("sub $3, $5, $3");
                    System.out.println("div $3, $4");
                    System.out.println("mflo $3");
                }
                else if(operand1.equals("int*") && operand2.equals("int")) {
                    System.out.println("mult $3, $4");
                    System.out.println("mflo $3");
                    System.out.println("sub $3, $5, $3");
                }
                else {
                    System.out.println("sub $3, $5, $3");
                }
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
                Boolean isAmpLocal = isAmp;
                isAmp = false;
                children.get(1).genCode();
                if (!isAmpLocal) {
                    System.out.println("lw $3, 0($3)");
                }
                return;

            }
            else if (rule.toString().equals("[statement, DELETE, LBRACK, RBRACK, expr, SEMI]")) {
                children.get(3).genCode();
                if (!jalrWord.equals("delete")) {
                    System.out.println("lis $6");
                    System.out.println(".word delete");
                    jalrWord = "delete";
                }

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
                if (!jalrWord.equals("new")) {
                    System.out.println("lis $6");
                    System.out.println(".word new");
                    jalrWord = "new";
                }
                System.out.println("sw $31, -4($30)");
                System.out.println("sub $30, $30, $4");
                System.out.println("add $1, $3, $0");
                System.out.println("jalr $6");
                System.out.println("lw $31, 0($30)");
                System.out.println("add $30, $30, $4");

                return;

            }
            else if (rule.toString().equals("[factor, AMP, lvalue]")) {
                isAmp = true;
                children.get(1).genCode();
                if (isAmp) {
                    reference(globalId);
                    isAmp = false;
                }
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
            // System.out.println(expr.toString());
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

    void reference(String id) {
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
        System.out.println(".import print");
        System.out.println(".import init");
        System.out.println(".import new");
        System.out.println(".import delete");
        genSymbols();
        initAllocation();
        printSymbols();
        parseTree.genCode();
        System.out.println("jr $31");
    }
}
