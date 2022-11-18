import java.util.ArrayList;

public class Parser {
    private ArrayList<Symbol> symbolList;
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
        throw new Exception("UNEXPECTED TOKEN : " + s.toString());
    }
    Symbol nextToken() {return symbolList.get(0);}

    public ParseTree getTree() {
        return this.root;
    }


    void program() throws Exception {
        root = new ParseTree(new Symbol(null, "Program"));
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
                return;
        }
        // else
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
        father.addChild(node);
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
                assign(node); break;
            case IF:
                if_(node); break;
            case WHILE:
                while_(node); break;
            case PRINT:
                print_(node); break;
            case READ:
                read_(node); break;
            default:
                error(tok); break;
        }
    }

    void assign(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Assign"));
        father.addChild(node);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.ASSIGN, node);
        exprArith(node);
    }

    void cond(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Cond"));
        father.addChild(node);
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
                match(LexicalUnit.EQUAL, node); break;
            case GREATER:
                match(LexicalUnit.GREATER, node); break;
            case SMALLER:
                match(LexicalUnit.SMALLER, node); break;
            default:
                error(tok); break;
        }
    }

    void exprArith(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "ExprArith"));
        father.addChild(node);
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
                return;
            case PLUS:
                father.addChild(node);
                match(LexicalUnit.PLUS, node);
                prod(node);
                exprArithPrime(node);
                break;
            case MINUS:
                father.addChild(node);
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
                return;
            case TIMES:
                father.addChild(node);
                match(LexicalUnit.TIMES, node);
                atom(node);
                prodPrime(node);
                break;
            case DIVIDE:
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
                match(LexicalUnit.MINUS, node);
                atom(node);
                break;
            case LPAREN:
                match(LexicalUnit.LPAREN, node);
                exprArith(node);
                match(LexicalUnit.RPAREN, node);
                break;
            case VARNAME:
                match(LexicalUnit.VARNAME, node);
                break;
            case NUMBER:
                match(LexicalUnit.NUMBER, node);
                break;
            default:
                error(tok); break;
        }
    }


    void if_(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "If"));
        father.addChild(node);

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
                match(LexicalUnit.END, node);
                break;
            case ELSE:
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
        match(LexicalUnit.PRINT, node);
        match(LexicalUnit.LPAREN, node);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.RPAREN, node);
    }
    void read_(ParseTree father) throws Exception {
        ParseTree node = new ParseTree(new Symbol(null, "Read"));
        father.addChild(node);
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