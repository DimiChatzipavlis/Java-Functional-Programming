/**
 * EDUCATIONAL MODULE 1: LAMBDAS & INTERFACES
 * 
 * CORE CONCEPTS DEMONSTRATED:
 * =============================
 * This file teaches Functional Programming fundamentals in Java:
 * 
 * 1. CUSTOM FUNCTIONAL INTERFACES
 *    - Define interfaces with exactly ONE abstract method (@FunctionalInterface)
 *    - These become "contracts" that lambdas can implement
 *    - Example: MathOperation interface for mathematical operations
 * 
 * 2. LAMBDA EXPRESSION SYNTAX & VARIATIONS
 *    - Type 1: Explicit type declaration - (int x, int y) -> x + y
 *    - Type 2: Type inference - (x, y) -> x - y (compiler infers types)
 *    - Type 3: Block body - (x, y) -> { statements; return value; }
 *    - KEY IDEA: Lambdas are "anonymous functions" - concise implementations of interfaces
 * 
 * 3. REAL-WORLD APPLICATION: COMPARATOR
 *    - Use lambdas with built-in functional interfaces (Comparator)
 *    - Pass BEHAVIOR as DATA to Collections.sort()
 *    - Demonstrates how lambdas enable functional programming style
 * 
 * FLOW OF EXECUTION:
 * ==================
 * Step 1: Accept two numbers from user
 * Step 2: Create three lambda implementations of MathOperation
 * Step 3: Execute each lambda and display results
 * Step 4: Accept list of names from user input
 * Step 5: Sort names using a lambda comparator
 * Step 6: Display sorted results
 * 
 * KEY TAKEAWAY:
 * Lambdas simplify code by letting you pass custom behavior directly,
 * making code more readable and functional.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Lambdas {

    // 1. DEFINING A FUNCTIONAL INTERFACE
    // It must have exactly one abstract method.
    @FunctionalInterface
    interface MathOperation {
        int operate(int a, int b);
    }

    public static void main(String[] args) {
        System.out.println("--- 1. Lambda Syntax Demos ---");
        System.out.println("--- Give a and b ---");
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();

        // 2. IMPLEMENTING WITH LAMBDAS
        // Type 1: Type declaration (explicit)
        MathOperation addition = (int x, int y) -> x + y;

        // Type 2: Type inference (implicit - simpler)
        MathOperation subtraction = (x, y) -> x - y;

        // Type 3: With block body (for complex logic)
        MathOperation multiplication = (x, y) -> {
            System.out.println("Multiplying " + x + " and " + y);
            return x * y;
        };

        System.out.println(a +" + "+ b + " = " + addition.operate(a,b));
        System.out.println(a +" - "+ b + " = " + subtraction.operate(a,b));
        System.out.println(a +" * "+ b + " = " + multiplication.operate(a,b));
        

        // 3. REAL WORLD USAGE: COMPARATOR
        System.out.println("\n--- 2. Sorting with Lambdas ---");
        System.out.println("Enter names to sort (type 'done' when finished):");
        
        List<String> names = new ArrayList<>();
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("done")) {
                break;
            }
            if (!input.isEmpty()) {
                names.add(input);
            }
        }

        // WHY THIS IS A LAMBDA & FUNCTIONAL INTERFACE:
        // =============================================
        // 1. Comparator<String> is a BUILT-IN FUNCTIONAL INTERFACE
        //    - It has exactly ONE abstract method: compare(T o1, T o2)
        //    - Java declares it as: @FunctionalInterface interface Comparator<T> { ... }
        //
        // 2. (s1, s2) -> s1.compareTo(s2) is a LAMBDA EXPRESSION
        //    - It's an ANONYMOUS IMPLEMENTATION of the Comparator interface
        //    - Without lambdas, you'd need verbose anonymous class syntax (see below)
        //    - The lambda replaces the compare() method body
        //
        // 3. Collections.sort() ACCEPTS A FUNCTIONAL INTERFACE
        //    - Signature: sort(List list, Comparator c)
        //    - It expects any object that implements Comparator
        //    - Our lambda IS a Comparator implementation!
        //    - We're passing BEHAVIOR (how to compare) as DATA
        //
        // 4. TYPE INFERENCE IN ACTION
        //    - Compiler infers: s1 and s2 are String (from List<String>)
        //    - Return type is int (from Comparator.compare() signature)
        //    - This is the same power as our MathOperation lambdas above!

        // OLD WAY (without lambdas - verbose anonymous class):
        // Collections.sort(names, new Comparator<String>() {
        //     @Override
        //     public int compare(String s1, String s2) {
        //         return s1.compareTo(s2);
        //     }
        // });

        // NEW WAY (with lambda - clean and concise!):
        Collections.sort(names, (s1, s2) -> s1.compareTo(s2));

        System.out.println("Sorted names: " + names);
    }
}