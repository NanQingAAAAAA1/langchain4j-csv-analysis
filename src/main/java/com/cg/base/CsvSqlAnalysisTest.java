package com.cg.base;

/**
 * CSV SQL分析服务测试类
 */
public class CsvSqlAnalysisTest {

    public static void main(String[] args) {
        System.out.println("开始初始化CSV SQL分析服务...");
        
        CsvSqlAnalysisService service = new CsvSqlAnalysisService();
        
        System.out.println("服务初始化完成！");
        System.out.println(service.getDatabaseStats());
        
        // 验证数据库数据
        System.out.println("\n验证数据库数据...");
        service.validateDatabaseData();
        
        // 生成结构化分析结果
        System.out.println("\n开始生成结构化分析结果...");
        service.generateStructuredAnalysis();
        
        System.out.println("分析完成！请查看 src/main/resources/json/sql_analysis_result.json 文件");
    }
} 