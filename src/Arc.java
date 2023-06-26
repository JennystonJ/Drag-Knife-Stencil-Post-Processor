
public class Arc {

	public final static int CLOCKWISE = 0, COUNTERCLOCKWISE = 1;
	
	private double x, y, radius;
	private int direction;
	
	public Arc() {
		x = y = radius = 0;
		direction = CLOCKWISE;
	}
	
	public Arc(double x, double y, double radius, int direction) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.direction = direction;
	}
	
	public void setArc(double x, double y, double radius, int direction) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.direction = direction;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
}