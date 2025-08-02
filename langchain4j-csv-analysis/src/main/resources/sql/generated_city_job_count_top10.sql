-- city_job_count_top10
SELECT city AS cityName, COUNT(*) AS jobCount 
FROM job_data 
GROUP BY city 
ORDER BY jobCount DESC 
LIMIT 10;;
