package interpreter;

public class Symbol implements Comparable<Symbol> {
	String id, attr, type;
	double value_d; int value_i;
	Table table;
	Node startNode;
	public Symbol(String id, String attr){
		this.id = id;
		this.attr = attr;
		this.table = null;
		this.type=null;
		this.startNode=null;
	}
	
	public Symbol(String id, String attr, String t){
		this(id, attr);
		this.type = t;
	}
	
	public Symbol(String id, String attr, Table table, String t){
		this(id, attr, t);
		this.table = table;
		this.table.parent = this;
	}
	
	public void assignValue(double v){
		if(this.type.equals("int"))
			this.value_i = (int) v;
		else
			this.value_d = v;
	}
	
	public double getValue(){
		if(this.type.equals("int"))
			return this.value_i;
		else
			return this.value_d;
	}
	
	public void setStartNode(Node n){
		this.startNode = n;
	}
	
	public Node getStartNode(){
		return startNode;
	}
	
	@Override
	public String toString(){
		if(attr.equals("param")||attr.equals("var"))
			if(this.type.equals("int"))
				return "{id: "+id+", attr: "+attr+", type: "+type+", value: "+value_i+"}";
			else
				return "{id: "+id+", attr: "+attr+", type: "+type+", value: "+value_d+"}";
		return "{id: "+id+", attr: "+attr+"}";
	}

	@Override
	public int compareTo(Symbol tok) {
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
	
	@Override
	public boolean equals(Object o) {
		Symbol tok = (Symbol)o;
		if(this.id.equals(tok.id)){
			if(this.attr!=null&&tok.attr!=null){
				if(this.attr.equals(tok.attr)){
					return true;
				}
			}else if(this.attr==null&&tok.attr==null){
				return true;
			}
		}
		return false;
	}
}
