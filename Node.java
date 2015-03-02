package interpreter;
/* created for A3, this Node class will be used as an 
 * intermediate representation of the program
 */

public class Node{
	public Node left, right, parent;
	public String val;
	public Node(){
		this.parent=null;
		this.left=null;
		this.right=null;
		this.val="";
	}
	public Node(String s){
		this();
		this.val=s;
	}
	public Node(String s, Node l, Node r){
		this();
		this.left = l;
		if(l!=null)
			l.parent=this;
		this.right = r;
		if(r!=null)
			r.parent=this;
		this.val=s;
	}
	public void setLeft(Node l){
		this.left = l;
		if(l!=null)
			l.parent=this;
	}
	public void setLeft(Node l, boolean b){
		this.left = l;
		if(b)
			if(l!=null)
				l.parent=this;
	}
	public void setRight(Node r){
		this.right = r;
		if(r!=null)
			r.parent=this;
	}
	public void setRight(Node r, boolean b){
		this.right = r;
		if(b)
			if(r!=null)
				r.parent=this;
	}
	public void setLeftAndRight(Node l, Node r){
		this.setLeft(l);
		this.setRight(r);
	}
	public Symbol toSymbol(){
		return new Symbol(this.val, "var");
	}
	public Node goToEnd(){
		return goToEnd(this);
	}
	private Node goToEnd(Node n){
		while(n.right!=null){
			n=n.right;
		}
		return n;
	}
	public Node goToTop(){
		return goToTop(this);
	}
	private Node goToTop(Node n){
		while(n.parent!=null){
			n=n.parent;
		}
		return n;
	}
	@Override
	public String toString(){
		String s = "";
		s+="{val: "+this.val+", left: ";
		if(left!=null){
			s+=this.left.val;
		}else{
			s+="null";
		}
		s+=", right: ";
		if(right!=null){
			s+=this.right.val;
		}else{
			s+="null";
		}
		s+=", parent: ";
		if(parent!=null){
			s+=this.parent.val;
		}else{
			s+="null";
		}
		s+="}";
		return s;
	}
}