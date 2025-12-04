# Java Functional Programming — Examples & Notes

This workspace contains small, focused Java examples that demonstrate core functional‑programming ideas and the Java Streams API (written to be Java 8 compatible).

Current Java files
- BuiltInFunctions.java — utilities / examples showing built‑in functional helpers.
- FunctionalCollatz.java — Collatz sequence with PNG visualization (x = iteration, y = value).
- FunctionalCollatzPlain.java — compact, plain (non-graphical) Collatz demo.
- HigherOrderLogic.java — higher‑order functions, interactive multiplier and converter example.
- Lambdas.java — short lambda examples and method references.
- OnlineStreamSort.java — stream-driven sorting/online processing demo.
- RobotCalibrationML.java — online linear regression (SGD) with chart output showing actual vs predicted and learned slope.
- RobotCalibrationMLPlain.java — plain console version of the calibration demo.
- StreamAlg.java — concise Streams primer (generate/iterate/range, primitive streams).
- StreamXorShift.java — XORShift64* PRNG demo using LongStream.iterate; optional seed and bounded sampling.
- VisualForecast.java — Swing visualizer plotting forecast vs actual from a synthetic time series.



Quick build & run (Windows / PowerShell)
- Compile all:
  ```powershell
  javac --release 8 *.java
  ```
- Run one example:
  ```powershell
  java -cp . StreamAlg
  java -cp . FunctionalCollatz        # prints path + writes collatz_path_<n>.png
  java -cp . VisualForecast           # opens Swing window with forecast chart
  java -cp . RobotCalibrationML       # generates chart calibration_chart.png
  ```

Notes on themes & interpretation
- Streams: express pipelines (map/filter/reduce) declaratively. Use `limit()` to consume infinite streams safely. Primitive streams (IntStream/LongStream/DoubleStream) reduce boxing overhead and provide numeric helpers (sum, average, summaryStatistics).
- Higher‑order functions: functions that return or accept functions — see `HigherOrderLogic.createMultiplier`.
- PRNG (StreamXorShift): shows how to produce pseudorandom sequences with `LongStream.iterate(seed, nextState)` and map to outputs.
- Online learning (RobotCalibrationML): the stream is the dataflow: Sensor → map(trainAndPredict) → collect/plot. Inspect charts: blue = actual, red = predicted; slope progression indicates learning convergence.
- Visualizations: examples write PNG files or open Swing frames to map iteration → x and value/prediction → y.

Troubleshooting
- UnsupportedClassVersionError: indicates the runtime Java is older than the compiler target. Either:
  - install a newer JRE/JDK that matches the compiled classes, or
  - recompile with `javac --release 8 <file>.java`.
- Large sample sizes or very long sequences may use significant memory; use moderate sizes for experimentation (10–10000).

Tips / experiments
- Try changing sample sizes interactively (many examples accept input).
- Replace `.collect(Collectors.toList())` with streaming consumers (`forEach`) to avoid storing all data.
- Swap to `parallel()` on heavy numeric streams to experiment with parallelism (careful with shared mutable state).

License
- Use and adapt examples for learning. Improve and submit concise changes as needed.
