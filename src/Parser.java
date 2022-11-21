import java.util.ArrayList;

public class Parser {
    private ArrayList<Symbol> symbolList;
    private ArrayList<Integer> ruleSequence = new ArrayList<>();
    private ParseTree root;
    void match(LexicalUnit lu, ParseTree node) throws Exception {
        Symbol s = symbolList.get(0);
        if (s.getType() != lu) {
            error(s);
        } else {
            node.addChild(new ParseTree(s));
            symbolList.remove(0);
        }
    }
    void error(Symbol s) throws Exception {
        throw new Exception("\nUNEXPECTED TOKEN AT LINE " + s.getLine() + " : \n" + s.toString()+ "\n");
    }
    Symbol nextToken() {return symbolList.get(0);}

    public ParseTree getTree() {
        return this.root;
    }

    public ArrayList<Integer> getRuleSequence() {
        return this.ruleSequence;
    }


    void program() throws Exception {
        root = new ParseTree(new Symbol(null, "Program"));
        ruleSequence.add(1);
        match(LexicalUnit.BEGIN, root);
        match(LexicalUnit.PROGNAME, root);
        code(root);
        match(LexicalUnit.END, root);
    }

    void code(ParseTree father) throws Exception {
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
        father.addChild(node);
        ruleSequence.add(2);
        instruction(node);
        match(LexicalUnit.COMMA, node);
        code(node);
    }

    void instruction(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Instruction"));
        father.addChild(node);
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

    void assign(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Assign"));
        father.addChild(node);
        ruleSequence.add(9);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.ASSIGN, node);
        exprArith(node);
    }

    void cond(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Cond"));
        father.addChild(node);
        ruleSequence.add(10);
        exprArith(node);
        comp(node);
        exprArith(node);
    }

    void comp(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Comp"));
        father.addChild(node);
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

    void exprArith(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "ExprArith"));
        father.addChild(node);
        ruleSequence.add(14);
        prod(node);
        exprArithPrime(node);
    }

    void exprArithPrime(ParseTree father) throws Exception {
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
                father.addChild(node);
                ruleSequence.add(15);
                match(LexicalUnit.PLUS, node);
                prod(node);
                exprArithPrime(node);
                break;
            case MINUS:
                father.addChild(node);
                ruleSequence.add(16);
                match(LexicalUnit.MINUS, node);
                prod(node);
                exprArithPrime(node);
                break;
            default:
                error(tok); break;
        }   

    }

    void prod(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Prod"));
        father.addChild(node);
        ruleSequence.add(18);
        atom(node);
        prodPrime(node);
    }

    void prodPrime(ParseTree father) throws Exception {
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
                father.addChild(node);
                ruleSequence.add(19);
                match(LexicalUnit.TIMES, node);
                atom(node);
                prodPrime(node);
                break;
            case DIVIDE:
                ruleSequence.add(20);
                father.addChild(node);
                match(LexicalUnit.DIVIDE, node);
                atom(node);
                prodPrime(node);
                break;
            default:
                error(tok);
                break;
        }
    }

    void atom(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Atom"));
        father.addChild(node);
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


    void if_(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "If"));
        father.addChild(node);
        ruleSequence.add(26);
        match(LexicalUnit.IF, node);
        match(LexicalUnit.LPAREN, node);
        cond(node);
        match(LexicalUnit.RPAREN, node);
        match(LexicalUnit.THEN, node);
        code(node);
        ifSeq(node);
    }

    void ifSeq(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "IfSeq"));
        father.addChild(node);
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

    void while_(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "While"));
        father.addChild(node);
        ruleSequence.add(29);
        match(LexicalUnit.WHILE, node);
        match(LexicalUnit.LPAREN, node);
        cond(node);
        match(LexicalUnit.RPAREN, node);
        match(LexicalUnit.DO, node);
        code(node);
        match(LexicalUnit.END, node);
    }

    void print_(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Print"));
        father.addChild(node);
        ruleSequence.add(30);
        match(LexicalUnit.PRINT, node);
        match(LexicalUnit.LPAREN, node);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.RPAREN, node);
    }
    void read_(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Read"));
        father.addChild(node);
        ruleSequence.add(31);
        match(LexicalUnit.READ, node);
        match(LexicalUnit.LPAREN, node);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.RPAREN, node);

    }


    public void run(ArrayList<Symbol> symbolList) throws Exception {
        this.symbolList = symbolList;
        program();
    }
}