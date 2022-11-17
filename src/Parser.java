public class Parser {
    void match(LexicalUnit lu, ParseTree node) {}
    void error(Symbol s) {}
    Symbol nextToken() {return null;}


    void program() {
        ParseTree root = new ParseTree(new Symbol(null, "S"));
        match(LexicalUnit.BEGIN, root);
        match(LexicalUnit.PROGNAME, root);
        code(root);
        match(LexicalUnit.END, root);
    }

    void code(ParseTree father) {
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

    void instruction(ParseTree father) {
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
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

    void assign(ParseTree father) {
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
        father.addChild(node);
        match(LexicalUnit.VARNAME, node);
        match(LexicalUnit.ASSIGN, node);
        exprArith(node);
    }

    void cond(ParseTree father) {
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
        father.addChild(node);
        exprArith(node);
        comp(node);
        exprArith(node);
    }

    void comp(ParseTree father) {
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
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

    void exprArith(ParseTree father) {
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
        father.addChild(node);
        prod(node);
        exprArithPrime(node);
    }

    void exprArithPrime(ParseTree father) {
        ParseTree node = new ParseTree(new Symbol(null, "Code"));
        
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

    void if_(ParseTree father) {}
    void while_(ParseTree father) {}
    void print_(ParseTree father) {}
    void read_(ParseTree father) {}
    void prod(ParseTree father) {}

    public static void main(String[] args) {

    }
}