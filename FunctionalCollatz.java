/**
 * FunctionalCollatz
 *
 * - Demonstrates Supplier/Consumer/Predicate/Function with the Collatz sequence.
 * - After computing the Collatz path the program generates a simple PNG image
 *   that visualizes the flow: x = iteration index, y = sequence value.
 *
 * Chart details:
 * - X axis: step index (0..n-1)
 * - Y axis: value (scaled to fit image height)
 * - Points are connected with lines; each value also drawn as a small circle.
 * - Image auto-scales width; very long sequences are compressed to a reasonable width.
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.imageio.ImageIO;

public class FunctionalCollatz {

    public static void main(String[] args) {
        // 1. SUPPLIER: Provides data (here, it just supplies the prompt text)
        Supplier<String> promptProvider = () -> "Enter a number (0 to quit): ";

        // 2. CONSUMER: Performs an action (here, printing to console)
        Consumer<String> printer = System.out::println;

        // 3. PREDICATE: Tests a condition (here, checks if a number is Even)
        Predicate<Integer> isEven = n -> n % 2 == 0;

        // 4. FUNCTION: Transforms data (here, calculates the next step)
        Function<Integer, Integer> nextStep = n -> isEven.test(n) ? n / 2 : n * 3 + 1;

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(promptProvider.get());
            if (!scanner.hasNextInt()) {
                scanner.nextLine();
                break;
            }
            int number = scanner.nextInt();
            scanner.nextLine();
            if (number == 0) break;
            if (number < 1) {
                printer.accept("Enter a positive integer.");
                continue;
            }

            List<Integer> pathList = new ArrayList<>();
            pathList.add(number);
            while (number != 1) {
                number = nextStep.apply(number);
                pathList.add(number);
                // safety: avoid infinite loops (practical guard)
                if (pathList.size() > 10000) break;
            }

            // Print textual path
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < pathList.size(); i++) {
                if (i > 0) path.append(" -> ");
                path.append(pathList.get(i));
            }
            printer.accept("Path: " + path.toString());
            printer.accept("-----------------------");

            // Generate simple graphic of the path (x iterations, y value)
            String filename = "collatz_path_" + pathList.get(0) + ".png";
            try {
                drawCollatzImage(pathList, filename);
                printer.accept("Image saved: " + filename);
            } catch (IOException ex) {
                printer.accept("Failed to write image: " + ex.getMessage());
            }
        }
        scanner.close();
    }

    // Draws a 2-axis chart: x = iteration index, y = value
    private static void drawCollatzImage(List<Integer> seq, String filename) throws IOException {
        final int nodes = seq.size();
        final int margin = 60;
        final int maxWidth = 1800;
        final int imgH = 600;

        // compute x step to keep width reasonable
        final int xStep = (nodes <= 1) ? 0 : Math.max(2, Math.min(40, (maxWidth - 2 * margin) / (nodes - 1)));
        final int imgW = Math.max(300, margin * 2 + Math.max(0, (nodes - 1) * xStep));

        // find min/max values for y-scaling
        long minV = Long.MAX_VALUE, maxV = Long.MIN_VALUE;
        for (int v : seq) {
            if (v < minV) minV = v;
            if (v > maxV) maxV = v;
        }
        if (minV == maxV) { // avoid division by zero
            minV = Math.min(0, minV);
            maxV = Math.max(1, maxV);
        }

        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgW, imgH);

        // axes
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        int x0 = margin, y0 = imgH - margin;
        g.drawLine(x0, y0, imgW - margin + 10, y0); // x axis
        g.drawLine(x0, y0, x0, margin - 10);         // y axis

        // grid and labels: y ticks
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(200, 200, 200));
        int yTicks = 8;
        for (int i = 0; i <= yTicks; i++) {
            double t = i / (double) yTicks;
            int yy = margin + (int) ((1 - t) * (imgH - 2 * margin));
            g.drawLine(x0 + 1, yy, imgW - margin, yy);
            long val = minV + Math.round(t * (maxV - minV));
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.valueOf(val), 8, yy + 4);
            g.setColor(new Color(200, 200, 200));
        }

        // x ticks every up-to step to avoid clutter
        g.setColor(new Color(180, 180, 180));
        int xtickPeriod = Math.max(1, nodes / 10);
        for (int i = 0; i < nodes; i += xtickPeriod) {
            int xx = x0 + i * xStep;
            g.drawLine(xx, y0 - 3, xx, margin);
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.valueOf(i), xx - 6, y0 + 18);
            g.setColor(new Color(180, 180, 180));
        }

        // compute plotted points
        int[] xs = new int[nodes];
        int[] ys = new int[nodes];
        for (int i = 0; i < nodes; i++) {
            xs[i] = x0 + i * xStep;
            double norm = (seq.get(i) - (double) minV) / (maxV - minV);
            ys[i] = margin + (int) ((1 - norm) * (imgH - 2 * margin));
        }

        // polyline
        g.setColor(new Color(30, 120, 200));
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < nodes - 1; i++) {
            g.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        // points
        g.setColor(new Color(220, 40, 40));
        for (int i = 0; i < nodes; i++) {
            int r = 4;
            g.fillOval(xs[i] - r, ys[i] - r, r * 2, r * 2);
        }

        // labels
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Collatz sequence (x = iteration, y = value)", margin, 20);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("iterations →", imgW - margin - 80, y0 + 30);
        g.drawString("value →", 8, margin - 20);

        g.dispose();
        ImageIO.write(img, "png", new File(filename));
    }
}