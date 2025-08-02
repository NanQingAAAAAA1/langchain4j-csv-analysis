-- education_distribution
SELECT education AS educationLevel, COUNT(*) AS jobCount FROM job_data GROUP BY education;;
