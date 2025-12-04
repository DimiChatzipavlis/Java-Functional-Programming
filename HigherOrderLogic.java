/**
 * HigherOrderLogic - compact, educational demo (Java 8)
 *
 * Concepts:
 * - Higher-order function: method that returns a function (createMultiplier).
 * - Interactivity: Scanner drives parameters at runtime.
 * - Simple custom functional interface (SafeConverter) used with a lambda.
 *
 * Usage:
 * - Enter a scale to create a multiplier function, then apply it to a value.
 * - Convert a EUR amount to USD/JPY/GBP using the converter lambda.
 */
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;

@FunctionalInterface
interface SafeConverter { double convert(double amount, String currency); }

public class HigherOrderLogic {

    // higher-order: returns a function (DoubleUnaryOperator) built from scale
    static DoubleUnaryOperator createMultiplier(double scale) {
        return x -> x * scale;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Multiplier scale (e.g. 2): ");
        double scale = readDouble(sc);

        DoubleUnaryOperator multiplier = createMultiplier(scale);
        System.out.print("Value to multiply: ");
        double value = readDouble(sc);
        System.out.printf("Result: %.4f%n", multiplier.applyAsDouble(value));

        // simple converter lambda
        SafeConverter conv = (amt, cur) -> {
            switch (cur.toUpperCase()) {
                case "USD": return amt * 1.10;
                case "JPY": return amt * 140.0;
                case "GBP": return amt * 0.85;
                default: throw new IllegalArgumentException("Unsupported: " + cur);
            }
        };

        System.out.print("Amount in EUR to convert: ");
        double eur = readDouble(sc);
        System.out.print("Target currency (USD, JPY, GBP): ");
        String cur = sc.next().trim();

        try {
            double out = conv.convert(eur, cur);
            System.out.printf("Converted: %.2f %s%n", out, cur.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        sc.close();
    }

    private static double readDouble(Scanner sc) {
        while (true) {
            if (!sc.hasNextDouble()) { sc.next(); System.out.print("Enter a number: "); continue; }
            return sc.nextDouble();
        }
    }
}