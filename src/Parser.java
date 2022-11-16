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

    void assign() {}
    void if_() {}
    void while_() {}
    void print_() {}
    void read_() {}

    public static void main(String[] args) {

    }
}