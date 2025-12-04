import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionalCollatz {

    public static void main(String[] args) {
        // 1. SUPPLIER: Provides data (here, it just supplies the prompt text)
        //    Logic: "I take no input, but I give you a String."
        Supplier<String> promptProvider = () -> "Enter a number (0 to quit): ";

        // 2. CONSUMER: Performs an action (here, printing to console)
        //    Logic: "I take a String, and I return nothing (void)."
        Consumer<String> printer = System.out::println; // same as: s -> System.out.println(s)

        // 3. PREDICATE: Tests a condition (here, checks if a number is Even)
        //    Logic: "I take an Integer, and I return true or false."
        Predicate<Integer> isEven = n -> n % 2 == 0;

        // 4. FUNCTION: Transforms data (here, calculates the next step)
        //    Logic: "I take an Integer, and I return a new Integer."
        //    Notice: We reuse the 'isEven' predicate inside here!
        Function<Integer, Integer> nextStep = n -> {
            if (isEven.test(n)) {
                return n / 2;       // Rule 1: Even -> divide by 2
            } else {
                return n * 3 + 1;   // Rule 2: Odd -> 3n + 1
            }
        };

        // --- Execution Logic ---
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print(promptProvider.get()); // Use Supplier
            if (!scanner.hasNextInt()) break;       // Safety check
            
            int number = scanner.nextInt();
            if (number == 0) break;

            String path = number + ""; // Start the result string

            // Run the algorithm loop
            while (number != 1) {
                // Use the FUNCTION to calculate the next number
                number = nextStep.apply(number);
                path += " -> " + number;
            }

            // Use the CONSUMER to print the final result
            printer.accept("Path: " + path);
            printer.accept("-----------------------");
        }
        scanner.close();
    }
}