CREATE DATABASE IF NOT EXISTS jobportal_db;
USE jobportal_db;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS jobs (
    job_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    employment_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    posted_date DATE NOT NULL,
    PRIMARY KEY (job_id)
);

CREATE TABLE IF NOT EXISTS applications (
    application_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    resume_file_name VARCHAR(255) NOT NULL,
    cover_letter TEXT,
    application_status VARCHAR(50) NOT NULL,
    applied_date DATETIME NOT NULL,
    PRIMARY KEY (application_id),
    CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE RESTRICT,
    CONSTRAINT uk_applications_user_job UNIQUE (user_id, job_id)
);
