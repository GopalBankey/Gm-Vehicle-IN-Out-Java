package com.gmvehicleinout.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtil {

    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    public static LocalDateTime nowIST() {
        return LocalDateTime.now(IST_ZONE);
    }
}
