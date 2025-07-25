package com.easyjava.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PropertiesUtils {
    // Properties对象用于加载配置文件
    private static Properties props = new Properties();
    // 用于存储配置项的键值对，线程安全
    private static Map<String, String> PROPER_MAP = new ConcurrentHashMap<>();

    // 静态代码块，类加载时执行一次
    static {
        InputStream is = null;
        try {
            // 通过类加载器获取配置文件的输入流
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties");
            // 加载配置文件内容到props对象
            props.load(new InputStreamReader(is, "gbk"));

            // 遍历所有配置项，将其存入PROPER_MAP
            Iterator<Object> iterator = props.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                PROPER_MAP.put(key, props.getProperty(key));
            }
        } catch (Exception e) {
            // 异常处理，实际未做任何处理
        } finally {
            // 关闭输入流，释放资源
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // 根据key获取配置项的值
    public static String getString(String key) {
        return PROPER_MAP.get(key);
    }
    // 测试方法，输出指定key的配置项
    public static void main(String[] args) {
        System.out.println(getString("db.driver.name"));
    }
}
