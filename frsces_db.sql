CREATE TABLE IF NOT EXISTS students (
    student_id        VARCHAR(50)  PRIMARY KEY,
    full_name         VARCHAR(150) NOT NULL,
    parent_number      VARCHAR(100),
    grade_level       VARCHAR(20),
    section           VARCHAR(50),
    registration_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance_logs (
    log_id     SERIAL       PRIMARY KEY,
    student_id VARCHAR(50)  REFERENCES students(student_id) ON DELETE CASCADE,
    scan_date  DATE         DEFAULT CURRENT_DATE,
    scan_time  TIME         DEFAULT CURRENT_TIME
);

SELECT * FROM students;