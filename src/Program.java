import java.io.*;

public class Program {
	public static void main(String[] args) {

		Prompt prompt = new Prompt();

		File fileIn;
		File fileOut;

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
			System.out.println("Invalid command line arguments!");
		}

		
	}

}
