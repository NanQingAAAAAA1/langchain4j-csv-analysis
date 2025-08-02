-- education_salary_relation
SELECT education AS educationLevel, AVG(salary_numeric) AS avgSalary FROM job_data GROUP BY education;;
