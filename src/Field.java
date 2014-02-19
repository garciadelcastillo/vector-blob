
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import processing.core.*;

public class Field {
	PApplet p;

	public static final int RESOLUTION = 10;  // pixels between FieldPoint samples
	public static final float POTENTIAL_CUTOFF = 0.01f;  // a threshold below which the force doesn't contribute to the field
	public static final int A = 0, AA = 1, BB = 2, B = 3;  // node flags: external to blob, adjacent to external boundary of blob, adjacent to internal boundary, internal
	int pCount, px, py;  // number of points total + XY directions

	Node[][] nodes;
	Set<Node> nodesA;  // nodes on the outer boundary of an isocurve
	Set<Node> nodesB;  // ibid for inner boundary
	ArrayList<Force> forces;
	ArrayList<Vertex> blobUnsorted;
	ArrayList<Vertex> blobSorted;
	ArrayList<Node> tempCheckedNodes;
	ArrayList<Node> tempFirstCheckedNode = new ArrayList<Node>();
	int tempIterations = 0;
	boolean tempClosed = false;
	
	PFont font7, font5;

	Field(PApplet sketch) {
		p = sketch;
		px = (int) p.width / RESOLUTION + 1;
		py = (int) p.height / RESOLUTION + 1;
		pCount = px * py;
		
		nodes = new Node[px][py];
		initializeNodes();
		
		nodesA = new HashSet<Node>();
		nodesB = new HashSet<Node>();
		
		blobUnsorted = new ArrayList<Vertex>();
		blobSorted = new ArrayList<Vertex>();
				
		forces = new ArrayList<Force>();
		forces.add(new Force(156, 156, 69));
		forces.add(new Force(p.width / 2, p.height / 2, 109));
		
		font7 = p.createFont("Arial", 7);
		font5 = p.createFont("Arial", 5);

	}

	void display() {
		p.pushStyle();
	
//		drawForces();
		drawNodeDots();
//		drawNodeValues();
//		drawNodeBoundaries();
		drawBlobVertices(blobUnsorted, 1, 0x7f00ffff, 0, false);
		drawBlobVertices(blobSorted, 2, 0x7fffff00, 0, false);
		drawNodeList(tempFirstCheckedNode, 4, 0xffff0000, 0);
//		drawNodeList(tempCheckedNodes, 2, 0xffffffff, 0);
		drawBlobCurve(blobSorted);
		
		p.popStyle();
	}

	void update() {
		calcNodeValues();
		loadBoundaryNodes(1.0f);
		loadBlobUnsorted(1.0f);
		loadBlobSorted(1.0f);
	}
	
	void calcNodeValues() {
		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				nodes[i][j].val = 0;
				for (Force f : forces) {
					float falloff = f.calcFalloff(nodes[i][j].x, nodes[i][j].y);
					nodes[i][j].val += falloff > POTENTIAL_CUTOFF ? falloff : 0;
				}
			}
		}
	}
	
	/**
	 * iterate over neighbor nodes clockwise and add them to boundary list if on both sides of threshold value
	 * @param threshold
	 */
	void loadBoundaryNodes(float threshold) {
		nodesA.clear();
		nodesB.clear();
		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				if (nodes[i][j].val > threshold) {  // perform calculations only for nodes inside blob (usually less)
					for (int k = 0; k < 4; k++) {
						if (nodes[i][j].nv[k] != null && nodes[i][j].nv[k].val < threshold) {
							nodesB.add(nodes[i][j]);
							nodesA.add(nodes[i][j].nv[k]);
						}
					}
				}
			}
		}
	}
	
	/**
	 * iterate over inner boundary nodes, check if any neighbor is in the outer boundary, and if so, add an interpolated point to the blob
	 * @param threshold
	 */
	void loadBlobUnsorted(float threshold) {
		blobUnsorted.clear();
		for (Node n : nodesB) {
			for (int k = 0; k < 4; k++) {
				if (n.nv[k] != null && nodesA.contains(n.nv[k]))
					blobUnsorted.add(interpolateNodes(n.nv[k], n, threshold));
			}
		}
	}
	
	void loadBlobSorted(float threshold) {
		blobSorted.clear();
		ArrayList<Node> parsed = new ArrayList<Node>();  // nodes in B that were already searched
		
		Node node = nodesB.iterator().next();  // grab a random node
		tempFirstCheckedNode.clear();
		tempFirstCheckedNode.add(node);
		
		
		// start checking its neighbors
		int rotor = 64;  // int counter for 90 deg cw turns on node search
		int i = 0;  // iteration counter
		
		// check first node's for all intersections
		int initRot = 0;  // last successful rot
		for (int k = 0; k < 4; k++) {
			int rot = rotor % 4;  // find relative rotor rotation (0 up, 1 left, etc)
			if (node.nv[rot] == null) {   // if border node
				rotor++;  // advance the cw rotation counter and do nothing
			} else if (nodesA.contains(node.nv[rot])) {  // if neighbor was boundary below threshold (AA)
				blobSorted.add(interpolateNodes(node.nv[rot], node, threshold));  // add the interpolation to the blob
				initRot = rotor;
				rotor++;
			} else {  // if anything else, just keep trying
				rotor++;
			}
		}
		i++;
//		parsed.add(node);
		
		boolean stop = false;
		
		rotor = initRot + 1;
		while (!stop ) {
			for (int k = 0; k < 4; k++) {
				int rot = rotor % 4;  // find relative rotor rotation (0 up, 1 left, etc)
				if (node.nv[rot] == null) {   // if border node
					rotor++;  // advance the cw rotation counter and do nothing
				} else if (nodesA.contains(node.nv[rot])) {  // if neighbor was boundary below threshold (AA)
					blobSorted.add(interpolateNodes(node.nv[rot], node, threshold));  // add the interpolation to the blob
					rotor++;
				} else if (nodesB.contains(node.nv[rot])) {  // if neighbor i a BB one
					parsed.add(node);  // archive this node
					node = node.nv[rot];  // jump to that node
					if (parsed.contains(node)) {  // if have been here already
						stop = true;  // get the hell outtahere
						break;
					}
					rotor--;  // check paralel to previous orientation
					break;  // stop this for loop
				} else {  // if here, neighbor must (presumably) be inner non-boundary (B)
					// pivot CCW around last AA found and select the next BB
					parsed.add(node);  // archive this node
					Node AA;
					if (node.nv[rotor%4] != null) {
						AA = node.nv[(rotor-1)%4];
					} else {  // grab last not accepted node
						stop = true;  // get the hell outtahere
						break;
					}
					if (AA.nv[rot] != null) {
						node = AA.nv[rot];  // TODO apply sanity here
						rotor -= 2;
						break;
					} else {
						stop = true;
						break;
					}
				}
			}
			i++;
		}
		
		tempClosed = parsed.size() == nodesB.size();
		
		tempIterations = i;
		tempCheckedNodes = parsed;
		
//		while (i < nodesB.size()){
//			for (int k = 0; k < 4; k++) {
//				int rot = rotor % 4;  // find relative rotor rotation (0 up, 1 left, etc)
//				if (node.nv[rot] == null) {   // if border node
//					rotor++;  // advance the cw rotation counter and do nothing
//				} else if (nodesA.contains(node.nv[rot])) {  // if neighbor was boundary below threshold (AA)
//					blobSorted.add(interpolateNodes(node.nv[rot], node, threshold));  // add the interpolation to the blob
//					rotor++;  // advance the cw rotation counter
//				} else if (nodesB.contains(node.nv[rot])) {  // if neighbor is boundary over threshold (BB)
//					parsed.add(node);
//					node = node.nv[rot];  // change and check that node
//					// do not advance rotor, check same previous orientation
//				} else {  // if here, neighbor must be inner non-boundary (B)
//					// pivot CCW around last AA found and select the next BB
//					Node AA = node.nv[(rotor-1)%4];
//					if (AA.nv[rot] != null) {
//						Node BB = AA.nv[rot];  // do sanity here
//						node = BB;
//					}
//					rotor--;
//				}
//			}
//			i++;
//		}
		
		
	}
	
	
	
	
	
	Vertex interpolateNodes(Node nA, Node nB, float threshold) {
		float x = nA.x + (nB.x - nA.x) * (threshold - nA.val) / (nB.val - nA.val);
		float y = nA.y + (nB.y - nA.y) * (threshold - nA.val) / (nB.val - nA.val);
		return new Vertex(x, y);
	}
	
	void drawNodeDots() {
		p.fill(255, 0, 0);
		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				p.ellipse(nodes[i][j].x, nodes[i][j].y, .5f, .5f);
			}
		}
	}
	
	void drawNodeBoundaries() {
		p.fill(0, 0, 255);
		for (Node nA : nodesA)
			p.ellipse(nA.x, nA.y, 2, 2);
		p.fill(0, 255, 0);
		for (Node nB : nodesB)
			p.ellipse(nB.x, nB.y, 2, 2);
	}
	
//	void drawBlobVertices() {
////		p.textFont(font);
//		int count = 0;
//		p.fill(0, 255, 255);
//		for (Vertex v : blobSorted) {
//			p.ellipse(v.x, v.y, 2, 2);
////			p.text(count, v.x+count, v.y+count);
//			count++;
//		}
//	}
	
	void drawBlobVertices(ArrayList<Vertex> blob, int size, int color, int offset, boolean drawCounter) {
		p.fill(color);
		for (Vertex v : blob)
			p.ellipse(v.x + offset, v.y + offset, size, size);
		if (drawCounter) {
			int i = 0;
			p.textFont(font7);
			for (Vertex v : blob) {
				p.text(i, v.x + offset, v.y + offset);
				i++;
			}
		}
	}
	
	void drawNodeList(ArrayList<Node> nodes, float size, int color, int offset) {
		p.fill(color);
		for (Node n : nodes) 
			p.ellipse(n.x + offset, n.y + offset, size, size);
	}
	
	void drawBlobCurve(ArrayList<Vertex> blob) {
		p.pushStyle();
		p.noFill();
		p.stroke(255);
		p.strokeWeight(1);
		p.beginShape();
		for (Vertex v : blob) {
			p.curveVertex(v.x, v.y);
		}
		p.endShape();
		p.popStyle();
	}
	
	void drawNodeValues() {
		p.textFont(font7);
		p.fill(255);
		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				p.text(PApplet.nf(nodes[i][j].val, 0, 2), nodes[i][j].x, nodes[i][j].y);
			}
		}
	}
	
	void drawForces() {
		// temp display forces
		p.pushStyle();
		p.stroke(255);
		p.strokeWeight(1);
		p.noFill();
		for (Force f : forces) {
			p.ellipse(f.x, f.y, f.r, f.r);
		}
		p.popStyle();
	}
	
	void initializeNodes() {
		// create nodes
		for (int i = 0; i < px; i++)
			for (int j = 0; j < py; j++)
				nodes[i][j] = new Node(i, j, i * RESOLUTION, j * RESOLUTION);
		
		// link to neighbors
		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				Node n = nodes[i][j];
				n.nv[0] = j - 1 > 0  ? nodes[i][j-1] : null;  // top neighbor
				n.nv[1] = i + 1 < px ? nodes[i+1][j] : null;  // right neighbor
				n.nv[2] = j + 1 < py ? nodes[i][j+1] : null;  // bottom neighbor
				n.nv[3] = i - 1 > 0  ? nodes[i-1][j] : null;  // left neighbor				
			}
		}

	}
	

}
