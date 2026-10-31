USE [FUCarRentingSystemDB]
GO

-- =====================================================================
-- DỮ LIỆU "BẨN" - CHỈ DÙNG ĐỂ KIỂM THỬ KHẢ NĂNG CHỊU LỖI (chạy SAU seed_data.sql)
-- Mô phỏng dữ liệu nhập tay trực tiếp vào DB, không đi qua ứng dụng.
-- Dọn dẹp: chạy reset_data.sql rồi seed lại.
--
--  D1. legacy_user / legacy123 : mật khẩu lưu plain-text           -> đăng nhập phải bị từ chối, không lỗi 500
--  D2. orphan_user / 123456    : role Customer nhưng không có hồ sơ -> vào trang khách hàng phải báo lỗi thân thiện
--  D3. Xe 'Dirty Lowercase Car': status 'available' (chữ thường)    -> danh sách xe vẫn phải hiển thị
--  D4. Xe 'Dirty Price Car'    : giá 80.5 vào cột decimal(10)       -> DB tự làm tròn thành 81
--  D5. dup_mobile_user         : trùng SĐT với customer1            -> cập nhật hồ sơ không được lỗi 500
-- =====================================================================

DECLARE @Pwd123456 VARCHAR(200) = '$2a$10$NaGLVT7IJevrABWdS0xrCejQwZBDrBX6ux5M6QsTo7OGNANaQTReC';

-- D1
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'legacy_user')
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role]) VALUES (N'legacy_user', 'legacy@gmail.com', 'legacy123', N'Customer');

-- D2
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'orphan_user')
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role]) VALUES (N'orphan_user', 'orphan@gmail.com', @Pwd123456, N'Customer');

-- D3
IF NOT EXISTS (SELECT 1 FROM [dbo].[car] WHERE [car_name] = N'Dirty Lowercase Car')
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT TOP 1 N'Dirty Lowercase Car', 2020, N'Black', 5, N'Trạng thái viết thường', '2020-01-01', [producer_id], 40, N'available' FROM [dbo].[car_producer] ORDER BY [producer_id];

-- D4
IF NOT EXISTS (SELECT 1 FROM [dbo].[car] WHERE [car_name] = N'Dirty Price Car')
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT TOP 1 N'Dirty Price Car', 2020, N'Black', 5, N'Giá có phần thập phân', '2020-01-01', [producer_id], 80.5, N'Available' FROM [dbo].[car_producer] ORDER BY [producer_id];

-- D5
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'dup_mobile_user')
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role]) VALUES (N'dup_mobile_user', 'dupmobile@gmail.com', @Pwd123456, N'Customer');
IF NOT EXISTS (SELECT 1 FROM [dbo].[customer] WHERE [identity_card] = '001099000099')
    INSERT INTO [dbo].[customer] ([full_name], [mobile], [birthday], [identity_card], [licence_number], [licence_date], [account_id])
    SELECT N'Khách Trùng SĐT', '0901234567', '1999-09-09', '001099000099', 'B2-000099', '2020-09-09', [account_id] FROM [dbo].[account] WHERE [account_name] = N'dup_mobile_user';
GO
