-- Show current users and their roles
SELECT id, username, email, role FROM users;

-- Check if admin exists
SELECT * FROM users WHERE email = 'admin@repairapp.com';

-- Delete any existing admin user (in case it's corrupted)
DELETE FROM users WHERE email = 'admin@repairapp.com';

-- Create fresh admin user
INSERT INTO users (username, full_name, email, password, role, shop_id)
VALUES (
    'admin',
    'System Administrator',
    'admin@repairapp.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'ADMINISTRATOR',
    NULL
); 