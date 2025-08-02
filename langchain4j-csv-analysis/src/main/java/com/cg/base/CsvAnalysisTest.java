package com.cg.base;

public class CsvAnalysisTest {
    public static void main(String[] args) {
        CsvAnalysisService service = new CsvAnalysisService();
        try {
            System.out.println("开始分析CSV数据...");
            service.processCsvData();
            System.out.println("分析完成！");
        } catch (Exception e) {
            System.err.println("分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 