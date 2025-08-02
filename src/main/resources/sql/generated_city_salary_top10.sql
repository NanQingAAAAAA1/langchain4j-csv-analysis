-- city_salary_top10
SELECT city AS cityName, AVG(salary_numeric) AS avgSalary 
FROM job_data 
GROUP BY city 
ORDER BY avgSalary DESC 
LIMIT 10;;
