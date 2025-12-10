/**
 * XORShift64* demo with Streams (Java 8)
 *
 * Algorithm (brief):
 * - Maintain 64-bit state and update with x ^= x>>>12; x ^= x<<25; x ^= x>>>27;
 * - Multiply state by constant (xorshift*) to improve output mixing.
 *
 * Streams usage:
 * - LongStream.iterate(seed, nextState) produces an infinite stream of internal states.
 * - map(applyStar) converts state -> output value.
 * - limit(n) caps generation; forEach prints results.
 *
 * Run: the program asks for count and an optional seed (press Enter to use System.nanoTime()).
 */
import java.util.Scanner;
import java.util.stream.LongStream;

public class StreamXorShift {

    private static final long MULT = 0x2545F4914F6CDD1DL;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("How many numbers to generate (1-1000000): ");
        int count = readInt(sc, 1, 1_000_000);

        System.out.print("Optional seed (integer) or Enter for time-based seed: ");
        String seedLine = sc.nextLine().trim();
        long seed = 0;
        if (!seedLine.isEmpty()) {
            try { seed = Long.parseLong(seedLine); } catch (NumberFormatException e) { seed = System.nanoTime(); }
        } else seed = System.nanoTime();
        if (seed == 0) seed = 1; // XORShift must use non-zero state

        System.out.printf("Seed: %d%nGenerating %d pseudorandom numbers:%n", seed, count);
        LongStream.iterate(seed, StreamXorShift::nextState)
                  .skip(1)
                  .map(StreamXorShift::applyStar)
                  .limit(count)
                  .forEach(StreamXorShift::printResult);

        sc.close();
    }

    private static long nextState(long x) {
        x ^= (x >>> 12);
        x ^= (x << 25);
        x ^= (x >>> 27);
        return x;
    }

    private static long applyStar(long x) { return x * MULT; }

    private static void printResult(long v) { System.out.printf("Hex: %016X | Dec: %d%n", v, v); }

    private static int readInt(Scanner sc, int min, int max) {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) return v;
            } catch (Exception ignored) {}
            System.out.print("Invalid. Enter integer between " + min + " and " + max + ": ");
        }
    }
}