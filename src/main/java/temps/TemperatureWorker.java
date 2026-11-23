package temps;

import java.util.OptionalDouble;

/**
 * Computes the average temperature of one city on its own thread.
 *
 * <p>
 * The worker increments {@link #checked} once for every reading it consumes.
 * That volatile counter is the only state the main thread reads while this
 * worker is still running. The worker does not print and does not sleep. The
 * average is read after {@link #join()}.
 */
public class TemperatureWorker extends Thread {

    /**
     * How many readings this worker has processed. The main thread reads this
     * while the worker is running in order to draw the progress bar.
     */
    public volatile int checked;

    private final String city;
    private final TemperatureSource source;
    private double sum;

    /**
     * Creates a worker for one city.
     *
     * @param city   the city name shown on the progress bar, for example
     *               {@code "Las Palmas"} or {@code "Helsinki"}
     * @param source the readings this worker consumes
     */
    public TemperatureWorker(String city, TemperatureSource source) {
        super(city);
        this.city = city;
        this.source = source;
    }

    /**
     * Returns the city name given to the constructor.
     *
     * @return the city name
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the average of the readings processed so far, in degrees Celsius.
     *
     * <p>
     * Call this after {@link #join()}. The result is {@code 0} when no reading
     * has been processed. The division uses {@link #checked} as the count.
     *
     * @return the arithmetic mean of the processed readings
     */
    public double average() {
        // TODO: return sum / checked, or 0 when checked is 0.
        return 0;
    }

    @Override
    public void run() {
        /*
         * TODO: read source.next() until it returns an empty OptionalDouble.
         * Add each reading to sum, then increment checked. Do not print and do
         * not call Thread.sleep.
         */
    }
}
