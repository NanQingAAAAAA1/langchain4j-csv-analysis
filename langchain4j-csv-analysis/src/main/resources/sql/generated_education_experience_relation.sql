-- education_experience_relation
SELECT education AS educationLevel, experience AS experienceLevel, COUNT(*) AS jobCount FROM job_data GROUP BY education, experience;;
