-- Show all users and their roles to verify
SELECT id, username, email, role FROM users;

-- Verify the enum values allowed in the role column
SHOW COLUMNS FROM users LIKE 'role'; 