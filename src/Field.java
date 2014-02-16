
import java.util.ArrayList;
import processing.core.*;

public class Field {
	PApplet p;

	public static final int RESOLUTION = 20;  // pixels between FieldPoint samples
	int pCount, px, py;  // number of points total + XY directions

	Node[][] nodes;
	ArrayList<Force> forces;

	Field(PApplet sketch) {
		p = sketch;
		px = (int) p.width / RESOLUTION + 1;
		py = (int) p.height / RESOLUTION + 1;
		pCount = px * py;
		nodes = new Node[px][py];

		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				nodes[i][j] = new Node(i, j, i * RESOLUTION, j * RESOLUTION);
			}
		}
		
		forces = new ArrayList<Force>();
		forces.add(new Force(156, 156, 40));

	}

	void display() {
		p.pushStyle();
		
		// temp display forces
		p.fill(0, 255, 0);
		for (Force f : forces) {
			p.ellipse(f.x, f.y, f.r, f.r);
		}
		
		// temp display nodes
		drawNodeDots();
		
		p.popStyle();
	}

	void update() {

	}
	
	void calcNodeValues() {
		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				
			}
		}
	}
	
	void drawNodeDots() {
		p.fill(255, 0, 0);
		for (int i = 0; i < px; i++) {
			for (int j = 0; j < py; j++) {
				p.ellipse(nodes[i][j].x, nodes[i][j].y, 5, 5);
			}
		}
	}
	
	

}
