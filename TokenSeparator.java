package interpreter;

public class TokenSeparator {
	char[] delimeter = {'(', ')', ' ', ';', ',', '\n', '\t', '\r', (char)65535};
	char[] op = {'+', '-', '*', '/', '=', '%', '>', '<', };
	String[] keywords = {"def", "fed", "int", "double", "if", "then", "else", "true", 
			"false", "fi", "while", "do", "od", "print", "return", "or", "and", "not"};
	char la; int i; //index of input
	String s;
	public TokenSeparator(String u) {
		s = u;
		i = -1;
		la = getChar();
	}
	
	public String getNext(){
		String token = "";
		
		while(!isDelimiter(la)&&!isOperator(la)){
			token+=la;
			match(la);
		}
		
		//builds operators and negative numbers
		if (token.equals("")&&isOperator(la)){
			token+=la;
			match(la);
			if(token.equals("-")){
				if(isDigit(la))
					while(!isDelimiter(la)&&!isOperator(la)){
						token+=la;
						match(la);
					}
			}
			if(token.equals("<")){
				if(la=='>'){
					token+=la;
					match(la);
				}else if(la=='='){
					token+=la;
					match(la);
				}
			}
			if(token.equals(">")){
				if(la=='='){
					token+=la;
					match(la);
				}
			}
		}
		
		if (token.equals("")) {
			token+=la;
			match(la);
		}
		
		return token;
	}
	
	public Token analyzeToken(String t){
		if(checkIfTokenIsKeyword(t.toCharArray())){
			return new Token(t, "keyword");
		}
		
		if(checkIfTokenIsID(t.toCharArray())){
			return new Token(t, "id");
		}

		if(checkIfTokenIsNumber(t.toCharArray())){
			return new Token(t, "num");
		}
		
		if(checkIfTokenIsOperator(t.toCharArray())){
			return new Token(t, "op");
		}
		
		return new Token(t, null);
	}
	
	public Token getNextToken(){
		Token temp = analyzeToken(getNext());
		while(temp.id.equals(" ")||temp.id.equals((char)65535)||temp.id.equals("\t")||temp.id.equals("\r")||temp.id.equals("\n")){
			temp = analyzeToken(getNext());
		}
		return temp;
	}
	
	public boolean checkIfTokenIsKeyword(char[] t){
		for(int j = 0; j < keywords.length; j++){	
			if (areTheSame(t, keywords[j].toCharArray())){
				return true;
			}
		}
		return false;
	}
	
	private boolean checkIfTokenIsID(char[] t){
		if(!isLetter(t[0])){
			return false;
		}
		
		for(int i = 1; i < t.length; i++){
			if(!isLetter(t[i])==!isDigit(t[i])){
				return false;
			}
		}
		return true;
    }
	    
	private boolean checkIfTokenIsNumber(char[] t){
		if(t[0]!='-'&&t[0]!='.'&&!isDigit(t[0])){
			return false;
		}
		
		return isANumber(t[0]=='-'?1:0, t);
	}
	
	private boolean isANumber(int s, char[] t){
		boolean seenDecimal = false;
		boolean seenNumber = false;
		for(int i = s; i < t.length; i++){
			if(t[i] == '.' && !seenDecimal){
				seenDecimal=true;
			}else if(isDigit(t[i])){
				seenNumber = true;
			}else if(!isDigit(t[i])){
				return false;
			}
		}
		
		return seenNumber;
	}
	
	private boolean checkIfTokenIsOperator(char[] t){
		if (t.length==1){
	    	for(int i = 0; i < op.length; i++)
	    		if(t[0]==op[i])
	    			return true;
		} else if (t.length==2){
			if(t[0]=='<'&&t[1]=='>'){
				return true;
			}else if(t[0]=='<'&&t[1]=='='){
				return true;
			}else if(t[0]=='>'&&t[1]=='='){
				return true;
			}
		}
		return false;
	}
	
	//getChar and match(char c) are used to read through the input character by character
	private char getChar() {
		i++;
		if(i < s.length())
			return(s.charAt(i));
		else{
			return 65535;
		}
	}
    private boolean match(char c){
    	if (c==la){
    		la = getChar();
    		return true;
    	}else{
    		return false;
    	}
    }
    
    //checks if two strings are equals (assumed we couldn't use String.equals())
    private boolean areTheSame(char[] s1, char[] s2){
    	boolean matched = true; //assume its correct then check for problems
    	
    	if(s1.length!=s2.length){
    		return false;
    	}
    	
    	for(int i = 0; i < s1.length; i++){
    		matched = matched&&s1[i]==s2[i];
    	}
    	
    	return matched;
    }
    
    private boolean isDelimiter(char c){
    	for(int i = 0; i < delimeter.length; i++)
    		if(c==delimeter[i])
    			return true;
    	return false;
    }
    
    private boolean isOperator(char c){
    	for(int i = 0; i < op.length; i++)
    		if(c==op[i])
    			return true;
    	return false;
    }
    
    private boolean isLetter(char c){
    	return (c>='a'&&c<='z')||(c>='A'&&c<='Z');
    }
    
    private boolean isDigit(char c){
    	return c>='0'&&c<='9';
    }
}
