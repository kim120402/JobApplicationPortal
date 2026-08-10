USE jobportal_db;

-- Find incomplete application rows.
SELECT *
FROM applications
WHERE user_id IS NULL
   OR job_id IS NULL
   OR application_status IS NULL
   OR applied_date IS NULL;

-- Find applications whose user record is missing.
SELECT a.*
FROM applications a
LEFT JOIN users u ON a.user_id = u.user_id
WHERE u.user_id IS NULL;

-- Find applications whose job record is missing.
SELECT a.*
FROM applications a
LEFT JOIN jobs j ON a.job_id = j.job_id
WHERE j.job_id IS NULL;

-- Find legacy or invalid statuses.
SELECT *
FROM applications
WHERE application_status NOT IN ('APPLIED', 'UNDER_REVIEW', 'INTERVIEW', 'REJECTED', 'HIRED');
