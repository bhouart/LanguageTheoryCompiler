import java.util.ArrayList;
import java.util.List;

/**
 * Class that generates llvm code based on an abstract tree
 */
public class CodeGenerator {

    private ParseTree tree;
    private int varCounter = 1;
    private List<String> declaredVars = new ArrayList<String>();
    private String code;
    private int ifLabelCounter = 0;
    private int whileLabelCounter = 0;
    
    public CodeGenerator(ParseTree ast) {
        this.tree = ast;
        setInitialCode();
    }

    /**
     * Adds to code string some initialisation llvm code
     */
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

        define i32 @readInt() #0 {
            %x = alloca i32, align 4
            %1 = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)
            %2 = load i32, i32* %x, align 4
            ret i32 %2
        }
        
        declare i32 @__isoc99_scanf(i8*, ...) #1""";
    }

    /**
     * Start the code generation
     */
    public void generate() throws Exception {
        code += "\n@tmp = global i32 0";
        code += "\ndefine i32 @main(){";

        loopInstruct(tree.getChildren());
        
        code += "\n    ret i32 0\n}";
        System.out.println(code);
    }

    /*
     * returns the next anonym variable
     */
    private String getTmpVar() {
        String tmpVar = "%" + Integer.toString(varCounter);
        varCounter += 1;
        return tmpVar;
    }

    /**
     * Loop that evaluates a list of instruction 
     */
    private void loopInstruct(List<ParseTree> instructs) throws Exception {
        for (ParseTree instruct : instructs) {
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
                    break;
                case "If":
                    ifInstruct(instruct);
                    break;
                case "While":
                    whileInstrut(instruct);
                    break;

            }
        }
    }

    /**
     * Evaluates while instruction
     */
    private void whileInstrut(ParseTree instruct) throws Exception {
        List<ParseTree> children = instruct.getChildren();
        
        String whileNumber = Integer.toString(whileLabelCounter);
        whileNumber += 1;

        code += "\n    br label %whileCond" + whileNumber;
        code += "\n    whileCond" + whileNumber + ":";
        String boolVal = comparator(children.get(0));
        code += "\n    br i1 " + boolVal + ", label %whileStart" + whileNumber + ", label %whileEnd" + whileNumber;

        code += "\n    whileStart" + whileNumber + ":";
        loopInstruct(children.subList(1, children.size()));

        code += "\n    br label %whileCond" + whileNumber;
        code += "\n    whileEnd" + whileNumber + ":";
    }

    /**
     * Evaluates if instruction
     */
    private void ifInstruct(ParseTree instruct) throws Exception {
        List<ParseTree> children = instruct.getChildren();
        String boolVal = comparator(children.get(0));
        String ifNumber = Integer.toString(ifLabelCounter);

        ifLabelCounter += 1;

        String jmp1;
        String jmp2;

        if (children.get(children.size() - 1).getChildren().size() == 0) {  // without else
            jmp1 = "ifTrue" + ifNumber;
            jmp2 = "ifEnd" + ifNumber;
            
        } else {    // with else
            jmp1 = "ifTrue" + ifNumber;
            jmp2 = "ifFalse" + ifNumber;
        }
        
        code += "\n    br i1 " + boolVal + ", label %" + jmp1 + ", label %" + jmp2;
        code += "\n    ifTrue" + ifNumber + ":";

        loopInstruct(children.subList(1, children.size() - 1));
        code += "\n    br label %ifEnd" + ifNumber;   // jump outside if

        if (children.get(children.size() - 1).getChildren().size() !=0) {  // else
            code += "\n    ifFalse" + ifNumber + ":";
            loopInstruct(children.get(children.size() - 1).getChildren());
            code += "\n    br label %ifEnd" + ifNumber;   // jump outside if
        }
        code += "\n    ifEnd" + ifNumber + ":";
    }

    /**
     * Evaluates a comparison
     */
    private String comparator(ParseTree instruct) throws Exception {
        List<ParseTree> children = instruct.getChildren();
        String left = expr(children.get(0));
        String right = expr(children.get(2));
        String compString = ""; // never empty
        switch(children.get(1).getSymbol().getType()){
            case EQUAL:
                compString = "eq";
                break;
            case GREATER:
                compString = "sgt";
                break;
            case SMALLER:
                compString = "slt";
                break;
        }
        String tmpVar = getTmpVar();
        code += "\n    " + tmpVar + " = icmp " + compString + " i32 " + left + ", " + right;
        return tmpVar;
    }

    /**
     * Evaluates read instruction
     */
    private void readInstruct(ParseTree instruct) {
        String left = assignLeft(instruct.getChildren().get(0));

        String tmpVar = getTmpVar();
        code += "\n    " + tmpVar + " = call i32 @readInt()";
        code += "\n    store i32 " + tmpVar + ", i32* " + left;             
    }

    /**
     * Evaluates print instruction
     */
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

    /**
     * Evaluates assign instruction
     */
    private void assignInstruct(ParseTree instruct) throws Exception {
        List<ParseTree> children = instruct.getChildren();
        String left = assignLeft(children.get(0));
        String right = assignRight(children.get(1));
        
        code += "\n    store i32 " + right + ", i32* " + left;
    }

    /**
     * Evaluates left side of an assign instruction
     */
    private String assignLeft(ParseTree assignLeft) {
        String varName = assignLeft.getSymbol().getValue().toString();
        String codeVar = "%"+varName;
        if (!declaredVars.contains(varName)) {
            code += "\n    " + codeVar + " = alloca i32";
            declaredVars.add(varName);
        }
        return codeVar;
    }

    /**
     * Evaluates right side of an assign instruction
     */
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

    /**
     * Evaluates expression recursively
     */
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

    /**
     * Stores number or variable
     */
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