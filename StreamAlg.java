/**
 * StreamAlg - concise demonstration of Java Streams (Java 8)
 *
 * Purpose / value of streams:
 * - Streams express data-processing pipelines (map/filter/reduce) in a declarative style.
 * - Streams are lazy: intermediate ops are not executed until a terminal operation runs.
 * - Streams can be infinite (iterate/generate) and safely consumed with limit().
 * - Primitive streams (IntStream/LongStream/DoubleStream) avoid boxing for numeric ops.
 * - Streams are single-use: create a new stream each time or use a supplier/collection.
 *
 * This file shows three short pipelines, with user-provided sample size:
 * 1) an infinite IntStream.generate -> boxed() -> sample (random numbers),
 * 2) Stream.iterate for a sequence (powers of two),
 * 3) IntStream.rangeClosed with map -> sum (sum of squares up to sample).
 */
import java.util.Random;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamAlg {

    private static <T> void printSample(Stream<T> stream, int sampleSize) {
        stream.limit(sampleSize).forEach(item -> System.out.print(item + " "));
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter sample size (1-1000): ");
        int sampleSize = 0;
        while (sampleSize < 1) {
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.print("Enter a number: "); continue; }
            sampleSize = sc.nextInt(); sc.nextLine();
            if (sampleSize < 1) System.out.print("Must be >=1: ");
            if (sampleSize > 1000) { sampleSize = 1000; System.out.println("Capped to 1000."); }
        }

        Random rand = new Random();

        System.out.println("\n--- 1. Infinite stream (random 0..99) ---");
        printSample(IntStream.generate(() -> rand.nextInt(100)).boxed(), sampleSize);

        System.out.println("\n--- 2. Stream.iterate (powers of two) ---");
        printSample(Stream.iterate(1, n -> n * 2), sampleSize);

        System.out.println("\n--- 3. Primitive stream (sum of squares 1..n) ---");
        int sumSquares = IntStream.rangeClosed(1, sampleSize).map(n -> n * n).sum();
        System.out.println("Sum of squares (1.." + sampleSize + "): " + sumSquares);

        sc.close();
    }
}