package interpreter;

public class Token implements Comparable<Token> {
	String id, attr;
	
	public Token(String id){
		this.id = id;
		this.attr = null;
	}
	
	public Token(String id, String attr){
		this(id);
		this.attr = attr;
	}
	
	public Symbol toSymbol(String i){
		return new Symbol(i, id);
	}
	
	@Override
	public String toString(){
		return "{id: "+id+", attr: "+attr+"}";
	}

	@Override
	public int compareTo(Token tok) {
		if(this.id.equals(tok.id)){
			if(this.attr!=null&&tok.attr!=null){
				if(this.attr.equals(tok.attr)){
					return 0;
				}
			}else if(this.attr==null&&tok.attr==null){
				return 0;
			}
		}
		return -1;
	}
}
