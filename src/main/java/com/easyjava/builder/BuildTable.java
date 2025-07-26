package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.JsonUtils;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildTable {
    // 定义日志记录器
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    // 数据库连接对象
    private static Connection conn = null;
    // 查询所有表状态的SQL语句
    private static String SQL_SHOW_TABLE_STATUS = "show table status";

    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";

    private static String SQL_SHOW_TABLE_INDEX = "show index from %s";

    // 静态代码块，类加载时执行一次
    static {
        // 从配置文件获取数据库驱动名
        String driverName = PropertiesUtils.getString("db.driver.name");
        // 从配置文件获取数据库URL
        String url = PropertiesUtils.getString("db.url");
        // 从配置文件获取数据库用户名
        String user = PropertiesUtils.getString("db.username");
        // 从配置文件获取数据库密码
        String password = PropertiesUtils.getString("db.password");
        try {
            // 加载数据库驱动类
            Class.forName(driverName);
            // 获取数据库连接
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            // 连接失败时记录错误日志
            logger.error("数据库连接失败", e);
        }
    }

    // 获取所有表信息的方法
    public static List<TableInfo> getTables() {
        // 声明预编译SQL对象
        PreparedStatement ps = null;
        // 声明结果集对象
        ResultSet tableResult = null;

        // 创建一个用于存放所有表信息的列表
        List<TableInfo> tableInfoList = new ArrayList<>();
        try {
            // 使用数据库连接对象conn，预编译SQL语句"show table status"
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            // 执行SQL查询，返回所有表的状态信息，结果存入tableResult
            tableResult = ps.executeQuery();
            // 遍历结果集，每次循环处理一张表的信息
            while (tableResult.next()) {
                // 获取当前表的名称（name字段）
                String tableName = tableResult.getString("name");
                // 获取当前表的注释（comment字段）
                String comment = tableResult.getString("comment");

                // 初始化beanName为表名
                String beanName = tableName;
                // 如果配置要求忽略表名前缀，则去掉下划线前的部分
                if (Constants.IGNOGE_TABLE_PREFIX) {
                    beanName = tableName.substring(beanName.indexOf("_") + 1);
                }
                // 将beanName转换为驼峰命名，首字母大写
                beanName = processFiled(beanName, true);
                // 创建一个TableInfo对象用于封装表信息
                TableInfo tableInfo = new TableInfo();
                // 设置表名
                tableInfo.setTableName(tableName);
                // 设置JavaBean名称
                tableInfo.setBeanName(beanName);
                // 设置表注释
                tableInfo.setComment(comment);
                // 设置Java参数Bean名称（在beanName后拼接常量后缀）
                tableInfo.setBeanParamName(beanName + Constants.SUFFIX_BEAN_QUERY);

                readFieldInfo(tableInfo);
                getKeyIndexInfo(tableInfo);
                tableInfoList.add(tableInfo);
            }
        } catch (Exception e) {
            // 查询或遍历失败时记录错误日志
            logger.error("读取表失败", e);
        } finally {
            // 关闭结果集，释放资源
            if (tableResult != null) {
                try {
                    tableResult.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            // 关闭预编译SQL对象，释放资源
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            // 关闭数据库连接，释放资源
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return tableInfoList;
    }

    private static void readFieldInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        List<FieldInfo> fieldInfoExtendList = new ArrayList<>();
        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            Boolean haveDateTime = false;
            Boolean haveDate = false;
            Boolean haveBigDecimal = false;

            while (fieldResult.next()) {
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");
                String extra = fieldResult.getString("extra");
                String comment = fieldResult.getString("comment");
                if (type.indexOf("(") > 0) {
                    type = type.substring(0, type.indexOf("("));
                }
                String propertyName = processFiled(field, false);
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfoList.add(fieldInfo);

                fieldInfo.setFieldName(field);
                fieldInfo.setSqlType(type);
                fieldInfo.setComment(comment);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra) ? true : false);
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
                    haveDateTime = true;
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {
                    haveDate = true;
                }
                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
                    haveBigDecimal = true;
                }

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
                    FieldInfo fuzzyFieldInfo = new FieldInfo();
                    fuzzyFieldInfo.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyFieldInfo.setJavaType(fieldInfo.getJavaType());
                    fuzzyFieldInfo.setSqlType(type);
                    fuzzyFieldInfo.setFieldName(fieldInfo.getFieldName());
                    fieldInfoExtendList.add(fuzzyFieldInfo);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {
                    FieldInfo timeStartField = new FieldInfo();
                    timeStartField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_TIME_START);
                    timeStartField.setJavaType("String");
                    timeStartField.setSqlType(type);
                    timeStartField.setFieldName(fieldInfo.getFieldName());
                    fieldInfoExtendList.add(timeStartField);

                    FieldInfo timeEndField = new FieldInfo();
                    timeEndField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_TIME_END);
                    timeEndField.setJavaType("String");
                    timeEndField.setSqlType(type);
                    timeEndField.setFieldName(fieldInfo.getFieldName());
                    fieldInfoExtendList.add(timeEndField);
                }
            }
            tableInfo.setHaveDateTime(haveDateTime);
            tableInfo.setHaveDate(haveDate);
            tableInfo.setHaveBigDecimal(haveBigDecimal);
            tableInfo.setFieldList(fieldInfoList);
            tableInfo.setFieldExtendList(fieldInfoExtendList);
        } catch (Exception e) {
            // 查询或遍历失败时记录错误日志
            logger.error("读取表失败", e);
        } finally {
            // 关闭结果集，释放资源
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            // 关闭预编译SQL对象，释放资源
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static List<FieldInfo> getKeyIndexInfo(TableInfo tableInfo) {
        // 声明预编译SQL对象
        PreparedStatement ps = null;
        // 声明结果集对象
        ResultSet fieldResult = null;
        // 创建一个用于存放索引字段信息的列表（当前方法未实际使用，可用于扩展）
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        try {
            Map<String, FieldInfo> tempMap = new HashMap<>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                tempMap.put(fieldInfo.getFieldName(), fieldInfo);
            }
            // 预编译SQL，查询指定表的所有索引信息
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX, tableInfo.getTableName()));
            // 执行SQL，获取索引信息结果集
            fieldResult = ps.executeQuery();
            // 遍历每一条索引记录
            while (fieldResult.next()) {
                // 获取索引名称
                String keyName = fieldResult.getString("key_name");
                // 获取索引唯一性（1表示非唯一，0表示唯一）
                Integer nonUnique = fieldResult.getInt("non_unique");
                // 获取当前索引对应的字段名
                String columnName = fieldResult.getString("column_name");
                // 如果索引不是唯一索引，则跳过
                if (nonUnique == 1) {
                    continue;
                }
                // 从TableInfo的keyIndexMap中获取当前索引名对应的字段列表
                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);
                // 如果该索引名还没有对应的字段列表，则新建一个并放入map
                if (null == keyFieldList) {
                    keyFieldList = new ArrayList<>();
                    tableInfo.getKeyIndexMap().put(keyName, keyFieldList);
                }
                /*// 遍历表的所有字段，找到与当前索引字段名相同的FieldInfo对象
                for(FieldInfo fieldInfo : tableInfo.getFieldList()) {
                    if (fieldInfo.getFieldName().equals(columnName)) {
                        // 将该字段加入到索引字段列表中
                        keyFieldList.add(fieldInfo);
                    }
                }*/
                keyFieldList.add(tempMap.get(columnName));
            }
        } catch (Exception e) {
            // 查询或遍历失败时记录错误日志
            logger.error("读取索引失败", e);
        } finally {
            // 关闭结果集，释放资源
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            // 关闭预编译SQL对象，释放资源
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 返回索引字段信息列表（当前未实际使用）
        return fieldInfoList;
    }

    // 将下划线分隔的字符串转换为驼峰命名，upperCaseFirstLetter为true时首字母大写，否则小写
    private static String processFiled(String field, Boolean upperCaseFirstLetter) {
        // 创建一个可变字符串对象，用于拼接结果
        StringBuffer sb = new StringBuffer();
        // 按下划线分割原始字符串，得到各个单词
        String[] fields = field.split("_");
        // 处理第一个单词：如果upperCaseFirstLetter为true则首字母大写，否则保持原样
        sb.append(upperCaseFirstLetter ? StringUtils.upperCaseFirstLetter(fields[0]) : fields[0]);
        // 遍历剩余的单词，从第二个开始，每个单词首字母都大写后拼接到结果中
        for (int i = 1, len = fields.length; i < len; i++) {
            sb.append(StringUtils.upperCaseFirstLetter(fields[i]));
        }
        // 返回拼接后的驼峰命名字符串
        return sb.toString();
    }

    private static String processJavaType(String type) {
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
            return "BigDecimal";
        } else {
            throw new RuntimeException("不支持的类型：" + type);
        }
    }
}
