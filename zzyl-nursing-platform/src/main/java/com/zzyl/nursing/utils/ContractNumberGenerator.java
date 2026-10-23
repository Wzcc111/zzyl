package com.zzyl.nursing.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 生成形如 HT202408261544080001 的合同编号。 */
public final class ContractNumberGenerator {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static String lastSecond = "";
    private static int sequence = 0;

    private ContractNumberGenerator() {
    }

    public static synchronized String next() {
        String currentSecond = LocalDateTime.now().format(TIME_FORMATTER);
        if (!currentSecond.equals(lastSecond)) {
            lastSecond = currentSecond;
            sequence = 0;
        }
        sequence++;
        if (sequence > 9999) {
            throw new IllegalStateException("同一秒生成的合同编号已超过9999个");
        }
        return "HT" + currentSecond + String.format("%04d", sequence);
    }
}
