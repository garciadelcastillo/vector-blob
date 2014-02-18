
import processing.core.*;

@SuppressWarnings("serial")
public class Main extends PApplet {

	Field field;

	public void setup() {
		size(1200, 800);
		smooth();
		noStroke();
		ellipseMode(RADIUS);

		field = new Field(this);
		field.update();
	}

	public void draw() {
		background(0);

		field.display();
	}
	
	@Override
	public void mouseMoved() {
		field.forces.get(0).setXY(mouseX, mouseY);
		field.update();
		super.mouseMoved();
	}
	
	public void keyPressed() {
//		println("nodesA: " + field.nodesA.size());
//		println("nodesB: " + field.nodesB.size());
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "Main"});
	}

}
