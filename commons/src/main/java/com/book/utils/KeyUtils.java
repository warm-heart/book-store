package com.book.utils;

import java.util.Random;

/**
 * @author wangqianlong
 * @create 2019-07-29 16:28
 */

public class KeyUtils {

    /**
     * 生成唯一的主键
     * 格式: 时间+随机数
     * @return
     */
    public static synchronized String genUniqueKey() {
        Random random = new Random();
        Integer number = random.nextInt(900000) + 100000;

        return System.currentTimeMillis() + String.valueOf(number);
    }
}
