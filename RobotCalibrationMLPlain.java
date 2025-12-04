import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RobotCalibrationMLPlain {

    public static void main(String[] args) {
        // 1. The Real Truth (Unknown to the robot)
        double realSlope = 2.0;
        double realIntercept = 5.0;

        // 2. The Learning Model (SGD Linear Regression)
        OnlineLinearRegression robotBrain = new OnlineLinearRegression(0.01); // learning rate

        System.out.println("--- Starting Robot Calibration Stream ---");
        System.out.printf("Target Logic: y = %.1fx + %.1f%n%n", realSlope, realIntercept);

        // 3. THE STREAM PIPELINE
        Stream.generate(new Sensor(realSlope, realIntercept)) // Infinite Sensor Data
              .limit(20)                                      // Stop after 20 readings (for demo)
              .map(robotBrain::trainAndPredict)               // Learn & Return Result
              .filter(Result::isAnomaly)                      // Decide which results to show
              .forEach(System.out::println);                  // Output
    }

    // --- Data Classes (replacing records) ---

    // Simple container for sensor data
    static class SensorData {
        private final double voltage;
        private final double measuredSpeed;

        public SensorData(double voltage, double measuredSpeed) {
            this.voltage = voltage;
            this.measuredSpeed = measuredSpeed;
        }

        public double voltage() { return voltage; }
        public double measuredSpeed() { return measuredSpeed; }
    }

    // Container for the output of our ML model
    static class Result {
        private final double voltage;
        private final double predicted;
        private final double actual;
        private final double error;
        private final double currentSlope;

        public Result(double voltage, double predicted, double actual, double error, double currentSlope) {
            this.voltage = voltage;
            this.predicted = predicted;
            this.actual = actual;
            this.error = error;
            this.currentSlope = currentSlope;
        }

        // In this demo we treat small error results as the ones to print
        public boolean isAnomaly() {
            return Math.abs(error) < 2.0;
        }

        @Override
        public String toString() {
            return String.format("Volt: %.2f | Act: %.2f | Pred: %.2f | Error: %6.3f | Learned Slope: %.3f",
                    voltage, actual, predicted, error, currentSlope);
        }
    }

    // --- The Online Algorithm (SGD) ---
    static class OnlineLinearRegression {
        private double m = 0.0; // slope
        private double b = 0.0; // intercept
        private final double learningRate;

        public OnlineLinearRegression(double learningRate) {
            this.learningRate = learningRate;
        }

        // mapper: update model and return Result
        public Result trainAndPredict(SensorData data) {
            double x = data.voltage();
            double y = data.measuredSpeed();

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
            double voltage = rng.nextDouble() * 10; // 0..10V
            double noise = (rng.nextDouble() - 0.5) * 2; // +/-1.0 noise
            double speed = (realM * voltage) + realB + noise;
            return new SensorData(voltage, speed);
        }
    }
}