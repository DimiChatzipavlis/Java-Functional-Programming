/**
 * Miller-Rabin demo using built-in functional interfaces (compact).
 *
 * How the program runs (high level):
 * - Ask the user for the number of Miller-Rabin iterations (k).
 * - Create small "built-in function" objects:
 *     * Supplier<SecureRandom> rngSupplier  -> provides randomness
 *     * Predicate<BigInteger> isProbableMR   -> wraps millerRabin(n,k,rng)
 *     * Function<BigInteger,String> summary -> maps n -> readable result string
 *     * Consumer<String> out                -> prints lines (System.out::println)
 * - Read decimal integers from the user (blank line to stop).
 * - For each input n:
 *     * Run the Predicate (which runs the Miller-Rabin test k times)
 *     * Format and print a short summary including Java's builtin isProbablePrime
 *
 * Where built-in functional interfaces appear and why:
 * - Predicate<T>: represents a boolean test -> here it encapsulates "is n probably prime?"
 * - Function<T,R>: represents a transformation -> here it builds a readable result from n
 * - Supplier<T>: represents a lazy provider -> here it creates SecureRandom instances on demand
 * - Consumer<T>: represents an action with side-effect -> here it prints output
 *
 * Miller-Rabin algorithm (brief):
 * - For odd n > 2, write n-1 as d * 2^s with d odd.
 * - Repeat k times:
 *     * choose random base a in [2, n-2]
 *     * compute x = a^d mod n
 *     * if x == 1 or x == n-1, this round passes
 *     * else square x up to s-1 times: if x becomes n-1 the round passes
 *     * if no check passes, n is composite
 * - If all k rounds pass, n is declared "probably prime".
 *
 * When Miller-Rabin may fail (limitations):
 * - Miller-Rabin is probabilistic: it can falsely label a composite as "probably prime".
 *   The error probability for random bases is at most 1/4 per independent round -> overall
 *   error <= 4^{-k}. Use larger k to reduce error.
 * - There exist strong pseudoprimes (composites that pass many bases); deterministic
 *   correctness can be achieved by testing specific fixed bases up to certain limits
 *   (e.g. deterministic lists for 32/64-bit ranges), but not in general for arbitrary big n.
 * - Poor RNG or biased base selection can increase the risk of false positives.
 * - For absolute certainty use deterministic primality proofs (e.g. AKS or elliptic-curve
 *   primality proving) or known deterministic base sets for bounded ranges.
 *
 * Practical notes:
 * - This demo compares our MR result to BigInteger.isProbablePrime for reference.
 * - Choose k based on acceptable risk (k = 5..10 is common for general use).
 */
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BuiltInFunctions {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Supplier<SecureRandom> rngSupplier = SecureRandom::new;
        Consumer<String> out = System.out::println;

        System.out.print("Enter Miller-Rabin iterations (e.g. 5): ");
        int iterations = safeReadInt(scanner);

        Predicate<BigInteger> isProbableMR = n -> millerRabin(n, iterations, rngSupplier.get());
        Function<BigInteger, String> summary = n ->
            String.format("%s -> MR=%b (k=%d) | builtin=%b",
                          n.toString(),
                          isProbableMR.test(n),
                          iterations,
                          n.isProbablePrime(Math.max(1, iterations)));

        out.accept("Enter numbers to test (blank to quit):");
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) break;
            try {
                BigInteger n = new BigInteger(line);
                out.accept(summary.apply(n));
            } catch (NumberFormatException ex) {
                out.accept("Invalid integer, try again.");
            }
        }

        scanner.close();
    }

    private static int safeReadInt(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid integer: ");
            scanner.nextLine();
        }
        int v = scanner.nextInt();
        scanner.nextLine();
        return v;
    }

    // Miller-Rabin primality test (returns true = probable prime)
    private static boolean millerRabin(BigInteger n, int k, SecureRandom rnd) {
        final BigInteger TWO = BigInteger.valueOf(2);
        final BigInteger THREE = BigInteger.valueOf(3);

        if (n.compareTo(BigInteger.ONE) <= 0) return false;
        if (n.equals(TWO) || n.equals(THREE)) return true;
        if (n.mod(TWO).equals(BigInteger.ZERO)) return false;

        // write n-1 as d * 2^s with d odd
        BigInteger d = n.subtract(BigInteger.ONE);
        int s = 0;
        while (d.mod(TWO).equals(BigInteger.ZERO)) {
            d = d.divide(TWO);
            s++;
        }

        for (int i = 0; i < k; i++) {
            BigInteger a = uniformRandom(TWO, n.subtract(TWO), rnd);
            BigInteger x = a.modPow(d, n);
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) continue;
            boolean found = false;
            for (int r = 1; r < s; r++) {
                x = x.modPow(TWO, n);
                if (x.equals(n.subtract(BigInteger.ONE))) {
                    found = true;
                    break;
                }
            }
            if (!found) return false; // composite
        }
        return true; // probably prime
    }

    // uniform random in [min, max] inclusive
    private static BigInteger uniformRandom(BigInteger min, BigInteger max, SecureRandom rnd) {
        BigInteger range = max.subtract(min).add(BigInteger.ONE); // inclusive
        int bits = range.bitLength();
        BigInteger r;
        do {
            r = new BigInteger(bits, rnd);
        } while (r.compareTo(range) >= 0);
        return r.add(min);
    }
}