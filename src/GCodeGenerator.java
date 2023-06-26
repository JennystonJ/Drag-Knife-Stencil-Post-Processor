import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GCodeGenerator {
    
    private List<String> instructions;
    private int linearFeedrate;

    public GCodeGenerator() {
        instructions = new ArrayList<>();
        linearFeedrate = 300;
    }

    public GCodeGenerator(int linearFeedrate) {
        instructions = new ArrayList<>();
        this.linearFeedrate = linearFeedrate;
    }

    public void setLinearFeedrate(int linearFeedrate) {
        this.linearFeedrate = linearFeedrate;
    }

    public int getLinearFeedrate() {
        return linearFeedrate;
    }

    public void addRapid(Coordinate coordinate) {
        instructions.add(String.format("G00 X%.4f Y%.4f Z%.4f",
            coordinate.getX(), coordinate.getY(), coordinate.getZ()));
    }

    public void addLinear(Coordinate coordinate) {
        instructions.add(String.format("G01 X%.4f Y%.4f Z%.4f F%d",
            coordinate.getX(), coordinate.getY(), coordinate.getZ(), 
            linearFeedrate));
    }

    public void addLinearZ(double z) {
        instructions.add(String.format("G01 Z%.4f F%d", z, 
        linearFeedrate));
    }

    public void addClockwiseArc(PointXY endPoint, double radius) {
        instructions.add(String.format("G02 X%.4f Y%.4f R%.4f", 
            endPoint.getX(), endPoint.getY(), radius));
    }

    public void writeToFile(File file, String header, String footer) 
        throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

        writer.write(header + "\n");

        for(int i = 0; i < instructions.size(); i++) {
            writer.write(instructions.get(i) + "\n");
        }

        writer.write("\n" + footer + "\n");

        writer.close();
    }

}
