IF DB_ID(N'FUCarRentingSystemDB') IS NULL
BEGIN
    CREATE DATABASE [FUCarRentingSystemDB]
END
GO

USE [FUCarRentingSystemDB]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

/****** Object:  Table [dbo].[account] ******/
CREATE TABLE [dbo].[account](
    [account_id] [int] IDENTITY(1,1) NOT NULL,
    [account_name] [nvarchar](100) NOT NULL,
    [email] [varchar](200) NOT NULL,
    [password] [varchar](200) NOT NULL,
    [role] [nvarchar](10) NOT NULL,
    CONSTRAINT [PK_account] PRIMARY KEY CLUSTERED ([account_id] ASC)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[car_producer] ******/
CREATE TABLE [dbo].[car_producer](
    [producer_id] [int] IDENTITY(1,1) NOT NULL,
    [producer_name] [nvarchar](100) NOT NULL,
    [address] [nvarchar](200) NOT NULL,
    [country] [nvarchar](100) NOT NULL,
    CONSTRAINT [PK_car_producer] PRIMARY KEY CLUSTERED ([producer_id] ASC)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[car] ******/
CREATE TABLE [dbo].[car](
    [car_id] [int] IDENTITY(1,1) NOT NULL,
    [car_name] [nvarchar](200) NOT NULL,
    [car_model_year] [int] NOT NULL,
    [color] [nvarchar](50) NOT NULL,
    [capacity] [int] NOT NULL,
    [description] [nvarchar](1000) NOT NULL,
    [import_date] [date] NOT NULL,
    [producer_id] [int] NOT NULL,
    [rent_price] [decimal](10) NOT NULL,
    [status] [nvarchar](10) NOT NULL,
    CONSTRAINT [PK_car] PRIMARY KEY CLUSTERED ([car_id] ASC)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[customer] ******/
CREATE TABLE [dbo].[customer](
    [customer_id] [int] IDENTITY(1,1) NOT NULL,
    [full_name] [nvarchar](200) NOT NULL,
    [mobile] [varchar](15) NOT NULL,
    [birthday] [date] NOT NULL,
    [identity_card] [varchar](20) NOT NULL,
    [licence_number] [varchar](20) NOT NULL,
    [licence_date] [date] NOT NULL,
    [account_id] [int] NOT NULL,
    CONSTRAINT [PK_customer] PRIMARY KEY CLUSTERED ([customer_id] ASC)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[car_rental] ******/
CREATE TABLE [dbo].[car_rental](
    [car_rent_id] [int] IDENTITY(1,1) NOT NULL,
    [customer_id] [int] NOT NULL,
    [car_id] [int] NOT NULL,
    [pickup_date] [date] NOT NULL,
    [return_date] [date] NOT NULL,
    [rent_price] [decimal](10) NOT NULL,
    [status] [nvarchar](10) NOT NULL,
    CONSTRAINT [PK_car_rental] PRIMARY KEY CLUSTERED ([car_rent_id] ASC)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[review] ******/
CREATE TABLE [dbo].[review](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [car_rent_id] [int] NOT NULL,
    [review_star] [int] NOT NULL,
    [comment] [nvarchar](500) NOT NULL,
    CONSTRAINT [PK_review] PRIMARY KEY CLUSTERED ([id] ASC)
) ON [PRIMARY]
GO

/****** Add constraints and foreign keys to car ******/
ALTER TABLE [dbo].[car] WITH CHECK ADD CONSTRAINT [FK_car_car_producer] FOREIGN KEY([producer_id])
REFERENCES [dbo].[car_producer] ([producer_id])
GO
ALTER TABLE [dbo].[car] CHECK CONSTRAINT [FK_car_car_producer]
GO

/****** Add constraints and foreign keys to customer ******/
ALTER TABLE [dbo].[customer] WITH CHECK ADD CONSTRAINT [FK_customer_account] FOREIGN KEY([account_id])
REFERENCES [dbo].[account] ([account_id])
GO
ALTER TABLE [dbo].[customer] CHECK CONSTRAINT [FK_customer_account]
GO

/****** Add constraints and foreign keys to car_rental ******/
ALTER TABLE [dbo].[car_rental] WITH CHECK ADD CONSTRAINT [FK_car_rental_car] FOREIGN KEY([car_id])
REFERENCES [dbo].[car] ([car_id])
GO

ALTER TABLE [dbo].[car_rental] CHECK CONSTRAINT [FK_car_rental_car]
GO

ALTER TABLE [dbo].[car_rental] WITH CHECK ADD CONSTRAINT [FK_car_rental_customer] FOREIGN KEY([customer_id])
REFERENCES [dbo].[customer] ([customer_id])
GO

ALTER TABLE [dbo].[car_rental] CHECK CONSTRAINT [FK_car_rental_customer]
GO

ALTER TABLE [dbo].[car_rental] WITH CHECK ADD CONSTRAINT [CHK_car_rental_return_date]
CHECK ([return_date] > [pickup_date])
GO

ALTER TABLE [dbo].[car_rental] CHECK CONSTRAINT [CHK_car_rental_return_date]
GO

/****** Add constraints and foreign keys to review ******/
ALTER TABLE [dbo].[review] WITH CHECK ADD CONSTRAINT [FK_review_car_rental] FOREIGN KEY([car_rent_id])
REFERENCES [dbo].[car_rental] ([car_rent_id])
GO

ALTER TABLE [dbo].[review] CHECK CONSTRAINT [FK_review_car_rental]
GO

ALTER TABLE [dbo].[review] WITH CHECK ADD CONSTRAINT [CHK_review_star]
CHECK ([review_star] >= 1 AND [review_star] <= 5)
GO

ALTER TABLE [dbo].[review] CHECK CONSTRAINT [CHK_review_star]
GO