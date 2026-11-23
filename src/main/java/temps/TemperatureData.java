package temps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Loads the bundled daily temperature files and, when asked, feeds them through
 * a queue with a pause between readings.
 *
 * <p>
 * The pause belongs to the feeder thread inside this class. A
 * {@link TemperatureWorker} never sleeps: it only calls
 * {@link TemperatureSource#next()}.
 */
public final class TemperatureData {

    /** Daily means for Las Palmas, year 2024. Do not modify the file. */
    public static final Path LAS_PALMAS = Path.of("data/las-palmas.csv");

    /** Daily means for Helsinki, year 2024. Do not modify the file. */
    public static final Path HELSINKI = Path.of("data/helsinki.csv");

    private TemperatureData() {
    }

    /**
     * Reads the temperature column from a CSV file.
     *
     * <p>
     * Lines that are empty, start with {@code #}, or are the header are ignored.
     * Each remaining line is {@code date,temperature_c}.
     *
     * @param csvFile the CSV file to read
     * @return the temperature values in file order, in degrees Celsius
     * @throws IOException if the file cannot be read
     */
    public static double[] readValues(Path csvFile) throws IOException {
        List<Double> values = new ArrayList<>();
        for (String line : Files.readAllLines(csvFile, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("date")) {
                continue;
            }
            String[] columns = trimmed.split(",");
            values.add(Double.parseDouble(columns[1]));
        }
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    /**
     * Opens a city's CSV file as a temperature source.
     *
     * @param csvFile         the bundled CSV file
     * @param feedDelayMillis pause between readings. {@code 0} delivers every
     *                        reading immediately on the worker's calls to
     *                        {@link TemperatureSource#next()}, without an extra
     *                        feeder thread. A positive value starts a feeder
     *                        thread that publishes one reading at a time.
     * @return a source the city worker can consume
     * @throws IOException if the file cannot be read
     */
    public static TemperatureSource open(Path csvFile, long feedDelayMillis) throws IOException {
        double[] values = readValues(csvFile);
        if (feedDelayMillis <= 0) {
            return new ArrayTemperatureSource(values);
        }
        QueueTemperatureSource queued = new QueueTemperatureSource(values.length);
        Thread feeder = new Thread(() -> feed(queued, values, feedDelayMillis), "temperature-feeder");
        feeder.setDaemon(true);
        feeder.start();
        return queued;
    }

    private static void feed(QueueTemperatureSource queued, double[] values, long feedDelayMillis) {
        try {
            for (double value : values) {
                if (feedDelayMillis > 0) {
                    Thread.sleep(feedDelayMillis);
                }
                queued.publish(value);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            queued.close();
        }
    }

    /**
     * An in-memory source used when the readings are already available.
     */
    static final class ArrayTemperatureSource implements TemperatureSource {

        private final double[] values;
        private int index;

        ArrayTemperatureSource(double[] values) {
            this.values = values;
        }

        @Override
        public synchronized OptionalDouble next() {
            if (index >= values.length) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(values[index++]);
        }

        @Override
        public int size() {
            return values.length;
        }
    }
}
