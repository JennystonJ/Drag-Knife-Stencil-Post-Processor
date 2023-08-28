
public class DragKnife {

    private enum DragKnifeDirection {
		HORIZONTAL,
		VERTICAL;
	}

    private final static double DRAG_KNIFE_ORIENT_Z_HEIGHT = -0.15;
	private final static int DRAG_KNIFE_ORIENT_TIMES = 2;
    private final static int DRAG_KNIFE_ORIENT_DISTANCE = 5;
	// private final static double DRAG_KNIFE_OFFSET_FROM_CENTER = 0.35;
	// private final static double DRAG_KNIFE_OVER_CUT = 0.0;
    
    private GCodeGenerator generator;
    
    private DragKnifeDirection knifeDirection;

    private int orientAfterMoves;
    private int orientAfterMovesCount;
    
    private double offset;
    private double cutDepth;
    private double clearanceHeight;

    private double rampHeight;
    private double rampLength;
    
    private double hEntryOffset;
    private double hExitOffset;
    private double vEntryOffset;
    private double vExitOffset;

    public DragKnife(GCodeGenerator generator, double offset, double cutDepth,
        double clearanceHeight) {
        this.generator = generator;
        this.offset = offset;
        this.cutDepth = cutDepth;
        this.clearanceHeight = clearanceHeight;

        rampHeight = 2;
        rampLength = 0;//8;

        hEntryOffset = 0;
        hExitOffset = 0;

        vEntryOffset = 0;
        vExitOffset = 0;

        this.knifeDirection = null;
        orientAfterMoves = 4;
        orientAfterMovesCount = 0;
    }

    public double getHEntryOffset() {
        return hEntryOffset;
    }

    public void setHEntryOffset(double hEntryOffset) {
        this.hEntryOffset = hEntryOffset;
    }

    public double getHExitOffset() {
        return hExitOffset;
    }

    public void setHExitOffset(double hExitOffset) {
        this.hExitOffset = hExitOffset;
    }

    public void setHOffsets(double hEntryOffset, double hExitOffset) {
        this.hEntryOffset = hEntryOffset;
        this.hExitOffset = hExitOffset;
    }

    public double getVEntryOffset() {
        return vEntryOffset;
    }

    public void setVEntryOffset(double vEntryOffset) {
        this.vEntryOffset = vEntryOffset;
    }

    public double getVExitOffset() {
        return vExitOffset;
    }

    public void setVExitOffset(double vExitOffset) {
        this.vExitOffset = vExitOffset;
    }

    public void setVOffsets(double vEntryOffset, double vExitOffset) {
        this.vEntryOffset = vEntryOffset;
        this.vExitOffset = vExitOffset;
    }

    public int getOrientAfterMoves() {
        return orientAfterMoves;
    }

    public void setOrientAfterMoves(int orientAfterMoves) {
        this.orientAfterMoves = orientAfterMoves;
    }

    public void drawLine(PointXY a, PointXY b) {

        // Determine if cut direction matches current direction
        if(a.getY() == b.getY()) {
            if(knifeDirection != DragKnifeDirection.HORIZONTAL) {
                // Make drag knife horizontal (left to right)
                orientDragKnife(DragKnifeDirection.HORIZONTAL);
            }
        }
        else if(a.getX() == b.getX()) {
            if(knifeDirection != DragKnifeDirection.VERTICAL) {
                // Make drag knfie vertical (bottom to top)
                orientDragKnife(DragKnifeDirection.VERTICAL);
            }
        }
        else {
            System.err.println(
                "Only horizontal and vertical lines are supported.");
            return;
        }

        orientAfterMovesCount++;
        if(orientAfterMovesCount == orientAfterMoves) {
            orientAfterMovesCount = 0;
            orientDragKnife(knifeDirection);
        }

        PointXY[] points = {a, b};
        sortPoints(points, knifeDirection);
        applyKnifeOffsets(points, knifeDirection);
        applyDirectionalOffsets(points, knifeDirection);

        generateEntryRamp(points[0], knifeDirection);

        //initiate cut
        generator.addLinear(new Coordinate(points[0].getX(), points[0].getY(), 
            cutDepth));

        //go to cut end position
        generator.addLinear(new Coordinate(points[1].getX(), points[1].getY(), 
            cutDepth));

        generateExitRamp(points[1], knifeDirection);

    }

    private void applyDirectionalOffsets(PointXY[] points, 
        DragKnifeDirection direction) {

        //offset along x axis
        if(direction == DragKnifeDirection.HORIZONTAL) {
            points[0].setX(points[0].getX() + hEntryOffset);
            points[1].setX(points[1].getX() + hExitOffset);
        }
        //offset along y axis
        else if(direction == DragKnifeDirection.VERTICAL) {
            points[0].setY(points[0].getY() + vEntryOffset);
            points[1].setY(points[1].getY() + vExitOffset);
        }
        //error
        else {
            System.err.println(
                "Only horizontal and vertical lines are supported.");
            return;
        }
    }

    private void generateEntryRamp(PointXY startPoint, 
        DragKnifeDirection direction) {
        PointXY rampStartPoint = null;

        // Determine ramp direction
        if(direction == DragKnifeDirection.HORIZONTAL) {

            // Calculate horizontal ramp start
            rampStartPoint = new PointXY(startPoint.getX() - rampLength, 
                startPoint.getY());
        }
        else if(direction == DragKnifeDirection.VERTICAL) {

            // Calculate vertical ramp start
            rampStartPoint = new PointXY(startPoint.getX(), 
                startPoint.getY() - rampLength);
        }

        generator.addRapid(new Coordinate(rampStartPoint.getX(), 
            rampStartPoint.getY(), rampHeight));
    }

    private void generateExitRamp(PointXY endPoint, 
        DragKnifeDirection direction) {
        PointXY rampExitPoint = null;

        // Determine ramp direction
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

        generator.addRapid(new Coordinate(rampExitPoint.getX(), 
            rampExitPoint.getY(), rampHeight));
    }

    private void applyKnifeOffsets(PointXY[] points,
        DragKnifeDirection direction) {
        if(direction == DragKnifeDirection.HORIZONTAL) {
            
            // Offset x component for entry
            points[0] = new PointXY(points[0].getX() + offset, 
                points[0].getY());

            // Offset x component for exit
            points[1] = new PointXY(points[1].getX() + offset, 
                points[1].getY());
        }
        else if (direction == DragKnifeDirection.VERTICAL) {

            // Offset y component for entry
            points[0] = new PointXY(points[0].getX(), 
                points[0].getY() + offset);

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
            PointXY tempPoint = points[0];
            points[0] = points[1];
            points[1] = tempPoint;
        }
        // Sort points vertically
        else if(direction == DragKnifeDirection.VERTICAL && 
            Math.min(points[0].getY(), points[1].getY()) != 
            points[0].getY()) {
            
            // Swap points to sorted order
            PointXY tempPoint = points[0];
            points[0] = points[1];
            points[1] = tempPoint;
        }
    }

    private void orientDragKnife(DragKnifeDirection direction) {
			
			PointXY point;

            // Determine desired direction
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
			generator.addRapid(new Coordinate(point.getX(), point.getY(), 
				clearanceHeight));

			// lower drag knife to orient height
			generator.addLinearZ(DRAG_KNIFE_ORIENT_Z_HEIGHT);

			// spin drag knife to desired orientation drag knife orient times
			for(int i = 0; i < DRAG_KNIFE_ORIENT_TIMES; i++) {
				generator.addClockwiseArc(-point.getX(), -point.getY());
			}

            // move drag knife to ensure correct orientation
            // find end point
            PointXY endPoint;
            // determine knife direction
            if(direction == DragKnifeDirection.HORIZONTAL) {
                endPoint = new PointXY(DRAG_KNIFE_ORIENT_DISTANCE + offset, 0);
            }
            else if(direction == DragKnifeDirection.VERTICAL) {
                endPoint = new PointXY(0, DRAG_KNIFE_ORIENT_DISTANCE + offset);
            }
            else {
                System.err.println("Drag Knife Direction not recognized!");
                return;
            }
            //move knife by orient distance in appropriate direction
            generator.addLinear(new Coordinate(endPoint.getX(), endPoint.getY(), 
                DRAG_KNIFE_ORIENT_Z_HEIGHT));

			// raise drag knife to clearance height for next move
			generator.addLinearZ(clearanceHeight);

            knifeDirection = direction;
	}

}
