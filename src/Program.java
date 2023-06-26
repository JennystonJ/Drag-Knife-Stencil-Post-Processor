import java.io.*;
import java.util.Scanner;

public class Program {
	
		private final static boolean LIFT_DRAG_KNIFE_TO_TRAVEL = false;
		private final static double DRAG_KNIFE_ACTION_Z_HEIGHT = 0;
		private final static double DRAG_KNIFE_ACTION_Z_LIFT_FEED_RATE = 600.0;
		private final static double DRAG_KNIFE_ACTION_FEED_RATE = 1200.0;
		private final static double DRAG_KNIFE_OFFSET_FROM_CENTER = 0.25;
	
		private final static double ARC_THRESHOLD_IN_DEGREES = 5.0;
		private final static int NUM_ORIENT_SPINS = 2;
	public static void main(String[] args) {

		Prompt prompt = new Prompt();

		File fileIn = null;
		File fileOut = null;

		ArgParser argParser = new ArgParser(args);
		try {
			fileIn = new File(argParser.getArg(ArgParser.Type.PATH_IN));

			// Validate input file
			if(!fileIn.exists()) {
				System.out.println("Input file does not exist!");
				return;
			}

			fileOut = new File(argParser.getArg(ArgParser.Type.PATH_OUT));

			// Prompt user to overwrite if file already exists
			if(fileOut.exists()) {
				boolean overwrite = prompt.promptBoolean(
					"Output file aready exists, overwrite? (y/n)", "y", "n");
				if(!overwrite) {
					return;
				}
			}
		}
		catch(ArgDoesNotExistException ex) {
			System.err.println("Invalid command line arguments!");
		}

		GCodeToPoints gParser = null;
		GCodeCutParamExtractor gExtractor = null;
		double clearanceHeight = Double.NaN;
		double cutDepth = Double.NaN;
		try {
			gParser = new GCodeToPoints(fileIn);

			gExtractor = new GCodeCutParamExtractor(fileIn);
			clearanceHeight = gExtractor.getClearanceHeight();
			cutDepth = gExtractor.getCutDepth();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		System.out.printf("Detected clearance height: %.4f\n" + 
			"Detected cut depth: %.4f\n", clearanceHeight, cutDepth);
		
		String header;
		try {
			header = fileContentsToString(new File("header.txt"));
		} catch (FileNotFoundException e) {
			System.err.println("header.txt is missing!");
			return;
		}

		String footer;
		try {
			footer = fileContentsToString(new File("footer.txt"));
		} catch (FileNotFoundException e) {
			System.err.println("footer.txt is missing!");
			return;
		}

		GCodeGenerator generator = new GCodeGenerator(600);
		generateVMoves(gParser, gExtractor, generator);
		try {
			generator.writeToFile(fileOut, header, footer);
		} catch (IOException e) {
			System.err.println("Failed to write to file!");
			return;
		}
	}

	public static String fileContentsToString(File file) 
		throws FileNotFoundException {
		StringBuilder contents = new StringBuilder();
		Scanner fInput = new Scanner(file);

		while(fInput.hasNextLine()) {
			contents.append(fInput.nextLine());
			contents.append("\n");
		}

		return contents.toString();
	}

	public static void generateVMoves(GCodeToPoints gParser, 
		GCodeCutParamExtractor gExtractor, GCodeGenerator generator) {

			for(int item = 0; item < gParser.getNumItems(); item++) {
				for(int p = 1; p < gParser.getNumPointsAt(item); p++) {
					PointXY a = gParser.getPoint(item, p - 1);
					PointXY b = gParser.getPoint(item, p);

					// check if points share same x coordinate
					if(a.getX() == b.getX() && a.getY() != b.getY()) {

						// maintain direction of cut (bottom to top)
						double yPos1 = Math.min(a.getY(), b.getY());
						double yPos2 = Math.max(a.getY(), b.getY());
						double xPos = a.getX();

						// move to cut start position
						generator.addLinear(new Coordinate(xPos, yPos1, 
							gExtractor.getClearanceHeight()));

						// lower drag knife
						generator.addLinear(new Coordinate(xPos, yPos1, 
							gExtractor.getCutDepth()));

						// move drag knife to end position
						generator.addLinear(new Coordinate(xPos, yPos2, 
							gExtractor.getCutDepth()));

						/*
						 * lift drag knife to clearance height to prepare for
						 * next cut
						 */
						generator.addLinear(new Coordinate(xPos, yPos2, 
							gExtractor.getClearanceHeight()));

				}
			}
		}
	}

}
