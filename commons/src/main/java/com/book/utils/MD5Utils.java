package com.book.utils;

import org.springframework.util.DigestUtils;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:35
 */

public class MD5Utils {

    private static final String SALT = "jdfhcd";

    /**
     * @param password 需要加密的密码
     * @return 加密后字符串
     */
    public static String encode(String password) {
        String rePassword = password + "/" + SALT;
        return DigestUtils.md5DigestAsHex(rePassword.getBytes());
    }


    /**
     * @param password  用户输入密码
     * @param rePassword 数据库密码
     * @return
     */
    public static boolean matches(String password, String rePassword) {
        String rawPassword = password + "/" + SALT;
        if (DigestUtils.md5DigestAsHex(rawPassword.getBytes()).equals(rePassword))
            return true;
        return false;

    }
}
