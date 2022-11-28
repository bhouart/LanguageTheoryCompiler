all:
	jflex src/LexicalAnalyzer.flex
	javac -d bin -cp src/ src/Main.java
	jar cfe dist/part2.jar Main -C bin .
#	javadoc -private src/Main.java -d doc/javadoc

testing:
	java -jar dist/part2.jar -wt Factorial.tex test/Factorial.fs

testFibo:
	java -jar dist/part2.jar -wt Fibo.tex test/Fibo.fs

testOpe:
	java -jar dist/part2.jar -wt Operators.tex test/Operators.fs

javadoc:
	cd src && javadoc -d ../doc/javadoc *.java