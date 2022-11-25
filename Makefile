all:
	jflex src/LexicalAnalyzer.flex
	javac -d bin -cp src/ src/Main.java
	jar cfe dist/part1.jar Main -C bin .
#	javadoc -private src/Main.java -d doc/javadoc

testing:
	java -jar dist/part1.jar -wt Factorial.tex test/Factorial.fs

testFibo:
	java -jar dist/part1.jar test/Fibo.fs

testOpe:
	java -jar dist/part1.jar test/Operators.fs
