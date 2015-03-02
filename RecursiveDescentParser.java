package interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/* when constructed A2 will attempt to compile the string:
 * 
 * A2.compiled is set to:
 *   true  -  if it compiles successfully 
 *   false -  if it has syntax errors it will
 *            display them
 *
 * A2.output is everything that is sent through System.out.println
 * A2.formatted is a string containing the code properly formatted
 */
public class RecursiveDescentParser {
	TokenSeparator t; Token token;
	//the global symbol table to store variables and functions
	Table globalSymTable;
	String input, output, error;
	/*
	 * compiled - 
	 * inDeclaration - true  = declaration of variables/functions
	 * 				   false = past declarations() and fdecls()
	 */
	boolean compiled, inDeclaration;
	/*
	 * tabbed - for formatting, it tracks the amount of space to put in front of the next line
	 * type - the current variables type (int, double)
	 * formatted - the output after it is formatted properly
	 * varOrParam - the current declaration type (variable or parameter)
	 */
	private String tabbed, type, formatted, varOrParam;
	
	//tokens i use a lot. most are used to track syntax in the compiler (2 per line)
	Token sym_ob = new Token("("); Token sym_cb = new Token(")"); 
	Token sym_space = new Token(" "); Token sym_semi = new Token(";"); 
	Token sym_comma = new Token(","); Token sym_newline = new Token("\n");
	Token sym_tab = new Token("\t"); Token sym_r = new Token("\r");
	Token key_def = new Token("def", "keyword"); Token key_fed = new Token("fed", "keyword");
	Token key_int = new Token("int", "keyword"); Token key_doub = new Token("double", "keyword");
	Token key_if = new Token("if", "keyword"); Token key_then = new Token("then", "keyword");
	Token key_else = new Token("else", "keyword"); Token key_true = new Token("true", "keyword");
	Token key_false = new Token("false", "keyword"); Token key_fi = new Token("fi", "keyword");
	Token key_whi = new Token("while", "keyword"); Token key_do = new Token("do", "keyword");
	Token key_od = new Token("od", "keyword"); Token key_print = new Token("print", "keyword");
	Token key_ret = new Token("return", "keyword"); Token key_or = new Token("or", "keyword");
	Token key_and = new Token("and", "keyword"); Token key_not = new Token("not", "keyword");
	
	public RecursiveDescentParser(String str){
		input = str;
		tabbed=output=formatted="";
		//creating a custom PrintStream for System.out to print to
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    //saving the old PrintStream to set back at the end
	    PrintStream old = System.out;
	    System.setOut(ps);

	    //create a TokenSeparator for the input string
		t = new TokenSeparator(input);
		
		//initialize the global symbol table, 
		//there are also private symbol tables for functions with variables or parameters
		globalSymTable = new Table();
		
		compiled = program(globalSymTable);
		
		//if !compiled the program didn't compile successfully.  tell the user what's wrong
		if(!compiled){
			//to locate the troubled line we just need to look for the
			//last line in formatted (since it was the last line to be 
			//accepted though the compiler)
			String lineFinder = formatted; int lineNum=1;
			while(lineFinder.indexOf('\n')>0){
				lineFinder=lineFinder.substring(lineFinder.indexOf('\n')+1);
				lineNum++;
			}
			System.out.println("Error! "+error+" on line "+lineNum+": "+getLine(formatted,lineNum));
		}else{
			//if it compiled successfully we can print out the symbol tables generated
			System.out.println("Base Symbol table:");
			System.out.println(globalSymTable);
			
			for(int i = 0; i < globalSymTable.list.size(); i++){
				if(globalSymTable.list.get(i).attr.equals("func")){
					System.out.println();
					System.out.println("Symbol table for "+globalSymTable.list.get(i).id+":");
					System.out.println(globalSymTable.list.get(i).table);
				}
			}
		}

	    //flush out all the data and reset the PrintStream for System.out
	    System.out.flush();
	    System.setOut(old);
		
		output = baos.toString();
	}
	
	public String getFormattedInput(){
		return formatted;
	}
	
	private boolean program(Table sym){
		//returns the next Token object in the input
		token = t.getNextToken();
		inDeclaration = true;
		varOrParam = "var";
		
		/*
		 * this is essentially the entire recursive descent parser for the syntax.
		 * The stages of a program are the variable declarations, function
		 * declarations, statements, and finally every program must end
		 * with a .
		 */
		if (declarations(sym) && fdecls(sym) && statement_seq(sym) && match(new Token(".")))
			return true;
		
		return false;
	}
	
	private boolean declarations(Table sym) {
		/*
		 * declaring an int, 
		 * the &&declarations at the end of the return starts the 
		 * check for the next variable, descending deeper into 
		 * the program.  double does this as well
		 */
		if(token.compareTo(key_int)==0){
			return decl(sym)&&match(sym_semi)&&declarations(sym);
		//declaring a double
		}else if(token.compareTo(key_doub)==0){
			return decl(sym)&&match(sym_semi)&&declarations(sym);
		}else{
			//done declarations, onto fdecls.
			return true;
		}	
	}

	//declare a new int or double
	private boolean decl(Table sym) {
		return type()&&varlist(sym);
	}
	
	//determine the type of variable
	private boolean type() {
		if(match(key_int)){
			type = "int";
			return true;
		}else if(match(key_doub)){
			type = "double";
			return true;
		}else{
			error=("Type failed");
			return false;
		}
	}
	
	//takes in multiple variables on one line (ie "int x,y;")
	private boolean varlist(Table sym) {
		return var(sym)&&varlist_R(sym);
	}	
	private boolean varlist_R(Table sym) {
		if(match(sym_comma)) {
			return var(sym)&&varlist_R(sym);
		} else {
			//done declaring variables on this line
			return true;
		}
	}
	
	//builds symbol tables for variables and parameters
	private boolean var(Table sym){
		if(token.attr!=null && token.attr.equals("id")){
			if(inDeclaration){
				if(sym.hasSymbol(new Symbol(token.id, "var", "int"))||sym.hasSymbol(new Symbol(token.id, "var", "double"))){
					error=("Double variable declaration: "+token);
					return false;
				}else if(sym.hasSymbol(new Symbol(token.id, "param", "int"))||sym.hasSymbol(new Symbol(token.id, "param", "double"))){
					error=("Double variable declaration: "+token);
					return false;
				}else{
					sym.addSymbol(new Symbol(token.id, varOrParam, type));
				}
			}else{
				if(!sym.hasSymbol(new Symbol(token.id, "var"))&&!sym.hasSymbol(new Symbol(token.id, "param"))){
					error=("Trying to use undeclared variable: "+token);
					return false;
				}
			}
			match(token);
			return true;
		}
		
		error=("Invalid variable name: "+token);
		return false;
	}

	//function declaration
	private boolean fdecls(Table sym) {		
		if(token.compareTo(key_def)==0){
			return fdec(sym)&&fdecls(sym);
		}else{
			//done function declaration
			return true;
		}	
	}
	private boolean fdec(Table sym){
		inDeclaration = true;
		//setting to param because we are in a function right now
		varOrParam = "param";
		if(match(key_def)&&type()&&fname(sym)&&match(sym_ob)&&params(sym.getLastSymbol().table)&&match(sym_cb)){
			formatted+="\n"+tabbed;
			varOrParam = "var";
			return declarations(sym.getLastSymbol().table)&&statement_seq(sym.getLastSymbol().table)&&match(key_fed);
		}
		return false;
	}
	private boolean params(Table sym){
		if(token.compareTo(new Token(")"))==0)
			return true;
		return type()&&var(sym)&&params_R(sym);
	}
	private boolean params_R(Table sym){
		if(match(sym_comma)) {
			return type()&&var(sym)&&params_R(sym);
		} else {
			//done declaring parameters
			return true;
		}
	}
	//reading in a function name
	private boolean fname(Table sym){
		Token functionName;
		if((functionName=id())!=null){
			if(inDeclaration){
				if(!sym.hasSymbol(new Symbol(functionName.id, "func"))){
					sym.addSymbol(new Symbol(functionName.id, "func", new Table(), type));
				}else{
					error=("Double function declaration: ");
					return false;
				}	
			}else{
				if(!sym.hasSymbol(new Symbol(functionName.id, "func"))&&!globalSymTable.hasSymbol(new Symbol(functionName.id, "func"))){
					error=("Trying to use undeclared function: "+token);
					return false;
				}
			}
			
			return true;
		}
		
		error=("Invalid function name: "+token);
		return false;
	}
	
	private boolean statement_seq(Table sym){
		inDeclaration = false;

		if(token.compareTo(key_if)==0){
			return statement(sym)&&match(sym_semi)&&statement_seq(sym);
		}else if(token.compareTo(key_whi)==0){
			return statement(sym)&&match(sym_semi)&&statement_seq(sym);
		}else if(token.compareTo(key_print)==0){
			return statement(sym)&&match(sym_semi)&&statement_seq(sym);
		}else if(token.compareTo(key_ret)==0){
			return statement(sym)&&match(sym_semi)&&statement_seq(sym);
		}else{
			if(token.compareTo(key_fed)!=0 && token.compareTo(key_fi)!=0 && token.compareTo(key_else)!=0 && token.compareTo(new Token(".", null))!=0 && token.compareTo(key_od)!=0){
				return statement(sym)&&match(sym_semi)&&statement_seq(sym);
			}else{
				return true;
			}
		}
	}	

	private boolean statement(Table sym){
		//if statement
		if(token.compareTo(key_if)==0){
			return match(key_if)&&bexpr(sym)&&match(key_then)&&statement_seq(sym)&&statement_R(sym);
		//while loop
		}else if(token.compareTo(key_whi)==0){
			return match(key_whi)&&bexpr(sym)&&match(key_do)&&statement_seq(sym)&&match(key_od);
		//print statement
		}else if(token.compareTo(key_print)==0){
			return match(key_print)&&expr(sym);
		//return statement
		}else if(token.compareTo(key_ret)==0){
			return match(key_ret)&&expr(sym);
		}else{
			//if its a variable
			if(token.attr!=null){
				if(token.attr.equals("id")){
					return var(sym)&&match(new Token("=", "op"))&&expr(sym);
				}
			}
			//follow
			if(token.compareTo(key_fed)==0){
				return true;
			}else if(token.compareTo(key_fi)==0){
				return true;
			}else if(token.compareTo(key_od)==0){
				return true;
			}else if(token.compareTo(new Token("."))==0){
				return true;
			}
			error=("End of program?  Missing '.'");
			return false;
		}
	}
	private boolean statement_R(Table sym){
		if(token.compareTo(key_else)==0){
			return match(key_else)&&statement_seq(sym)&&match(key_fi);
		}else if(match(key_fi)){
			return true;
		}else{
			//if statement didn't end
			error=("If statement doesn't have matching fi: "+token);
			return false;
		}
	}
	//an expression.  it handles addition and subtraction
	//of an expression and a term
	private boolean expr(Table sym){
		return term(sym)&&expr_R(sym);
	}
	private boolean expr_R(Table sym){
		if(token.compareTo(new Token("+", "op"))==0){
			return match(new Token("+", "op"))&&term(sym)&&expr_R(sym);
		}else if(token.compareTo(new Token("-", "op"))==0){
			return match(new Token("-", "op"))&&term(sym)&&expr_R(sym);
		}else{
			//FOLLOW
			error="Operator expected";
			return true;
		}
	}
	//a term.  it handles multiplication, divison and mod
	//of a term and a factor
	private boolean term(Table sym){
		return factor(sym)&&term_R(sym);
	}
	private boolean term_R(Table sym){
		if(token.compareTo(new Token("*", "op"))==0){
			return match(new Token("*", "op"))&&factor(sym)&&term_R(sym);
		}else if(token.compareTo(new Token("/", "op"))==0){
			return match(new Token("/", "op"))&&factor(sym)&&term_R(sym);
		}else if(token.compareTo(new Token("%", "op"))==0){
			return match(new Token("%", "op"))&&factor(sym)&&term_R(sym);
		}else{
			//FOLLOW
			error="Operator expected";
			return true;
		}
	}
	//any variable, number, or function call
	private boolean factor(Table sym){
		if(token.attr!=null){
			if(token.attr=="num"){
				return match(token);
			}else if(token.attr=="id"){
				//either a var or fname
				if(var(sym)){
					return true;
				} else if(sym.hasSymbol(new Symbol(token.id,"func"))){
					return fname(sym)&&match(sym_ob)&&exprseq(sym)&&match(sym_cb);
				} else if(globalSymTable.hasSymbol(new Symbol(token.id,"func"))){
					return fname(sym)&&match(sym_ob)&&exprseq(sym)&&match(sym_cb);
				}
				//didn't find the symbol
				error=("Unable to find symbol in symbol table (in factor): "+token);
				return false;
			}else{
				error=("Token must be num or id (in factor): "+token);
				return false;
			}
		}else if(token.compareTo(sym_ob)==0){
			return match(sym_ob)&&exprseq(sym)&&match(sym_cb);
		}

		error=("Token has no attribute (in factor): "+token);
		return false;
	}
	private boolean exprseq(Table sym){
		if(token.compareTo(sym_cb)==0){
			return true;
		}
		return expr(sym)&&exprseq_R(sym);
	}
	private boolean exprseq_R(Table sym){
		if(match(sym_comma)) {
			return expr(sym)&&exprseq_R(sym);
		} else {
			//FOLLOW
			return true;
		}
	}
	
	//boolean expression. handles 'or' of booleans
	private boolean bexpr(Table sym){
		return bterm(sym)&&bexpr_R(sym);
	}
	private boolean bexpr_R(Table sym){
		if(token.compareTo(key_or)==0){
			return match(key_or)&&bterm(sym)&&bexpr_R(sym);
		}else{
			//FOLLOW
			return true;
		}
	}
	//handles 'and' of bterms and bexpr
	private boolean bterm(Table sym){
		return bfactor(sym)&&bterm_R(sym);
	}
	private boolean bterm_R(Table sym){
		if(token.compareTo(key_and)==0){
			return match(key_and)&&bfactor(sym)&&bterm_R(sym);
		}else{
			//FOLLOW
			return true;
		}
	}
	//handles 'not' and '<', '>', '<=', '>=' and '=' for booleans
	private boolean bfactor(Table sym){
		if(token.compareTo(sym_ob)==0){
			return match(sym_ob)&&expr(sym)&&comp(sym)&&expr(sym)&&match(sym_cb);
		}else if(token.compareTo(key_not)==0){
			return match(key_not)&&bfactor(sym);
		}else{
			error=("Missing boolean factor (expected '(' or 'not') in bfactor: "+token);
			return false;
		}
	}
	private boolean comp(Table sym){
		if(token.compareTo(new Token("<", "op"))==0){
			return match(new Token("<", "op"));
		}else if(token.compareTo(new Token(">", "op"))==0){
			return match(new Token(">", "op"));
		} else if(token.compareTo(new Token("=", "op"))==0){
			return match(new Token("=", "op"));
		} else if(token.compareTo(new Token(">=", "op"))==0){
			return match(new Token(">=", "op"));
		} else if(token.compareTo(new Token("<>", "op"))==0){
			return match(new Token("<>", "op"));
		} else if(token.compareTo(new Token("<=", "op"))==0){
			return match(new Token("<=", "op"));
		} else {
			error=("Operand expected (in comp): "+token);
			return false;
		}
	}
	
	//return the token if it's attribute is "id"
	private Token id(){
		Token tmp = token;
		if(token.attr!=null){
			if (token.attr.equals("id")){
				match(token);
				return tmp;
			}
		}
		
		return null;
	}
	
	//matches two tokens
	private boolean match(Token c){
		if (token.compareTo(c)==0){
			formatted=formatToken(formatted, token);
			token = t.getNextToken();
			return true;
		} else {
			if(c.compareTo(sym_semi)==0){
				error="Missing semicolon";
				//formatted=formatToken(formatted, token);
			}
			return false;
		}
	}
	//formats the input code with the correct indentation
	private String formatToken(String str, Token t){
		if(t.compareTo(key_if)==0||t.compareTo(key_whi)==0||t.compareTo(key_def)==0){
			tabbed+="    ";
		}else if(t.compareTo(key_od)==0||t.compareTo(key_fed)==0||t.compareTo(key_fi)==0||t.compareTo(key_else)==0){
			if(tabbed.length()>=4&&t.compareTo(key_else)!=0)
				tabbed=tabbed.substring(0,tabbed.length()-4);
			if(str.length()>=4)
				if(str.charAt(str.length()-1)==' '&&str.charAt(str.length()-2)==' '&&str.charAt(str.length()-3)==' '&&str.charAt(str.length()-4)==' ')
					str=str.substring(0,str.length()-4);
		}
		
		if(t.compareTo(sym_semi)==0){
			if(str.charAt(str.length()-1)==' '){
				str=str.substring(0, str.length()-1);//removes the space
			}
			str+=t.id+"\n"+tabbed;
		}else if(t.compareTo(key_then)==0||t.compareTo(key_do)==0||t.compareTo(key_fed)==0||t.compareTo(key_else)==0){		
			str+=t.id+"\n"+tabbed;
		}else if(t.compareTo(sym_ob)==0||t.compareTo(sym_cb)==0){
			if(str.charAt(str.length()-1)==' '){
				str=str.substring(0, str.length()-1);//removes the space
			}
			str+=t.id;
		}else if(t.compareTo(sym_comma)==0){
			if(str.charAt(str.length()-1)==' '){
				str=str.substring(0, str.length()-1);//removes the space
			}
			str+=t.id+" ";
		}else{
			str+=t.id+" ";
		}
		return str;
	}
	
	//reads line n from parameter str
	private String getLine(String str, int n){
		int m = 1;
		while(str.indexOf('\n')>0&&str!=null){
			str=str.substring(str.indexOf('\n')+1);
			m++;
			if(m==n){
				return str.substring(0, str.indexOf('\n')>0?str.indexOf('\n'):str.length()>0?str.length()-1:0);
			}
		}
		return "";
	}
}	


