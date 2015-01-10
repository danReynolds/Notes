import java.util.*;
import java.math.*;
import java.util.*;

public class WLPPScan {

  public static final void main(String[] args) {
    new WLPPScan().run();
  }

  private Lexer lexer = new Lexer();

  private void run() {
    Scanner in = new Scanner(System.in);
    ArrayList<String> printValues =  new ArrayList<String>();


    while(in.hasNextLine()) {
      String line = in.nextLine();
      Token[] tokens;
      tokens = lexer.scan(line);
      for (int x = 0; x < tokens.length; x++) {
        printValues.add(tokens[x].kind + " " + tokens[x].lexeme);
      }
    }
    for (int x = 0; x < printValues.size(); x++)
      System.out.println(printValues.get(x));
    return;
  }
}

// the kinds of tokens

enum Kind {
  ID,       // a string consisting of a letter in range a-z or A-Z followed by zero or more letters/digits
  NUM,      // a string consisting of a single digit 0-9 or two or more not starting 0
  LPAREN,
  RPAREN,
  LBRACE,
  RBRACE,
  RETURN, 
  IF,
  ELSE,
  WHILE,
  PRINTLN,
  WAIN,
  BECOMES,
  INT,
  EQ,
  NE,
  LT,
  GT,
  LE,
  GE,
  PLUS,
  MINUS,
  STAR,
  SLASH,
  COMMA,
  SEMI,
  NEW,
  DELETE,
  LBRACK,
  RBRACK,
  AMP,
  NULL,
  BANG,
  PCT,
  COMMENT,
  WHITESPACE;
}

class Token {
  public Kind kind;
  public String lexeme;

  public Token(Kind kind, String lexeme) {
    this.kind = kind;
    this.lexeme = lexeme;
  }
}

// reads an input line and partitions it into a list of tokens

class Lexer {
  public Lexer() {
    CharSet whitespace = new Chars(" \t\n\r");
    CharSet letters = new Chars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    CharSet digitsNoZero = new Chars("123456789");
    CharSet digits = new Chars("0123456789");
    CharSet alphaletters = new Chars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
    CharSet all = new Chars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789%&*(){};,[]-+!/\t\n\r' ");

    table = new Transition[] {
      new Transition(State.START, new Chars("r"), State.R),
      new Transition(State.START, new Chars("("), State.LPAREN),
      new Transition(State.START, new Chars(")"), State.RPAREN),
      new Transition(State.START, new Chars("["), State.LBRACK),
      new Transition(State.START, new Chars("]"), State.RBRACK),
      new Transition(State.START, new Chars("{"), State.LBRACE),
      new Transition(State.START, new Chars("}"), State.RBRACE),
      new Transition(State.START, new Chars("*"), State.STAR),
      new Transition(State.START, new Chars("/"), State.SLASH),
      new Transition(State.START, new Chars("%"), State.PCT),
      new Transition(State.START, new Chars("<"), State.LT),
      new Transition(State.START, new Chars(">"), State.GT),
      new Transition(State.START, new Chars("="), State.BECOMES),
      new Transition(State.START, new Chars("-"), State.MINUS),
      new Transition(State.START, new Chars("+"), State.PLUS),
      new Transition(State.START, new Chars("!"), State.BANG), 
      new Transition(State.START, new Chars("i"), State.I),
      new Transition(State.START, new Chars("p"), State.P),
      new Transition(State.START, new Chars("e"), State.E),
      new Transition(State.START, new Chars("w"), State.W),
      new Transition(State.START, new Chars(","), State.COMMA),
      new Transition(State.START, new Chars(";"), State.SEMI),
      new Transition(State.START, new Chars("d"), State.D),
      new Transition(State.START, new Chars("&"), State.AMP),
      new Transition(State.START, new Chars("N"), State.NCAPS),
      new Transition(State.START, new Chars("n"), State.N),
      new Transition(State.START, whitespace, State.WHITESPACE),
      new Transition(State.START, digitsNoZero, State.NUM),
      new Transition(State.START, new Chars("0"), State.ZERO),
      new Transition(State.START, letters, State.ID),
      new Transition(State.R, new Chars("e"), State.RE),
      new Transition(State.R, alphaletters, State.ID),
      new Transition(State.RE, new Chars("t"), State.RET),
      new Transition(State.RE,  alphaletters, State.ID),
      new Transition(State.RET, new Chars("u"), State.RETU),
      new Transition(State.RET, alphaletters, State.ID),
      new Transition(State.RETU, new Chars("r"), State.RETUR),
      new Transition(State.RETU, alphaletters, State.ID),
      new Transition(State.RETUR, new Chars("n"), State.RETURN),
      new Transition(State.RETUR, alphaletters, State.ID),
      new Transition(State.RETURN, alphaletters, State.ID),
      new Transition(State.ID, alphaletters, State.ID),
      new Transition(State.NUM, digits, State.NUM),
      new Transition(State.I, new Chars("f"), State.IF),
      new Transition(State.I, new Chars("n"), State.IN),
      new Transition(State.I, alphaletters, State.ID),
      new Transition(State.E, new Chars("l"), State.EL),
      new Transition(State.E, alphaletters, State.ID),
      new Transition(State.EL, new Chars("s"), State.ELS),
      new Transition(State.EL, alphaletters, State.ID),
      new Transition(State.ELS, new Chars("e"), State.ELSE),
      new Transition(State.ELS, alphaletters, State.ID),
      new Transition(State.ELSE, alphaletters, State.ID),
      new Transition(State.IF, alphaletters, State.ID),
      new Transition(State.W, new Chars("h"), State.WH),
      new Transition(State.W, new Chars("a"), State.WA),
      new Transition(State.W, alphaletters, State.ID),
      new Transition(State.WH, new Chars("i"), State.WHI),
      new Transition(State.WH, alphaletters, State.ID),
      new Transition(State.WHI, new Chars("l"), State.WHIL),
      new Transition(State.WHI, alphaletters, State.ID),
      new Transition(State.WHIL, new Chars("e"), State.WHILE),
      new Transition(State.WHIL, alphaletters, State.ID),
      new Transition(State.WHILE, alphaletters, State.ID),
      new Transition(State.P, new Chars("r"), State.PR),
      new Transition(State.P, alphaletters, State.ID),
      new Transition(State.PR, new Chars("i"), State.PRI),
      new Transition(State.PR, alphaletters, State.ID),
      new Transition(State.PRI, new Chars("n"), State.PRIN),
      new Transition(State.PRIN, new Chars("t"), State.PRINT),
      new Transition(State.PRIN, alphaletters, State.ID),
      new Transition(State.PRINT, new Chars("l"), State.PRINTL),
      new Transition(State.PRINT, alphaletters, State.ID),
      new Transition(State.PRINTL, new Chars("n"), State.PRINTLN),
      new Transition(State.PRINTL, alphaletters, State.ID),
      new Transition(State.PRINTLN, alphaletters, State.ID),
      new Transition(State.WA, new Chars("i"), State.WAI),
      new Transition(State.WA, alphaletters, State.ID),
      new Transition(State.WAI, new Chars("n"), State.WAIN),
      new Transition(State.WAI, alphaletters, State.ID),
      new Transition(State.WAIN, alphaletters, State.ID),
      new Transition(State.BECOMES, new Chars("="), State.EQ),
      new Transition(State.IN, new Chars("t"), State.INT),
      new Transition(State.IN, alphaletters, State.ID),
      new Transition(State.INT, alphaletters, State.ID),
      new Transition(State.LT, new Chars("="), State.LE),
      new Transition(State.GT, new Chars("="), State.GE),
      new Transition(State.N, new Chars("e"), State.NE),
      new Transition(State.N, alphaletters, State.ID),
      new Transition(State.NE, new Chars("w"), State.NEW),
      new Transition(State.NE, alphaletters, State.ID),
      new Transition(State.NEW, alphaletters, State.NEW),
      new Transition(State.D, new Chars("e"), State.DE),
      new Transition(State.D, alphaletters, State.ID),
      new Transition(State.DE, new Chars("l"), State.DEL),
      new Transition(State.DE, alphaletters, State.ID),
      new Transition(State.DEL, new Chars("e"), State.DELE),
      new Transition(State.DEL, alphaletters, State.ID),
      new Transition(State.DELE, new Chars("t"), State.DELET),
      new Transition(State.DELE, alphaletters, State.ID),
      new Transition(State.DELET, new Chars("e"), State.DELETE),
      new Transition(State.DELET, alphaletters, State.ID),
      new Transition(State.DELETE, alphaletters, State.ID),
      new Transition(State.NCAPS, new Chars("U"), State.NU),
      new Transition(State.NCAPS, alphaletters, State.ID),
      new Transition(State.NU, new Chars("L"), State.NUL),
      new Transition(State.NU, alphaletters, State.ID),
      new Transition(State.NUL, new Chars("L"), State.NULL),
      new Transition(State.NUL, alphaletters, State.ID),
      new Transition(State.NULL, alphaletters, State.ID),
      new Transition(State.BANG, new Chars("="), State.NEQ),
      new Transition(State.SLASH, new Chars("/"), State.COMMENT),
      new Transition(State.COMMENT, all, State.COMMENT)

      // new Transition(State.SLASH, "/", State.COMMENT),


    };
  }

  public Token[] scan(String input) {
    List<Token> tokenList = new ArrayList<Token>();
    if(input.length() == 0) return new Token[0];
    int i = 0;
    State validToStates[] = {State.ID, State.BECOMES, State.NUM, State.RETURN, State.IF, State.ELSE, State.WHILE, State.PRINTLN, State.WAIN, State.INT, State.NEW, State.NULL, State.DELETE, State.NEQ, State.NE, State.LE, State.GT, State.LT, State.GE, State.ZERO};
    int startIndex = 0;
    State state = State.START;
    while(true) {
        Transition t = null;
        if(i < input.length()) t = findTransition(state, input.charAt(i));
        // if (t != null)
        //   System.out.println("Transitioning from " + t.fromState + " To " + t.toState);
        if(t == null) {
            // no more transitions possible
            if(!state.isFinal() ||  i < input.length() && Arrays.asList(validToStates).contains(state) && Arrays.asList(validToStates).contains(findTransition(State.START, input.charAt(i)).toState)   ) {
                System.err.println("ERROR in lexing after reading "+input.substring(0, i));
                System.exit(1);
            }
            if( state.kind != Kind.WHITESPACE && state.kind != Kind.COMMENT) {
                tokenList.add(new Token(state.kind,
                            input.substring(startIndex, i)));
            }
            else if (state.kind == Kind.COMMENT) {
              break;
            }
            startIndex = i;
            state = State.START;
            if(i >= input.length()) break;
        } else {
            state = t.toState;
            i++;
        }
    }
    return tokenList.toArray(new Token[tokenList.size()]);
  }

  ///////////////////////////////////////////////////////////////
  // END OF PUBLIC METHODS
  ///////////////////////////////////////////////////////////////

  private static enum State {
    START(Kind.ID),
    ID(Kind.ID),
    NUM(Kind.NUM),
    LPAREN(Kind.LPAREN),
    RPAREN(Kind.RPAREN),
    LBRACE(Kind.LBRACE),
    RBRACE(Kind.RBRACE),
    R(Kind.ID),
    RE(Kind.ID),
    RET(Kind.ID),
    RETU(Kind.ID),
    RETUR(Kind.ID),
    RETURN(Kind.RETURN),
    I(Kind.ID),
    IF(Kind.IF),
    E(Kind.ID),
    EL(Kind.ID),
    ELS(Kind.ID),
    ELSE(Kind.ELSE),
    W(Kind.ID),
    WH(Kind.ID),
    WHI(Kind.ID),
    WHIL(Kind.ID),
    WHILE(Kind.WHILE),
    P(Kind.ID),
    PR(Kind.ID),
    PRI(Kind.ID),
    PRIN(Kind.ID),
    PRINT(Kind.ID),
    PRINTL(Kind.ID),
    PRINTLN(Kind.PRINTLN),
    WA(Kind.ID),
    WAI(Kind.ID),
    WAIN(Kind.WAIN),
    BECOMES(Kind.BECOMES),
    IN(Kind.ID),
    INT(Kind.INT),
    EQ(Kind.EQ),
    NE(Kind.NE),
    LT(Kind.LT),
    GT(Kind.GT),
    LE(Kind.LE),
    GE(Kind.GE),
    PLUS(Kind.PLUS),
    MINUS(Kind.MINUS),
    STAR(Kind.STAR),
    SLASH(Kind.SLASH),
    PCT(Kind.PCT),
    COMMA(Kind.COMMA),
    SEMI(Kind.SEMI),
    N(Kind.ID),
    NEW(Kind.NEW),
    D(Kind.ID),
    DE(Kind.ID),
    DEL(Kind.ID),
    DELE(Kind.ID),
    DELET(Kind.ID),
    DELETE(Kind.DELETE),
    LBRACK(Kind.LBRACK),
    RBRACK(Kind.RBRACK),
    AMP(Kind.AMP),
    NU(Kind.ID),
    NUL(Kind.ID),
    NULL(Kind.NULL),
    BANG(null),
    ZERO(Kind.NUM),
    NCAPS(Kind.ID),
    NEQ(Kind.NE),
    COMMENT(Kind.COMMENT),
    WHITESPACE(Kind.WHITESPACE);

    State(Kind kind) {
      this.kind = kind;
    }
    Kind kind;
    boolean isFinal() {
      return kind != null;
    }
  }

  private Transition findTransition(State state, char c) {
      for( int j = 0; j < table.length; j++ ) {
          Transition t = table[j];
          if(t.fromState == state && t.chars.contains(c)) {
              return t;
          }
      }
      return null;
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
