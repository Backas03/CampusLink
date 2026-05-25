package moe.vitamin.campuslink.config.impl;

import moe.vitamin.campuslink.util.TimeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeDigitTest {

    @Test
    void test() {
        long[] times = {
                (3 * 60 + 18) * 1000,
                (2 * 60 + 1) * 1000,
                57 * 1000,
                0,
                60 * 1000,
                950,
                1150
        };

        String[] results = {
                "3분 18초",
                "2분 1초",
                "57초",
                "0초",
                "1분",
                "0초",
                "1초"
        };
        for (int i=0; i<times.length; i++) {
            Assertions.assertEquals(results[i],
                    TimeUtil.convertMillisecondToHumanReadable(times[i]));
        }
    }
}
