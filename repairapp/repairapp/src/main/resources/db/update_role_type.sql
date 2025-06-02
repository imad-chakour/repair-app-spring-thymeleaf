-- First, update the enum type to include ADMINISTRATOR
ALTER TYPE role_enum ADD VALUE IF NOT EXISTS 'ADMINISTRATOR';

-- Then insert the admin user
INSERT INTO users (username, full_name, email, password, role)
VALUES (
    'admin',
    'System Administrator',
    'admin@repairapp.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'ADMINISTRATOR'
); 