USE [FUCarRentingSystemDB]
GO

-- =====================================================================
-- DỮ LIỆU KHỐI LƯỢNG VỪA - DÙNG ĐỂ XEM PHÂN TRANG / TÌM KIẾM (chạy SAU seed_data.sql và seed_scenarios.sql)
-- Chạy lại nhiều lần không bị trùng (kiểm tra tài khoản bulk01).
-- Mật khẩu mọi tài khoản bulk01..bulk40: 123456 (BCrypt)
--
--  40 khách hàng   : bulk01..bulk40
--  30 xe           : tên có hậu tố (BK-01)..(BK-30); 25 Available, 3 Rented (có đơn Active), 2 Inactive
--  123 đơn thuê    : 120 đơn trong năm 2025 (Completed, cứ 7 đơn có 1 đơn Cancelled) + 3 đơn Active
--  ~35 đánh giá    : trên một phần đơn Completed
--
-- Không làm lệch bảng nghiệm thu (tmp/agents/nghiem-thu.md):
--  - Đơn cũ đều ở năm 2025 nên báo cáo AD9 / AD11 (năm 2026) không đổi.
--  - Xe giá 35..95, sức chứa 4/5/7 chỗ, không có tên Mazda nên P3 / P4 / P5 không đổi.
-- =====================================================================

IF EXISTS (SELECT 1 FROM [dbo].[account] WHERE [account_name] = N'bulk01')
BEGIN
    PRINT N'Dữ liệu bulk đã có, bỏ qua.';
    RETURN;
END

DECLARE @Pwd123456 VARCHAR(200) = '$2a$10$NaGLVT7IJevrABWdS0xrCejQwZBDrBX6ux5M6QsTo7OGNANaQTReC';
DECLARE @Today DATE = CAST(GETDATE() AS DATE);
DECLARE @i INT;

-- 1. Khách hàng: họ + tên đệm + tên ghép theo số thứ tự
DECLARE @Ho TABLE (n INT, v NVARCHAR(20));
INSERT INTO @Ho VALUES (0, N'Nguyễn'), (1, N'Trần'), (2, N'Lê'), (3, N'Phạm'), (4, N'Hoàng'), (5, N'Vũ'), (6, N'Đặng'), (7, N'Bùi');
DECLARE @Dem TABLE (n INT, v NVARCHAR(20));
INSERT INTO @Dem VALUES (0, N'Văn'), (1, N'Thị'), (2, N'Minh'), (3, N'Thu'), (4, N'Quốc');
DECLARE @Ten TABLE (n INT, v NVARCHAR(20));
INSERT INTO @Ten VALUES (0, N'An'), (1, N'Bình'), (2, N'Chi'), (3, N'Dũng'), (4, N'Giang'), (5, N'Hải'), (6, N'Khánh');

SET @i = 1;
WHILE @i <= 40
BEGIN
    DECLARE @Code VARCHAR(2) = RIGHT('0' + CAST(@i AS VARCHAR(2)), 2);
    DECLARE @Birthday DATE = DATEADD(DAY, @i * 211, '1975-01-15');

    INSERT INTO [dbo].[account] ([account_name], [email], [password], [role])
    VALUES (N'bulk' + @Code, 'bulk' + @Code + '@gmail.com', @Pwd123456, N'Customer');

    INSERT INTO [dbo].[customer] ([full_name], [mobile], [birthday], [identity_card], [licence_number], [licence_date], [account_id])
    SELECT (SELECT v FROM @Ho WHERE n = @i % 8) + N' ' + (SELECT v FROM @Dem WHERE n = @i % 5) + N' ' + (SELECT v FROM @Ten WHERE n = @i % 7),
           '09220000' + @Code,
           @Birthday,
           '0012000000' + @Code,
           'B2-1000' + @Code,
           DATEADD(YEAR, 20, @Birthday),
           SCOPE_IDENTITY();

    SET @i = @i + 1;
END

-- 2. Xe: 15 mẫu x 2 màu
DECLARE @Model TABLE (n INT, producer NVARCHAR(100), name NVARCHAR(150), capacity INT, price INT, descr NVARCHAR(200));
INSERT INTO @Model VALUES
    (0,  N'Toyota',     N'Toyota Vios 1.5G',        5, 40, N'Sedan hạng B bền bỉ, tiết kiệm nhiên liệu'),
    (1,  N'Toyota',     N'Toyota Corolla Cross',    5, 65, N'SUV đô thị, gầm cao, nhiều trang bị an toàn'),
    (2,  N'Toyota',     N'Toyota Fortuner',         7, 90, N'SUV 7 chỗ máy dầu, phù hợp đi tỉnh'),
    (3,  N'Honda',      N'Honda City RS',           5, 45, N'Sedan thể thao, khoang lái rộng'),
    (4,  N'Honda',      N'Honda Civic RS',          5, 70, N'Sedan hạng C, vận hành linh hoạt'),
    (5,  N'Hyundai',    N'Hyundai Accent',          5, 38, N'Sedan phổ thông, chi phí thấp'),
    (6,  N'Hyundai',    N'Hyundai Tucson',          5, 75, N'SUV hạng C, nội thất hiện đại'),
    (7,  N'VinFast',    N'VinFast VF5 Plus',        5, 35, N'SUV điện cỡ nhỏ, sạc nhanh'),
    (8,  N'VinFast',    N'VinFast VF6',             5, 60, N'SUV điện hạng B, trợ lý lái'),
    (9,  N'Ford',       N'Ford Ranger Wildtrak',    5, 85, N'Bán tải mạnh mẽ, thùng sau rộng'),
    (10, N'Ford',       N'Ford Territory',          5, 68, N'SUV hạng C, màn hình lớn'),
    (11, N'Kia',        N'Kia Seltos',              5, 55, N'SUV cỡ nhỏ, thiết kế trẻ trung'),
    (12, N'Kia',        N'Kia Carnival',            7, 95, N'MPV cao cấp, cửa trượt điện'),
    (13, N'Mitsubishi', N'Mitsubishi Attrage',      5, 36, N'Sedan nhỏ gọn, tiết kiệm xăng'),
    (14, N'Mitsubishi', N'Mitsubishi Pajero Sport', 7, 88, N'SUV 7 chỗ, dẫn động 4 bánh');
DECLARE @Color TABLE (n INT, v NVARCHAR(50));
INSERT INTO @Color VALUES (0, N'White'), (1, N'Gray');

SET @i = 1;
WHILE @i <= 30
BEGIN
    INSERT INTO [dbo].[car] ([car_name], [car_model_year], [color], [capacity], [description], [import_date], [producer_id], [rent_price], [status])
    SELECT m.name + N' (BK-' + RIGHT('0' + CAST(@i AS VARCHAR(2)), 2) + N')',
           2020 + @i % 5,
           (SELECT v FROM @Color WHERE n = @i % 2),
           m.capacity,
           m.descr,
           DATEADD(DAY, @i * 37, '2021-01-01'),
           p.[producer_id],
           m.price,
           CASE WHEN @i IN (29, 30) THEN N'Inactive' WHEN @i IN (26, 27, 28) THEN N'Rented' ELSE N'Available' END
    FROM @Model m
    JOIN [dbo].[car_producer] p ON p.[producer_name] = m.producer
    WHERE m.n = (@i - 1) % 15;

    SET @i = @i + 1;
END

-- 3. Đơn thuê năm 2025: khách bulk xoay vòng, xe BK-01..BK-25
SET @i = 1;
WHILE @i <= 120
BEGIN
    DECLARE @CustomerId INT = (SELECT [customer_id] FROM [dbo].[customer]
                               WHERE [mobile] = '09220000' + RIGHT('0' + CAST((@i - 1) % 40 + 1 AS VARCHAR(2)), 2));
    DECLARE @CarId INT, @Price DECIMAL(10);
    SELECT @CarId = [car_id], @Price = [rent_price] FROM [dbo].[car]
    WHERE [car_name] LIKE N'% (BK-' + RIGHT('0' + CAST((@i - 1) % 25 + 1 AS VARCHAR(2)), 2) + N')';
    DECLARE @Days INT = @i % 5 + 1;
    DECLARE @Pickup DATE = DATEADD(DAY, @i * 3, '2025-01-01');

    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@CustomerId, @CarId, @Pickup, DATEADD(DAY, @Days, @Pickup), @Price * @Days,
            CASE WHEN @i % 7 = 0 THEN N'Cancelled' ELSE N'Completed' END);

    -- Đánh giá cho khoảng 1/3 số đơn Completed
    IF @i % 3 = 0 AND @i % 7 <> 0
        INSERT INTO [dbo].[review] ([car_rent_id], [review_star], [comment])
        VALUES (SCOPE_IDENTITY(), 5 - @i % 4,
                CASE @i % 4 WHEN 0 THEN N'Xe sạch, chạy êm, giao xe đúng giờ'
                            WHEN 1 THEN N'Xe ổn, thủ tục nhanh gọn'
                            WHEN 2 THEN N'Xe hơi cũ nhưng vẫn chạy tốt'
                            ELSE N'Điều hòa yếu, cần bảo dưỡng thêm' END);

    SET @i = @i + 1;
END

-- 4. Đơn Active cho 3 xe Rented (BK-26..BK-28), khớp trạng thái xe
SET @i = 26;
WHILE @i <= 28
BEGIN
    DECLARE @ActiveCustomerId INT = (SELECT [customer_id] FROM [dbo].[customer]
                                     WHERE [mobile] = '09220000' + RIGHT('0' + CAST(@i - 20 AS VARCHAR(2)), 2));
    DECLARE @ActiveCarId INT, @ActivePrice DECIMAL(10);
    SELECT @ActiveCarId = [car_id], @ActivePrice = [rent_price] FROM [dbo].[car]
    WHERE [car_name] LIKE N'% (BK-' + CAST(@i AS VARCHAR(2)) + N')';

    INSERT INTO [dbo].[car_rental] ([customer_id], [car_id], [pickup_date], [return_date], [rent_price], [status])
    VALUES (@ActiveCustomerId, @ActiveCarId, @Today, DATEADD(DAY, 3, @Today), @ActivePrice * 3, N'Active');

    SET @i = @i + 1;
END
GO
