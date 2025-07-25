package com.easyjava.utils; // 声明当前类所在的包路径

public class StringUtils { // 定义一个字符串工具类，提供静态方法处理字符串

    // 将字符串的首字母转换为大写
    public static String upperCaseFirstLetter(String field) {
        // 判断传入的字符串是否为null或空字符串
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)) {
            return field; // 如果为空，直接返回原字符串
        }
        // 截取字符串的第一个字符并转为大写，再拼接剩余部分
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    // 将字符串的首字母转换为小写
    public static String lowerCaseFirstLetter(String field) {
        // 判断传入的字符串是否为null或空字符串
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)) {
            return field; // 如果为空，直接返回原字符串
        }
        // 截取字符串的第一个字符并转为小写，再拼接剩余部分
        return field.substring(0, 1).toLowerCase() + field.substring(1);
    }

    // 主方法，用于测试工具类中的方法
    public static void main(String[] args) {
        // 调用upperCaseFirstLetter方法，将"helloWorld"首字母大写，输出结果
        System.out.println(upperCaseFirstLetter("helloWorld")); // 输出: HelloWorld
        // 调用lowerCaseFirstLetter方法，将"HelloWorld"首字母小写，输出结果
        System.out.println(lowerCaseFirstLetter("HelloWorld")); // 输出: helloWorld
    }
}