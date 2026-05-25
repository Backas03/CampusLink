package moe.vitamin.campuslink.util;

public class TimeUtil {

    public static String convertMillisecondToHumanReadable(long millis) {
        int totalSeconds = (int) millis / 1000;

        String[] digits = {"시간", "분", "초"};
        StringBuilder builder = new StringBuilder();

        int remaining = totalSeconds;
        for (int i = 0; i < digits.length; i++) {
            int divisor = (int) Math.pow(60, digits.length - i - 1);
            int value = remaining / divisor;
            if (value != 0) {
                if (!builder.isEmpty()) {
                    builder.append(" ");
                }
                builder.append(value).append(digits[i]);
            }
            remaining %= divisor;
        }

        return !builder.isEmpty() ? builder.toString() : "0초";
    }

}
