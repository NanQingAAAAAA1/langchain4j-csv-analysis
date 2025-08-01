package com.cg.base;

import com.cg.entity.JobData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CSV分析服务类
 * 实现CSV数据读取、AI分析、JSON格式化输出
 */
public class CsvAnalysisService {

    /**
     * 将列表分页
     */
    private List<List<JobData>> paginate(List<JobData> list, int pageSize) {
        List<List<JobData>> pages = new java.util.ArrayList<>();
        int total = list.size();
        for (int i = 0; i < total; i += pageSize) {
            pages.add(list.subList(i, Math.min(total, i + pageSize)));
        }
        return pages;
    }

    /**
     * 合并多个AI返回的JSON片段为一个总JSON
     * 统计类数据需要重新计算，列表类数据可以合并
     */
    private String mergeJsonResults(List<String> jsonList, List<JobData> allJobData) {
        ObjectMapper mapper = new ObjectMapper();
        
        // 收集所有公司信息（可以合并）
        List<Object> allCompanyInfo = new ArrayList<>();
        
        // 解析每个json片段，只收集公司信息
        for (String json : jsonList) {
            try {
                java.util.Map<String, Object> map = mapper.readValue(json, java.util.Map.class);
                Object companyInfo = map.get("公司信息");
                if (companyInfo instanceof List) {
                    allCompanyInfo.addAll((List<?>) companyInfo);
                }
            } catch (Exception e) {
                System.err.println("合并JSON时解析失败: " + e.getMessage());
            }
        }
        
        // 基于全部数据重新计算统计类数据
        String finalJson = calculateStatisticsFromAllData(allJobData, allCompanyInfo);
        
        return finalJson;
    }
    
    /**
     * 基于全部数据计算统计结果
     */
    private String calculateStatisticsFromAllData(List<JobData> allJobData, List<Object> allCompanyInfo) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 1. 计算城市薪资TOP10
            Map<String, List<Integer>> citySalaries = new HashMap<>();
            for (JobData job : allJobData) {
                String city = job.getCity();
                if (city != null && !city.isEmpty()) {
                    String salaryStr = job.getSalary();
                    if (salaryStr != null && !salaryStr.isEmpty()) {
                        // 提取薪资数字（假设格式如"15k-25k"或"15000-25000"）
                        Integer salary = extractSalaryNumber(salaryStr);
                        if (salary != null) {
                            citySalaries.computeIfAbsent(city, k -> new ArrayList<>()).add(salary);
                        }
                    }
                }
            }
            
            List<Map<String, Object>> citySalaryTop10 = new ArrayList<>();
            citySalaries.entrySet().stream()
                .map(entry -> {
                    double avgSalary = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
                    Map<String, Object> cityData = new HashMap<>();
                    cityData.put("cityName", entry.getKey());
                    cityData.put("avgSalary", (int) avgSalary);
                    return cityData;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("avgSalary"), (Integer) a.get("avgSalary")))
                .limit(10)
                .forEach(citySalaryTop10::add);
            
            // 2. 计算学历薪资关系（与SQL逻辑一致）
            Map<String, List<Integer>> educationSalaries = new HashMap<>();
            for (JobData job : allJobData) {
                String education = normalizeEducation(job.getEducation());
                if (education != null) {
                    String salaryStr = job.getSalary();
                    if (salaryStr != null && !salaryStr.isEmpty()) {
                        Integer salary = extractSalaryNumber(salaryStr);
                        if (salary != null) {
                            educationSalaries.computeIfAbsent(education, k -> new ArrayList<>()).add(salary);
                        }
                    }
                }
            }
            
            List<Map<String, Object>> educationSalaryRelation = new ArrayList<>();
            educationSalaries.forEach((education, salaries) -> {
                double avgSalary = salaries.stream().mapToInt(Integer::intValue).average().orElse(0);
                Map<String, Object> eduData = new HashMap<>();
                eduData.put("educationLevel", education);
                eduData.put("avgSalary", (int) avgSalary);
                educationSalaryRelation.add(eduData);
            });
            
            // 3. 计算学历经验关系（与SQL逻辑一致）
            Map<String, Integer> educationExperienceCount = new HashMap<>();
            for (JobData job : allJobData) {
                String education = job.getEducation();
                String experience = job.getExperience();
                if (education != null && !education.isEmpty() && experience != null && !experience.isEmpty()) {
                    String key = education + "|" + experience;
                    educationExperienceCount.put(key, educationExperienceCount.getOrDefault(key, 0) + 1);
                }
            }
            
            List<Map<String, Object>> educationExperienceRelation = new ArrayList<>();
            educationExperienceCount.forEach((key, count) -> {
                String[] parts = key.split("\\|");
                Map<String, Object> relationData = new HashMap<>();
                relationData.put("educationLevel", parts[0]);
                relationData.put("experienceLevel", parts[1]);
                relationData.put("jobCount", count);
                educationExperienceRelation.add(relationData);
            });
            
            // 4. 计算城市职位数量
            Map<String, Integer> cityJobCount = new HashMap<>();
            for (JobData job : allJobData) {
                String city = job.getCity();
                if (city != null && !city.isEmpty()) {
                    cityJobCount.put(city, cityJobCount.getOrDefault(city, 0) + 1);
                }
            }
            
            List<Map<String, Object>> cityJobCountList = new ArrayList<>();
            cityJobCount.forEach((city, count) -> {
                Map<String, Object> cityData = new HashMap<>();
                cityData.put("cityName", city);
                cityData.put("jobCount", count);
                cityJobCountList.add(cityData);
            });
            
            // 5. 计算城市职位数量TOP10
            List<Map<String, Object>> cityJobCountTop10 = cityJobCountList.stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("jobCount"), (Integer) a.get("jobCount")))
                .limit(10)
                .collect(Collectors.toList());
            
            // 6. 计算学历要求分布（与SQL逻辑一致）
            Map<String, Integer> educationDistribution = new HashMap<>();
            for (JobData job : allJobData) {
                String education = normalizeEducation(job.getEducation());
                if (education != null) {
                    educationDistribution.put(education, educationDistribution.getOrDefault(education, 0) + 1);
                }
            }
            
            List<Map<String, Object>> educationDistributionList = new ArrayList<>();
            educationDistribution.forEach((education, count) -> {
                Map<String, Object> eduData = new HashMap<>();
                eduData.put("educationLevel", education);
                eduData.put("jobCount", count);
                educationDistributionList.add(eduData);
            });
            
            // 7. 构建最终JSON
            Map<String, Object> finalResult = new LinkedHashMap<>();
            finalResult.put("城市薪资TOP10", citySalaryTop10);
            finalResult.put("学历薪资关系", educationSalaryRelation);
            finalResult.put("公司信息", allCompanyInfo);
            finalResult.put("学历经验关系", educationExperienceRelation);
            finalResult.put("城市职位数量", cityJobCountList);
            finalResult.put("城市职位数量TOP10", cityJobCountTop10);
            finalResult.put("学历要求分布", educationDistributionList);
            
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalResult);
            
        } catch (Exception e) {
            System.err.println("计算统计结果时发生错误: " + e.getMessage());
            return "{}";
        }
    }
    
    /**
     * 从薪资字符串中提取数字（与SQL逻辑一致）
     */
    private Integer extractSalaryNumber(String salaryStr) {
        try {
            // 检查是否包含范围分隔符"-"
            if (salaryStr.contains("-")) {
                String[] parts = salaryStr.split("-");
                if (parts.length == 2) {
                    String minSalary = parts[0].trim();
                    String maxSalary = parts[1].trim();
                    
                    // 计算最低薪资
                    Integer minValue = parseSalaryValue(minSalary);
                    // 计算最高薪资
                    Integer maxValue = parseSalaryValue(maxSalary);
                    
                    if (minValue != null && maxValue != null) {
                        // 返回平均值
                        return (minValue + maxValue) / 2;
                    }
                }
            } else {
                // 单个薪资值
                return parseSalaryValue(salaryStr);
            }
        } catch (Exception e) {
            // 解析失败，返回null
        }
        return null;
    }
    
    /**
     * 解析单个薪资值
     */
    private Integer parseSalaryValue(String salaryValue) {
        try {
            // 移除空格
            String cleaned = salaryValue.replaceAll("\\s", "");
            
            if (cleaned.contains("万")) {
                // 万转换为元
                String numberStr = cleaned.replace("万", "");
                double number = Double.parseDouble(numberStr);
                return (int) (number * 10000);
            } else if (cleaned.contains("千")) {
                // 千转换为元
                String numberStr = cleaned.replace("千", "");
                double number = Double.parseDouble(numberStr);
                return (int) (number * 1000);
            } else if (cleaned.matches(".*\\d+.*")) {
                // 提取数字
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(cleaned);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
        } catch (Exception e) {
            // 解析失败，返回null
        }
        return null;
    }
    
    /**
     * 标准化学历字段（与SQL逻辑一致）
     */
    private String normalizeEducation(String education) {
        if (education == null || education.isEmpty()) {
            return "不限";
        }
        switch (education.trim()) {
            case "本科":
            case "硕士":
            case "大专":
                return education.trim();
            case "不限":
                return "不限";
            default:
                return "不限";
        }
    }

    /**
     * 1. 使用opencsv读取resources文件夹下的qcwy_all.csv文件
     */
    private List<JobData> readCsvFile() throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(getClass().getResourceAsStream("/csv/qcwy_all.csv"), StandardCharsets.UTF_8))) {
            
            List<String[]> rows = reader.readAll();
            
            // 跳过标题行，从第二行开始处理
            return rows.stream()
                    .skip(1) // 跳过标题行
                    .filter(row -> row.length >= 12) // 确保有足够的列
                    .map(row -> new JobData(
                            row[0],  // 职位
                            row[1],  // 薪资
                            row[2],  // 城市
                            row[3],  // 区域
                            row[4],  // 经验
                            row[5],  // 学历
                            row[6],  // 公司
                            row[7],  // 公司领域
                            row[8],  // 公司性质
                            row[9],  // 公司规模
                            row[10], // 公司详情页
                            row[11]  // 职位详情页
                    ))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 将JobData列表转换为CSV格式的字符串
     */
    private String convertToCsvSample(List<JobData> jobDataList) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("职位,薪资,城市,区域,经验,学历,公司,公司领域,公司性质,公司规模\n");
        
        for (JobData job : jobDataList) {
            csvBuilder.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    job.getPosition(),
                    job.getSalary(),
                    job.getCity(),
                    job.getArea(),
                    job.getExperience(),
                    job.getEducation(),
                    job.getCompany(),
                    job.getCompanyField(),
                    job.getCompanyType(),
                    job.getCompanySize()
            ));
        }
        
        return csvBuilder.toString();
    }

    /**
     * 3. 将解析后的内容，填入PromptTemplate中
     */
    private String analyzeWithPromptTemplate(String csvSample) {
        // 使用用户提供的prompt模板
        String prompt = String.format("""
            以下是一个CSV表格的实际数据（共%d行）：
            %s
            
            请严格按照以下要求分析该表格数据，并将结果以 JSON 格式输出：
            
            **重要要求：**
            1. 必须基于提供的实际CSV数据进行计算，不要使用示例数据
            2. 对于薪资数据，请从CSV中提取实际的薪资数值进行计算
            3. 对于职位数量，请统计CSV中实际出现的城市、学历、经验等字段
            4. 如果某个字段在数据中不存在，请返回空数组或合理的默认值
            5. 不要添加任何说明文字，只返回JSON格式数据
            
            **薪资计算规则（必须严格按照此规则）：**
            - 对于"15k-25k"格式：提取最低值15k=15000元，最高值25k=25000元，计算平均值(15000+25000)/2=20000元
            - 对于"1.5万-3万"格式：提取最低值1.5万=15000元，最高值3万=30000元，计算平均值(15000+30000)/2=22500元
            - 对于"8千"格式：直接计算8000元
            - 万转换为元：乘以10000；千转换为元：乘以1000
            
            **学历标准化规则：**
            - 只保留：本科、硕士、大专、不限
            - 其他学历类型统一归为"不限"
            - 空值或null归为"不限"
            
            **分析要求：**
            1. 城市薪资TOP10：计算每个城市的平均薪资，取前10名（柱状图）
            2. 学历薪资关系：统计每个学历级别的平均薪资（玫瑰图）
            3. 公司信息：提取公司名称、经验要求、学历要求与薪资信息（滚动列表）
            4. 学历经验关系：统计每个学历和经验组合的职位数量（热力图）
            5. 城市职位数量：统计每个城市的职位数量（全国地图）
            6. 城市职位数量TOP10：统计职位数量前10的城市（柱状图）
            7. 学历要求分布：统计每个学历要求的职位数量（漏斗图）
            
            **数据格式要求：**
            - avgSalary字段必须为整数（从CSV薪资字段计算得出）
            - jobCount字段必须为整数（从CSV实际统计得出）
            - 如果数据不足，请返回实际统计结果，不要填充示例数据
            
            请严格按照以下格式输出（只输出JSON，不要其他文字）：
            
            {
                "城市薪资TOP10": [
                    {"cityName": "实际城市名", "avgSalary": 实际计算的平均薪资}
                ],
                "学历薪资关系": [
                    {"educationLevel": "本科/硕士/大专/不限", "avgSalary": 实际计算的平均薪资}
                ],
                "公司信息": [
                    {"companyName": "实际公司名", "experienceLevel": "实际经验要求", "educationLevel": "实际学历要求", "salary": "实际薪资"}
                ],
                "学历经验关系": [
                    {"educationLevel": "实际学历", "experienceLevel": "实际经验", "jobCount": 实际统计数量}
                ],
                "城市职位数量": [
                    {"cityName": "实际城市名", "jobCount": 实际统计数量}
                ],
                "城市职位数量TOP10": [
                    {"cityName": "实际城市名", "jobCount": 实际统计数量}
                ],
                "学历要求分布": [
                    {"educationLevel": "本科/硕士/大专/不限", "jobCount": 实际统计数量} 
                ]
            }
            """, csvSample.split("\n").length - 1, csvSample);

        // 创建OpenAI模型（使用application.properties中的配置）
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey("demo")
                    .modelName("gpt-4o-mini")
                    .build();

            // 执行分析
            String response = model.generate(prompt);
            return response;
        } catch (Exception e) {
            // API调用失败
            return "API调用失败";
        }
    }
    /**
     * 4. 将AI的回答格式化为json文件，并用LangChain4j的Document接口，封装为Document，保存在resources文件夹下
     */
    private void saveAnalysisResult(String aiResponse) throws IOException {
        // 去掉markdown格式字符
        String cleanedResponse = aiResponse;
        if (aiResponse.startsWith("```json")) {
            cleanedResponse = aiResponse.substring(7);
        }
        if (cleanedResponse.endsWith("```")) {
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
        }
        // 去掉开头的```json和结尾的```
        cleanedResponse = cleanedResponse.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "");
        
        // 创建Document对象
        Document document = Document.from(cleanedResponse);
        
        // 保存到resources/json文件夹下
        String outputPath = "src/main/resources/json/analysis_result.json";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(document.text());
        }
        
        System.out.println("分析结果已保存到: " + outputPath);
        System.out.println("Document内容长度: " + document.text().length() + " 字符");
    }

    /**
     * 主处理方法
     */
    public void processCsvData() {
        try {
            // 1. 使用opencsv读取resources文件夹下的qcwy_all.csv文件
            List<JobData> jobDataList = readCsvFile();
            System.out.println("成功读取 " + jobDataList.size() + " 条职位数据");

            // 2. 分页处理
            int pageSize = 5; // 每页5行
            List<List<JobData>> pages = paginate(jobDataList, pageSize);
            System.out.println("共分为 " + pages.size() + " 页");

            List<String> jsonResults = new ArrayList<>();
            int pageNum = 1;
            for (List<JobData> page : pages) {
                System.out.println("处理第 " + pageNum + " 页, 数据量: " + page.size());
                String csvSample = convertToCsvSample(page);
                String aiResponse = analyzeWithPromptTemplate(csvSample);
                System.out.println("第 " + pageNum + " 页AI分析结果:\n" + aiResponse);
                // 清理markdown格式
                String cleaned = aiResponse.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "");
                jsonResults.add(cleaned);
                pageNum++;
            }

            // 3. 合并所有JSON片段
            String mergedJson = mergeJsonResults(jsonResults, jobDataList);
            System.out.println("合并后JSON:\n" + mergedJson);

            // 4. 保存
            saveAnalysisResult(mergedJson);

        } catch (Exception e) {
            System.err.println("处理CSV数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 