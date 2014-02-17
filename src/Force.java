
public class Force {

	float x, y;
	float r;
	
	Force(float x_, float y_, float r_) {
		x = x_;
		y = y_;
		r = r_;
	}
	
	float calcFalloff(float nx, float ny) {
		float dx = nx - x;
		float dy = ny - y;
		
		// falloff function: f(d) = r / d, squared for faster performance
		return r * r / (dx * dx + dy * dy);
	}

	public void setXY(float x_, float y_) {
		x = x_;
		y = y_;
	}

}
