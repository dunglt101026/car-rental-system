# FU Car Renting System

Website quản lý cho thuê xe, làm theo Assignment 01 môn HSF302 (Building Websites Using Spring Boot and Spring Data JPA).

## Công nghệ

- Java 21, Spring Boot 4.1.2 (Spring MVC, Spring Data JPA, Bean Validation)
- Thymeleaf + Thymeleaf Layout Dialect, Bootstrap 5; giao diện render phía server, không có JavaScript tự viết (chỉ dùng hộp `confirm()` của trình duyệt khi xóa/hủy)
- SQL Server, driver `mssql-jdbc`
- Lombok, MapStruct, BCrypt (`spring-security-crypto`) để mã hóa mật khẩu

## Chức năng

**Khách vãng lai**
- Xem danh mục xe đang có sẵn, lọc theo hãng, số chỗ, khoảng giá, từ khóa; xem chi tiết xe.
- Đăng ký tài khoản khách hàng.

**Khách hàng (Customer)**
- Đặt thuê một hoặc nhiều xe cùng lúc; mỗi xe là một đơn thuê riêng. Bấm "Tính chi phí" để xem tổng tiền trước khi xác nhận.
- Xem lịch sử đơn thuê; đánh giá (1-5 sao) đơn đã hoàn thành.
- Cập nhật hồ sơ cá nhân.

**Quản trị (Admin)**
- Quản lý xe: thêm, sửa, xóa. Xe chưa từng có đơn thuê thì xóa hẳn; xe đã có đơn thuê thì chỉ chuyển sang `Inactive`.
- Quản lý khách hàng: thêm, sửa, xóa (không xóa được khách đã có đơn thuê).
- Quản lý đơn thuê: xác nhận khách đã trả xe (`Completed`) hoặc hủy đơn (`Cancelled`); xe tự chuyển về `Available`.
- Báo cáo doanh thu theo khoảng ngày, sắp xếp giảm dần theo ngày nhận xe hoặc theo doanh thu (không tính đơn đã hủy).

## Quy tắc nghiệp vụ chính

- Đơn thuê: ngày trả phải sau ngày nhận; đặt trước tối đa 90 ngày; mỗi đơn tối đa 30 ngày. Chỉ đặt được xe đang `Available`; nếu một xe không khả dụng thì không tạo đơn nào.
- Trạng thái đơn: chỉ `Active` mới chuyển sang `Completed` hoặc `Cancelled`; chưa đến ngày nhận xe thì chỉ được hủy.
- Khách hàng: phải đủ 18 tuổi; ngày cấp GPLX sau ngày sinh ít nhất 18 năm; không trùng SĐT, CCCD, tên đăng nhập, email.
- Xe: năm sản xuất từ 1990 đến năm sau; ngày nhập kho không ở tương lai và không trước năm sản xuất; giá thuê là số nguyên (khớp cột `decimal(10)`).

## Cấu trúc mã nguồn

Mỗi chức năng là một package, bên trong chia theo tầng (controller → service → repository):

```
com.hsf.fucarrenting
├── auth       đăng nhập, đăng ký, tài khoản
├── car        xe, hãng xe
├── customer   khách hàng, hồ sơ
├── rental     đơn thuê, đặt xe
├── review     đánh giá
├── report     báo cáo doanh thu
├── home       trang chủ
└── common     cấu hình, phân quyền (AuthInterceptor), xử lý lỗi, validation dùng chung
```

Mỗi package con gồm `controller/ dto/ entity/ mapper/ repository/ service/` (service tách interface và `Impl`).
Giao diện nằm trong `src/main/resources/templates` (`layouts/` cho bố cục chung, `admin/` cho trang quản trị).

## Cài đặt và chạy

### 1. Tạo cơ sở dữ liệu

Chạy lần lượt trong SSMS (các file nằm trong `src/main/resources/db/`):

1. `01_create_database.sql`: tạo database `FUCarRentingSystemDB` và các bảng.
2. `02_seed_data.sql`: dữ liệu mẫu (tài khoản, hãng xe, xe, một đơn thuê).

### 2. Cấu hình kết nối

Thông tin kết nối nằm trong `src/main/resources/application.properties` (mặc định `localhost:1433`, user `sa`).
Mật khẩu không commit lên repo: tạo file `src/main/resources/application-local.properties` (đã có trong `.gitignore`) theo mẫu `application-local.properties.example`:

```properties
spring.datasource.password=mat_khau_sql_server_cua_ban
```

### 3. Chạy ứng dụng

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Hoặc mở thư mục project bằng IntelliJ IDEA và chạy `FUCarRentingApplication`.

Mở trình duyệt tại http://localhost:8082

### Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản trị | `admin` | `admin123` |
| Khách hàng | `customer1` | `123456` |
| Khách hàng | `customer2` | `123456` |

## Kiểm thử

```bash
./mvnw clean test
```

Gồm unit test cho service, validation và controller (MockMvc). Test `FUCarRentingApplicationTests` cần kết nối được SQL Server.
