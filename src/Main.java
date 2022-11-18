import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
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
        if(args.length != 1){
            System.out.println("Usage:  java -jar part1.jar file.fs\n"
                             + "or\tjava "+Main.class.getSimpleName()+" file.fs");
            System.exit(0);
        }

        // Open the file given in argument
        FileReader source = new FileReader(args[0]);
        final LexicalAnalyzer analyzer = new LexicalAnalyzer(source);

        ArrayList<Symbol> symbolList = new ArrayList();
        TreeMap<String,Symbol> variablesTable = new TreeMap<String,Symbol>();
        // symbol represents the currently read symbol
        Symbol symbol = null;
        // We iterate while we do not reach the end of the file (marked by EOS)
        while(!(symbol = analyzer.nextToken()).getType().equals(LexicalUnit.EOS)){
            System.out.println(symbol.toString());
            symbolList.add(symbol);
            // If it is a variable, add it to the table
            if(symbol.getType().equals(LexicalUnit.VARNAME)){
                if(!variablesTable.containsKey(symbol.getValue())){
                    variablesTable.put(symbol.getValue().toString(),symbol);
                }
            }
        }
        System.out.println("\nVariables");
        // Print the variables
        for(Map.Entry<String, Symbol> variable : variablesTable.entrySet())
            System.out.println(variable.getKey()+"\t"+(variable.getValue().getLine()));
        System.out.println(symbolList.size());
        try {
            p.run(symbolList);
        } catch (Exception e) {
            System.out.println(e);
        }

        ParseTree tree = p.getTree();
        System.out.println(tree.toLaTeX());
        System.out.println("fini");
        /*
        System.out.println(tree.getLabel().toString());
        for (ParseTree t : tree.getChildren()) {
            System.out.println(t.getLabel().toString());
        }*/

    }   
}
