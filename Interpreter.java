package interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Stack;

/*
 *
 */
public class Interpreter {
	String output;
	Node tree;
	Table globalSymTable;
	Stack<Table> stack;

	public Interpreter(Node t, Table s){
		tree = t;
		globalSymTable = s;
		stack = new Stack<Table>();
		stack.push(globalSymTable);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    PrintStream old = System.out;
	    System.setOut(ps);
	    
	    if(tree!=null){
			boolean fedMatched=true;
			boolean done = false;
			while((!tree.val.equals("statement")||!fedMatched) && !done){
				if(tree.val.equals("def"))
					fedMatched=false;
				if(tree.val.equals("fed"))
					fedMatched=true;
				if(tree.right!=null)
					tree=tree.right;
				else
					done = true;
			}
		    eval(tree);
	    }
		
	    System.out.flush();
	    System.setOut(old);
	    output = baos.toString();
	}
	
	private double eval(Node n){
		/*Now we're past the declarations and functions*/
		if(n.val.equals("statement")){
		    switch (n.left.val) {
	        case "=": 
	        	stack.peek().getSymbol(n.left.left.toSymbol()).assignValue(eval_expr(n.left.right));
	            break;
	        case "print":
	        	System.out.println(eval_expr(n.left.left));
	        	break;
	        case "if":
	        	if(eval_bexpr(n.left.left)){
	        		return eval(n.left.right);
	        	}else{
	        		return eval(n.left.right.left);
	        	}
	        case "while":
	        	while(eval_bexpr(n.left.left)){
	        		eval(n.left.right);
	        	}
	        	break;
	        case "return":
	        	return eval_expr(n.left.left);
		    }
		}	    
		if(n.right!=null)
	    	if(!n.right.val.equals("fed"))
	    		return eval(n.right);
		
		return 0;
	}
	
	private double eval_expr(Node n){	
		switch(n.val){
		case "+":
			return eval_expr(n.left)+eval_expr(n.right);
		case "-":
			return eval_expr(n.left)-eval_expr(n.right);
		case "*":
			return eval_expr(n.left)*eval_expr(n.right);
		case "/":
			return eval_expr(n.left)/eval_expr(n.right);
		case "%":
			return eval_expr(n.left)%eval_expr(n.right);
		}
		
		//checking for functions
		if(globalSymTable.getSymbol(new Symbol(n.val,"func"))!=null){
			Symbol function = globalSymTable.getSymbol(new Symbol(n.val,"func"));
			n=n.left;//set to parameters
			LinkedList<Double> list;
			list = new LinkedList<Double>();
			if(n!=null){
				list.add(eval_expr(n.left));
				while(n.right!=null){
					//reading in params
					n=n.right;
					list.add(eval_expr(n.left));
				}
			}
			
			//trying to load paramters
			try{
			int i = 0;
				while(!list.isEmpty()){
					function.table.get(i).assignValue(list.remove());
					i++;
				}
			}catch(Exception e){
				System.out.println("invalid parameters");
			}
			stack.push(function.table);
			double val = eval(function.getStartNode().parent);
			stack.pop();
			if(function.type.equals("int"))
				return (int)val;
			return val;
		}
		
		//checking for variables
		if (stack.peek().getSymbol(n.toSymbol())!=null){
			return stack.peek().getSymbol(n.toSymbol()).getValue();
		}else if(stack.peek().getSymbol(n.val,"param")!=null){
			return stack.peek().getSymbol(n.val,"param").getValue();
		}else if(globalSymTable.getSymbol(n.toSymbol())!=null){
			return globalSymTable.getSymbol(n.toSymbol()).getValue();
		}else if(globalSymTable.getSymbol(n.val,"param")!=null){
			return globalSymTable.getSymbol(n.val,"param").getValue();
		}

		if(checkIfStringIsNumber(n.val)){
			return Double.parseDouble(n.val);
		}
		
		System.out.println("Found nothing");
		return 0;
	}
	
	private boolean eval_bexpr(Node n){
		switch(n.val){
		case "or":
			return eval_bexpr(n.left)||eval_bexpr(n.right);
		case "and":
			return eval_bexpr(n.left)&&eval_bexpr(n.right);
		case "not":
			return !eval_bexpr(n.left);
		case "<":
			return eval_expr(n.left)<eval_expr(n.right);
		case "<=":
			return eval_expr(n.left)<=eval_expr(n.right);
		case ">":
			return eval_expr(n.left)>eval_expr(n.right);
		case ">=":
			return eval_expr(n.left)>=eval_expr(n.right);
		case "<>":
			return eval_expr(n.left)!=eval_expr(n.right);
		case "=":
			return eval_expr(n.left)==eval_expr(n.right);
		}
		
		return false;
	}
	
	private boolean checkIfStringIsNumber(String t){
		if(t.charAt(0)!='-'&&t.charAt(0)!='.'&&!isDigit(t.charAt(0))){
			return false;
		}
		
		return isANumber(t.charAt(0)=='-'?1:0, t);
	}
	private boolean isANumber(int s, String t){
		boolean seenDecimal = false;
		boolean seenNumber = false;
		for(int i = s; i < t.length(); i++){
			if(t.charAt(i)== '.' && !seenDecimal){
				seenDecimal=true;
			}else if(isDigit(t.charAt(i))){
				seenNumber = true;
			}else if(!isDigit(t.charAt(i))){
				return false;
			}
		}
		
		return seenNumber;
	}
    private boolean isDigit(char c){
    	return c>='0'&&c<='9';
    }
}
