import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

/**
 *
 * Project Part 1: Lexical Analyzer
 *
 * @author Sarah Winter, Marie Van Den Bogaard, Léo Exibard, Gilles Geeraerts
 *
 */

public class Main{
    /**
     *
     * The scanner
     *
     * @param args  The argument(s) given to the program
     * @throws IOException java.io.IOException if an I/O-Error occurs
     * @throws FileNotFoundException java.io.FileNotFoundException if the specified file does not exist
     *
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, SecurityException{
        Parser p = new Parser();
        // Display the usage when the number of arguments is wrong (should be 1)
        if(args.length < 1){
            System.out.println("Usage:  java -jar part1.jar file.fs\n"
                             + "or\tjava "+Main.class.getSimpleName()+" file.fs");
            System.exit(0);
        }

        // Open the file given in argument
        FileReader source = new FileReader(args[args.length - 1]);
        final LexicalAnalyzer analyzer = new LexicalAnalyzer(source);

        ArrayList<Symbol> symbolList = new ArrayList();
        TreeMap<String,Symbol> variablesTable = new TreeMap<String,Symbol>();
        // symbol represents the currently read symbol
        Symbol symbol = null;
        // We iterate while we do not reach the end of the file (marked by EOS)
        while(!(symbol = analyzer.nextToken()).getType().equals(LexicalUnit.EOS)){
            symbolList.add(symbol);
            // If it is a variable, add it to the table
            if(symbol.getType().equals(LexicalUnit.VARNAME)){
                if(!variablesTable.containsKey(symbol.getValue())){
                    variablesTable.put(symbol.getValue().toString(),symbol);
                }
            }
        }
        
        try {
            p.run(symbolList);
            
            ParseTree tree = p.getTree();
            tree.removeChildIndex(3);
            tree.removeChildIndex(1);
            tree.removeChildIndex(0);
            clean(tree);
            CodeGenerator cg = new CodeGenerator(tree);
            cg.generate();
            
            
            if(args.length == 3 && args[0].equals("-wt")){
                        tree.exportTexFile(tree.toLaTeX(), args[1]);
            }
            
            String sequence = "";
            for (Integer i : p.getRuleSequence()) {
                sequence += String.valueOf(i) + ' ';
            }
            //System.out.println(sequence);
        } catch (Exception e) {
            System.out.println(e);
        } 



    }   



    private static Boolean cleanSymbol(ParseTree father, ParseTree child, int index) {
        List<ParseTree> children = child.getChildren();

        Boolean changed = false;


        if (child.getSymbol().getValue() == "Atom") {
            changed = true;

            if (children.size() == 1) {
                father.setChildIndex(children.get(0), index);
            
            } else if (children.size() == 2) {  // times -1
                ParseTree ope = new ParseTree(new Symbol(LexicalUnit.TIMES, "*"));
                ope.addChild(new ParseTree(new Symbol(LexicalUnit.NUMBER, "-1")));
                ope.addChild(children.get(1));

                father.setChildIndex(ope, index);
            
            } else if (children.size() == 3) {  // ( expr )
                    father.setChildIndex(children.get(1), index);
            }
        }

        else if (child.getSymbol().getValue() == "Prod") {   
            changed = true;
            
            if (children.size() == 1) {
                father.setChildIndex(children.get(0), index);
            
            } else {

                List<ParseTree> childrenPrime = children.get(1).getChildren();
                ParseTree ope = childrenPrime.remove(0);    // * /

                List<ParseTree> grandeFamille = new ArrayList<ParseTree>();
                grandeFamille.add(children.get(0));
                grandeFamille.addAll(childrenPrime);

                ope.setChildren(grandeFamille);
                
                while (ope.getChildren().size() == 3) {
                    ope = reducePrime(ope);
                }
                father.setChildIndex(ope, index);                
            }
        }

        else if (child.getSymbol().getValue() == "ExprArith") {
            changed = true;
            
            if (children.size() == 1) {
                father.setChildIndex(children.get(0), index);
            
            } else {
                
                List<ParseTree> childrenPrime = children.get(1).getChildren();
                ParseTree ope = childrenPrime.remove(0);

                List<ParseTree> grandeFamille = new ArrayList<ParseTree>();
                grandeFamille.add(children.get(0));
                grandeFamille.addAll(childrenPrime);

                ope.setChildren(grandeFamille);
                while (ope.getChildren().size() == 3) {
                    ope = reducePrime(ope);
                }

                father.setChildIndex(ope, index);  
            }
        } 
        
        else if (child.getSymbol().getValue() == "Assign") {
            if (children.size() == 3) {
                changed = true;
                father.getChildren().get(index).removeChildIndex(1);
            }    
        }

        else if (child.getSymbol().getValue() == "Read") { 
            if (child.getChildren().size() == 4) {
                List<ParseTree> newList = Arrays.asList(child.getChildren().get(2));
                child.setChildren(newList);
            }
            
        }

        else if (child.getSymbol().getValue() == "Print") {
            if (child.getChildren().size() == 4) {
                List<ParseTree> newList = Arrays.asList(child.getChildren().get(2));
                child.setChildren(newList);
            }
        }
         
        else if (child.getSymbol().getValue() == "Instruction") {
            changed = true;
            father.setChildIndex(child.getChildren().get(0), index);
        }

        else if (child.getSymbol().getValue() == "Code") {
            changed = true;
            father.setChildIndex(children.get(0), index);

            if (children.size() == 3) {
                ParseTree tmp = children.get(2);
                father.insertChildIndex(tmp, index+1);
            }

        }

        else if (child.getSymbol().getValue() == "If") {
            child.removeChildIndex(4);
            child.removeChildIndex(3);
            child.removeChildIndex(1);
            child.removeChildIndex(0);
        }

        else if (child.getSymbol().getValue() == "Comp") {
            father.setChildIndex(child.getChildren().get(0), index);
        }

        else if (child.getSymbol().getValue() == "While") {
            child.removeChildIndex(children.size() - 1);
            child.removeChildIndex(4);
            child.removeChildIndex(3);
            child.removeChildIndex(1);
            child.removeChildIndex(0);
        }

        else if (child.getSymbol().getValue() == "IfSeq") {
            if (children.size() == 1) {
                father.removeChildIndex(index);
            } else {
                child.removeChildIndex(children.size() - 1);
                child.removeChildIndex(0);
            }

        }


        return changed;
    }


    private static ParseTree reducePrime(ParseTree prev) {
        List<ParseTree> familleMoyenne = prev.getChildren().get(2).getChildren();
        prev.removeChildIndex(2);
        ParseTree newOpe = familleMoyenne.remove(0);
        List<ParseTree> newChildren = new ArrayList<>();
        newChildren.add(prev);
        newChildren.addAll(familleMoyenne);
        newOpe.setChildren(newChildren);
        return newOpe;
    }

    private static void clean(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        
        
        for (int i = 0; i < children.size(); i++) {
            Boolean changed = cleanSymbol(tree, children.get(i), i);
            while (changed) {
                changed = cleanSymbol(tree, children.get(i), i);
            }
        }

        for (ParseTree child : tree.getChildren()) {
            clean(child);
        }
    }








}
