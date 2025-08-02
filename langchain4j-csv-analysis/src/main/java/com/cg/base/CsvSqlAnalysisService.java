package com.cg.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSV SQL分析服务类
 * 使用LangChain4j + H2数据库实现CSV数据问答
 */
public class CsvSqlAnalysisService {

    private final ChatLanguageModel model;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final SqlGeneratorAssistant sqlGeneratorAssistant;

    /**
     * AI助手接口，用于生成SQL语句
     */
    public interface SqlGeneratorAssistant {
        @SystemMessage({
            "你是一名SQL分析专家。我会给你SQL相关的DDL，你需要根据DDL生成合理且可执行的SQL语句并返回。",
            "数据库类型：H2数据库",
            "数据库表结构：",
            "CREATE TABLE job_data (",
            "    id INT AUTO_INCREMENT PRIMARY KEY,",
            "    position VARCHAR(255),",
            "    salary VARCHAR(100),",
            "    salary_numeric INT,",
            "    city VARCHAR(100),",
            "    area VARCHAR(100),",
            "    experience VARCHAR(100),",
            "    education VARCHAR(100),",
            "    company VARCHAR(255),",
            "    company_field VARCHAR(255),",
            "    company_type VARCHAR(100),",
            "    company_size VARCHAR(100),",
            "    company_url VARCHAR(500),",
            "    job_url VARCHAR(500)",
            ");",
            "重要说明：",
            "- salary字段：原始薪资字符串（如'6-8千'、'1.5-3万'、'15k-25k'）",
            "- salary_numeric字段：已解析的数值薪资（整数，单位：元）",
            "- 对于薪资分析，优先使用salary_numeric字段进行计算",
            "- 对于显示原始薪资，使用salary字段",
            "学历标准化规则：",
            "- 只保留：本科、硕士、大专、不限",
            "- 其他学历类型统一归为\"不限\"",
            "输出字段名规范（必须严格按照以下格式）：",
            "- 城市名称：cityName",
            "- 平均薪资：avgSalary（使用salary_numeric字段计算）",
            "- 学历级别：educationLevel",
            "- 公司名称：companyName",
            "- 经验级别：experienceLevel",
            "- 职位数量：jobCount",
            "- 原始薪资：salary（小写）",
            "查询要求：",
            "- 城市薪资TOP10：按城市分组，计算salary_numeric的平均值，字段别名：cityName, avgSalary",
            "- 学历薪资关系：按学历分组，计算salary_numeric的平均值，字段别名：educationLevel, avgSalary",
            "- 公司信息：返回公司名、经验、学历、原始薪资，字段别名：companyName, experienceLevel, educationLevel, salary",
            "- 学历经验关系：按学历和经验分组，统计职位数量，字段别名：educationLevel, experienceLevel, jobCount",
            "- 城市职位数量：按城市分组，统计职位数量，字段别名：cityName, jobCount",
            "- 学历要求分布：按学历分组，统计职位数量，字段别名：educationLevel, jobCount",
            "请根据用户要求生成一条SQL语句，要求从H2数据库的`job_data`表中查询。",
            "只返回SQL语句，不要包含任何其他文字说明或代码块。",
            "确保SQL语句在H2数据库中可执行。",
            "对于薪资计算，使用salary_numeric字段进行数值运算。",
            "确保所有字段别名都使用小写，特别是salary字段。"
        })
        String generateSql(@UserMessage String userQuery);
    }

    /**
     * 初始化服务
     */
    public CsvSqlAnalysisService() {
        this.model = OpenAiChatModel.builder()
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
        
        this.objectMapper = new ObjectMapper();
        
        // 先创建数据源
        this.dataSource = createDataSource();
        
        // 然后创建AI助手
        this.sqlGeneratorAssistant = AiServices.builder(SqlGeneratorAssistant.class)
                .chatLanguageModel(model)
                .build();
    }

    /**
     * 创建数据源
     */
    private DataSource createDataSource() {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:jobdata;DB_CLOSE_DELAY=-1");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("sa");

        // 创建表结构
        createTables(h2DataSource);
        
        // 导入CSV数据
        importCsvData(h2DataSource);

        return h2DataSource;
    }

    /**
     * 创建数据库表结构
     */
    private void createTables(DataSource dataSource) {
        String createTablesScript = read("sql/create_tables.sql");
        executeSQL(createTablesScript, dataSource);
    }

    /**
     * 读取SQL文件
     */
    private String read(String path) {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/" + path);
            if (inputStream == null) {
                throw new RuntimeException("找不到资源文件: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行SQL语句
     */
    private void executeSQL(String sql, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.err.println("执行SQL时发生错误: " + e.getMessage());
        }
    }

    /**
     * 导入CSV数据到数据库
     */
    private void importCsvData(DataSource dataSource) {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(getClass().getResourceAsStream("/csv/qcwy_all.csv"), StandardCharsets.UTF_8))) {
            
            List<String[]> rows = reader.readAll();
            
            // 跳过标题行，从第二行开始处理
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 12) {
                    insertJobData(row, dataSource);
                }
            }
            
            System.out.println("成功导入 " + (rows.size() - 1) + " 条职位数据到数据库");
            
        } catch (IOException | CsvException e) {
            System.err.println("导入CSV数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 插入职位数据
     */
    private void insertJobData(String[] row, DataSource dataSource) {
        String insertSQL = String.format("""
            INSERT INTO job_data (position, salary, salary_numeric, city, area, experience, education, 
                                 company, company_field, company_type, company_size, company_url, job_url)
            VALUES ('%s', '%s', %s, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')
            """,
            escapeSQL(row[0]), escapeSQL(row[1]), parseSalaryNumeric(row[1]), escapeSQL(row[2]),
            escapeSQL(row[3]), escapeSQL(row[4]), normalizeEducation(row[5]), escapeSQL(row[6]),
            escapeSQL(row[7]), escapeSQL(row[8]), escapeSQL(row[9]), escapeSQL(row[10]), escapeSQL(row[11])
        );
        
        executeSQL(insertSQL, dataSource);
    }

    /**
     * 标准化学历字段（与CsvAnalysisService逻辑一致）
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
     * 解析薪资字符串为数值（参考CsvAnalysisService的逻辑）
     */
    private Integer parseSalaryNumeric(String salaryStr) {
        if (salaryStr == null || salaryStr.isEmpty()) {
            return null;
        }
        
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
     * 转义SQL字符串
     */
    private String escapeSQL(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    /**
     * 根据用户查询生成SQL并返回JSON结果
     */
    public String queryAsJson(String userQuery) {
        try {
            // 1. 使用AiService生成SQL
            String sql = sqlGeneratorAssistant.generateSql(userQuery);
            System.out.println("生成的SQL: " + sql);
            
            // 2. 执行SQL并转换为JSON
            String jsonResult = executeSQLToJson(sql);
            
            return jsonResult;
            
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 执行SQL并转换为JSON
     */
    private String executeSQLToJson(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            List<Map<String, Object>> rows = new ArrayList<>();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows);
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }

    /**
     * 生成结构化分析结果
     */
    public void generateStructuredAnalysis() {
        try {
            System.out.println("开始生成结构化分析结果...");
            
            Map<String, Object> finalResult = new LinkedHashMap<>();
            
            // 1. 城市薪资TOP10
            String citySalarySql = sqlGeneratorAssistant.generateSql("查询城市薪资TOP10，按平均薪资降序排列");
            System.out.println("城市薪资SQL: " + citySalarySql);
            saveSqlToFile("city_salary_top10", citySalarySql);
            finalResult.put("城市薪资TOP10", executeSQLToObjectList(citySalarySql));
            
            // 2. 学历薪资关系
            String educationSalarySql = sqlGeneratorAssistant.generateSql("查询学历薪资关系，统计每个学历级别的平均薪资");
            System.out.println("学历薪资SQL: " + educationSalarySql);
            saveSqlToFile("education_salary_relation", educationSalarySql);
            finalResult.put("学历薪资关系", executeSQLToObjectList(educationSalarySql));
            
            // 3. 公司信息
            String companyInfoSql = sqlGeneratorAssistant.generateSql("查询公司信息，包括公司名、经验要求、学历要求、薪资");
            System.out.println("公司信息SQL: " + companyInfoSql);
            saveSqlToFile("company_info", companyInfoSql);
            finalResult.put("公司信息", executeSQLToObjectList(companyInfoSql));
            
            // 4. 学历经验关系
            String educationExperienceSql = sqlGeneratorAssistant.generateSql("查询学历经验关系，统计每个学历和经验组合的职位数量");
            System.out.println("学历经验SQL: " + educationExperienceSql);
            saveSqlToFile("education_experience_relation", educationExperienceSql);
            finalResult.put("学历经验关系", executeSQLToObjectList(educationExperienceSql));
            
            // 5. 城市职位数量
            String cityJobCountSql = sqlGeneratorAssistant.generateSql("查询城市职位数量，统计每个城市的职位数量");
            System.out.println("城市职位数量SQL: " + cityJobCountSql);
            saveSqlToFile("city_job_count", cityJobCountSql);
            finalResult.put("城市职位数量", executeSQLToObjectList(cityJobCountSql));
            
            // 6. 城市职位数量TOP10
            String cityJobCountTop10Sql = sqlGeneratorAssistant.generateSql("查询城市职位数量TOP10，按职位数量降序排列");
            System.out.println("城市职位数量TOP10 SQL: " + cityJobCountTop10Sql);
            saveSqlToFile("city_job_count_top10", cityJobCountTop10Sql);
            finalResult.put("城市职位数量TOP10", executeSQLToObjectList(cityJobCountTop10Sql));
            
            // 7. 学历要求分布
            String educationDistributionSql = sqlGeneratorAssistant.generateSql("查询学历要求分布，统计每个学历要求的职位数量");
            System.out.println("学历要求分布SQL: " + educationDistributionSql);
            saveSqlToFile("education_distribution", educationDistributionSql);
            finalResult.put("学历要求分布", executeSQLToObjectList(educationDistributionSql));
            
            // 保存为JSON文件
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalResult);
            saveAnalysisResult(jsonResult);
            
            System.out.println("结构化分析结果已生成并保存");
            
        } catch (Exception e) {
            System.err.println("生成结构化分析结果时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据analysis_result.json的格式调整列标签
     */
    private String adjustColumnLabel(String originalLabel) {
        switch (originalLabel.toLowerCase()) {
            case "city":
            case "cityname":
                return "cityName";
            case "avgsalary":
            case "average_salary":
                return "avgSalary";
            case "educationlevel":
            case "education_level":
                return "educationLevel";
            case "companyname":
            case "company_name":
                return "companyName";
            case "experiencelevel":
            case "experience_level":
                return "experienceLevel";
            case "jobcount":
            case "position_count":
                return "jobCount";
            case "salary":
                return "salary";
            default:
                return originalLabel;
        }
    }

    /**
     * 保存SQL语句到文件
     */
    private void saveSqlToFile(String sqlName, String sql) {
        try {
            String outputPath = "src/main/resources/sql/generated_" + sqlName + ".sql";
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write("-- " + sqlName + "\n");
                writer.write(sql + ";\n");
            }
            System.out.println("SQL已保存到: " + outputPath);
        } catch (IOException e) {
            System.err.println("保存SQL文件时发生错误: " + e.getMessage());
        }
    }

    /**
     * 执行SQL并转换为对象列表
     */
    private List<Map<String, Object>> executeSQLToObjectList(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            List<Map<String, Object>> rows = new ArrayList<>();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    
                    // 根据analysis_result.json的格式调整字段名
                    String adjustedLabel = adjustColumnLabel(columnLabel);
                    
                    // 确保avgSalary字段为整数
                    if ("avgSalary".equals(adjustedLabel) && value instanceof Number) {
                        value = ((Number) value).intValue();
                    }
                    
                    row.put(adjustedLabel, value);
                }
                rows.add(row);
            }

            return rows;
            
        } catch (SQLException e) {
            System.err.println("执行SQL时发生错误: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 保存分析结果到JSON文件
     */
    private void saveAnalysisResult(String jsonContent) throws IOException {
        // 验证JSON格式
        try {
            objectMapper.readTree(jsonContent);
        } catch (Exception e) {
            System.err.println("JSON格式验证失败: " + e.getMessage());
            return;
        }
        
        // 保存到resources/json文件夹下
        String outputPath = "src/main/resources/json/sql_analysis_result.json";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(jsonContent);
        }
        
        System.out.println("分析结果已保存到: " + outputPath);
        System.out.println("JSON内容长度: " + jsonContent.length() + " 字符");
    }

    /**
     * 获取数据库统计信息
     */
    public String getDatabaseStats() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            var resultSet = statement.executeQuery("SELECT COUNT(*) as total FROM job_data");
            if (resultSet.next()) {
                int total = resultSet.getInt("total");
                return "数据库中共有 " + total + " 条职位数据";
            }
        } catch (SQLException e) {
            System.err.println("获取数据库统计信息时发生错误: " + e.getMessage());
        }
        return "无法获取数据库统计信息";
    }

    /**
     * 验证数据库数据
     */
    public void validateDatabaseData() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            System.out.println("=== 数据库数据验证 ===");
            
            // 检查总记录数
            var countResult = statement.executeQuery("SELECT COUNT(*) as total FROM job_data");
            if (countResult.next()) {
                System.out.println("总记录数: " + countResult.getInt("total"));
            }
            
            // 检查实际数据样本
            var sampleResult = statement.executeQuery("SELECT city, salary, education FROM job_data LIMIT 5");
            System.out.println("数据样本:");
            while (sampleResult.next()) {
                System.out.println("  城市: " + sampleResult.getString("city") + 
                                ", 薪资: " + sampleResult.getString("salary") + 
                                ", 学历: " + sampleResult.getString("education"));
            }
            
            System.out.println("数据库连接正常，数据已导入");
            System.out.println("AI将自动生成SQL查询来分析数据");
            System.out.println("=== 验证完成 ===");
            
        } catch (SQLException e) {
            System.err.println("验证数据库数据时发生错误: " + e.getMessage());
        }
    }
} 