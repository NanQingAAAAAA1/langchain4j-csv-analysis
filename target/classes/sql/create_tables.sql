CREATE TABLE IF NOT EXISTS job_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    position VARCHAR(255),
    salary VARCHAR(100),
    salary_numeric INT,
    city VARCHAR(100),
    area VARCHAR(100),
    experience VARCHAR(100),
    education VARCHAR(100),
    company VARCHAR(255),
    company_field VARCHAR(255),
    company_type VARCHAR(100),
    company_size VARCHAR(100),
    company_url VARCHAR(500),
    job_url VARCHAR(500)
); 