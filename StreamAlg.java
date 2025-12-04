import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamAlg {

    // 1. GENERIC METHOD
    // Takes a Stream<T> of ANY type.
    public static <T> void printSample(Stream<T> stream, int sampleSize) {
        System.out.print("Sample: ");
        stream.limit(sampleSize)
              .forEach(item -> System.out.print(item + " "));
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("--- 1. Infinite Stream Generation ---");
        
        Random rand = new Random();
        
        // Generate an infinite stream of random integers
        // We use 'boxed()' to convert IntStream to Stream<Integer> for our generic method
        Stream<Integer> randomNumbers = IntStream.generate(() -> rand.nextInt(100))
                                                 .boxed();
                                                 
        printSample(randomNumbers, 5); // Prints 5 random numbers

        System.out.println("\n--- 2. Stream Iteration (Sequence) ---");
        // Generate a sequence: 1, 2, 4, 8, 16...
        // iterate(seed, UnaryOperator)
        Stream<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2);
        
        printSample(powersOfTwo, 6);

        System.out.println("\n--- 3. Primitive Streams Logic ---");
        // Calculate sum of squares for numbers 1 to 5
        int sumSquares = IntStream.rangeClosed(1, 5) // 1,2,3,4,5
                                  .map(n -> n * n)   // 1,4,9,16,25
                                  .sum();            // 55
                                  
        System.out.println("Sum of squares (1-5): " + sumSquares);
    }
}