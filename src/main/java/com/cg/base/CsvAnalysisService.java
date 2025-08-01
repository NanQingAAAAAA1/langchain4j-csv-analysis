package com.cg.base;

import com.cg.entity.JobData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        java.util.List<Object> allCompanyInfo = new java.util.ArrayList<>();
        
        // 解析每个json片段，只收集公司信息
        for (String json : jsonList) {
            try {
                java.util.Map<String, Object> map = mapper.readValue(json, java.util.Map.class);
                Object companyInfo = map.get("公司信息");
                if (companyInfo instanceof java.util.List) {
                    allCompanyInfo.addAll((java.util.List<?>) companyInfo);
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
    private String calculateStatisticsFromAllData(List<JobData> allJobData, java.util.List<Object> allCompanyInfo) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 1. 计算城市薪资TOP10
            java.util.Map<String, java.util.List<Integer>> citySalaries = new java.util.HashMap<>();
            for (JobData job : allJobData) {
                String city = job.getCity();
                if (city != null && !city.isEmpty()) {
                    String salaryStr = job.getSalary();
                    if (salaryStr != null && !salaryStr.isEmpty()) {
                        // 提取薪资数字（假设格式如"15k-25k"或"15000-25000"）
                        Integer salary = extractSalaryNumber(salaryStr);
                        if (salary != null) {
                            citySalaries.computeIfAbsent(city, k -> new java.util.ArrayList<>()).add(salary);
                        }
                    }
                }
            }
            
            java.util.List<java.util.Map<String, Object>> citySalaryTop10 = new java.util.ArrayList<>();
            citySalaries.entrySet().stream()
                .map(entry -> {
                    double avgSalary = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
                    java.util.Map<String, Object> cityData = new java.util.HashMap<>();
                    cityData.put("cityName", entry.getKey());
                    cityData.put("avgSalary", (int) avgSalary);
                    return cityData;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("avgSalary"), (Integer) a.get("avgSalary")))
                .limit(10)
                .forEach(citySalaryTop10::add);
            
            // 2. 计算学历薪资关系
            java.util.Map<String, java.util.List<Integer>> educationSalaries = new java.util.HashMap<>();
            for (JobData job : allJobData) {
                String education = job.getEducation();
                if (education != null && !education.isEmpty()) {
                    String salaryStr = job.getSalary();
                    if (salaryStr != null && !salaryStr.isEmpty()) {
                        Integer salary = extractSalaryNumber(salaryStr);
                        if (salary != null) {
                            educationSalaries.computeIfAbsent(education, k -> new java.util.ArrayList<>()).add(salary);
                        }
                    }
                }
            }
            
            java.util.List<java.util.Map<String, Object>> educationSalaryRelation = new java.util.ArrayList<>();
            educationSalaries.forEach((education, salaries) -> {
                double avgSalary = salaries.stream().mapToInt(Integer::intValue).average().orElse(0);
                java.util.Map<String, Object> eduData = new java.util.HashMap<>();
                eduData.put("educationLevel", education);
                eduData.put("avgSalary", (int) avgSalary);
                educationSalaryRelation.add(eduData);
            });
            
            // 3. 计算学历经验关系
            java.util.Map<String, Integer> educationExperienceCount = new java.util.HashMap<>();
            for (JobData job : allJobData) {
                String education = job.getEducation();
                String experience = job.getExperience();
                if (education != null && !education.isEmpty() && experience != null && !experience.isEmpty()) {
                    String key = education + "|" + experience;
                    educationExperienceCount.put(key, educationExperienceCount.getOrDefault(key, 0) + 1);
                }
            }
            
            java.util.List<java.util.Map<String, Object>> educationExperienceRelation = new java.util.ArrayList<>();
            educationExperienceCount.forEach((key, count) -> {
                String[] parts = key.split("\\|");
                java.util.Map<String, Object> relationData = new java.util.HashMap<>();
                relationData.put("educationLevel", parts[0]);
                relationData.put("experienceLevel", parts[1]);
                relationData.put("jobCount", count);
                educationExperienceRelation.add(relationData);
            });
            
            // 4. 计算城市职位数量
            java.util.Map<String, Integer> cityJobCount = new java.util.HashMap<>();
            for (JobData job : allJobData) {
                String city = job.getCity();
                if (city != null && !city.isEmpty()) {
                    cityJobCount.put(city, cityJobCount.getOrDefault(city, 0) + 1);
                }
            }
            
            java.util.List<java.util.Map<String, Object>> cityJobCountList = new java.util.ArrayList<>();
            cityJobCount.forEach((city, count) -> {
                java.util.Map<String, Object> cityData = new java.util.HashMap<>();
                cityData.put("cityName", city);
                cityData.put("jobCount", count);
                cityJobCountList.add(cityData);
            });
            
            // 5. 计算城市职位数量TOP10
            java.util.List<java.util.Map<String, Object>> cityJobCountTop10 = cityJobCountList.stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("jobCount"), (Integer) a.get("jobCount")))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
            
            // 6. 计算学历要求分布
            java.util.Map<String, Integer> educationDistribution = new java.util.HashMap<>();
            for (JobData job : allJobData) {
                String education = job.getEducation();
                if (education != null && !education.isEmpty()) {
                    educationDistribution.put(education, educationDistribution.getOrDefault(education, 0) + 1);
                }
            }
            
            java.util.List<java.util.Map<String, Object>> educationDistributionList = new java.util.ArrayList<>();
            educationDistribution.forEach((education, count) -> {
                java.util.Map<String, Object> eduData = new java.util.HashMap<>();
                eduData.put("educationLevel", education);
                eduData.put("jobCount", count);
                educationDistributionList.add(eduData);
            });
            
            // 7. 构建最终JSON
            java.util.Map<String, Object> finalResult = new java.util.LinkedHashMap<>();
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
     * 从薪资字符串中提取数字
     */
    private Integer extractSalaryNumber(String salaryStr) {
        try {
            // 移除k、K、万等字符，提取数字
            String cleaned = salaryStr.replaceAll("[kK万]", "").replaceAll("[\\s-]", "");
            // 提取第一个数字
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(cleaned);
            if (matcher.find()) {
                int number = Integer.parseInt(matcher.group(1));
                // 如果原字符串包含"万"，转换为千
                if (salaryStr.contains("万")) {
                    number *= 10;
                }
                return number;
            }
        } catch (Exception e) {
            // 解析失败，返回null
        }
        return null;
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

**分析要求：**
1. 提取城市薪资TOP10：计算每个城市的平均薪资，取前10名
2. 提取学历与薪资关系：统计每个学历级别的平均薪资
3. 提取公司信息：提取公司名称、经验要求、学历要求与薪资信息
4. 提取学历经验关系：统计每个学历和经验组合的职位数量
5. 提取所有城市的职位数量：统计每个城市的职位数量
6. 提取学历要求分布：统计每个学历要求的职位数量

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
        {"educationLevel": "实际学历", "avgSalary": 实际计算的平均薪资}
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
        {"educationLevel": "实际学历", "jobCount": 实际统计数量} 
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
            int pageSize = 50; // 每页50行
            List<List<JobData>> pages = paginate(jobDataList, pageSize);
            System.out.println("共分为 " + pages.size() + " 页");

            List<String> jsonResults = new java.util.ArrayList<>();
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