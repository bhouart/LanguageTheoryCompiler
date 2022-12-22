import java.util.ArrayList;

/**
 * Class that parses a list of tokens to build a syntax tree
 */
public class Parser {
    private ArrayList<Symbol> symbolList;
    private ArrayList<Integer> ruleSequence = new ArrayList<>();
    private ParseTree root;
    /**
     * Checks if the next symbol has the expected type
     * Removes it from the list
     * Add it to the tree
     * @param lu expected type
     * @param node node where symbol must be attached
     * @throws Exception
     */
    void match(LexicalUnit lu, ParseTree node) throws Exception {
        Symbol s = symbolList.get(0);
        if (s.getType() != lu) {
            error(s);
        } else {
            node.addChild(new ParseTree(s));
            symbolList.remove(0);
        }
    }

    /**
     * throws error
     * @param s the unexpected symbol
     * @throws Exception
     */
    void error(Symbol s) throws Exception {
        throw new Exception("\nUNEXPECTED TOKEN AT LINE " + s.getLine() + " : \n" + s.toString()+ "\n");
    }

    /**
     * 
     * @return the next symbol from the list
     */
    Symbol nextToken() {return symbolList.get(0);}

    /**
     * 
     * @return return the built tree
     */
    public ParseTree getTree() {
        return this.root;
    }

    /**
     * 
     * @return the rule sequence
     */
    public ArrayList<Integer> getRuleSequence() {
        return this.ruleSequence;
    }

    /**
     * first method called by parser
     * @throws Exception
     */
    void program() throws Exception {
        root = new ParseTree(new Symbol(null, "Program"));
        ruleSequence.add(1);
        match(LexicalUnit.BEGIN, root);
        match(LexicalUnit.PROGNAME, root);
        code(root);
        match(LexicalUnit.END, root);
    }

    /**
     * method for "code" variable
     * @param parent parent node
     * @throws Exception
     */
    void code(ParseTree parent) throws Exception {
        Symbol tok = nextToken();
        // follow set, if code = ε
        switch(tok.getType()) {
            case END:
            case ELSE:
                ruleSequence.add(3);
                return;
        }
        // else
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
        parent.addChild(node);
        ruleSequence.add(2);
        instruction(node);
        match(LexicalUnit.COMMA, node);
        code(node);
    }

    /**
     * method for "instruction" variable
     * @param parent parent node
     * @throws Exception
     */
    void instruction(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Instruction"));
        parent.addChild(node);
        Symbol tok = nextToken();
        switch (tok.getType()) {
            case VARNAME:
                ruleSequence.add(4);
                assign(node); break;
            case IF:
                ruleSequence.add(5);
                if_(node); break;
            case WHILE:
                ruleSequence.add(6);
                while_(node); break;
            case PRINT:
                ruleSequence.add(7);
                print_(node); break;
            case READ:
                ruleSequence.add(8);
                read_(node); break;
            default:
                error(tok); break;
        }
    }

    /**
     * method for "assign" variable
     * @param parent parent node
     * @throws Exception
     */
    void assign(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Assign"));
        parent.addChild(node);
        ruleSequence.add(9);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.ASSIGN, node);
        exprArith(node);
    }

    /**
     * method for "cond" variable
     * @param parent parent node
     * @throws Exception
     */
    void cond(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Cond"));
        parent.addChild(node);
        ruleSequence.add(10);
        exprArith(node);
        comp(node);
        exprArith(node);
    }

    /**
     * method for "comp" variable
     * @param parent parent node
     * @throws Exception
     */
    void comp(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Comp"));
        parent.addChild(node);
        Symbol tok = nextToken();
        switch (tok.getType()) {
            case EQUAL:
                ruleSequence.add(11);
                match(LexicalUnit.EQUAL, node); break;
            case GREATER:
                ruleSequence.add(12);
                match(LexicalUnit.GREATER, node); break;
            case SMALLER:
                ruleSequence.add(13);
                match(LexicalUnit.SMALLER, node); break;
            default:
                error(tok); break;
        }
    }

    /**
     * method for "exprArith" variable
     * @param parent parent node
     * @throws Exception
     */
    void exprArith(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "ExprArith"));
        parent.addChild(node);
        ruleSequence.add(14);
        prod(node);
        exprArithPrime(node);
    }

    /**
     * method for "exprArith'" variable
     * @param parent parent node
     * @throws Exception
     */
    void exprArithPrime(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "ExprArithPrime"));
        
        Symbol tok = nextToken();
        switch(tok.getType()) {
            case EQUAL:
            case GREATER:
            case SMALLER:
            case RPAREN:
            case COMMA:
                ruleSequence.add(17);
                return;
            case PLUS:
                parent.addChild(node);
                ruleSequence.add(15);
                match(LexicalUnit.PLUS, node);
                prod(node);
                exprArithPrime(node);
                break;
            case MINUS:
                parent.addChild(node);
                ruleSequence.add(16);
                match(LexicalUnit.MINUS, node);
                prod(node);
                exprArithPrime(node);
                break;
            default:
                error(tok); break;
        }   

    }

    /**
     * method for "prod" variable
     * @param parent parent node
     * @throws Exception
     */
    void prod(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Prod"));
        parent.addChild(node);
        ruleSequence.add(18);
        atom(node);
        prodPrime(node);
    }

    /**
     * method for "prod'" variable
     * @param parent parent node
     * @throws Exception
     */
    void prodPrime(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "ProdPrime"));
        Symbol tok = nextToken();
        switch (tok.getType()) {
            case PLUS:
            case MINUS:
            case EQUAL:
            case GREATER:
            case SMALLER:
            case RPAREN:
            case COMMA:
                ruleSequence.add(21);
                return;
            case TIMES:
                parent.addChild(node);
                ruleSequence.add(19);
                match(LexicalUnit.TIMES, node);
                atom(node);
                prodPrime(node);
                break;
            case DIVIDE:
                ruleSequence.add(20);
                parent.addChild(node);
                match(LexicalUnit.DIVIDE, node);
                atom(node);
                prodPrime(node);
                break;
            default:
                error(tok);
                break;
        }
    }

    /**
     * method for "atom" variable
     * @param parent parent node
     * @throws Exception
     */
    void atom(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Atom"));
        parent.addChild(node);
        Symbol tok = nextToken();
        switch (tok.getType()) {
            case MINUS:
                ruleSequence.add(22);
                match(LexicalUnit.MINUS, node);
                atom(node);
                break;
            case LPAREN:
                ruleSequence.add(23);
                match(LexicalUnit.LPAREN, node);
                exprArith(node);
                match(LexicalUnit.RPAREN, node);
                break;
            case VARNAME:
                ruleSequence.add(24);
                match(LexicalUnit.VARNAME, node);
                break;
            case NUMBER:
                ruleSequence.add(25);
                match(LexicalUnit.NUMBER, node);
                break;
            default:
                error(tok); break;
        }
    }

    /**
     * method for "if" variable
     * @param parent parent node
     * @throws Exception
     */
    void if_(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "If"));
        parent.addChild(node);
        ruleSequence.add(26);
        match(LexicalUnit.IF, node);
        match(LexicalUnit.LPAREN, node);
        cond(node);
        match(LexicalUnit.RPAREN, node);
        match(LexicalUnit.THEN, node);
        code(node);
        ifSeq(node);
    }

    /**
     * method for "ifSeq" variable
     * @param parent parent node
     * @throws Exception
     */
    void ifSeq(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "IfSeq"));
        parent.addChild(node);
        Symbol tok = nextToken();
        switch (tok.getType()) {
            case END:
                ruleSequence.add(27);                
                match(LexicalUnit.END, node);
                break;
            case ELSE:
                ruleSequence.add(28);
                match(LexicalUnit.ELSE, node);
                code(node);
                match(LexicalUnit.END, node);
                break;
            default:
                error(tok); break;
        }
    }

    /**
     * method for "while" variable
     * @param parent parent node
     * @throws Exception
     */
    void while_(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "While"));
        parent.addChild(node);
        ruleSequence.add(29);
        match(LexicalUnit.WHILE, node);
        match(LexicalUnit.LPAREN, node);
        cond(node);
        match(LexicalUnit.RPAREN, node);
        match(LexicalUnit.DO, node);
        code(node);
        match(LexicalUnit.END, node);
    }

    /**
     * method for "print" variable
     * @param parent parent node
     * @throws Exception
     */
    void print_(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Print"));
        parent.addChild(node);
        ruleSequence.add(30);
        match(LexicalUnit.PRINT, node);
        match(LexicalUnit.LPAREN, node);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.RPAREN, node);
    }

    /**
     * method for "read" variable
     * @param parent parent node
     * @throws Exception
     */
    void read_(ParseTree parent) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Read"));
        parent.addChild(node);
        ruleSequence.add(31);
        match(LexicalUnit.READ, node);
        match(LexicalUnit.LPAREN, node);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.RPAREN, node);

    }

    /**
     * start the parsing
     * @param symbolList list of tokens 
     * @throws Exception
     */
    public void run(ArrayList<Symbol> symbolList) throws Exception {
        this.symbolList = symbolList;
        program();
    }
}