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
            ParseTree newTree = new ParseTree(tree.getSymbol());
            clean(tree, newTree);
            if(args.length == 3 && args[0].equals("-wt")){
                        tree.exportTexFile(tree.toLaTeX(), args[1]);
            }
            
            String sequence = "";
            for (Integer i : p.getRuleSequence()) {
                sequence += String.valueOf(i) + ' ';
            }
            System.out.println(sequence);
        } catch (Exception e) {
            System.out.println(e);
        } 



    }   



    private static Boolean cleanSymbol(ParseTree father, ParseTree child, Integer index) {
        List<ParseTree> children = child.getChildren();

        Boolean changed = false;


        if (child.getSymbol().getValue() == "Atom") {
            
            if (children.size() == 1) {
                changed = true;
                father.setChildIndex(children.get(0), index);
            } else if (children.size() == 2) {
                changed = true;
                ParseTree ope = new ParseTree(new Symbol(LexicalUnit.TIMES, "*"));
                ope.addChild(new ParseTree(new Symbol(LexicalUnit.NUMBER, "-1")));
                ope.addChild(children.get(1));
                father.setChildIndex(ope, index);
            } else if (children.size() == 3) {
                    
                    changed = true;
                    
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

                //List<ParseTree> grandeFamille = Arrays.asList(children.get(0));
                List<ParseTree> grandeFamille = new ArrayList<ParseTree>();
                grandeFamille.add(children.get(0));
                grandeFamille.addAll(childrenPrime);

                ope.setChildren(grandeFamille);

                father.setChildIndex(ope, index);  

            }
        }

        return changed;
    }


    private static void clean(ParseTree tree, ParseTree newTree) {
        List<ParseTree> children = tree.getChildren();
        
        
        for (int i = 0; i < children.size(); i++) {
            Boolean changed = cleanSymbol(tree, children.get(i), i);
            while (changed) {
                changed = cleanSymbol(tree, children.get(i), i);
            }
        }

        for (ParseTree child : tree.getChildren()) {
            clean(child, newTree);
        }
    }








}
