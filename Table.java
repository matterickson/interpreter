package interpreter;

import java.util.LinkedList;

public class Table {
	Symbol parent;
	LinkedList<Symbol> list;
	
	public Table() {
		parent = null;
		list = new LinkedList<Symbol>();
	}

	public void addSymbol(Symbol s){
		list.add(s);
	}
	
	public boolean hasSymbol(Symbol s){
		return list.contains(s);
	}
	
	public Symbol getSymbol(Symbol s){
		if(list.contains(s))
			return list.get(list.indexOf(s));
		return null;
	}
	
	public Symbol getSymbol(String s, String t){
		if(list.contains(new Symbol(s, t))){
			return list.get(list.indexOf(new Symbol(s,t)));
		}
		return null;
	}
	
	public Symbol getLastSymbol(){
		if (list.isEmpty()){
			return null;
		}
		return list.getLast();
	}
	
	public Symbol getFirstSymbol(){
		if (list.isEmpty()){
			return null;
		}
		return list.getFirst();
	}
	
	public Symbol get(int i){
		if(list.size()>=i){
			return list.get(i);
		}
		return null;
	}
	
	@Override
	public String toString(){
		return list.toString();
	}
}