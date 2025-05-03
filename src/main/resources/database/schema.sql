CREATE TABLE Country (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    guid VARCHAR(100) NOT NULL UNIQUE,
    created_on DATETIME,
    updated_on DATETIME
);

CREATE TABLE OAuthUser (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    country INT,
    is_verified BOOLEAN DEFAULT FALSE,
    created_on DATETIME,
    updated_on DATETIME,
    FOREIGN KEY (country) REFERENCES Country(id)
);

CREATE TABLE Package (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    country INT,
    price DECIMAL(10, 2),
    credit_amount INT,
    expiration_days INT,
    status BOOLEAN DEFAULT TRUE,
    created_on DATETIME,
    updated_on DATETIME,
    FOREIGN KEY (country) REFERENCES Country(id)
);

CREATE TABLE UserPackage (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    package_id INT,
    remaining_credits INT,
    created_on DATETIME,
    updated_on DATETIME,
    FOREIGN KEY (user_id) REFERENCES OAuthUser(id),
    FOREIGN KEY (package_id) REFERENCES Package(id)
);

CREATE TABLE ClassSchedule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100),
    country INT,
    required_credits INT,
    start_time DATETIME,
    end_time DATETIME,
    slot_count INT,
    created_on DATETIME,
    updated_on DATETIME,
    FOREIGN KEY (country) REFERENCES Country(id)
);

CREATE TABLE Booking (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    schedule_id INT,
    user_package_id INT,
    status VARCHAR(50),
    created_on DATETIME,
    updated_on DATETIME,
    FOREIGN KEY (user_id) REFERENCES OAuthUser(id),
    FOREIGN KEY (schedule_id) REFERENCES ClassSchedule(id),
    FOREIGN KEY (user_package_id) REFERENCES UserPackage(id)
);

CREATE TABLE WaitingList (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    schedule_id INT,
    user_package_id INT,
    created_on DATETIME,
    updated_on DATETIME,
    FOREIGN KEY (user_id) REFERENCES OAuthUser(id),
    FOREIGN KEY (schedule_id) REFERENCES ClassSchedule(id),
    FOREIGN KEY (user_package_id) REFERENCES UserPackage(id)
);
