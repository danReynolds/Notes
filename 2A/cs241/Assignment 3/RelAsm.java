import java.util.*;
import java.math.*;

/** A sample main class demonstrating the use of the Lexer.
 * This main class just outputs each line in the input, followed by
 * the tokens returned by the lexer for that line.
 *
 * @version 071011.0
 */
public class RelAsm {
    public static final void main(String[] args) {
        new RelAsm().run();
    }

    private Lexer lexer = new Lexer();

    public ArrayList<String> globInstruction(Token[] tokens, int relative, Map<String, int[]> instructionBinaries, ArrayList<String> printValues, int pc) {
        int instruction = 0;
        String labelAppend = "!@%,";
        int initialRelative = relative;
        int[] instructionTemplate = instructionBinaries.get(tokens[relative].lexeme);
        for (int x = 0; x < instructionTemplate.length; x += 2) {
            if (instructionTemplate[x+1] != -1) {
                instruction = instruction | instructionTemplate[x+1] << instructionTemplate[x];
            }
            else {
                try {
                    if (relative == initialRelative) { //this if block ensures that there are commas/parens between tokens
                        relative++;
                    }
                    else if (relative == initialRelative + 3 && (tokens[initialRelative].lexeme.equals("lw") || tokens[initialRelative].lexeme.equals("sw"))) {
                        if (tokens[relative + 1].kind == Kind.LPAREN && tokens[relative + 3].kind == Kind.RPAREN) {
                            relative +=2;
                        }
                        else {
                            System.err.println("ERROR Invalid load word format");
                            System.exit(0);
                        }
                    }
                    else if (tokens[relative +1].kind != Kind.COMMA) {
                        System.err.println("ERROR Invalid operand formatting");
                        System.exit(0);
                    }
                    else {
                        relative+=2;
                    }

                    String stringInsert;
                    if (tokens[relative].lexeme.startsWith("$")) {
                        if (((tokens[initialRelative].lexeme.equals("lw") || tokens[initialRelative].lexeme.equals("sw")) && relative == initialRelative + 3) || (relative == tokens.length-1 && (tokens[initialRelative].lexeme.equals("beq") || tokens[initialRelative].lexeme.equals("bne")))) {
                            System.err.println("register used where integer expected");
                            System.exit(0);
                        }
                        stringInsert = tokens[relative].lexeme.substring(1, tokens[relative].lexeme.length());
                    }
                    else {
                        if (!((tokens[initialRelative].lexeme.equals("lw") || tokens[initialRelative].lexeme.equals("sw")) && relative == initialRelative + 3) && (!(relative == tokens.length - 1 && (tokens[initialRelative].lexeme.equals("beq") || tokens[initialRelative].lexeme.equals("bne"))) && tokens[relative].kind != Kind.REGISTER)) {
                            System.err.println("ERROR require register operands"); //complains if not a register and not the last of a bne or beq or the i value of a lw/sw
                            System.exit(0);
                        }
                        stringInsert = tokens[relative].lexeme.substring(0, tokens[relative].lexeme.length());
                    }
                    int intInsert;
                    if (stringInsert.startsWith("0x")) {
                        intInsert = Integer.parseInt(stringInsert.substring(2,stringInsert.length()),16);
                        if (intInsert > 32767)
                            intInsert = intInsert - 2*32768;
                    }
                    else
                        intInsert = Integer.parseInt(stringInsert);

                    if (intInsert > 32767 || intInsert < -32768) {
                        System.err.println("ERROR exceeds allowed register jump");
                        System.exit(0);
                    }

                    if (tokens[relative].kind == Kind.REGISTER && (intInsert > 31 || intInsert < 0)) {
                        System.err.println("ERROR Invalid Register");
                        System.exit(0);
                    }

                    if (((tokens[initialRelative].lexeme.equals("sw") || tokens[initialRelative].lexeme.equals("lw")) && initialRelative + 3 == relative) || (tokens[initialRelative].lexeme.equals("beq") || tokens[initialRelative].lexeme.equals("bne")) && tokens.length -1 == relative) 
                        intInsert =  intInsert & 0x0000FFFF;
                    instruction = instruction | intInsert << instructionTemplate[x];

                }
                catch (Exception e) { //will go here if cant parse int for register
                    if (e instanceof NumberFormatException && (tokens[initialRelative].lexeme.equals("beq") || tokens[initialRelative].lexeme.equals("bne"))) {
                        labelAppend += tokens[relative].lexeme + "!@%," + instructionTemplate[x] + "!@%," + pc + "!@%,";
                        // System.out.println("Label(s): " + labelAppend + " used in place of register");
                    }
                    else if (e instanceof ArrayIndexOutOfBoundsException) {
                        System.err.println("ERROR too few OPERANDS");
                        System.exit(0);
                    }
                    else {
                        System.err.println("ERROR String used instead of register, but label not accepted here");
                        System.exit(0);
                    }
                    //will die if it is not long enough for instructions
                    // or if parse error indicating label
                }
            }
        }
        if (relative == tokens.length-1 || ((relative == tokens.length -2) && (tokens[initialRelative].lexeme.equals("lw") || tokens[initialRelative].lexeme.equals("sw")))) { //relative will be at last token if they gave a valid amount of operands
            if (!labelAppend.equals("!@%,")) {
                printValues.add(Integer.toString(instruction) + labelAppend);
                return printValues;
            }
            else {
                printValues.add(Integer.toString(instruction) + "&noLabelInserted&");
                return printValues;
            }
        }
        else {
            System.err.println("ERROR invalid operands given");
            System.exit(0);
        }
        return printValues;
    }

    private void run() {
        Scanner in = new Scanner(System.in);
        Map<String,Integer> labels = new LinkedHashMap<String, Integer>();
        ArrayList<String> printValues =  new ArrayList<String>();
        ArrayList<Integer> symbolTable = new ArrayList<Integer>();
        int instructionCount = 3;

        //Setup the Binary Instruction Templates
        Map<String, int[]> instructionBinaries = new LinkedHashMap<String, int[]>(); //Map for each opcode containing each section index, and then section value
        instructionBinaries.put("jr", new int[]{21,-1,0,8}); //jr shift value, then value to put in, -1 for register or label value
        instructionBinaries.put("jalr", new int[]{21,-1,0,9}); //jalr
        instructionBinaries.put("add", new int[]{11,-1,21,-1,16,-1,0,32});
        instructionBinaries.put("sub", new int[]{11, -1, 21,-1, 16,-1,0,34});
        instructionBinaries.put("slt", new int[]{11, -1, 21,-1, 16,-1,0,42});
        instructionBinaries.put("sltu", new int[]{11, -1, 21,-1, 16,-1,0,43});
        instructionBinaries.put("beq", new int[]{28,1, 21, -1, 16, -1, 0, -1});
        instructionBinaries.put("bne", new int[]{26,5, 21, -1, 16, -1, 0, -1});
        instructionBinaries.put("lis", new int[]{11,-1,0,20});
        instructionBinaries.put("mflo", new int[]{11, -1, 0, 18});
        instructionBinaries.put("mfhi", new int[]{11, -1, 0, 16});
        instructionBinaries.put("mult", new int[]{21, -1, 16, -1, 0, 24});
        instructionBinaries.put("multu", new int[]{21, -1, 16, -1, 0, 25});
        instructionBinaries.put("div", new int[]{21, -1, 16, -1, 0, 26});
        instructionBinaries.put("divu", new int[]{21, -1, 16, -1, 0, 27});
        instructionBinaries.put("lw", new int[]{26, 35, 16, -1, 0, -1, 21, -1});
        instructionBinaries.put("sw", new int[]{26, 43, 16, -1, 0, -1, 21, -1});


        int pc = 3;

        while(in.hasNextLine()) {
            String line = in.nextLine();
            int relative = 0;

            // Scan the line into an array of tokens.
            Token[] tokens;
            tokens = lexer.scan(line);

            while ((tokens.length > relative) && tokens[relative].kind == Kind.LABEL) {
                //check if label used
                String key = tokens[relative].lexeme.substring(0, tokens[relative].lexeme.length() -1);
                if (null != labels.get(key)) {
                    System.err.println("ERROR Label Already Used");
                    System.exit(0);
                }
                labels.put(key, pc*4);
                relative++;
            }

            if (relative == tokens.length)
                continue;

            else if (tokens[relative].kind == Kind.ID && (tokens[relative].lexeme.equals("jalr") || tokens[relative].lexeme.equals("jr")) || tokens[relative].lexeme.equals("add") || tokens[relative].lexeme.equals("sub") || tokens[relative].lexeme.equals("slt") || tokens[relative].lexeme.equals("sltu") || tokens[relative].lexeme.equals("beq") || tokens[relative].lexeme.equals("bne") || tokens[relative].lexeme.equals("lis") || tokens[relative].lexeme.equals("mflo") || tokens[relative].lexeme.equals("mfhi")|| tokens[relative].lexeme.equals("mult") || tokens[relative].lexeme.equals("multu") || tokens[relative].lexeme.equals("div") || tokens[relative].lexeme.equals("divu") || tokens[relative].lexeme.equals("lw") || tokens[relative].lexeme.equals("sw")) {
                printValues = globInstruction(tokens, relative, instructionBinaries, printValues, pc);
                instructionCount++;
            }
            else if (tokens[relative].kind == Kind.DOTWORD) {
                if (tokens.length != relative + 2) {
                    System.err.println("ERROR invalid word length");
                    System.exit(0);
                }
                else if (tokens[relative + 1].kind != Kind.HEXINT && tokens[relative + 1].kind != Kind.INT && tokens[relative + 1].kind != Kind.ID) {
                    System.err.println("ERROR not a hexint or int");
                    System.exit(0);
                }
                else if (tokens[relative + 1].kind == Kind.ID) {
                    printValues.add(tokens[relative + 1].lexeme.substring(0, tokens[relative + 1].lexeme.length()));
                    symbolTable.add(pc*4);
                    instructionCount++;
                }
                else {
                    String hexString = Integer.toHexString(tokens[relative + 1].toInt());
                    int length = hexString.length();
                    for (int y = 0; y < 8-length; y++)
                        hexString = "0" + hexString;

                    for (int x = 0; x < 4; x++) {
                        String hexsubstring = hexString.substring(x*2, x*2+2);
                        int hexsubint = Integer.parseInt(hexsubstring, 16);
                        printValues.add(Integer.toString(hexsubint));
                    }
                    instructionCount++;
                }
            }
            else {
                System.err.println("ERROR not a valid opcode");
                System.exit(0);
            }
            pc++;
        }

        String branch = "10000002";
        String endOfModule = Integer.toHexString((instructionCount + symbolTable.size() * 2)*4);
        String endOfCode = Integer.toHexString((instructionCount)*4);

        for (int z = endOfModule.length(); z < 8; z++)
            endOfModule = "0" + endOfModule;
        for (int z = endOfCode.length(); z < 8; z++)
            endOfCode = "0" + endOfCode;

        for (int z = 0; z < 4; z++)
            System.out.write(Integer.parseInt(branch.substring(z*2, z*2+2), 16));
        for (int z = 0; z < 4; z++)
            System.out.write(Integer.parseInt(endOfModule.substring(z*2, z*2+2),16));
        for (int z = 0; z < 4; z++)
            System.out.write(Integer.parseInt(endOfCode.substring(z*2, z*2+2), 16));

        for (int x =  0; x < printValues.size(); x++) {
            try {
                int val = Integer.parseInt(printValues.get(x));
                System.out.write(val);
            }
            catch (NumberFormatException nfe) {
                if (printValues.get(x).endsWith("&noLabelInserted&"))  {
                    String instructionString = Integer.toHexString(Integer.parseInt(printValues.get(x).split("&noLabelInserted&")[0]));
                    for (int y = instructionString.length(); y<8; y++)
                        instructionString = "0" + instructionString;
                    for (int y = 0; y < 4; y++) {
                        String substring = instructionString.substring(y*2, y*2+2);
                        int subint = Integer.parseInt(substring, 16);
                        System.out.write(subint);
                    }
                }   
                else if (printValues.get(x).endsWith("!@%,")) { //for label insertion
                    String[] instructionPieces = printValues.get(x).split("!@%,"); //0th holds the 32-bits, 1st holds the label, 2nd the index, 3rd the branch pc
                    int instructionInt = Integer.parseInt(instructionPieces[0]);

                    for (int y = 0; y < instructionPieces.length; y+=4) { 
                        if (labels.get(instructionPieces[y+1]) != null) {
                            int branchPC = Integer.parseInt(instructionPieces[y+3]);
                            int relativeValue = ((labels.get(instructionPieces[y+1]) - branchPC*4)/4) -1;

                            if (relativeValue > 32767 || relativeValue < -32768) {
                                System.err.println("ERROR relative address exceeds max range");
                                System.exit(0);
                            }
                            relativeValue = relativeValue & 0x0000FFFF; //calculate proper relative instruction address

                            instructionInt = instructionInt | relativeValue << Integer.parseInt(instructionPieces[y+2]);
                        }
                        else {
                            System.err.println("ERROR label not defined1");
                            System.exit(0);
                        }
                    }
                    String instructionString = Integer.toHexString(instructionInt);
                    for (int y = instructionString.length(); y<8; y++)
                        instructionString = "0" + instructionString;

                    for (int y = 0; y < 4; y++) {
                        String substring = instructionString.substring(y*2, y*2+2);
                        int subint = Integer.parseInt(substring, 16);
                        System.out.write(subint);
                    }
                }
                else if (labels.get(printValues.get(x)) != null) { //meaning that it is a saved label or an instruction
                    String hexString = Integer.toHexString(labels.get(printValues.get(x))); //get value of operand
                    int length = hexString.length();
                    for (int y = 0; y < 8-length; y++)
                        hexString = "0" + hexString;

                    for (int y = 0; y < 4; y++) {
                        String hexsubstring = hexString.substring(y*2, y*2+2);
                        int hexsubint = Integer.parseInt(hexsubstring, 16);
                        System.out.write(hexsubint);
                    }
                }
                else
                    System.err.println("ERROR label not defined2");
            }
        }
        String word1 = "00000001";

        for (int z = 0; z < symbolTable.size(); z++) {
            String hexToPrint = Integer.toHexString(symbolTable.get(z));
            for (int y = hexToPrint.length(); y < 8; y++)
                hexToPrint = "0" + hexToPrint;

            for (int q = 0; q < 4; q++)
                System.out.write(Integer.parseInt(word1.substring(q*2, q*2+2),16));
            for (int q = 0; q < 4; q++)
                System.out.write(Integer.parseInt(hexToPrint.substring(q*2, q*2+2),16));
        }

        System.out.flush();
    }
}

/** The various kinds of tokens. */
enum Kind {
    ID,         // Opcode or identifier (use of a label)
    INT,        // Decimal integer
    HEXINT,     // Hexadecimal integer
    REGISTER,   // Register number
    COMMA,      // Comma
    LPAREN,     // (
    RPAREN,     // )
    LABEL,      // Declaration of a label (with a colon)
    DOTWORD,    // .word directive
    WHITESPACE; // Whitespace
}

/** Representation of a token. */
class Token {
    public Kind kind;     // The kind of token.
    public String lexeme; // String representation of the actual token in the
                          // source code.

    public Token(Kind kind, String lexeme) {
        this.kind = kind;
        this.lexeme = lexeme;
    }
    public String toString() {
        return kind+" {"+lexeme+"}";
    }
    /** Returns an integer representation of the token. For tokens of kind
     * INT (decimal integer constant) and HEXINT (hexadecimal integer
     * constant), returns the integer constant. For tokens of kind
     * REGISTER, returns the register number.
     */
    public int toInt() {
        if(kind == Kind.INT) return parseLiteral(lexeme, 10, 32);
        else if(kind == Kind.HEXINT) return parseLiteral(lexeme.substring(2), 16, 32);
        else if(kind == Kind.REGISTER) return parseLiteral(lexeme.substring(1), 10, 5);
        else {
            System.err.println("ERROR in to-int conversion.");
            System.exit(1);
            return 0;
        }
    }
    private int parseLiteral(String s, int base, int bits) {
        BigInteger x = new BigInteger(s, base);
        if(x.signum() > 0) {
            if(x.bitLength() > bits) {
                System.err.println("ERROR in parsing: constant out of range: "+s);
                System.exit(1);
            }
        } else if(x.signum() < 0) {
            if(x.negate().bitLength() > bits-1
            && x.negate().subtract(new BigInteger("1")).bitLength() > bits-1) {
                System.err.println("ERROR in parsing: constant out of range: "+s);
                System.exit(1);
            }
        }
        return (int) (x.longValue() & ((1L << bits) - 1));
    }
}

/** Lexer -- reads an input line, and partitions it into a list of tokens. */
class Lexer {
    public Lexer() {
        CharSet whitespace = new Chars("\t\n\r ");
        CharSet letters = new Chars(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        CharSet lettersDigits = new Chars(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
        CharSet digits = new Chars("0123456789");
        CharSet hexDigits = new Chars("0123456789ABCDEFabcdef");
        CharSet oneToNine = new Chars("123456789");
        CharSet all = new AllChars(); 

        table = new Transition[] {
                new Transition(State.START, whitespace, State.WHITESPACE),
                new Transition(State.START, letters, State.ID),
                new Transition(State.ID, lettersDigits, State.ID),
                new Transition(State.START, oneToNine, State.INT),
                new Transition(State.INT, digits, State.INT),
                new Transition(State.START, new Chars("-"), State.MINUS),
                new Transition(State.MINUS, digits, State.INT),
                new Transition(State.START, new Chars(","), State.COMMA),
                new Transition(State.START, new Chars("("), State.LPAREN),
                new Transition(State.START, new Chars(")"), State.RPAREN),
                new Transition(State.START, new Chars("$"), State.DOLLAR),
                new Transition(State.DOLLAR, digits, State.REGISTER),
                new Transition(State.REGISTER, digits, State.REGISTER),
                new Transition(State.START, new Chars("0"), State.ZERO),
                new Transition(State.ZERO, new Chars("x"), State.ZEROX),
                new Transition(State.ZERO, digits, State.INT),
                new Transition(State.ZEROX, hexDigits, State.HEXINT),
                new Transition(State.HEXINT, hexDigits, State.HEXINT),
                new Transition(State.ID, new Chars(":"), State.LABEL),
                new Transition(State.START, new Chars(";"), State.COMMENT),
                new Transition(State.START, new Chars("."), State.DOT),
                new Transition(State.DOT, new Chars("w"), State.DOTW),
                new Transition(State.DOTW, new Chars("o"), State.DOTWO),
                new Transition(State.DOTWO, new Chars("r"), State.DOTWOR),
                new Transition(State.DOTWOR, new Chars("d"), State.DOTWORD),
                new Transition(State.COMMENT, all, State.COMMENT)
        };
    }
    /** Partitions the line passed in as input into an array of tokens.
     * The array of tokens is returned.
     */
    public Token[] scan( String input ) {
        List<Token> ret = new ArrayList<Token>();
        if(input.length() == 0) return new Token[0];
        int i = 0;
        int startIndex = 0;
        State state = State.START;
        while(true) {
            Transition t = null;
            if(i < input.length()) t = findTransition(state, input.charAt(i));
            if(t == null) {
                // no more transitions possible
                if(!state.isFinal()) {
                    System.err.println("ERROR in lexing after reading "+input.substring(0, i));
                    System.exit(1);
                }
                if( state.kind != Kind.WHITESPACE ) {
                    ret.add(new Token(state.kind,
                                input.substring(startIndex, i)));
                }
                startIndex = i;
                state = State.START;
                if(i >= input.length()) break;
            } else {
                state = t.toState;
                i++;
            }
        }
        return ret.toArray(new Token[ret.size()]);
    }

    ///////////////////////////////////////////////////////////////
    // END OF PUBLIC METHODS
    ///////////////////////////////////////////////////////////////

    private Transition findTransition(State state, char c) {
        for( int j = 0; j < table.length; j++ ) {
            Transition t = table[j];
            if(t.fromState == state && t.chars.contains(c)) {
                return t;
            }
        }
        return null;
    }

    private static enum State {
        START(null),
        DOLLAR(null),
        MINUS(null),
        REGISTER(Kind.REGISTER),
        INT(Kind.INT),
        ID(Kind.ID),
        LABEL(Kind.LABEL),
        COMMA(Kind.COMMA),
        LPAREN(Kind.LPAREN),
        RPAREN(Kind.RPAREN),
        ZERO(Kind.INT),
        ZEROX(null),
        HEXINT(Kind.HEXINT),
        COMMENT(Kind.WHITESPACE),
        DOT(null),
        DOTW(null),
        DOTWO(null),
        DOTWOR(null),
        DOTWORD(Kind.DOTWORD),
        WHITESPACE(Kind.WHITESPACE);
        State(Kind kind) {
            this.kind = kind;
        }
        Kind kind;
        boolean isFinal() {
            return kind != null;
        }
    }

    private interface CharSet {
        public boolean contains(char newC);
    }
    private class Chars implements CharSet {
        private String chars;
        public Chars(String chars) { this.chars = chars; }
        public boolean contains(char newC) {
            return chars.indexOf(newC) >= 0;
        }
    }
    private class AllChars implements CharSet {
        public boolean contains(char newC) {
            return true;
        }
    }

    private class Transition {
        State fromState;
        CharSet chars;
        State toState;
        Transition(State fromState, CharSet chars, State toState) {
            this.fromState = fromState;
            this.chars = chars;
            this.toState = toState;
        }
    }
    private Transition[] table;
}
