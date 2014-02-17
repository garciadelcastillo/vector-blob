
import java.util.ArrayList;
import java.util.HashSet;
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
	ArrayList<Vertex> blob;
	ArrayList<Force> forces;
	
	PFont font;

	Field(PApplet sketch) {
		p = sketch;
		px = (int) p.width / RESOLUTION + 1;
		py = (int) p.height / RESOLUTION + 1;
		pCount = px * py;
		
		nodes = new Node[px][py];
		initializeNodes();
		
		nodesA = new HashSet<Node>();
		nodesB = new HashSet<Node>();
		
		blob = new ArrayList<Vertex>();
		
		forces = new ArrayList<Force>();
		forces.add(new Force(156, 156, 39));
		forces.add(new Force(p.width / 2, p.height / 2, 59));
		
		font = p.createFont("Arial", 7);

	}

	void display() {
		p.pushStyle();
	
		drawForces();
//		drawNodeDots();
//		drawNodeValues();
//		drawNodeBoundaries();
//		drawBlobCurve();
		drawBlobVertices();
		
		p.popStyle();
	}

	void update() {

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
	void loadBlob(float threshold) {
		blob.clear();
		for (Node n : nodesB) {
			for (int k = 0; k < 4; k++) {
			  if (n.nv[k] != null && nodesA.contains(n.nv[k]))
				  blob.add(interpolateNodes(n.nv[k], n, threshold));
			}
		}
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
				p.ellipse(nodes[i][j].x, nodes[i][j].y, 1, 1);
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
	
	void drawBlobVertices() {
		p.fill(0, 255, 255);
		for (Vertex v : blob) {
			p.ellipse(v.x, v.y, 2, 2);
		}
	}
	
	void drawBlobCurve() {
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
		p.textFont(font);
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
