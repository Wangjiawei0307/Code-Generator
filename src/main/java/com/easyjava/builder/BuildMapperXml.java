package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildMapperXml {
    private static final Logger logger = LoggerFactory.getLogger(BuildMapperXml.class);

    private static final String BASE_COLUMN_LIST = "base_column_list";

    private static final String BASE_QUERY_CONDITION = "base_query_condition";
    private static final String BASE_QUERY_CONDITION_EXTEND = "base_query_condition_extend";

    private static final String QUERY_CONDITION = "query_condition";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPERS_XMLS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS;
        File poFile = new File(folder, className + ".xml");
        OutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            osw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(osw);

            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.newLine();
            bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"");
            bw.newLine();
            bw.write("        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.write("<mapper namespace=\"" + Constants.PACKAGE_MAPPERS + "." + className + "\">");
            bw.newLine();

            bw.write("\t<!--实体映射-->");
            bw.newLine();
            String poClass = Constants.PACKAGE_PO + "." + tableInfo.getTableName();
            bw.write("\t<resultMap id=\"base_result_map\" type=\"" + poClass + "\">");
            bw.newLine();

            // 用于保存主键字段信息，初始为 null
            FieldInfo idField = null;
            // 获取表的所有索引（包括主键和唯一索引等），key 是索引名，value 是字段列表
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            // 遍历所有索引
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                // 如果当前索引是主键
                if ("PRIMARY".equals(entry.getKey())) {
                    // 获取主键对应的字段列表
                    List<FieldInfo> fieldInfoList = entry.getValue();
                    // 如果主键只有一个字段（单主键）
                    if (fieldInfoList.size() == 1) {
                        // 记录主键字段
                        idField = fieldInfoList.get(0);
                        // 找到后直接跳出循环
                        break;
                    }
                }
            }
            // 遍历表的所有字段
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                // 写入字段的注释
                bw.write("\t\t<!-- " + fieldInfo.getComment() + " -->");
                bw.newLine();
                String key = "";
                // 如果当前字段是主键字段，则用 id 标签，否则用 result 标签
                if (idField != null && fieldInfo.getPropertyName().equals(idField.getPropertyName())) {
                    key = "id";
                } else {
                    key = "result";
                }
                // 写入 resultMap 的字段映射
                bw.write("\t\t<" + key + " column=\"" + fieldInfo.getFieldName() + "\" property=\"" + fieldInfo.getPropertyName() + "\"/>");
                bw.newLine();
            }
            bw.write("\t</resultMap>");
            bw.newLine();

            //通用查询列
            bw.newLine();
            bw.write("\t<!--通用查询结果列-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_COLUMN_LIST + "\">");
            bw.newLine();
            StringBuilder columnBuilder = new StringBuilder();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                columnBuilder.append(fieldInfo.getFieldName()).append(",");
            }
            String columnBuilderStr = columnBuilder.substring(0, columnBuilder.lastIndexOf(","));
            bw.write("\t\t" + columnBuilderStr);
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();

            // 换行，便于格式化 XML
            bw.newLine();
            // 写入基础查询条件的注释
            bw.write("\t<!--基础查询条件-->");
            bw.newLine();
            // 写入 <sql> 标签，定义基础查询条件片段，id 为 BASE_QUERY_CONDITION
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION + "\">");
            bw.newLine();
            // 遍历表的所有字段
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                // 初始化字符串类型的额外判断条件
                String stringQuery = "";
                // 如果字段是字符串类型，则添加非空字符串的判断
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    stringQuery = " and query." + fieldInfo.getPropertyName() + "!=''";
                }
                // 写入 <if> 标签，判断查询对象的属性不为 null（且字符串类型还要不为空串）
                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null" + stringQuery + "\">");
                bw.newLine();
                // 写入 SQL 查询条件，and id = #{query.属性名}
                bw.write("\t\t\t and id = #{query." + fieldInfo.getPropertyName() + "}");
                bw.newLine();
                // 关闭 <if> 标签
                bw.write("\t\t</if>");
                bw.newLine();
            }
            // 关闭 <sql> 标签
            bw.write("\t</sql>");
            bw.newLine();

            bw.newLine();
            bw.write("\t<!--扩展的查询条件-->");
            bw.newLine();
            // 写入 <sql> 标签，定义扩展查询条件片段，id 为 BASE_QUERY_CONDITION_EXTEND
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION_EXTEND + "\">");
            bw.newLine();
            // 遍历所有扩展查询字段
            for (FieldInfo fieldInfo : tableInfo.getFieldExtendList()) {
                // 初始化 where 条件字符串
                String andWhere = "";
                // 如果是字符串类型，使用 like 模糊查询
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    andWhere = "and " + fieldInfo.getPropertyName() + " like concat('%', #{query." + fieldInfo.getPropertyName() + "}, '%')";
                    // 如果是日期或时间类型，判断是起始还是结束时间
                } else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType()) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType())) {
                    // 起始时间，使用 >=
                    if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_START)) {
                        andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " >= str_to_date(#{" + fieldInfo.getPropertyName() + "}, '%Y-%m-%d') ]]>";
                        // 结束时间，使用 < 并加一天
                    } else if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_END)) {
                        andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " < date_sub(str_to_date(#{query." + fieldInfo.getPropertyName() + "},'%Y-%m-%d'), interval -1 day) ]]>";
                    }
                }
                // 写入 <if> 标签，判断查询对象的属性不为 null 且不为空串
                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null and query." + fieldInfo.getPropertyName() + " !=''\">");
                bw.newLine();
                // 写入 andWhere 条件
                bw.write("\t\t\t" + andWhere);
                bw.newLine();
                // 关闭 <if> 标签
                bw.write("\t\t</if>");
                bw.newLine();
            }
            // 关闭 <sql> 标签
            bw.write("\t</sql>");
            bw.newLine();

            //通用查询条件
            bw.newLine();
            bw.write("\t<!--通用查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + QUERY_CONDITION + "\">");
            bw.newLine();
            bw.write("\t\t<where>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION + "\"/>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION_EXTEND + "\"/>");
            bw.newLine();
            bw.write("\t\t</where>");
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();

            //查询列表
            bw.newLine();
            bw.write("\t<!--查询列表-->");
            bw.newLine();
            bw.write("\t<select id=\"selectList\" resultMap=\"base_result_map\">");
            bw.newLine();
            bw.write("\t\tSELECT <include refid=\"" + BASE_COLUMN_LIST + "\"/> FROM " + tableInfo.getTableName() + " <include refid=\"" + QUERY_CONDITION + "\"/>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();

            bw.write("</mapper>");
            bw.flush();
        } catch (Exception e) {
            logger.error("创建mappers XML失败", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
