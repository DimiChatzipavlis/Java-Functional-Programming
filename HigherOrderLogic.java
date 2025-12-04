/**
 * EDUCATIONAL MODULE 2: HIGHER-ORDER FUNCTIONS & INTERACTIVITY
 * * CONCEPTS COVERED:
 * 1. Function Factories (Higher-Order Functions):
 * - A method that returns a Function.
 * - We use this to build a dynamic "Multiplier" based on user input.
 * - Logic: scaleFactor -> (x -> x * scaleFactor)
 * * 2. Custom Functional Interfaces with Complex Logic:
 * - Defining an interface (SafeConverter) to handle multiple logic paths.
 * - Using a lambda block body {} to implement switch-cases dynamically.
 * * 3. Interactive Input:
 * - Using Scanner to drive the functional parameters at runtime.
 */

import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

@FunctionalInterface
interface SafeConverter {
    double convert(double value, String mode);
}

public class HigherOrderLogic {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // --- PART A: Dynamic Function Factory ---
        
        System.out.print("Enter a number to create a multiplier function (e.g., 2 for doubling): ");
        double userScale = scanner.nextDouble();

        Function<Double, DoubleUnaryOperator> scalerFactory = 
            (scaleFactor) -> (x) -> x * scaleFactor;

        DoubleUnaryOperator dynamicMultiplier = scalerFactory.apply(userScale);

        System.out.print("Enter a value to apply your new function to: ");
        double valueToProcess = scanner.nextDouble();

        double result = dynamicMultiplier.applyAsDouble(valueToProcess);
        System.out.println("Result: " + result);


        // --- PART B: Custom Interface with Logic ---
        
        SafeConverter currencyConverter = (amount, currency) -> {
            switch (currency.toUpperCase()) {
                case "USD": return amount * 1.1;
                case "JPY": return amount * 140.0;
                case "GBP": return amount * 0.85;
                default: throw new IllegalArgumentException("Unsupported currency: " + currency);
            }
        };

        System.out.println("\n--- Currency Converter ---");
        System.out.print("Enter Amount in EUR: ");
        double amount = scanner.nextDouble();
        
        System.out.print("Enter Target Currency (USD, JPY, GBP): ");
        String curr = scanner.next();

        try {
            double converted = currencyConverter.convert(amount, curr);
            System.out.printf("Converted: %.2f %s%n", converted, curr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        scanner.close();
    }
}