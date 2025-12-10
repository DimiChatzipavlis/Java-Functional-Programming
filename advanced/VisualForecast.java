import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

public class VisualForecast extends JPanel {

    // --- Configuration ---
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final int PADDING = 50;
    private static final int LABEL_PADDING = 25;

    // The data to display
    private final List<PredictionResult> data;
    private final double minScore;
    private final double maxScore;

    public VisualForecast(List<PredictionResult> data) {
        this.data = data;
        double min = data.stream().mapToDouble(d -> Math.min(d.getActual(), d.getForecast())).min().orElse(0);
        double max = data.stream().mapToDouble(d -> Math.max(d.getActual(), d.getForecast())).max().orElse(100);
        // add margin for better visuals
        this.minScore = Math.floor(min - 10);
        this.maxScore = Math.ceil(max + 10);
    }

    // --- The Painting Engine (Where the graphics happen) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Turn on Anti-Aliasing (makes lines smooth, not jagged)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Background & Grid
        drawGrid(g2);

        // 2. Draw The "Forecast" Line (Green)
        g2.setColor(new Color(46, 204, 113)); // Emerald Green
        g2.setStroke(new BasicStroke(2f));
        drawDataLine(g2, true);

        // 3. Draw The "Actual" Line (Blue)
        g2.setColor(new Color(52, 152, 219)); // Blue
        g2.setStroke(new BasicStroke(2f));
        drawDataLine(g2, false);

        // 4. Draw Anomalies (Red Dots)
        drawAnomalies(g2);
        
        // 5. Draw Legend
        drawLegend(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillRect(PADDING, PADDING, getWidth() - 2 * PADDING, getHeight() - 2 * PADDING);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(PADDING, PADDING, getWidth() - 2 * PADDING, getHeight() - 2 * PADDING);

        g2.setColor(Color.BLACK);
        int yDivs = 8;
        for (int i = 0; i <= yDivs; i++) {
            int y = PADDING + i * (getHeight() - 2 * PADDING) / yDivs;
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(PADDING, y, getWidth() - PADDING, y);
            g2.setColor(Color.DARK_GRAY);
            double val = maxScore - i * (maxScore - minScore) / yDivs;
            g2.drawString(String.format("%.0f", val), 8, y + 4);
        }
    }

    private void drawDataLine(Graphics2D g2, boolean useForecast) {
        if (data.size() < 2) return;
        double xScale = (double) (getWidth() - 2 * PADDING) / (data.size() - 1);
        double yScale = (double) (getHeight() - 2 * PADDING) / (maxScore - minScore);

        for (int i = 0; i < data.size() - 1; i++) {
            double v1 = useForecast ? data.get(i).getForecast() : data.get(i).getActual();
            double v2 = useForecast ? data.get(i + 1).getForecast() : data.get(i + 1).getActual();
            double x1 = PADDING + i * xScale;
            double y1 = PADDING + (maxScore - v1) * yScale;
            double x2 = PADDING + (i + 1) * xScale;
            double y2 = PADDING + (maxScore - v2) * yScale;
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
        }
        // draw points
        for (int i = 0; i < data.size(); i++) {
            double v = useForecast ? data.get(i).getForecast() : data.get(i).getActual();
            double x = PADDING + i * xScale;
            double y = PADDING + (maxScore - v) * yScale;
            int r = useForecast ? 4 : 5;
            g2.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
        }
    }

    private void drawAnomalies(Graphics2D g2) {
        double xScale = (double) (getWidth() - 2 * PADDING) / Math.max(1, data.size() - 1);
        double yScale = (double) (getHeight() - 2 * PADDING) / (maxScore - minScore);
        g2.setColor(Color.RED);
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isAlert()) {
                double x = PADDING + i * xScale;
                double y = PADDING + (maxScore - data.get(i).getActual()) * yScale;
                g2.fill(new Ellipse2D.Double(x - 6, y - 6, 12, 12));
                g2.drawString("ANOMALY", (int) x - 20, (int) y - 12);
            }
        }
    }

    private void drawLegend(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(52, 152, 219));
        g2.drawString("● Actual", getWidth() / 2 - 80, 24);
        g2.setColor(new Color(46, 204, 113));
        g2.drawString("● Forecast", getWidth() / 2 - 10, 24);
        g2.setColor(Color.RED);
        g2.drawString("● Anomaly", getWidth() / 2 + 80, 24);
    }

    // --- MAIN EXECUTION ---
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        Forecaster model = new Forecaster(0.25);

        // generate more varied data: stronger trend, multi-frequency cycles, larger noise and random spikes
        List<PredictionResult> results = generateServerLogCSV(60)
            .skip(1)
            .map(VisualForecast::parseCsv)
            .map(model::predictNext)
            .collect(Collectors.toList());

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Streams ML Visualization - More Variance");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new VisualForecast(results));
            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // --- Reuse: Logic & Data Classes (record -> class replacements) ---

    static class DataPoint {
        private final int time;
        private final double value;
        public DataPoint(int time, double value) { this.time = time; this.value = value; }
        public int getTime() { return time; }
        public double getValue() { return value; }
    }

    static class PredictionResult {
        private final int time;
        private final double actual;
        private final double forecast;
        private final double error;
        private final boolean alert;
        public PredictionResult(int time, double actual, double forecast, double error, boolean alert) {
            this.time = time; this.actual = actual; this.forecast = forecast; this.error = error; this.alert = alert;
        }
        public int getTime() { return time; }
        public double getActual() { return actual; }
        public double getForecast() { return forecast; }
        public double getError() { return error; }
        public boolean isAlert() { return alert; }
    }

    static class Forecaster {
        private final double alpha;
        private Double state = null;
        public Forecaster(double alpha) { this.alpha = alpha; }
        public PredictionResult predictNext(DataPoint current) {
            if (state == null) {
                state = current.getValue();
                return new PredictionResult(current.getTime(), current.getValue(), current.getValue(), 0.0, false);
            }
            double forecast = state;
            double error = current.getValue() - forecast;
            state = alpha * current.getValue() + (1 - alpha) * state;
            boolean isAnomaly = Math.abs(error) > 25.0; // larger threshold due to increased variance
            return new PredictionResult(current.getTime(), current.getValue(), forecast, error, isAnomaly);
        }
    }

    private static DataPoint parseCsv(String line) {
        String[] parts = line.split(",");
        return new DataPoint(Integer.parseInt(parts[0].trim()), Double.parseDouble(parts[1].trim()));
    }

    private static Stream<String> generateServerLogCSV(int limit) {
        Random rng = new Random();
        Stream<String> header = Stream.of("Timestamp,CPU_Load");
        Stream<String> data = Stream.iterate(0, t -> t + 1)
            .limit(limit)
            .map(t -> {
                // increase trend, combine multiple sine components, larger gaussian noise
                double trend = t * 1.2; // stronger upward trend
                double cycle1 = 18 * Math.sin(t * 0.35); // faster oscillation
                double cycle2 = 12 * Math.sin(t * 0.08); // slow wave
                double jitter = 6 * rng.nextGaussian();  // larger noise
                double value = 40 + trend + cycle1 + cycle2 + jitter;
                // occasional random spikes/dropouts
                if (rng.nextDouble() < 0.12) value += (rng.nextDouble() * 120 - 60);
                return t + "," + String.format("%.2f", value);
            });
        return Stream.concat(header, data);
    }
}