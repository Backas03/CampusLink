package moe.vitamin.campuslink.util;

import java.security.SecureRandom;

public class RandomCodeGenerator {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        return generate(DEFAULT_CODE_LENGTH);
    }

    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Code length must be greater than 0");
        }

        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return code.toString();
    }

}
