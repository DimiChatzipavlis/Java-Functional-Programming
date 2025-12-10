import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class RobotCalibrationML {

    public static void main(String[] args) {
        double realSlope = 2.0;
        double realIntercept = 5.0;
        OnlineLinearRegression robotBrain = new OnlineLinearRegression(0.01);

        System.out.println("--- Starting Robot Calibration Stream ---");
        System.out.printf("Target Logic: y = %.1fx + %.1f%n%n", realSlope, realIntercept);

        // collect the streamed results so we can both inspect and plot them
        final int N = 50;
        List<Result> results = Stream.generate(new Sensor(realSlope, realIntercept))
                                     .limit(N)
                                     .map(robotBrain::trainAndPredict)
                                     .collect(Collectors.toList());

        // print "interesting" results (same rule as before)
        results.stream().filter(Result::isAnomaly).forEach(System.out::println);

        // produce a simple chart: x = iteration, y = value (actual vs predicted),
        // and annotate learned slope progression
        try {
            drawCalibrationChart(results, "calibration_chart.png");
            System.out.println("Chart saved: calibration_chart.png");
        } catch (IOException ex) {
            System.out.println("Failed to save chart: " + ex.getMessage());
        }
    }

    // --- Data Classes (replacing records) ---

    static class SensorData {
        private final double voltage;
        private final double measuredSpeed;
        public SensorData(double voltage, double measuredSpeed) { this.voltage = voltage; this.measuredSpeed = measuredSpeed; }
        public double getVoltage() { return voltage; }
        public double getMeasuredSpeed() { return measuredSpeed; }
    }

    static class Result {
        private final double voltage;
        private final double predicted;
        private final double actual;
        private final double error;
        private final double currentSlope;

        public Result(double voltage, double predicted, double actual, double error, double currentSlope) {
            this.voltage = voltage; this.predicted = predicted; this.actual = actual; this.error = error; this.currentSlope = currentSlope;
        }

        public boolean isAnomaly() { return Math.abs(error) < 2.0; }

        public double getVoltage() { return voltage; }
        public double getPredicted() { return predicted; }
        public double getActual() { return actual; }
        public double getError() { return error; }
        public double getCurrentSlope() { return currentSlope; }

        @Override
        public String toString() {
            return String.format("Volt: %.2f | Act: %.2f | Pred: %.2f | Error: %6.3f | Learned Slope: %.3f",
                    voltage, actual, predicted, error, currentSlope);
        }
    }

    // --- The Online Algorithm (SGD) ---
    static class OnlineLinearRegression {
        private double m = 0.0;
        private double b = 0.0;
        private final double learningRate;
        public OnlineLinearRegression(double learningRate) { this.learningRate = learningRate; }

        public Result trainAndPredict(SensorData data) {
            double x = data.getVoltage();
            double y = data.getMeasuredSpeed();
            double prediction = (m * x) + b;
            double error = y - prediction;
            m = m + (learningRate * error * x);
            b = b + (learningRate * error);
            return new Result(x, prediction, y, error, m);
        }
    }

    // --- The Sensor (Supplier) ---
    static class Sensor implements Supplier<SensorData> {
        private final Random rng = new Random();
        private final double realM;
        private final double realB;
        public Sensor(double m, double b) { this.realM = m; this.realB = b; }
        @Override
        public SensorData get() {
            double voltage = rng.nextDouble() * 10;
            double noise = (rng.nextDouble() - 0.5) * 2;
            double speed = (realM * voltage) + realB + noise;
            return new SensorData(voltage, speed);
        }
    }

    // Simple chart: actual vs predicted, plus slope progression (bottom)
    private static void drawCalibrationChart(List<Result> results, String filename) throws IOException {
        final int W = 900, H = 500, M = 50;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0,0,W,H);

        // compute ranges
        double maxY = results.stream().mapToDouble(Result::getActual).max().orElse(1);
        maxY = Math.max(maxY, results.stream().mapToDouble(Result::getPredicted).max().orElse(1));
        double minY = results.stream().mapToDouble(Result::getActual).min().orElse(0);
        minY = Math.min(minY, results.stream().mapToDouble(Result::getPredicted).min().orElse(0));
        if (maxY == minY) { maxY += 1; minY = Math.max(0, minY - 1); }

        int n = results.size();
        double xScale = (double)(W - 2*M) / Math.max(1, n-1);
        double yScale = (double)(H - 3*M) / (maxY - minY);

        // axes
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(M, H - M, W - M, H - M); // x
        g.drawLine(M, H - M, M, M);         // y

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        // draw grid + y labels
        int yticks = 6;
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(230,230,230));
        for (int i = 0; i <= yticks; i++) {
            int yy = M + (int)((yticks - i) * (H - 3*M) / (double)yticks);
            g.drawLine(M+1, yy, W - M, yy);
            g.setColor(Color.DARK_GRAY);
            double val = minY + (maxY - minY) * i / (double)yticks;
            g.drawString(String.format("%.1f", val), 5, yy+4);
            g.setColor(new Color(230,230,230));
        }

        // plot actual (blue) and predicted (red)
        int[] ax = new int[n], ay = new int[n], px = new int[n], py = new int[n];
        for (int i = 0; i < n; i++) {
            Result r = results.get(i);
            ax[i] = M + (int)(i * xScale);
            ay[i] = H - M - (int)((r.getActual() - minY) * yScale);
            px[i] = ax[i];
            py[i] = H - M - (int)((r.getPredicted() - minY) * yScale);
        }

        g.setStroke(new BasicStroke(2f));
        // actual polyline
        g.setColor(new Color(30,120,200));
        for (int i = 0; i < n-1; i++) g.drawLine(ax[i], ay[i], ax[i+1], ay[i+1]);
        // predicted polyline
        g.setColor(new Color(200,40,40));
        for (int i = 0; i < n-1; i++) g.drawLine(px[i], py[i], px[i+1], py[i+1]);

        // points
        for (int i = 0; i < n; i++) {
            g.setColor(new Color(30,120,200)); g.fillOval(ax[i]-3, ay[i]-3, 7,7);
            g.setColor(new Color(200,40,40)); g.fillOval(px[i]-3, py[i]-3, 5,5);
        }

        // slope progression at bottom area
        double minSlope = results.stream().mapToDouble(Result::getCurrentSlope).min().orElse(0);
        double maxSlope = results.stream().mapToDouble(Result::getCurrentSlope).max().orElse(1);
        if (minSlope == maxSlope) { maxSlope += 1; minSlope -= 1; }
        int baseY = H - M/2;
        g.setColor(new Color(0,150,0));
        for (int i = 0; i < n; i++) {
            double s = results.get(i).getCurrentSlope();
            int sx = M + (int)(i * xScale);
            int sy = baseY - (int)((s - minSlope) / (maxSlope - minSlope) * (M/2));
            g.fillRect(sx-2, sy-2, 4,4);
        }
        g.setColor(Color.DARK_GRAY);
        g.drawString("green: learned slope (trend)", M, baseY + 15);

        // legend & title
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.BLACK);
        g.drawString("Robot Calibration — actual (blue) vs predicted (red)", M, 20);

        g.dispose();
        ImageIO.write(img, "png", new File(filename));
    }
}