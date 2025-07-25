package com.easyjava.utils; // 声明包名

import java.text.ParseException; // 导入解析异常类
import java.text.SimpleDateFormat; // 导入日期格式化类
import java.util.Date; // 导入日期类

// 日期工具类，提供日期格式化和解析方法
public class DateUtils {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss"; // 定义日期时间格式：年-月-日 时:分:秒
    // 定义常用日期格式：年-月-日
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    // 定义常用日期格式：年/月/日
    public static final String _YYYYMMDD = "yyyy/MM/dd";
    // 定义常用日期格式：年月日（无分隔符）
    public static final String YYYYMMDD = "yyyyMMdd";

    // 将Date对象按指定格式转为字符串
    public static String format(Date date, String pattern) {
        // 创建日期格式化对象，并格式化日期
        return new SimpleDateFormat(pattern).format(date);
    }

    // 将字符串按指定格式解析为Date对象
    public static Date parse(String date, String pattern) {
        try {
            // 创建日期格式化对象，解析字符串为Date
            new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            // 解析失败时打印异常信息
            e.printStackTrace();
        }
        // 解析失败返回null
        return null;
    }
}