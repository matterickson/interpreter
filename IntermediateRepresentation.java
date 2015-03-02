package interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IntermediateRepresentation {
	TokenSeparator t; Token token;
	//the global symbol table to store variables and functions
	Table globalSymTable;
	String input, output, tab=""; 
	/*
	 * inDeclaration - true  = declaration of variables/functions
	 * 				   false = past declarations() and fdecls()
	 */
	boolean inDeclaration;
	/*
	 * tree - the tree rep of the program
	 * endOfElse - keeps a pin on the end of the else statement, to tie back in later
	 * endOfIf - keeps a pin on the end of the if statement, to tie back in later 
	 */
	Node tree, endOfElse, endOfIf;
	/*
	 * type - the current variables type (int, double)
	 * varOrParam - the current declaration type (variable or parameter)
	 */
	String type, varOrParam; 
	
	//tokens i use a lot. most are used to track syntax in the compiler (2 per line)
	Token sym_ob = new Token("("); Token sym_cb = new Token(")"); 
	Token sym_space = new Token(" "); Token sym_semi = new Token(";"); 
	Token sym_comma = new Token(","); Token sym_newline = new Token("\n");
	Token sym_tab = new Token("\t"); Token sym_r = new Token("\r");
	Token key_def = new Token("def", "keyword"); Token key_fed = new Token("fed", "keyword");
	Token key_int = new Token("int", "keyword"); Token key_doub = new Token("double", "keyword");
	Token key_var = new Token("var", "keyword"); Token key_param = new Token("param", "keyword");
	Token key_if = new Token("if", "keyword"); Token key_then = new Token("then", "keyword");
	Token key_else = new Token("else", "keyword"); Token key_true = new Token("true", "keyword");
	Token key_false = new Token("false", "keyword"); Token key_fi = new Token("fi", "keyword");
	Token key_whi = new Token("while", "keyword"); Token key_do = new Token("do", "keyword");
	Token key_od = new Token("od", "keyword"); Token key_print = new Token("print", "keyword");
	Token key_ret = new Token("return", "keyword"); Token key_or = new Token("or", "keyword");
	Token key_and = new Token("and", "keyword"); Token key_not = new Token("not", "keyword");
	
	public IntermediateRepresentation(String str) {
		input = str;
		
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
		
		try{
			tree = program(globalSymTable);
			tree = tree.goToTop();
		}catch(Exception e){
			
		}

	    //flush out all the data and reset the PrintStream for System.out
	    System.out.flush();
	    System.setOut(old);
		
		output = baos.toString();
	}
	
	public Node getTree(){
		return tree;
	}
	
	private Node program(Table sym){
		//returns the next Token object in the input
		token = t.getNextToken();
		inDeclaration = true;
		varOrParam = "var";
		Node tree = declarations(sym);
		if(tree!=null){
			tree = tree.goToEnd();
			tree.setRight(fdecls(sym));
		}else{
			tree = fdecls(sym);
		}
		
		if(tree!=null){
			tree = tree.goToEnd();
			tree.setRight(statement_seq(sym));	
		}else{
			tree=statement_seq(sym);
		}
		match(new Token("."));
		return tree.goToTop();
	}
	
	private Node declarations(Table sym) {
		if(token.compareTo(key_int)==0){
			Node n = new Node(type(), decl(sym), declarations(sym));
			match(sym_semi);
			return n;
		}else if(token.compareTo(key_doub)==0){
			Node n = new Node(type(), decl(sym), declarations(sym));
			match(sym_semi);
			return n;
		}else{
			//FOLLOW
			return null;
		}	
	}
	private Node decl(Table sym) {
		//must have had to declare a new int or double
		Node n = varlist(sym);
		match(sym_semi);
		return n;
	}
	private String type() {
		if(match(key_int)){
			type = "int";
			return type;
		}else{
			match(key_doub);
			type = "double";
			return type;
		}
	}
	private Node varlist(Table sym) {
		return new Node(var(sym),varlist_R(sym),null);
	}	
	private Node varlist_R(Table sym) {
		if(match(sym_comma)) {
			return new Node(var(sym),null,varlist_R(sym));
		} else {
			//done reading in variables on this line
			return null;
		}
	}
	private String var(Table sym){
		if(inDeclaration){
			sym.addSymbol(new Symbol(token.id, varOrParam, type));
		}
		String s = token.id;//save token id to return for nodes
		match(token);
		return s;
	}

	private Node fdecls(Table sym) {		
		if(token.compareTo(key_def)==0){
			match(key_def);
			Node n = new Node("def");
			n.setLeft(fdec(sym));
			//build declarations part
			varOrParam = "var";
			n.setRight(declarations(sym.getLastSymbol().table));
			n=n.goToEnd();
			//build statement_seq part
			n.setRight(statement_seq(sym.getLastSymbol().table));
			n=n.goToEnd();
			match(key_fed);
			n.setRight(new Node("fed"));
			n=n.goToEnd();
			n.setRight(fdecls(sym));
			return n.goToTop();
		}else{
			//done function
			return null;
		}	
	}
	private Node fdec(Table sym){
		inDeclaration = true;
		varOrParam = "param";
		String returnType = type();
		Node t = new Node(fname(sym));
		match(sym_ob);
		t.setLeftAndRight(new Node(returnType), params(sym.getLastSymbol().table));
		sym.getLastSymbol().setStartNode(t);
		match(sym_cb);
		return t;
	}
	private Node params(Table sym){
		if(token.compareTo(sym_cb)==0){
			return null;
		}
		return new Node(type(),new Node(var(sym)),params_R(sym));
	}
	private Node params_R(Table sym){
		if(match(sym_comma)) {
			return new Node(type(), new Node(var(sym)),params_R(sym));
		} else {
			//done parameters for this function
			return null;
		}
	}
	private String fname(Table sym){
		Token functionName;
		if((functionName=id())!=null){
			if(inDeclaration){
				sym.addSymbol(new Symbol(functionName.id, "func", new Table(), type));
			}
			return functionName.id;
		}
		
		return "";
	}
	
	private Node statement_seq(Table sym){
		inDeclaration = false;

		if(token.compareTo(key_if)==0){
			Node n = new Node("statement", statement(sym), null);
			match(sym_semi);
			n.setRight(statement_seq(sym));
			
			if(endOfElse !=null){
				endOfElse.setRight(n.right, false);
				endOfElse = null;
			}
			if(endOfIf !=null){
				endOfIf.setRight(n.right, false);
				endOfIf = null;
			}
			return n.goToTop();
		}else if(token.compareTo(key_whi)==0){
			Node n = new Node("statement", statement(sym), null);
			match(sym_semi);
			n=n.goToEnd();
			n.setRight(statement_seq(sym));
			return n.goToTop();
		}else if(token.compareTo(key_print)==0){
			Node n = new Node("statement", statement(sym), null);
			match(sym_semi);
			n=n.goToEnd();
			n.setRight(statement_seq(sym));
			return n.goToTop();
		}else if(token.compareTo(key_ret)==0){
			Node n = new Node("statement", statement(sym), null);
			match(sym_semi);
			n=n.goToEnd();
			n.setRight(statement_seq(sym));
			return n.goToTop();
		}else{
			if(token.compareTo(key_fed)!=0 && token.compareTo(key_fi)!=0 && token.compareTo(key_else)!=0 && token.compareTo(new Token(".", null))!=0 && token.compareTo(key_od)!=0){
				Node n = new Node("statement", statement(sym), null);
				match(sym_semi);
				n=n.goToEnd();
				n.setRight(statement_seq(sym));
				return n.goToTop();
			}else{
				return null;
			}
		}
	}	

	private Node statement(Table sym){
		if(token.compareTo(key_if)==0){
			Node n = new Node("if");
			match(key_if);
			n.setLeft(bexpr(sym));
			match(key_then);
			n.setRight(new Node("then"));
			n=n.goToEnd();
			n.setRight(statement_seq(sym));
			endOfIf = n.goToEnd();
			n.setLeft(statement_R(sym));
			return n.goToTop();
		}else if(token.compareTo(key_whi)==0){
			Node n = new Node("while");
			match(key_whi);
			n.setLeft(bexpr(sym));
			match(key_do);
			n.setRight(new Node("do"));
			n=n.goToEnd();
			n.setRight(statement_seq(sym));
			match(key_od);
			n=n.goToEnd();
			n.setRight(new Node("od"));
			return n.goToTop();
		}else if(token.compareTo(key_print)==0){
			Node n = new Node("print");
			match(key_print);
			n.setLeft(expr(sym));
			return n.goToTop();
		}else if(token.compareTo(key_ret)==0){
			Node n = new Node("return");
			match(key_ret);
			n.setLeft(expr(sym));
			return n.goToTop();
		}else{
			//if its a variable
			if(token.attr!=null){
				if(token.attr.equals("id")){
					String s = var(sym);
					match(new Token("=", "op"));
					Node n = new Node("=", new Node(s), expr(sym));
					return n.goToTop();
				}
			}
			//follow
			if(token.compareTo(key_fed)==0){
				return null;
			}else if(token.compareTo(key_fi)==0){
				return null;
			}else if(token.compareTo(key_od)==0){
				return null;
			}else if(token.compareTo(new Token("."))==0){
				return null;
			}

			System.out.println("token: "+token);
			return null;
		}
	}
	private Node statement_R(Table sym){
		if(match(key_else)){
			Node n = statement_seq(sym);
			match(key_fi);
			n=n.goToEnd();
			endOfElse = n;
			return n.goToTop();
		}else if(match(key_fi)){
			match(sym_semi);
			Node n = statement_seq(sym);
			return n;
		}
		return null;
	}
	private Node expr(Table sym){
		Node t = term(sym);
		Node n = expr_R(sym);

		if(n==null){
			return t;
		}else{
			n.setLeft(t);
			return n.goToTop();
		}
	}
	private Node expr_R(Table sym){
		if(match(new Token("+", "op"))){
			Node n = new Node("+");
			Node t = term(sym);
			n.setRight(expr_R(sym));
			if(n.right==null){
				n.setRight(t);
				return n.goToTop();
			}else{
				n.right.setLeft(t);
				return n;
			}
		}else if(match(new Token("-", "op"))){
			Node n = new Node("-");
			Node t = term(sym);
			n.setRight(expr_R(sym));
			if(n.right==null){
				n.setRight(t);
				return n.goToTop();
			}else{
				n.right.setLeft(t);
				return n;
			}
		}else{
			return null;
		}
	}
	private Node term(Table sym){
		Node f = factor(sym);
		Node n = term_R(sym);
		if(n==null){
			return f;
		}else{
			n.setLeft(f);
			return n.goToTop();
		}
	}
	private Node term_R(Table sym){
		if(match(new Token("*", "op"))){
			Node n = new Node("*");
			Node f = factor(sym);
			n.setRight(term_R(sym));
			if(n.right==null){
				n.setRight(f);
				return n.goToTop();
			}else{
				n.right.setLeft(f);
				return n;
			}
		}else if(match(new Token("/", "op"))){
			Node n = new Node("/");
			Node f = factor(sym);
			n.setRight(term_R(sym));
			if(n.right==null){
				n.setRight(f);
				return n.goToTop();
			}else{
				n.right.setLeft(f);
				return n;
			}
		}else if(match(new Token("%", "op"))){
			Node n = new Node("%");
			Node f = factor(sym);
			n.setRight(term_R(sym));
			if(n.right==null){
				n.setRight(f);
				return n.goToTop();
			}else{
				n.right.setLeft(f);
				return n;
			}
		}else{
			return null;
		}
	}
	private Node factor(Table sym){
		if(token.attr!=null){
			if(token.attr=="num"){
				String tmp = token.id;
				match(token);
				return new Node(tmp);
			}else if(token.attr=="id"){
				//either a var or fname
				if(sym.hasSymbol(key_var.toSymbol(token.id))||sym.hasSymbol(key_param.toSymbol(token.id))){
					return new Node(var(sym));
				} else if(sym.hasSymbol(new Symbol(token.id,"func"))){
					Node n = new Node(fname(sym));
					match(sym_ob);
					n.setLeft(exprseq(sym));
					match(sym_cb);
					return n;
				} else if(globalSymTable.hasSymbol(new Symbol(token.id,"func"))){
					Node n = new Node(fname(sym));
					match(sym_ob);
					n.setLeft(exprseq(sym));
					match(sym_cb);
					return n;
				}
				//didn't find the symbol
				return null;
			}else{
				return null;
			}
		}else if(match(sym_ob)){
			Node n = expr(sym);
			match(sym_cb);
			return n;
		}

		return null;
	}
	private Node exprseq(Table sym){
		if(token.compareTo(sym_cb)==0){
			return null;
		}
		Node params = new Node("param");
		params.setLeft(expr(sym));
		params.setRight(exprseq_R(sym));
		return params;
	}
	private Node exprseq_R(Table sym){
		if(match(sym_comma)) {
			Node n = new Node("param");
			n.setLeft(expr(sym));
			n.setRight(exprseq_R(sym));
			return n;
		} else {
			return null;
		}
	}
	
	private Node bexpr(Table sym){
		Node b_term = bterm(sym);
		Node n = bexpr_R(sym);
		if(n==null){
			return b_term; 
		}else{
			n.setLeft(b_term);
			return n.goToTop();
		}
	}
	private Node bexpr_R(Table sym){
		if(token.compareTo(new Token("or", "keyword"))==0){
			match(key_or);
			Node n = new Node("or");
			Node b_term = bterm(sym);
			n.setRight(bexpr_R(sym));
			if(n.right==null){
				n.setRight(b_term);
				return n;
			}else{
				n.right.setLeft(b_term);
				return n;
			}
		}else{
			return null;
		}
	}
	private Node bterm(Table sym){
		Node b_factor = bfactor(sym);
		Node n = bterm_R(sym);
		if(n==null){
			return b_factor; 
		}else{
			n.setLeft(b_factor);
			return n.goToTop();
		}
	}
	private Node bterm_R(Table sym){
		if(token.compareTo(key_and)==0){
			match(key_and);
			Node n = new Node("and");
			Node b_factor = bfactor(sym);
			n.setRight(bterm_R(sym));
			if(n.right==null){
				n.setRight(b_factor);
				return n;
			}else{
				n.right.setLeft(b_factor);
				return n;
			}
		}else{
			return null;
		}
	}
	private Node bfactor(Table sym){
		if(token.compareTo(sym_ob)==0){
			match(sym_ob);
			Node expr1 = expr(sym);
			Node n = comp(sym);
			Node expr2 = expr(sym);
			n.setLeftAndRight(expr1, expr2);
			match(sym_cb);
			return n;
		}else if(token.compareTo(key_not)==0){
			match(key_not);
			Node n = new Node("not", bfactor(sym), null);
			return n;
		}else{
			return null;
		}
	}
	private Node comp(Table sym){
		if(token.compareTo(new Token("<", "op"))==0){
			match(new Token("<", "op"));
			return new Node("<");
			//return match(new Token("<", "op"));
		}else if(token.compareTo(new Token(">", "op"))==0){
			match(new Token(">", "op"));
			return new Node(">");
		} else if(token.compareTo(new Token("=", "op"))==0){
			match(new Token("=", "op"));
			return new Node("=");
		} else if(token.compareTo(new Token(">=", "op"))==0){
			match(new Token(">=", "op"));
			return new Node(">=");
		} else if(token.compareTo(new Token("<>", "op"))==0){
			match(new Token("<>", "op"));
			return new Node("<>");
		} else if(token.compareTo(new Token("<=", "op"))==0){
			match(new Token("<=", "op"));
			return new Node("<=");
		} else {
			System.out.println("Operand expected (in comp): "+token);
			return null;
		}
	}
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
			token = t.getNextToken();
			return true;
		} else {
			//System.out.println("Unable to match.  Expected: "+c+"    Received token: "+token);
			return false;
		}
	}	
}
