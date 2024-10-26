/*
 * Author: Jennyston Jeyachandra
 * Program: Drag Knife Stencil Post Processor
 * Description: Optimizes GCode for cutting SMD stencils using a drag knife on a
 *              CNC machine. Currently only supports vertical and horizontal
 *              GCode moves. Arcs (G02 and G03) are not supported. Tested with
 *              Estlcam 11.
 * Command Line Arguments: [INPUT GCODE] [OUTPUT GCODE]
 */

import java.io.*;
import java.util.Scanner;

public class Program {
	
	private final static double DRAG_KNIFE_ORIENT_Z_HEIGHT = 0.0;
	private final static int DRAG_KNIFE_ORIENT_TIMES = 2;
	private final static double DRAG_KNIFE_OFFSET_FROM_CENTER = 0.35;
	private final static double DRAG_KNIFE_OVER_CUT = 0.0;
	private final static double DRAG_KNIFE_H_ENTRY_OFFSET = -0.05;
	private final static double DRAG_KNIFE_H_EXIT_OFFSET = 0.0;
	private final static double DRAG_KNIFE_V_ENTRY_OFFSET = 0.0;
	private final static double DRAG_KNIFE_V_EXIT_OFFSET = 0.05;

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

		DragKnife knife = new DragKnife(generator, 
			DRAG_KNIFE_OFFSET_FROM_CENTER, 
			gExtractor.getCutDepth(), 
			gExtractor.getClearanceHeight());
		knife.setHOffsets(DRAG_KNIFE_H_ENTRY_OFFSET, DRAG_KNIFE_H_EXIT_OFFSET);
		knife.setVOffsets(DRAG_KNIFE_V_ENTRY_OFFSET, DRAG_KNIFE_V_EXIT_OFFSET);

		generateVMoves(gParser, gExtractor, generator, knife);
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
				}
			}
		}
	}
}
