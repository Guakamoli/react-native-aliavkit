package com.manwei.libs.utils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author : wuyq
 * Time : 2020/10/31 13:06
 * Description :
 */
public final class StringUtils {

    private static final SecureRandom M_RANDOM = new SecureRandom();

    /**
     * 生成日期+时间字符串，去除符号
     */
    public static String getStrDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String ret = sdf.format(new Date()).replace("-", "").replace(" ", "").replace(":", "").trim();
        return ret.trim();
    }


    /**
     * 生成随机数
     *
     * @param length 随机数长度
     * @return 随机数
     */
    public static String getStringRandom(int length) {
        StringBuilder val = new StringBuilder();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            String charOrNum = M_RANDOM.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = M_RANDOM.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (M_RANDOM.nextInt(26) + temp));
            } else {
                val.append(M_RANDOM.nextInt(10));
            }
        }
        return val.toString();
    }
}
