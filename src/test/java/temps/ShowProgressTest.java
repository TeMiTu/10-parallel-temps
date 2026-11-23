package temps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
public class ShowProgressTest {

    @Test
    void barWidthIsTwentyCharacters() {
        assertEquals(20, Main.BAR_WIDTH);
    }

    @Test
    void zeroProgressDrawsEmptyBars() {
        String frame = Main.showProgress("Las Palmas", 0, 366, 0, "Helsinki", 0, 366, 0);

        assertEquals("""
                Las Palmas | --------------------   0 %   0.0 °C
                Helsinki   | --------------------   0 %   0.0 °C""", frame);
        assertFalse(frame.contains("#"));
    }

    @Test
    void partialProgressDrawsBarsThatMatchTheShare() {
        String frame = Main.showProgress("Las Palmas", 147, 366, 19.5, "Helsinki", 55, 366, -5.9);

        assertEquals("""
                Las Palmas | ########------------  40 %  19.5 °C
                Helsinki   | ###-----------------  15 %  -5.9 °C""", frame);
        assertEquals(8, count(frame.split("\n")[0], '#'));
        assertEquals(3, count(frame.split("\n")[1], '#'));
    }

    @Test
    void fullProgressDrawsBothBarsAtOneHundredPercent() {
        String frame = Main.showProgress("Las Palmas", 366, 366, 20.7, "Helsinki", 366, 366, 7.1);

        assertEquals("""
                Las Palmas | #################### 100 %  20.7 °C
                Helsinki   | #################### 100 %   7.1 °C""", frame);
        assertTrue(frame.contains("Las Palmas"));
        assertTrue(frame.contains("Helsinki"));
        assertTrue(frame.contains("100 %"));
        assertTrue(frame.contains("°C"));
        assertFalse(frame.contains("-"));
    }

    @Test
    void barLengthFollowsCheckedDividedByTotal() {
        int total = 366;
        int[] samples = { 0, 1, 10, 55, 147, 183, 200, 365, 366 };
        for (int checked : samples) {
            String frame = Main.showProgress("Las Palmas", checked, total, 1.2, "Helsinki", total - checked, total, 4);
            String[] lines = frame.split("\n");
            assertEquals(2, lines.length, frame);
            assertEquals(expectedFilled(checked, total), count(lines[0], '#'), "Las Palmas checked=" + checked);
            assertEquals(expectedFilled(total - checked, total), count(lines[1], '#'), "Helsinki checked=" + (total - checked));
            assertEquals(Main.BAR_WIDTH, count(lines[0], '#') + count(lines[0], '-'));
            assertEquals(Main.BAR_WIDTH, count(lines[1], '#') + count(lines[1], '-'));
        }
    }

    @Test
    void labelsComeFromTheArguments() {
        String frame = Main.showProgress("Alpha", 1, 2, 3.25, "Beta", 2, 2, 4);

        assertTrue(frame.startsWith("Alpha"), frame);
        assertTrue(frame.contains("\nBeta"), frame);
        assertTrue(frame.contains("  3.3 °C") || frame.contains(" 3.3 °C") || frame.contains("3.3 °C"), frame);
        assertTrue(frame.contains("50 %"), frame);
        assertTrue(frame.contains("100 %"), frame);
    }

    @Test
    void zeroTotalDoesNotDivideByZero() {
        String frame = Main.showProgress("Las Palmas", 0, 0, 0, "Helsinki", 0, 0, 0);

        assertTrue(frame.contains("0 %"), frame);
        assertFalse(frame.contains("#"), frame);
    }

    private static int expectedFilled(int checked, int total) {
        if (total <= 0 || checked <= 0) {
            return 0;
        }
        if (checked >= total) {
            return Main.BAR_WIDTH;
        }
        return (int) ((long) checked * Main.BAR_WIDTH / total);
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
