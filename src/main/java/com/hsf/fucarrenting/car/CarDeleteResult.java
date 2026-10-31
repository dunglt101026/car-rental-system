package com.hsf.fucarrenting.car;

public record CarDeleteResult(
        boolean hardDeleted,
        String carName,
        Integer carId
) {
    public String getDisplayMessage() {
        if (hardDeleted) {
            return "Đã xóa vĩnh viễn xe \"" + carName + "\" (Mã xe: #" + carId + ") khỏi hệ thống cơ sở dữ liệu.";
        } else {
            return "Xe \"" + carName + "\" (Mã xe: #" + carId + ") đã từng có đơn thuê trong hệ thống nên không thể xóa vật lý. Hệ thống đã chuyển trạng thái xe sang Vô hiệu hóa (Inactive) theo quy định.";
        }
    }
}

