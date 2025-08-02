-- city_job_count
SELECT city AS cityName, COUNT(*) AS jobCount FROM job_data GROUP BY city;;
