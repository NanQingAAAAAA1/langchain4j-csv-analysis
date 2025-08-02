package com.cg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 职位数据实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobData {
    private String position;      // 职位
    private String salary;        // 薪资
    private String city;          // 城市
    private String area;          // 区域
    private String experience;    // 经验
    private String education;     // 学历
    private String company;       // 公司
    private String companyField;  // 公司领域
    private String companyType;   // 公司性质
    private String companySize;   // 公司规模
    private String companyUrl;    // 公司详情页
    private String jobUrl;        // 职位详情页

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
                           position, salary, city, area, experience, education, 
                           company, companyField, companyType, companySize, 
                           companyUrl, jobUrl);
    }
} 