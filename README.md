# interpreter
A year back I was working on an interpreter for a school project. We started by building a syntax analyzer, capable of colour coding keywords, variables, and numbers differently. We then moved on to analyzing syntax and finally providing the output of the code.  The algorithm in the file RecursiveDescentParser uses a recursive descent parser to determine if the program follows the correct syntax. The file IntermediateRepresentation then builds an intermediate representation of the program in a binary tree and finally Interpreter interprets the tree to determine the output.

The language supports integers and doubles, functions, recursion and a print statement.

The file Interpreter.jar is the entire project, runnable with just one click.

The file GUI.java is a runnable java file that contains a JPanel for the user to program in.  It has an input box, output box and some sample programs.

The file syntax.txt is the syntax of the project.

You are welcome to use this code for any of your own projects.  If you have any questions about this project you can email me at matterickson@hotmail.com
