
public class PointXY {

	private double x, y;
	
	public PointXY() {
		x = y = 0;
	}
	
	public PointXY(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
}
