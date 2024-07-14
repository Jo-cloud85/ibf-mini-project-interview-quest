drop database if exists interview_quest;

create database interview_quest;

use interview_quest;

create table user_auth_details(
    userId VARCHAR(128) PRIMARY KEY,
    firstName VARCHAR(64) NOT NULL,
    lastName VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    isEmailVerified BOOLEAN DEFAULT FALSE,
    hashedPassword VARCHAR(255) NOT NULL,
    profilePicUrl VARCHAR(255),
    isDisabled BOOLEAN DEFAULT FALSE,
    lastPasswordChange TIMESTAMP
);

create table job_summary(
    customJobId VARCHAR(8) PRIMARY KEY,
    userId VARCHAR(128),
    assistantId VARCHAR(64),
    threadId VARCHAR(64),
    firebaseThreadKey VARCHAR(64),
    title VARCHAR(255),
    level ENUM('INTERNSHIP', 'ENTRY', 'JUNIOR', 'MID', 'SENIOR', 'OTHERS') NOT NULL,
    createdTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isAttempted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (userId) REFERENCES user_auth_details(userId)
);

create table schedule (
    scheduleId INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(128),
    subject VARCHAR(128) NOT NULL,
    content TEXT,
    practiceDate DATE,

    FOREIGN KEY (userId) REFERENCES user_auth_details(userId)
);

-- grant all privileges on interview_quest.* to 'abcde'@'%';
-- flush privileges;