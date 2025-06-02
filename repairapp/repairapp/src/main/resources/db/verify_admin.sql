-- Check current role values
SELECT DISTINCT role FROM users;

-- Update the admin user's role if it exists but has wrong role
UPDATE users 
SET role = 'ADMINISTRATOR' 
WHERE email = 'admin@repairapp.com';

-- Insert admin if not exists
INSERT INTO users (username, full_name, email, password, role)
SELECT 'admin', 'System Administrator', 'admin@repairapp.com', 
       '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ADMINISTRATOR'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@repairapp.com'
); 