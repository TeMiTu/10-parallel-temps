package temps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(15)
public class ParallelTempsTest {

    @Test
    void finalFrameHasBothBarsFullAndAveragesInsideCityRanges() throws Exception {
        String frame = Main.runChart(0, 0);
        String[] lines = frame.split("\n");

        assertEquals(2, lines.length, frame);
        assertTrue(lines[0].startsWith("Las Palmas | #################### 100 %"), lines[0]);
        assertTrue(lines[1].startsWith("Helsinki   | #################### 100 %"), lines[1]);
        assertFalse(lines[0].contains("-"), lines[0]);
        assertFalse(lines[1].contains("-"), lines[1]);
        assertEquals(Main.BAR_WIDTH, count(lines[0], '#'));
        assertEquals(Main.BAR_WIDTH, count(lines[1], '#'));

        double lasPalmas = temperature(lines[0]);
        double helsinki = temperature(lines[1]);
        double lasPalmasMean = mean(TemperatureData.readValues(TemperatureData.LAS_PALMAS));
        double helsinkiMean = mean(TemperatureData.readValues(TemperatureData.HELSINKI));

        assertEquals(lasPalmasMean, lasPalmas, 0.05, "Las Palmas tolerance is tight");
        assertEquals(helsinkiMean, helsinki, 0.5, "Helsinki tolerance is wider");
        assertTrue(lasPalmas >= 19.0 && lasPalmas <= 24.0, "Las Palmas " + lasPalmas);
        assertTrue(helsinki >= 3.0 && helsinki <= 9.0, "Helsinki " + helsinki);
        assertTrue(frame.contains("°C"));
    }

    private static double temperature(String line) {
        String marker = "°C";
        int end = line.lastIndexOf(marker);
        assertTrue(end > 0, line);
        String head = line.substring(0, end).trim();
        String number = head.substring(head.lastIndexOf(' ') + 1);
        return Double.parseDouble(number);
    }

    private static double mean(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static int count(String text, char character) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }
}
