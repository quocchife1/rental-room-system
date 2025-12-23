-- Fix ENUM columns in partner_posts table
-- This script updates the database schema to match the new enum values in Java code

-- Step 1: Update status enum to include all 4 states
ALTER TABLE partner_posts MODIFY COLUMN status ENUM(
    'PENDING_PAYMENT',
    'PENDING_APPROVAL',
    'APPROVED',
    'REJECTED'
) NOT NULL;

-- Step 2: Update post_type enum to include all 4 package tiers
ALTER TABLE partner_posts MODIFY COLUMN post_type ENUM(
    'NORMAL',
    'VIP1',
    'VIP2',
    'VIP3'
) NOT NULL;

-- Verify the changes
SELECT COLUMN_NAME, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_SCHEMA = 'rentaldb'
    AND TABLE_NAME = 'partner_posts'
    AND COLUMN_NAME IN ('status', 'post_type');