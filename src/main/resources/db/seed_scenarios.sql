USE [FUCarRentingSystemDB]
GO

-- =====================================================================
-- DỮ LIỆU TÌNH HUỐNG NGHIỆP VỤ (chạy SAU seed_data.sql, chạy lại nhiều lần không bị trùng)
-- Ngày thuê tính tương đối theo ngày chạy script để báo cáo "30 ngày gần nhất" luôn có dữ liệu.
-- Mật khẩu mọi tài khoản bên dưới: 123456 (BCrypt)
--
--  Tài khoản  | Khách hàng        | Tình huống
--  customer3  | Lê Văn Cường      | Đang có đơn Active (Kia Morning) -> Trả xe / Hủy đơn, không xóa được khách
--  customer4  | Phạm Thị Dung     | Có đơn Completed CHƯA đánh giá (gửi đánh giá được) + 1 đơn Cancelled
--  customer5  | Hoàng Minh Đức    | Chưa có đơn nào -> Admin xóa được
--  customer6  | Vũ Thu Hà         | Nhiều đơn Completed ở nhiều thời điểm -> kiểm tra báo cáo, sắp xếp
--
--  Xe                      | Trạng thái | Tình huống
--  Kia Morning             | Rented     | Có đơn Active -> không xóa, không đổi trạng thái tay được
--  Mazda CX-5              | Available  | Chưa từng thuê -> xóa VẬT LÝ
--  Mitsubishi Xpander      | Available  | Đã từng thuê -> xóa chuyển Inactive (soft delete)
--  Toyota Innova           | Inactive   | Đã bị ẩn -> không hiện ở /cars, admin vẫn thấy
--  Ford Transit 16 chỗ     | Available  | Lọc số chỗ / giá cao
-- =====================================================================

DECLARE @Today DATE = CAST(GETDATE() AS DATE);
DECLARE @Pwd123456 VARCHAR(200) = '$2a$10$NaGLVT7IJevrABWdS0xrCejQwZBDrBX6ux5M6QsTo7OGNANaQTReC';

-- 1. Hãng xe bổ sung
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_producer] WHERE [producer_name] = N'Kia')
    INSERT INTO [dbo].[car_producer] ([producer_name], [address], [country]) VALUES (N'Kia', N'12 Heolleung-ro, Seocho-gu, Seoul', N'South Korea');
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_producer] WHERE [producer_name] = N'Mazda')
    INSERT INTO [dbo].[car_producer] ([producer_name], [address], [country]) VALUES (N'Mazda', N'3-1 Shinchi, Fuchu-cho, Hiroshima', N'Japan');
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_producer] WHERE [producer_name] = N'Mitsubishi')
    INSERT INTO [dbo].[car_producer] ([producer_name], [address], [country]) VALUES (N'Mitsubishi', N'1-21 Shibaura 3-chome, Minato-ku, Tokyo', N'Japan');

-- 2. Tài khoản + hồ sơ khách hàng
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'customer3')
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role]) VALUES (N'customer3', 'customer3@gmail.com', @Pwd123456, N'Customer');
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'customer4')
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role]) VALUES (N'customer4', 'customer4@gmail.com', @Pwd123456, N'Customer');
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'customer5')
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role]) VALUES (N'customer5', 'customer5@gmail.com', @Pwd123456, N'Customer');
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'customer6')
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role]) VALUES (N'customer6', 'customer6@gmail.com', @Pwd123456, N'Customer');

IF NOT EXISTS (SELECT 1 FROM [dbo].[customer] WHERE [identity_card] = '001090000003')
    INSERT INTO [dbo].[customer] ([full_name], [mobile], [birthday], [identity_card], [licence_number], [licence_date], [account_id])
    SELECT N'Lê Văn Cường', '0911000003', '1990-03-03', '001090000003', 'B2-000003', '2015-03-03', [account_id] FROM [dbo].[account] WHERE [account_name] = N'customer3';
IF NOT EXISTS (SELECT 1 FROM [dbo].[customer] WHERE [identity_card] = '001092000004')
    INSERT INTO [dbo].[customer] ([full_name], [mobile], [birthday], [identity_card], [licence_number], [licence_date], [account_id])
    SELECT N'Phạm Thị Dung', '0911000004', '1992-04-04', '001092000004', 'B2-000004', '2016-04-04', [account_id] FROM [dbo].[account] WHERE [account_name] = N'customer4';
IF NOT EXISTS (SELECT 1 FROM [dbo].[customer] WHERE [identity_card] = '001094000005')
    INSERT INTO [dbo].[customer] ([full_name], [mobile], [birthday], [identity_card], [licence_number], [licence_date], [account_id])
    SELECT N'Hoàng Minh Đức', '0911000005', '1994-05-05', '001094000005', 'B2-000005', '2018-05-05', [account_id] FROM [dbo].[account] WHERE [account_name] = N'customer5';
IF NOT EXISTS (SELECT 1 FROM [dbo].[customer] WHERE [identity_card] = '001096000006')
    INSERT INTO [dbo].[customer] ([full_name], [mobile], [birthday], [identity_card], [licence_number], [licence_date], [account_id])
    SELECT N'Vũ Thu Hà', '0911000006', '1996-06-06', '001096000006', 'B2-000006', '2019-06-06', [account_id] FROM [dbo].[account] WHERE [account_name] = N'customer6';

-- 3. Xe tình huống
IF NOT EXISTS (SELECT 1 FROM [dbo].[car] WHERE [car_name] = N'Kia Morning')
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT N'Kia Morning', 2022, N'Red', 4, N'Xe đô thị nhỏ gọn, tiết kiệm nhiên liệu', '2022-02-01', [producer_id], 30, N'Rented' FROM [dbo].[car_producer] WHERE [producer_name] = N'Kia';
IF NOT EXISTS (SELECT 1 FROM [dbo].[car] WHERE [car_name] = N'Mazda CX-5')
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT N'Mazda CX-5', 2024, N'Red', 5, N'Crossover thiết kế KODO, chưa từng cho thuê', '2024-03-01', [producer_id], 70, N'Available' FROM [dbo].[car_producer] WHERE [producer_name] = N'Mazda';
IF NOT EXISTS (SELECT 1 FROM [dbo].[car] WHERE [car_name] = N'Mitsubishi Xpander')
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT N'Mitsubishi Xpander', 2022, N'White', 7, N'MPV 7 chỗ phổ thông, đã từng cho thuê', '2022-01-15', [producer_id], 55, N'Available' FROM [dbo].[car_producer] WHERE [producer_name] = N'Mitsubishi';
IF NOT EXISTS (SELECT 1 FROM [dbo].[car] WHERE [car_name] = N'Toyota Innova')
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT N'Toyota Innova', 2019, N'Silver', 8, N'Xe đã ngừng kinh doanh (Inactive)', '2019-07-01', [producer_id], 60, N'Inactive' FROM [dbo].[car_producer] WHERE [producer_name] = N'Toyota';
IF NOT EXISTS (SELECT 1 FROM [dbo].[car] WHERE [car_name] = N'Ford Transit 16 chỗ')
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT N'Ford Transit 16 chỗ', 2023, N'White', 16, N'Xe khách 16 chỗ cho đoàn', '2023-09-01', [producer_id], 120, N'Available' FROM [dbo].[car_producer] WHERE [producer_name] = N'Ford';

-- 4. Đơn thuê tình huống (giá = đơn giá x số ngày)
DECLARE @C3 INT = (SELECT [customer_id] FROM [dbo].[customer] WHERE [identity_card] = '001090000003');
DECLARE @C4 INT = (SELECT [customer_id] FROM [dbo].[customer] WHERE [identity_card] = '001092000004');
DECLARE @C6 INT = (SELECT [customer_id] FROM [dbo].[customer] WHERE [identity_card] = '001096000006');
DECLARE @Kia INT = (SELECT [car_id] FROM [dbo].[car] WHERE [car_name] = N'Kia Morning');
DECLARE @Xpander INT = (SELECT [car_id] FROM [dbo].[car] WHERE [car_name] = N'Mitsubishi Xpander');
DECLARE @Innova INT = (SELECT [car_id] FROM [dbo].[car] WHERE [car_name] = N'Toyota Innova');
DECLARE @Transit INT = (SELECT [car_id] FROM [dbo].[car] WHERE [car_name] = N'Ford Transit 16 chỗ');
DECLARE @Crv INT = (SELECT TOP 1 [car_id] FROM [dbo].[car] WHERE [car_name] LIKE N'Honda CR-V%');

-- 4a. customer3: đơn Active đang chạy (Kia Morning đang Rented)
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_rental] WHERE [customer_id] = @C3 AND [car_id] = @Kia)
    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@C3, @Kia, DATEADD(DAY, -2, @Today), DATEADD(DAY, 3, @Today), 30 * 5, N'Active');

-- 4b. customer4: Completed chưa đánh giá + Cancelled
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_rental] WHERE [customer_id] = @C4 AND [car_id] = @Xpander)
    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@C4, @Xpander, DATEADD(DAY, -20, @Today), DATEADD(DAY, -17, @Today), 55 * 3, N'Completed');
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_rental] WHERE [customer_id] = @C4 AND [car_id] = @Crv)
    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@C4, @Crv, DATEADD(DAY, -10, @Today), DATEADD(DAY, -8, @Today), 75 * 2, N'Cancelled');

-- 4c. customer6: nhiều đơn Completed để test báo cáo (1 đơn nằm ngoài 30 ngày gần nhất)
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_rental] WHERE [customer_id] = @C6 AND [car_id] = @Innova)
    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@C6, @Innova, DATEADD(DAY, -60, @Today), DATEADD(DAY, -55, @Today), 60 * 5, N'Completed');
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_rental] WHERE [customer_id] = @C6 AND [car_id] = @Transit)
    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@C6, @Transit, DATEADD(DAY, -5, @Today), DATEADD(DAY, -2, @Today), 120 * 3, N'Completed');
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_rental] WHERE [customer_id] = @C6 AND [car_id] = @Xpander)
    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@C6, @Xpander, DATEADD(DAY, -28, @Today), DATEADD(DAY, -26, @Today), 55 * 2, N'Completed');

-- 5. Đánh giá cho một số đơn của customer6 (đơn của customer4 cố ý CHƯA có đánh giá)
INSERT INTO [dbo].[review] ([car_rent_id], [review_star], [comment])
SELECT r.[car_rent_id], 3, N'Xe hơi cũ nhưng chạy ổn định.'
FROM [dbo].[car_rental] r
WHERE r.[customer_id] = @C6 AND r.[car_id] = @Innova
  AND NOT EXISTS (SELECT 1 FROM [dbo].[review] v WHERE v.[car_rent_id] = r.[car_rent_id]);

INSERT INTO [dbo].[review] ([car_rent_id], [review_star], [comment])
SELECT r.[car_rent_id], 4, N'Xe rộng, phù hợp đi đoàn.'
FROM [dbo].[car_rental] r
WHERE r.[customer_id] = @C6 AND r.[car_id] = @Transit
  AND NOT EXISTS (SELECT 1 FROM [dbo].[review] v WHERE v.[car_rent_id] = r.[car_rent_id]);
GO
