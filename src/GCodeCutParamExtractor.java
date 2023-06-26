import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GCodeCutParamExtractor {

    private enum Compare {
        LESS_THAN(-1),
        EQUAL_TO(0),
        GREATER_THAN(1);

        private int value;

        private Compare(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private double cutDepth;
    private double clearanceHeight;

    public GCodeCutParamExtractor(File file) throws FileNotFoundException {
        Scanner fInput = new Scanner(file);


        // Find clearance height, where first Z occurance is greater than 0
        clearanceHeight = findZUntil(fInput, Compare.GREATER_THAN, 0);

        // boolean clearanceHeightFound = false;
        // while(fInput.hasNextLine() && !clearanceHeightFound) {
        //     String line = fInput.nextLine();
        //     if(line.startsWith("G00") || line.startsWith("G01")) {
        //         double z = extractZFromLine(line);
        //         if(z > 0 && !Double.isNaN(z)) {
        //             clearanceHeight = z;
        //             clearanceHeightFound = true;
        //         }
        //     }
        // }

        // if(!clearanceHeightFound) {
        //     throw new GCodeCutParamNotFoundException();
        // }

        /*
         * Find cut depth, where first Z occurance after clearance height is 
         * less than 0
         */
        cutDepth = findZUntil(fInput, Compare.LESS_THAN, 0);

        //  boolean cutDepthFound = false;
        //  while(fInput.hasNextLine())
    }

    public double getCutDepth() {
        return cutDepth;
    }

    public double getClearanceHeight() {
        return clearanceHeight;
    }

    private static double findZUntil(Scanner fInput, Compare compare, 
        double value) {
        while(fInput.hasNextLine()) {
            String line = fInput.nextLine();

            // look for rapid or linear move instruction
            if(line.startsWith("G00") || line.startsWith("G01")) {

                // obtain Z value
                double z = extractZFromLine(line);

                // compare based on provided compare value, ensure z isn't NaN
                if(Double.compare(z, value) == compare.getValue() && 
                    !Double.isNaN(z)) {
                    return z;
                }
            }
        }

        throw new GCodeCutParamNotFoundException();
    }

    private static double extractZFromLine(String line) {
        String[] fields = line.split(" ");
        for(int i = 0; i < fields.length; i++) {
            if(fields[i].charAt(0) == 'Z') {
                return Double.parseDouble(fields[i].substring(1));
            }
        }

        return Double.NaN;
    }
}
