public class Coordinate {
    
    private double xPos, yPos, zPos;
	
	public Coordinate() {
		xPos = yPos = zPos = 0;
	}
	
	public Coordinate(double x, double y, double z) {
		xPos = x;
		yPos = y;
		zPos = z;
	}
	
	public double getX() {
		return xPos;
	}
	
	public double getY() {
		return yPos;
	}
	
	public double getZ() {
		return zPos;
	}
	
	public void setX(double x) {
		xPos = x;
	}
	
	public void setY(double y) {
		yPos = y;
	}
	
	public void setZ(double z) {
		zPos = z;
	}
}
