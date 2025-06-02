-- Modify the role column to include ADMINISTRATOR
ALTER TABLE users MODIFY COLUMN role ENUM('ADMINISTRATOR', 'PROPRIETAIRE', 'REPARATEUR');

-- Insert admin user
INSERT INTO users (username, full_name, email, password, role)
VALUES (
    'admin',
    'System Administrator',
    'admin@repairapp.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'ADMINISTRATOR'
); 