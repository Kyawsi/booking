-- Countries
INSERT INTO Country (name, guid, created_on, updated_on) VALUES
('Myanmar', 'MM-001', NOW(), NOW()),
('Singapore', 'SG-001', NOW(), NOW());

-- Users
INSERT INTO OAuthUser (email, password, name, country, is_verified, created_on, updated_on) VALUES
('user1@example.com', 'pass123', 'User One', 1, TRUE, NOW(), NOW()),
('user2@example.com', 'pass456', 'User Two', 2, TRUE, NOW(), NOW());

-- Packages
INSERT INTO Package (name, country, price, credit_amount, expiration_days, status, created_on, updated_on) VALUES
('Basic Package', 1, 19.99, 10, 30, TRUE, NOW(), NOW()),
('Premium Package', 2, 39.99, 25, 60, TRUE, NOW(), NOW());

-- User Packages
INSERT INTO UserPackage (user_id, package_id, remaining_credits, created_on, updated_on) VALUES
(1, 1, 10, NOW(), NOW()),
(2, 2, 25, NOW(), NOW());

-- Class Schedules
INSERT INTO ClassSchedule (title, country, required_credits, start_time, end_time, slot_count, created_on, updated_on) VALUES
('Yoga Class', 1, 2, '2025-05-05 10:00:00', '2025-05-05 11:00:00', 10, NOW(), NOW()),
('Pilates Class', 2, 3, '2025-05-06 11:00:00', '2025-05-06 12:00:00', 15, NOW(), NOW());

-- Bookings
INSERT INTO Booking (user_id, schedule_id, user_package_id, status, created_on, updated_on) VALUES
(1, 1, 1, 'BOOKED', NOW(), NOW()),
(2, 2, 2, 'BOOKED', NOW(), NOW());

-- Waiting List
INSERT INTO WaitingList (user_id, schedule_id, user_package_id, created_on, updated_on) VALUES
(2, 1, 2, NOW(), NOW());
