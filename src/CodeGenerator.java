import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    private ParseTree tree;
    private int varCounter = 1;
    private List<String> declaredVars = new ArrayList<String>();
    private String code;
    
    public CodeGenerator(ParseTree ast) {
        this.tree = ast;
        setInitialCode();
    }

    private void setInitialCode(){
        code = """
        @.strP = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1

        define void @println(i32 %x) {
            %1 = alloca i32, align 4
            store i32 %x, i32* %1, align 4
            %2 = load i32, i32* %1, align 4
            %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
            ret void
        }
        declare i32 @printf(i8*, ...)""" + 

        """
        @.strR = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1
        ; Function Attrs: nounwind uwtable
        define i32 @readInt() #0 {
            %x = alloca i32, align 4
            %1 = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)
            %2 = load i32, i32* %x, align 4
            ret i32 %2
        }
        
        declare i32 @__isoc99_scanf(i8*, ...) #1""";
    }

    public void generate() throws Exception {
        code += "\n@tmp = global i32 0";
        code += "\ndefine i32 @main(){";
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
                printInstruct(instruct);
                case "If":
                case "While":
            }
        }
        code += "\n    ret i32 0\n}";
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

    private void printInstruct(ParseTree instruct) throws Exception{
        String varName = instruct.getChildren().get(0).getSymbol().getValue().toString();
        if (declaredVars.contains(varName)) {
            String tmpVar = getTmpVar();
            code += "\n   " + tmpVar + " = load i32, i32* %" + varName;
            code += "\n   call void @println(i32 " + tmpVar + ")";
        }
        else{
            throw new Exception("Unknown variable : " + varName);
        }
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
                return expr(assignRight);

            case NUMBER:
            case VARNAME:
                return singleValue(assignRight);
        }
        return "";  // never happens
    }

    private String expr(ParseTree ope) throws Exception {
        List<ParseTree> children = ope.getChildren();

        if (children.size() == 0) { // number or var
            return singleValue(ope);
        }
        else {
            String left = expr(children.get(0));
            String right = expr(children.get(1));

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

    private String singleValue(ParseTree number) throws Exception {
        String tmpVar = getTmpVar();
        if (number.getSymbol().getType() == LexicalUnit.NUMBER) {
            code += "\n    store i32 " + number.getSymbol().getValue().toString() + ", i32* @tmp";
            code += "\n    " + tmpVar + " = load i32, i32* @tmp";
            return tmpVar;
        } else {
            String varName = number.getSymbol().getValue().toString();
            if (declaredVars.contains(varName)) {
                code += "\n    " + tmpVar + " = load i32, i32* %" + varName; 
                return tmpVar;
            } else {
                throw new Exception("Unknown variable : " + varName);
            } 
        }

    }
}