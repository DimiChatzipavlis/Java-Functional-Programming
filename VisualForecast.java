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
    private double minScore;
    private double maxScore;

    public VisualForecast(List<PredictionResult> data) {
        this.data = data;
        // Calculate min/max for Y-Axis scaling
        this.minScore = 0; // Fixed floor for better visual context
        this.maxScore = data.stream()
                            .mapToDouble(d -> Math.max(d.getActual(), d.getForecast()))
                            .max().orElse(100) + 10;
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
        g2.fillRect(PADDING, PADDING, getWidth() - (2 * PADDING), getHeight() - (2 * PADDING));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(PADDING, PADDING, getWidth() - (2 * PADDING), getHeight() - (2 * PADDING));
        
        // Draw simple Y-axis markers
        g2.setColor(Color.BLACK);
        int numberYDivisions = 10;
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = PADDING;
            int x1 = PADDING + LABEL_PADDING;
            int y0 = getHeight() - ((i * (getHeight() - PADDING * 2)) / numberYDivisions + PADDING);
            g2.drawLine(PADDING, y0, getWidth() - PADDING, y0); // Grid line
            String label = String.format("%.0f", minScore + (maxScore - minScore) * ((i * 1.0) / numberYDivisions));
            g2.drawString(label, x0 - LABEL_PADDING - 5, y0 + 5); 
        }
    }

    private void drawDataLine(Graphics2D g2, boolean useForecast) {
        double xScale = ((double) getWidth() - (2 * PADDING)) / (data.size() - 1);
        double yScale = ((double) getHeight() - (2 * PADDING)) / (maxScore - minScore);

        for (int i = 0; i < data.size() - 1; i++) {
            double x1 = PADDING + i * xScale;
            double y1 = getHeight() - PADDING - ((useForecast ? data.get(i).getForecast() : data.get(i).getActual()) - minScore) * yScale;
            double x2 = PADDING + (i + 1) * xScale;
            double y2 = getHeight() - PADDING - ((useForecast ? data.get(i + 1).getForecast() : data.get(i + 1).getActual()) - minScore) * yScale;
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
        }
    }

    private void drawAnomalies(Graphics2D g2) {
        double xScale = ((double) getWidth() - (2 * PADDING)) / (data.size() - 1);
        double yScale = ((double) getHeight() - (2 * PADDING)) / (maxScore - minScore);

        g2.setColor(Color.RED);
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isAlert()) {
                double x = PADDING + i * xScale;
                double y = getHeight() - PADDING - (data.get(i).getActual() - minScore) * yScale;
                // Draw a big red circle
                Ellipse2D.Double circle = new Ellipse2D.Double(x - 6, y - 6, 12, 12);
                g2.fill(circle);
                g2.drawString("ANOMALY", (int)x - 20, (int)y - 15);
            }
        }
    }
    
    private void drawLegend(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(52, 152, 219)); 
        g2.drawString("--- Actual Sensor", getWidth()/2 - 100, 30);
        g2.setColor(new Color(46, 204, 113));
        g2.drawString("--- ML Forecast", getWidth()/2 + 20, 30);
    }

    // --- MAIN EXECUTION ---
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        // 1. Generate Data & Run ML (Same Logic as before)
        Forecaster model = new Forecaster(0.3);
        
        List<PredictionResult> results = generateServerLogCSV(35)
            .skip(1)
            .map(VisualForecast::parseCsv)
            .map(model::predictNext)
            .collect(Collectors.toList()); // Collect to List for the GUI

        // 2. Launch GUI Window
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java Streams ML Visualization");
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
        private Double smoothedState = null;

        public Forecaster(double alpha) { this.alpha = alpha; }

        public PredictionResult predictNext(DataPoint current) {
            if (smoothedState == null) {
                smoothedState = current.getValue();
                return new PredictionResult(current.getTime(), current.getValue(), current.getValue(), 0.0, false);
            }
            double forecast = smoothedState;
            double error = current.getValue() - forecast;
            smoothedState = (alpha * current.getValue()) + ((1 - alpha) * smoothedState);
            // Threshold 15.0
            boolean isAnomaly = Math.abs(error) > 15.0;
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
                double trend = t * 0.5;
                double cycle = 10 * Math.sin(t * 0.5);
                double noise = rng.nextGaussian() * 2;
                double value = 50 + trend + cycle + noise;
                if (t == 20) value += 40; // The Spike
                return t + "," + String.format("%.2f", value);
            });
        return Stream.concat(header, data);
    }
}