-- Migration: Thêm column firmware_url vào bảng tbl_firmware
-- Date: 2025-11-23
-- Purpose: Lưu Cloudinary URL thay vì local file path

-- 1. Thêm column firmware_url
ALTER TABLE tbl_firmware 
ADD COLUMN firmware_url VARCHAR(500);

-- 2. Thêm comment cho column
COMMENT ON COLUMN tbl_firmware.firmware_url IS 'Cloudinary URL for firmware binary file';

-- 3. Optional: Tạo index nếu cần search by URL
-- CREATE INDEX idx_firmware_url ON tbl_firmware(firmware_url);

-- 4. Kiểm tra kết quả
SELECT 
    id,
    version,
    version_number,
    firmware_url,
    released_at
FROM tbl_firmware
ORDER BY version_number DESC;
