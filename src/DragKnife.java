
public class DragKnife {

    private enum DragKnifeDirection {
		HORIZONTAL,
		VERTICAL;
	}

    private final static double DRAG_KNIFE_ORIENT_Z_HEIGHT = -0.15;
	private final static int DRAG_KNIFE_ORIENT_TIMES = 2;
	// private final static double DRAG_KNIFE_OFFSET_FROM_CENTER = 0.35;
	// private final static double DRAG_KNIFE_OVER_CUT = 0.0;
    
    private GCodeGenerator generator;
    
    private DragKnifeDirection knifeDirection;
    
    private double offset;
    private double cutDepth;
    private double clearanceHeight;
    private double rampHeight;
    private double rampLength;
    

    public DragKnife(GCodeGenerator generator, double offset, double cutDepth,
        double clearanceHeight) {
        this.generator = generator;
        this.offset = offset;
        this.cutDepth = cutDepth;
        this.clearanceHeight = clearanceHeight;

        rampHeight = 2;
        rampLength = 4;

        this.knifeDirection = null;
    }

    public void drawLine(PointXY a, PointXY b) {

        // Determine if cut direction matches current direction
        if(a.getY() == b.getY() && knifeDirection != 
            DragKnifeDirection.HORIZONTAL) {

            // Make drag knife horizontal (left to right)
            orientDragKnife(DragKnifeDirection.HORIZONTAL);
        }
        else if(a.getX() == b.getX() && knifeDirection !=
            DragKnifeDirection.VERTICAL) {

            // Make drag knfie vertical (bottom to top)
            orientDragKnife(DragKnifeDirection.VERTICAL);
        }
        else {
            System.err.println(
                "Only horizontal and vertical lines are supported.");
            return;
        }

        PointXY[] points = {a, b};
        sortPoints(points, knifeDirection);
        applyKnifeOffsets(points, knifeDirection);

        generateEntryRamp(points[0], knifeDirection);

        //initiate cut
        generator.addLinear(new Coordinate(points[0].getX(), points[0].getY(), 
            cutDepth));

        //go to cut end position
        generator.addLinear(new Coordinate(points[1].getX(), points[1].getY(), 
            cutDepth));

        generateExitRamp(points[1], knifeDirection);

    }

    private void generateEntryRamp(PointXY startPoint, 
        DragKnifeDirection direction) {
        PointXY rampStartPoint = null;
        if(direction == DragKnifeDirection.HORIZONTAL) {
            rampStartPoint = new PointXY(startPoint.getX() - rampLength, 
                startPoint.getY());
        }
        else if(direction == DragKnifeDirection.VERTICAL) {
            rampStartPoint = new PointXY(startPoint.getX(), 
                startPoint.getY() - rampLength);
        }

        generator.addLinear(new Coordinate(rampStartPoint.getX(), 
            rampStartPoint.getY(), rampHeight));
    }

    private void generateExitRamp(PointXY endPoint, 
        DragKnifeDirection direction) {
        PointXY rampExitPoint = null;

        // Determine direction
        if(direction == DragKnifeDirection.HORIZONTAL) {
            
            // Calculate horizontal ramp start
            rampExitPoint = new PointXY(endPoint.getX() + rampLength, 
                endPoint.getY());
        }
        else if(direction == DragKnifeDirection.VERTICAL) {

            // Calculate vertical ramp start
            rampExitPoint = new PointXY(endPoint.getX(), 
                endPoint.getY() + rampLength);
        }

        generator.addLinear(new Coordinate(rampExitPoint.getX(), 
            rampExitPoint.getY(), rampHeight));
    }

    private void applyKnifeOffsets(PointXY[] points,
        DragKnifeDirection direction) {
        if(direction == DragKnifeDirection.HORIZONTAL) {
            
            // Offset x component for entry
            points[0] = new PointXY(points[0].getX() - offset, 
                points[0].getY());

            // Offset x component for exit
            points[1] = new PointXY(points[1].getX() + offset, 
                points[1].getY());
        }
        else if (direction == DragKnifeDirection.VERTICAL) {

            // Offset y component for entry
            points[0] = new PointXY(points[0].getX(), 
                points[0].getY() - offset);

            // Offset y component for exit
            points[1] = new PointXY(points[1].getX(), 
                points[1].getY() + offset);
        }
    }

    private void sortPoints(PointXY[] points, 
        DragKnifeDirection direction) {

        // Sort points horizontally
        if(direction == DragKnifeDirection.HORIZONTAL && 
            Math.min(points[0].getX(), points[1].getX()) != 
            points[0].getX()) {
            // Swap point to sorted order
            if(Math.min(points[0].getX(), points[1].getX()) != 
                points[0].getX()) {
                PointXY tempPoint = points[0];
                points[0] = points[1];
                points[1] = tempPoint;
            }
        }
        // Sort points vertically
        else if(direction == DragKnifeDirection.VERTICAL && 
            Math.min(points[0].getY(), points[1].getY()) != 
            points[0].getY()) {
                // Swap points to sorted order
                if(Math.min(points[0].getX(), points[1].getX()) != 
                    points[0].getX()) {
                    PointXY tempPoint = points[0];
                    points[0] = points[1];
                    points[1] = tempPoint;
                }
        }
    }

    private void orientDragKnife(DragKnifeDirection direction) {
			
			PointXY point;

			if(direction == DragKnifeDirection.HORIZONTAL) {
				point = new PointXY(offset, 0);
			}
			else if(direction == DragKnifeDirection.VERTICAL) {
				point = new PointXY(0, offset);
			}
			else {
				System.err.println("Drag Knife Direction not recognized!");
				return;
			}

			// raise drag knife to clearance height
			generator.addLinearZ(clearanceHeight);

			// move to start position
			generator.addLinear(new Coordinate(point.getX(), point.getY(), 
				clearanceHeight));

			// lower drag knife to orient height
			generator.addLinearZ(DRAG_KNIFE_ORIENT_Z_HEIGHT);

			// spin drag knife to desired orientation drag knife orient times
			for(int i = 0; i < DRAG_KNIFE_ORIENT_TIMES; i++) {
				generator.addClockwiseArc(-point.getX(), -point.getY());
			}


			// raise drag knife to clearance height for next move
			generator.addLinearZ(clearanceHeight);

            knifeDirection = direction;
	}

}
