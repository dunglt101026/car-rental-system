USE [FUCarRentingSystemDB]
GO

-- 1. Insert Accounts
-- Default passwords:
-- 'admin@carrent.com' -> 'admin123'
-- 'customer1@gmail.com' -> '123456'
-- 'customer2@gmail.com' -> '123456'
-- Passwords are encrypted using BCrypt ($2a$10$...)
IF NOT EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = 'admin')
BEGIN
    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role])
    VALUES 
    (N'admin', 'admin@carrent.com', '$2a$10$ipR/v0NmCvTysEK2irLvteUUYeBKnW65fs.Kf0L93e4GEpo9uZL8C', N'Admin'),
    (N'customer1', 'customer1@gmail.com', '$2a$10$NaGLVT7IJevrABWdS0xrCejQwZBDrBX6ux5M6QsTo7OGNANaQTReC', N'Customer'),
    (N'customer2', 'customer2@gmail.com', '$2a$10$NaGLVT7IJevrABWdS0xrCejQwZBDrBX6ux5M6QsTo7OGNANaQTReC', N'Customer');
END
GO

-- 2. Insert Car Producers
IF NOT EXISTS (SELECT 1 FROM [dbo].[car_producer])
BEGIN
    INSERT INTO [dbo].[car_producer] ([producer_name], [address], [country])
    VALUES
    (N'Toyota', N'1 Toyota-Cho, Toyota City, Aichi', N'Japan'),
    (N'Honda', N'2-1-1, Minami-Aoyama, Minato-ku, Tokyo', N'Japan'),
    (N'Hyundai', N'12 Heolleung-ro, Seocho-gu, Seoul', N'South Korea'),
    (N'Ford', N'1 American Road, Dearborn, Michigan', N'USA'),
    (N'VinFast', N'Dinh Vu - Cat Hai Economic Zone, Hai Phong', N'Vietnam');
END
GO

-- 3. Insert Customers
DECLARE @AccCustomer1 INT = (SELECT [account_id] FROM [dbo].[account] WHERE [account_name] = 'customer1');
DECLARE @AccCustomer2 INT = (SELECT [account_id] FROM [dbo].[account] WHERE [account_name] = 'customer2');

IF NOT EXISTS (SELECT 1 FROM [dbo].[customer]) AND @AccCustomer1 IS NOT NULL AND @AccCustomer2 IS NOT NULL
BEGIN
    INSERT INTO [dbo].[customer] ([full_name], [mobile], [birthday], [identity_card], [licence_number], [licence_date], [account_id])
    VALUES
    (N'Nguyễn Văn A', '0901234567', '1995-05-15', '001095012345', 'B2-987654321', '2020-01-10', @AccCustomer1),
    (N'Trần Thị B', '0987654321', '1998-08-20', '001098054321', 'B2-123456789', '2021-06-15', @AccCustomer2);
END
GO

-- 4. Insert Cars
DECLARE @ToyotaId INT = (SELECT [producer_id] FROM [dbo].[car_producer] WHERE [producer_name] = 'Toyota');
DECLARE @HondaId INT = (SELECT [producer_id] FROM [dbo].[car_producer] WHERE [producer_name] = 'Honda');
DECLARE @HyundaiId INT = (SELECT [producer_id] FROM [dbo].[car_producer] WHERE [producer_name] = 'Hyundai');
DECLARE @VinFastId INT = (SELECT [producer_id] FROM [dbo].[car_producer] WHERE [producer_name] = 'VinFast');
DECLARE @FordId INT = (SELECT [producer_id] FROM [dbo].[car_producer] WHERE [producer_name] = 'Ford');

IF NOT EXISTS (SELECT 1 FROM [dbo].[car]) AND @ToyotaId IS NOT NULL
BEGIN
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    VALUES
    (N'Toyota Camry 2.5Q', 2023, N'Black', 5, N'Sedan hạng D sang trọng, nội thất da cao cấp', '2023-01-10', @ToyotaId, 80.00, N'Available'),
    (N'Honda CR-V L', 2022, N'White', 7, N'SUV gia đình tiện nghi, gói công nghệ an toàn Honda Sensing', '2022-05-15', @HondaId, 75.00, N'Available'),
    (N'Hyundai Santa Fe Premium', 2023, N'Silver', 7, N'SUV 7 chỗ rộng rãi, dẫn động 4 bánh HTRAC', '2023-03-20', @HyundaiId, 90.00, N'Available'),
    (N'VinFast VF8 Plus', 2023, N'Blue', 5, N'SUV điện thông minh, tăng tốc mạnh mẽ, trợ lý ảo thông minh', '2023-06-01', @VinFastId, 85.00, N'Available'),
    (N'Ford Everest Titanium', 2021, N'Gray', 7, N'SUV cơ bắp, offroad cực đỉnh', '2021-11-05', @FordId, 95.00, N'Available');
END
GO

-- 5. Insert Car Rentals
DECLARE @Cust1 INT = (SELECT TOP 1 [customer_id] FROM [dbo].[customer] WHERE [mobile] = '0901234567');
DECLARE @Car1 INT = (SELECT TOP 1 [car_id] FROM [dbo].[car] WHERE [car_name] LIKE '%Camry%');

IF NOT EXISTS (SELECT 1 FROM [dbo].[car_rental]) AND @Cust1 IS NOT NULL AND @Car1 IS NOT NULL
BEGIN
    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES
    (@Cust1, @Car1, '2026-09-01', '2026-09-05', 320.00, N'Completed');
END
GO

-- 6. Insert Reviews
DECLARE @Rent1 INT = (SELECT TOP 1 [car_rent_id] FROM [dbo].[car_rental]);

IF NOT EXISTS (SELECT 1 FROM [dbo].[review]) AND @Rent1 IS NOT NULL
BEGIN
    INSERT INTO [dbo].[review] ([car_rent_id], [review_star], [comment])
    VALUES
    (@Rent1, 5, N'Xe đi rất đầm, tiết kiệm xăng và chủ xe hỗ trợ nhiệt tình!');
END
GO
