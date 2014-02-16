
import processing.core.*;

public class Main extends PApplet {
	
	public void setup() {
		size(600, 600);
	}
	
	public void draw() {
		background(0);
		ellipse(width / 2, height / 2, 50, 50);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "Main"});
	}

}
