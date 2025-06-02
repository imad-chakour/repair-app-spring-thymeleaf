-- Show current role value for your user
SELECT id, username, email, role FROM users WHERE email = 'admin@repairapp.com';

-- Update the role to ADMINISTRATOR (if needed)
UPDATE users 
SET role = 'ADMINISTRATOR' 
WHERE email = 'admin@repairapp.com';

-- Verify the change
SELECT id, username, email, role FROM users WHERE email = 'admin@repairapp.com'; 