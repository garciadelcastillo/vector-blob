
public class Node {
	
	float x, y;
	int u, v; 
	float val;
	int pos;  // a flag to mark position accorgin to blob: A, AA, BB, B (0, 1, 2, 3)
	Node[] nv;  // reference to neighbor nodes: 0 is top, clockwise orientation
	
	public Node(int u_, int v_, float x_, float y_) {
		x = x_; y = y_;
		u = u_; v = v_;
		val = 0;
		pos = 0;
		nv = new Node[4];
	}

}
