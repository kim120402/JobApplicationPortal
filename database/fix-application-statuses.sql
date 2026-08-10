USE jobportal_db;

-- Safely migrate old application status values to the current SRS values.
UPDATE applications
SET application_status = 'APPLIED'
WHERE application_status = 'SUBMITTED';

UPDATE applications
SET application_status = 'UNDER_REVIEW'
WHERE application_status = 'REVIEWED';

UPDATE applications
SET application_status = 'HIRED'
WHERE application_status = 'ACCEPTED';
