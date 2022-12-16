import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    private ParseTree tree;
    private int varCounter = 1;
    private List<String> declaredVars = new ArrayList<String>();
    private String code = "";
    
    public CodeGenerator(ParseTree ast) {
        this.tree = ast;
    }

    public void generate() throws Exception {
        for (ParseTree instruct : tree.getChildren()) {
            switch (instruct.getSymbol().getValue().toString()) {
                case "Assign":
                    assignInstruct(instruct);
                    code += "\n";
                    break;
                case "Read":
                    readInstruct(instruct);
                    code += "\n";
                    break;
                case "Print":
                case "If":
                case "While":
            }
        }
        System.out.println(code);
    }

    private String getTmpVar() {
        String tmpVar = "%" + Integer.toString(varCounter);
        varCounter += 1;
        return tmpVar;
    }

    private void readInstruct(ParseTree instruct) {
        String left = assignLeft(instruct.getChildren().get(0));

        String tmpVar = getTmpVar();
        code += "\n    " + tmpVar + " = call i32 @readInt()";
        code += "\n    store i32 " + tmpVar + ", i32* " + left;             
    }


    private void assignInstruct(ParseTree instruct) throws Exception {
        List<ParseTree> children = instruct.getChildren();
        String left = assignLeft(children.get(0));
        String right = assignRight(children.get(1));
        
        code += "\n    store i32 " + right + ", i32* " + left;
    }

    private String assignLeft(ParseTree assignLeft) {
        String varName = assignLeft.getSymbol().getValue().toString();
        String codeVar = "%"+varName;
        if (!declaredVars.contains(varName)) {
            code += "\n    " + codeVar + " = alloca i32";
            declaredVars.add(varName);
        }
        return codeVar;
    }

    private String assignRight(ParseTree assignRight) throws Exception {
        switch(assignRight.getSymbol().getType()) {
            case PLUS:
            case MINUS:
            case TIMES:
            case DIVIDE:
                return operator(assignRight);
            case NUMBER:
                return number(assignRight);
            case VARNAME:
                String varname = assignRight.getSymbol().getValue().toString();
                if (declaredVars.contains(varname)) {
                    String tmpVar = getTmpVar();
                    code += "\n    " + tmpVar + " = load i32, i32* %" + varname; 
                    return tmpVar;
                } else {
                    throw new Exception("Unknown variable : " + varname);
                }            
        }
        return "";  // never happens
    }

    private String operator(ParseTree ope) {
        List<ParseTree> children = ope.getChildren();

        if (children.size() == 0) { // number
            return number(ope);
        }
        else {
            String left = operator(children.get(0));
            String right = operator(children.get(1));

            String tmpVar = getTmpVar();
            
            String opeFunc = "";
            switch(ope.getSymbol().getType()) {
                case PLUS:
                    opeFunc = " add ";
                    break;
                case MINUS:
                    opeFunc = " sub ";
                    break;
                case TIMES:
                    opeFunc = " mul ";
                    break;
                case DIVIDE:
                    opeFunc = " sdiv ";
                    break;
            }
            code += "\n    " + tmpVar + " = " + opeFunc + "i32 " + left + ", " + right;
            return tmpVar;

        }
    }

    private String number(ParseTree number) {
        code += "\n    store i32 " + number.getSymbol().getValue().toString() + ", i32* @tmp";
        String tmpVar = getTmpVar();
        code += "\n    " + tmpVar + " = load i32, i32* @tmp";
        return tmpVar;
    }
}