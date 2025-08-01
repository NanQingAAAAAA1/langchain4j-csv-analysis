package com.cg.controller;

import com.cg.base.CsvAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CSV分析控制器
 * 提供API接口来调用CSV分析服务
 */
@RestController
@RequestMapping("/api/csv")
public class CsvAnalysisController {

    @Autowired
    private CsvAnalysisService csvAnalysisService;

    /**
     * 处理CSV数据并生成分析结果
     */
    @GetMapping("/analyze")
    public String analyzeCsvData() {
        try {
            csvAnalysisService.processCsvData();
            return "CSV数据分析完成，结果已保存到 resources/analysis_result.json";
        } catch (Exception e) {
            return "分析失败: " + e.getMessage();
        }
    }
} 