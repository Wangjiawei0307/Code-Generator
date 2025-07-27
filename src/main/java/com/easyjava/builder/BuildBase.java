package com.easyjava.builder;

import com.easyjava.bean.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BuildBase {

    private static Logger logger = LoggerFactory.getLogger(BuildBase.class);

    public static void execute() {
        List<String> headerInfoList = new ArrayList<>();

        //生成date枚举
        headerInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headerInfoList, "DateTimePatternEnum", Constants.PATH_ENUMS);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_UTILS);
        build(headerInfoList, "DateUtils", Constants.PATH_UTILS);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_MAPPERS);
        build(headerInfoList, "BaseMapper", Constants.PATH_MAPPERS);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headerInfoList, "PageSize", Constants.PATH_ENUMS);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_QUERY);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".PageSize");
        build(headerInfoList, "SimplePage", Constants.PATH_QUERY);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_QUERY);
        build(headerInfoList, "BaseQuery", Constants.PATH_QUERY);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_VO);
        build(headerInfoList, "PaginationResultVO", Constants.PATH_VO);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headerInfoList, "ResponseCodeEnum", Constants.PATH_ENUMS);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_EXCEPTION);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        build(headerInfoList, "BusinessException", Constants.PATH_EXCEPTION);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_VO);
        build(headerInfoList, "ResponseVO", Constants.PATH_VO);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headerInfoList.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        build(headerInfoList, "ABaseController", Constants.PATH_CONTROLLER);
        headerInfoList.clear();

        headerInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headerInfoList.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        headerInfoList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        build(headerInfoList, "AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER);
        headerInfoList.clear();
    }

    private static void build(List<String> headerInfoList, String fileName, String outputPath) {
        // 创建输出目录的 File 对象
        File folder = new File(outputPath);
        // 如果目录不存在，则创建目录
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // 创建目标 Java 文件对象
        File javaFile = new File(outputPath, fileName + ".java");

        // 声明输出流相关变量
        OutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        // 声明输入流相关变量
        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            // 创建文件输出流，写入目标 Java 文件
            out = new FileOutputStream(javaFile);
            // 使用 UTF-8 编码包装输出流
            osw = new OutputStreamWriter(out, "UTF-8");
            // 使用缓冲流包装输出流，提高写入效率
            bw = new BufferedWriter(osw);

            // 获取模板文件路径
            String templatePath = BuildBase.class.getClassLoader().getResource("template/" + fileName + ".txt").getPath();
            // 创建文件输入流，读取模板文件
            in = new FileInputStream(templatePath);
            // 使用 UTF-8 编码包装输入流
            isr = new InputStreamReader(in, "UTF-8");
            // 使用缓冲流包装输入流，提高读取效率
            br = new BufferedReader(isr);

            for (String headerInfo : headerInfoList) {
                bw.write(headerInfo + ";");
                bw.newLine();
                bw.newLine();
            }
            // 逐行读取模板文件内容
            String lineInfo = null;
            while ((lineInfo = br.readLine()) != null) {
                // 写入每一行到目标 Java 文件
                bw.write(lineInfo);
                // 换行
                bw.newLine();
            }
            // 刷新缓冲区，确保内容写入文件
            bw.flush();
        } catch (Exception e) {
            logger.error("生成基础类：{},失败:", fileName, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
