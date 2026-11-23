package temps;

import java.util.OptionalDouble;

/**
 * A sequence of temperature readings in degrees Celsius.
 *
 * <p>
 * One city worker consumes one source. A source may return readings that are
 * already stored in this repository, or readings published later by a separate
 * feeder. {@link #next()} blocks while a feeder has not yet published the next
 * reading. An empty optional means the source is exhausted.
 */
public interface TemperatureSource {

    /**
     * Returns the next temperature reading.
     *
     * @return the next reading in degrees Celsius, or an empty optional when no
     *         further readings will arrive
     */
    OptionalDouble next();

    /**
     * Returns how many readings this source will produce in total.
     *
     * <p>
     * The total stays constant so a progress bar can compare the worker's
     * {@code checked} counter with the full set of readings.
     *
     * @return the number of readings, not counting the end of the source
     */
    int size();
}
