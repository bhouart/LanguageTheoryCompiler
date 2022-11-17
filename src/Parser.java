public class Parser {
    void match(LexicalUnit lu) {}
    void error(Symbol s) {}
    Symbol nextToken() {return null;}


    void program() {
        match(LexicalUnit.BEGIN);
        match(LexicalUnit.PROGNAME);
        code();
        match(LexicalUnit.END);
    }

    void code() {
        Symbol tok = nextToken();
        // follow set, if code = ε
        switch(tok.getType()) {
            case END:
            case ELSE:
                return;
        }
        // else
        instruction();
        match(LexicalUnit.COMMA);
        code();
    }

    void instruction() {
        Symbol tok = nextToken();
        switch (tok.getType()) {
            case VARNAME:
                assign(); break;
            case IF:
                if_(); break;
            case WHILE:
                while_(); break;
            case PRINT:
                print_(); break;
            case READ:
                read_(); break;
            default:
                error(tok); break;
        }
    }

    void assign() {
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.ASSIGN);
        exprArith();
    }

    void cond() {
        exprArith();
        comp();
        exprArith();
    }

    void comp() {
        Symbol tok = nextToken();
        switch (tok.getType()) {
            case EQUAL:
                match(LexicalUnit.EQUAL); break;
            case GREATER:
                match(LexicalUnit.GREATER); break;
            case SMALLER:
                match(LexicalUnit.SMALLER); break;
            default:
                error(tok); break;
        }
    }

    void exprArith() {
        prod();
        exprArithPrime();
    }

    void exprArithPrime() {
        Symbol tok = nextToken();
        switch(tok.getType()) {
            case EQUAL:
            case GREATER:
            case SMALLER:
            case RPAREN:
            case COMMA:
                return;
            case PLUS:
                match(LexicalUnit.PLUS);
                prod();
                exprArithPrime();
                break;
            case MINUS:
                match(LexicalUnit.MINUS);
                prod();
                exprArithPrime();
                break;
            default:
                error(tok); break;
        }   

    }

    void if_() {}
    void while_() {}
    void print_() {}
    void read_() {}
    void prod() {}

    public static void main(String[] args) {

    }
}