package com.cg.springboot;

import com.cg.base.CsvAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csv")
public class CsvAnalysisController {

    @Autowired
    private CsvAnalysisService csvAnalysisService;

    @GetMapping("/analyze")
    public String analyzeCsvData() {
        try {
            csvAnalysisService.processCsvData();
            return "CSV数据分析完成，结果已保存到 resources/json/analysis_result.json";
        } catch (Exception e) {
            return "分析失败: " + e.getMessage();
        }
    }
} 