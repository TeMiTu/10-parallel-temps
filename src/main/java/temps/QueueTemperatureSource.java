package temps;

import java.util.OptionalDouble;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link TemperatureSource} whose readings are published by another thread.
 *
 * <p>
 * This is the extension point for a program that feeds temperatures at its own
 * pace. The city worker only calls {@link #next()}. The feeder calls
 * {@link #publish(double)} once per reading and {@link #close()} after the last
 * one. The bundled files use the same queue when a feed delay is configured.
 */
public final class QueueTemperatureSource implements TemperatureSource {

    private final int size;
    private final BlockingQueue<OptionalDouble> readings = new LinkedBlockingQueue<>();

    /**
     * Creates a source that will publish {@code size} readings.
     *
     * @param size the total number of readings that will be published
     */
    public QueueTemperatureSource(int size) {
        this.size = size;
    }

    /**
     * Makes one reading available to the worker.
     *
     * @param celsius the temperature in degrees Celsius
     */
    public void publish(double celsius) {
        readings.offer(OptionalDouble.of(celsius));
    }

    /**
     * Tells the worker that no further readings will be published.
     */
    public void close() {
        readings.offer(OptionalDouble.empty());
    }

    @Override
    public OptionalDouble next() {
        try {
            return readings.take();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return OptionalDouble.empty();
        }
    }

    @Override
    public int size() {
        return size;
    }
}
