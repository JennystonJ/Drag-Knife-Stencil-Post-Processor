import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class GCodeToPoints {
    
    private File gcode;
    private List<List<PointXY>> items;

    public GCodeToPoints(File gcode) throws FileNotFoundException{
        this.gcode = gcode;
        items = parseGCode(gcode);
    }

    public PointXY getPoint(int itemIndex, int pointIndex) {
        return items.get(itemIndex).get(pointIndex);
    }

    public int getNumItems() {
        return items.size();
    }

    public int getNumPointsAt(int itemIndex) {
        return items.get(itemIndex).size();
    }

    private static List<List<PointXY>> parseGCode(File gcode) 
        throws FileNotFoundException {

        List<List<PointXY>> items = new ArrayList<>();
        Scanner fInput = new Scanner(gcode);

        //start at 0, 0, 0
        Coordinate currentCoordinate = new Coordinate(0, 0, 0);

        //find initial coordinate
        boolean initialCoordFound = false;
        while(fInput.hasNextLine() && !initialCoordFound) {
            String line = fInput.nextLine();

            //look for rapid or linear move instruction
            if(line.startsWith("G00") ||  line.startsWith("G01")) {
                currentCoordinate = parseLinearMove(line, currentCoordinate);
                initialCoordFound = true;
            }
        }

        if(!initialCoordFound) {
            fInput.close();
            throw new RuntimeException("Initial coordinate cannot be found!");
        }

        boolean endReached = false;
        do {
            List<PointXY> item = extractNextItem(fInput, currentCoordinate);
            
            //item found, add to list
            if(item != null) {
                items.add(item);
            }
            //no more items remaining, terminate loop
            else {
                endReached = true;
            }

        } while(!endReached);

        return items;
    }

    private static List<PointXY> extractNextItem(Scanner fInput, 
        Coordinate currentCoordinate) {
            
        //find item start
        boolean itemFound = false;
        while(fInput.hasNextLine() && !itemFound) {
            String line = fInput.nextLine();

            //look for rapid or linear move instruction
            if(line.startsWith("G00") ||  line.startsWith("G01")) {

                //update current coordinate
                currentCoordinate = parseLinearMove(line, currentCoordinate);

                //check if Z is negative (cutting)
                if(currentCoordinate.getZ() < 0) {
                    itemFound = true;
                }
            }
        }

        if(!itemFound) {
            return null;
        }

        List<PointXY> item = new LinkedList<>();
        //add initial point
        item.add(new PointXY(currentCoordinate.getX(), 
        currentCoordinate.getY()));

        boolean itemEndReached = false;
        while(fInput.hasNextLine() && !itemEndReached) {
            String line = fInput.nextLine();

            //look for rapid or linear move instruction
            if(line.startsWith("G00") ||  line.startsWith("G01")) {

                //update current coordinate
                currentCoordinate = parseLinearMove(line, currentCoordinate);

                //check if Z is negative (cutting)
                if(currentCoordinate.getZ() < 0) {
                    //point is part of item, so add to list
                    item.add(new PointXY(currentCoordinate.getX(), 
                    currentCoordinate.getY()));
                }
                //point is not part of item, terminate loop
                else {
                    itemEndReached = true;
                }
            }

        }

        return item;
    }

    private static Coordinate parseLinearMove(String line) {
        String[] fields = line.split(" ");

        double x = 0, y = 0, z = 0;
        for(int i = 1; i < fields.length; i++) {
            if(fields[i].charAt(0) == 'X') {
                x = Double.parseDouble(fields[i].substring(1));
            }
            else if(fields[i].charAt(0) == 'Y') {
                y = Double.parseDouble(fields[i].substring(1));
            }
            if(fields[i].charAt(0) == 'Z') {
                z = Double.parseDouble(fields[i].substring(1));
            }
        }

        return new Coordinate(x, y, z);
    }

    private static Coordinate parseLinearMove(String line, Coordinate initial) {
        String[] fields = line.split(" ");

        double x = initial.getX(), 
            y = initial.getY(), 
            z = initial.getZ();

        for(int i = 1; i < fields.length; i++) {
            if(fields[i].charAt(0) == 'X') {
                x = Double.parseDouble(fields[i].substring(1));
            }
            else if(fields[i].charAt(0) == 'Y') {
                y = Double.parseDouble(fields[i].substring(1));
            }
            if(fields[i].charAt(0) == 'Z') {
                z = Double.parseDouble(fields[i].substring(1));
            }
        }

        return new Coordinate(x, y, z);
    }

}
