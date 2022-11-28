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
}
