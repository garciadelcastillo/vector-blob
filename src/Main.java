
import processing.core.*;

@SuppressWarnings("serial")
public class Main extends PApplet {

	Field field;
	PFont font = createFont("Arial", 10);

	public void setup() {
		size(1200, 800);
		smooth();
		noStroke();
		ellipseMode(RADIUS);

		textFont(font);
		
		field = new Field(this);
		field.update();
	}

	public void draw() {
		background(0);

		field.display();
		drawDebugText();
	}
	
	public void drawDebugText() {
		pushStyle();
		fill(255);
		text(field.blobSorted.size(), 10, 10);
		text("iterations: " + field.tempIterations, 10, 20);
		text("closed: " + field.tempClosed, 10, 30);
		text("nodesB.size(): " + field.nodesB.size(), 10, 40);
		text("tempCheckedNodes.size(): " + field.tempCheckedNodes.size(), 10, 50);
		popStyle();
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
