USE [FUCarRentingSystemDB]
GO

-- Xóa toàn bộ dữ liệu (giữ nguyên cấu trúc bảng) theo thứ tự khóa ngoại,
-- sau đó reset IDENTITY để ID bắt đầu lại từ 1.
-- Dùng khi muốn chạy lại: reset_data.sql -> seed_data.sql -> seed_scenarios.sql
DELETE FROM [dbo].[review];
DELETE FROM [dbo].[car_rental];
DELETE FROM [dbo].[car];
DELETE FROM [dbo].[car_producer];
DELETE FROM [dbo].[customer];
DELETE FROM [dbo].[account];
GO

DBCC CHECKIDENT ('[dbo].[review]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[car_rental]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[car]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[car_producer]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[customer]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[account]', RESEED, 0);
GO
