USE jobportal_db;

-- Reusable sample job postings for Job Portal.
-- These inserts are idempotent and will not overwrite existing matching records.

INSERT INTO jobs (title, company_name, location, category, employment_type, description, requirements, status, posted_date)
SELECT 'Software Developer Co-op', 'Northstar Systems', 'Toronto', 'Technology', 'CO_OP',
       'Build and maintain internal web applications for student services.',
       'Java, Spring Boot, SQL, HTML, CSS, and teamwork skills.', 'ACTIVE', CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Software Developer Co-op' AND company_name = 'Northstar Systems');

INSERT INTO jobs (title, company_name, location, category, employment_type, description, requirements, status, posted_date)
SELECT 'Data Analyst Intern', 'Maple Analytics', 'Mississauga', 'Technology', 'INTERNSHIP',
       'Support reporting, spreadsheet automation, and dashboard projects.',
       'SQL, Excel, data visualization, and clear communication.', 'ACTIVE', CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Data Analyst Intern' AND company_name = 'Maple Analytics');

INSERT INTO jobs (title, company_name, location, category, employment_type, description, requirements, status, posted_date)
SELECT 'Junior Web Designer', 'Lakeside Digital Studio', 'Remote', 'Design', 'PART_TIME',
       'Create clean page layouts and update website content for small businesses.',
       'HTML, CSS, basic JavaScript, and attention to visual detail.', 'ACTIVE', CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Junior Web Designer' AND company_name = 'Lakeside Digital Studio');

INSERT INTO jobs (title, company_name, location, category, employment_type, description, requirements, status, posted_date)
SELECT 'IT Support Assistant', 'Harbour Business Centre', 'Brampton', 'Information Technology', 'CO_OP',
       'Help staff troubleshoot devices, software access, and internal support requests.',
       'Customer service, Windows basics, ticket tracking, and problem solving.', 'ACTIVE', CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'IT Support Assistant' AND company_name = 'Harbour Business Centre');

INSERT INTO jobs (title, company_name, location, category, employment_type, description, requirements, status, posted_date)
SELECT 'Business Operations Intern', 'Cedar Grove Logistics', 'Vaughan', 'Business', 'INTERNSHIP',
       'Assist with process documentation, scheduling, and weekly operations reports.',
       'Organization, Excel, written communication, and reliability.', 'ACTIVE', CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Business Operations Intern' AND company_name = 'Cedar Grove Logistics');

INSERT INTO jobs (title, company_name, location, category, employment_type, description, requirements, status, posted_date)
SELECT 'Office Assistant', 'Harbour Business Centre', 'Brampton', 'Administration', 'PART_TIME',
       'Assist with scheduling, filing, and office support.',
       'Organization and customer service skills.', 'CLOSED', CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Office Assistant' AND company_name = 'Harbour Business Centre');
