
import processing.core.*;

@SuppressWarnings("serial")
public class Main extends PApplet {

	Field field;

	public void setup() {
		size(600, 600);
		smooth();
		noStroke();
		ellipseMode(RADIUS);

		field = new Field(this);
	}

	public void draw() {
		background(0);

		field.display();

	}
	
	@Override
	public void mouseMoved() {
		field.forces.get(0).setXY(mouseX, mouseY);
		field.calcNodeValues();
		field.loadBoundaryNodes(1.0f);
		field.loadBlob(1.0f);
		super.mouseMoved();
	}
	
	public void keyPressed() {
		println("nodesA: " + field.nodesA.size());
		println("nodesB: " + field.nodesB.size());
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "Main"});
	}

}
