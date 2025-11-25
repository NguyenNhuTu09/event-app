CREATE DATABASE EMSdatabase;
USE EMSdatabase;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    address VARCHAR(255) NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NULL,
    date_of_birth DATE NULL,
    phone_number VARCHAR(20) NULL,
    avatar_url VARCHAR(255) NULL,
    avatar_s3_key VARCHAR(255) NULL

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE users 
MODIFY COLUMN role ENUM('USER', 'SADMIN', 'ORGANIZER', 'STUDENT_UNION') 
NOT NULL DEFAULT 'USER';

ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL;
SELECT *FROM users;


CREATE TABLE organizers (
    organizer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(255),
    contact_phone_number VARCHAR(20),
    contact_email VARCHAR(255),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
ALTER TABLE organizers 
ADD COLUMN is_approved BIT(1) NOT NULL DEFAULT 0;

SELECT *FROM organizers;
DROP TABLE organizers;


CREATE TABLE events (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizer_id INT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    banner_image_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    registration_deadline DATETIME,
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES organizers(organizer_id) ON DELETE CASCADE
);
ALTER TABLE events 
MODIFY COLUMN status ENUM('DRAFT', 'PENDING_APPROVAL', 'PUBLISHED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED') NOT NULL DEFAULT 'DRAFT';
SELECT *FROM events;









