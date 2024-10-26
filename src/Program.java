import java.io.*;
import java.util.Scanner;

public class Program {
	
	private final static double DRAG_KNIFE_ORIENT_Z_HEIGHT = 0.0;
	private final static int DRAG_KNIFE_ORIENT_TIMES = 2;
	private final static double DRAG_KNIFE_OFFSET_FROM_CENTER = 0.35;//0.45;//0.35;//0.25;//0.35;
	private final static double DRAG_KNIFE_OVER_CUT = 0.0;
	private final static double DRAG_KNIFE_H_ENTRY_OFFSET = -0.05;
	private final static double DRAG_KNIFE_H_EXIT_OFFSET = 0.0;
	private final static double DRAG_KNIFE_V_ENTRY_OFFSET = 0.0;
	private final static double DRAG_KNIFE_V_EXIT_OFFSET = 0.05;

	// TODO: Remove
	// private enum DragKnifeDirection {
	// 	HORIZONTAL(),
	// 	VERTICAL;
	// }

	public static void main(String[] args) {

		Prompt prompt = new Prompt();

		File fileIn = null;
		File fileOut = null;

		ArgParser argParser = new ArgParser(args);
		try {
			fileIn = new File(argParser.getArg(ArgParser.Type.PATH_IN));

			// Validate input file
			if(!fileIn.exists()) {
				System.err.println("Input file does not exist!");
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
			return;
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
			System.err.println("Input file does not exist!");
			return;
		}

		// Display extracted GCode information, prompt user to continue
		System.out.printf("\nDetected clearance height: %.4f\n" + 
			"Detected cut depth: %.4f\n", clearanceHeight, cutDepth);
		boolean confirm = prompt.promptBoolean(
			"\nProceed with generating file? (y/n) ", "y", "n");
		if(!confirm) {
			return;
		}
		
		// Read header and footer files for GCode output
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

		// TODO: Remove
		// // Spin drag knife so it's vertical (bottom to top)
		// orientDragKnife(DRAG_KNIFE_ORIENT_Z_HEIGHT, 
		// 	DRAG_KNIFE_OFFSET_FROM_CENTER, 
		// 	DragKnifeDirection.VERTICAL, gExtractor, generator);

		DragKnife knife = new DragKnife(generator, 
			DRAG_KNIFE_OFFSET_FROM_CENTER, 
			gExtractor.getCutDepth(), 
			gExtractor.getClearanceHeight());
		knife.setHOffsets(DRAG_KNIFE_H_ENTRY_OFFSET, DRAG_KNIFE_H_EXIT_OFFSET);
		knife.setVOffsets(DRAG_KNIFE_V_ENTRY_OFFSET, DRAG_KNIFE_V_EXIT_OFFSET);

		generateVMoves(gParser, gExtractor, generator, knife);

		// TODO: Remove
		// // Spin drag knife so it's horizontal (left to right)
		// orientDragKnife(DRAG_KNIFE_ORIENT_Z_HEIGHT, 
		// DRAG_KNIFE_OFFSET_FROM_CENTER, 
		// DragKnifeDirection.HORIZONTAL, gExtractor, generator);

		generateHMoves(gParser, gExtractor, generator, knife);

		// Write generated GCode to output file
		try {
			generator.writeToFile(fileOut, header, footer);
		} catch (IOException e) {
			System.err.println("Failed to write to file!");
			return;
		}

		System.out.println("File written to successfully!");
	}

	public static String fileContentsToString(File file) 
		throws FileNotFoundException {
		StringBuilder contents = new StringBuilder();
		Scanner fInput = new Scanner(file);

		while(fInput.hasNextLine()) {
			contents.append(fInput.nextLine());
			contents.append("\n");
		}

		fInput.close();

		return contents.toString();
	}

	public static void generateVMoves(GCodeToPoints gParser, 
		GCodeCutParamExtractor gExtractor, GCodeGenerator generator, 
		DragKnife knife) {

		for(int item = 0; item < gParser.getNumItems(); item++) {
			for(int p = 1; p < gParser.getNumPointsAt(item); p++) {
				PointXY a = gParser.getPoint(item, p - 1);
				PointXY b = gParser.getPoint(item, p);

				// check if points share same x coordinate
				if(a.getX() == b.getX() && a.getY() != b.getY()) {

					knife.drawLine(a, b);

					// TODO: Remove
					// // maintain direction of cut (bottom to top)
					// double yPos1 = Math.min(a.getY(), b.getY());
					// double yPos2 = Math.max(a.getY(), b.getY());
					// double xPos = a.getX();

					// /*
					//  * offset Y positions to compensate for drag knife 
					//  * off-center pivot (based on bottom to top cut)
					//  */
					// double yPosOffset1 = yPos1 + 
					// 	DRAG_KNIFE_OFFSET_FROM_CENTER - DRAG_KNIFE_OVER_CUT;
					// double yPosOffset2 = yPos2 + 
					// 	DRAG_KNIFE_OFFSET_FROM_CENTER + DRAG_KNIFE_OVER_CUT;

					// // move to cut start position
					// generator.addLinear(new Coordinate(xPos, yPosOffset1, 
					// 	gExtractor.getClearanceHeight()));

					// // lower drag knife
					// generator.addLinearZ(gExtractor.getCutDepth());


					// // move drag knife to end position
					// generator.addLinear(new Coordinate(xPos, yPosOffset2, 
					// 	gExtractor.getCutDepth()));

					// /*
					//  * lift drag knife to clearance height to prepare for
					//  * next cut
					//  */
					// generator.addLinearZ(gExtractor.getClearanceHeight());

				}
			}
		}
	}

	public static void generateHMoves(GCodeToPoints gParser, 
		GCodeCutParamExtractor gExtractor, GCodeGenerator generator, 
		DragKnife knife) {

		for(int item = 0; item < gParser.getNumItems(); item++) {
			for(int p = 1; p < gParser.getNumPointsAt(item); p++) {
				PointXY a = gParser.getPoint(item, p - 1);
				PointXY b = gParser.getPoint(item, p);

				// check if points share same y coordinate
				if(a.getX() != b.getX() && a.getY() == b.getY()) {

					knife.drawLine(a, b);

					// TODO: Remove
					// // maintain direction of cut (left to right)
					// double xPos1 = Math.min(a.getX(), b.getX());
					// double xPos2 = Math.max(a.getX(), b.getX());
					// double yPos = a.getY();

					// /*
					// * offset X positions to compensate for drag knife 
					// * off-center pivot (based on left to right cut)
					// */
					// double xPosOffset1 = xPos1 + 
					// 	DRAG_KNIFE_OFFSET_FROM_CENTER - DRAG_KNIFE_OVER_CUT;
					// double xPosOffset2 = xPos2 + 
					// 	DRAG_KNIFE_OFFSET_FROM_CENTER + DRAG_KNIFE_OVER_CUT;

					// // move to cut start position
					// generator.addLinear(new Coordinate(xPosOffset1, yPos, 
					// 	gExtractor.getClearanceHeight()));

					// // lower drag knife
					// generator.addLinearZ(gExtractor.getCutDepth());

					// // move drag knife to end position
					// generator.addLinear(new Coordinate(xPosOffset2, yPos, 
					// 	gExtractor.getCutDepth()));

					// /*
					//  * lift drag knife to clearance height to prepare for
					//  * next cut
					//  */
					// generator.addLinearZ(gExtractor.getClearanceHeight());
					
				}
			}
		}
	}

	// TODO: Remove
	// public static void orientDragKnife(double height, double offset, 
	// 	DragKnifeDirection dir, GCodeCutParamExtractor gExtractor, 
	// 	GCodeGenerator generator) {
			
	// 		// TODO: Remove (spin full circle instead)
	// 		// PointXY startPoint;
	// 		// PointXY endPoint;

	// 		PointXY point;

	// 		if(dir == DragKnifeDirection.HORIZONTAL) {
	// 			point = new PointXY(offset, 0);
	// 			// startPoint = new PointXY(-offset, 0);
	// 			// endPoint = new PointXY(offset, 0);
	// 		}
	// 		else if(dir == DragKnifeDirection.VERTICAL) {
	// 			point = new PointXY(0, offset);
	// 			// startPoint = new PointXY(0, -offset);
	// 			// endPoint = new PointXY(0, offset);
	// 		}
	// 		else {
	// 			System.err.println("Drag Knife Direction not recognized!");
	// 			return;
	// 		}

	// 		// raise drag knife to clearance height
	// 		generator.addLinearZ(gExtractor.getClearanceHeight());

	// 		// move to start position
	// 		generator.addLinear(new Coordinate(point.getX(), point.getY(), 
	// 			gExtractor.getClearanceHeight()));
	// 		// generator.addLinear(new Coordinate(startPoint.getX(), 
	// 		// 	startPoint.getY(), gExtractor.getClearanceHeight()));

	// 		// lower drag knife to orient height
	// 		generator.addLinearZ(height);

	// 		// spin drag knife to desired orientation drag knife orient times
	// 		for(int i = 0; i < DRAG_KNIFE_ORIENT_TIMES; i++) {
	// 			generator.addClockwiseArc(-point.getX(), -point.getY());
	// 		}
	// 		// generator.addClockwiseArc(endPoint, offset);


	// 		// raise drag knife to clearance height for next move
	// 		generator.addLinearZ(gExtractor.getClearanceHeight());
	// }

}
