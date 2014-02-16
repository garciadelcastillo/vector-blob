
import processing.core.*;

public class Main extends PApplet {

	Field field;

	public void setup() {
		size(600, 600);
		smooth();
		noStroke();

		field = new Field(this);
	}

	public void draw() {
		background(0);

		field.display();

	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "Main"});
	}

}
