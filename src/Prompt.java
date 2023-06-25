import java.util.Scanner;

public class Prompt {
    private Scanner input;

    public Prompt() {
        
        // Initialize Scanner for user input
        input = new Scanner(System.in);
    }

    public Prompt(Scanner input) {
        this.input = input;
    }

    /*
     * Prompts user for boolean input, defined in method options. Reprompts 
     * if input does not match provided options.
     */
    public boolean promptBoolean(String prompt, String optionTrue, 
    String optionFalse) { 
        
        // Display prompt message
        System.out.print(prompt + " ");

        // Prompt user until valid input is entered
        String result;
        boolean validInput = false;
        do {
            
            result = input.nextLine();
            if(result.equalsIgnoreCase(optionTrue) || 
            result.equalsIgnoreCase(optionFalse)) {
                validInput = true;
            }

            if(!validInput) {
                System.out.printf("Invalid option, enter %s or %s: ", 
                    optionTrue, optionFalse);
            }

        } while(!validInput);

        return result.equalsIgnoreCase(optionTrue);
    }
}
