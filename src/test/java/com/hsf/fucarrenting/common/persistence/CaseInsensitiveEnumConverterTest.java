package com.hsf.fucarrenting.common.persistence;

import com.hsf.fucarrenting.car.CarStatus;
import com.hsf.fucarrenting.car.CarStatusConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseInsensitiveEnumConverterTest {

    private final CarStatusConverter converter = new CarStatusConverter();

    @Test
    @DisplayName("Đọc giá trị DB không phân biệt hoa thường và bỏ khoảng trắng thừa")
    void testReadIsCaseInsensitive() {
        assertThat(converter.convertToEntityAttribute("Available")).isEqualTo(CarStatus.Available);
        assertThat(converter.convertToEntityAttribute("available")).isEqualTo(CarStatus.Available);
        assertThat(converter.convertToEntityAttribute(" RENTED ")).isEqualTo(CarStatus.Rented);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("Ghi xuống DB luôn dùng đúng tên enum")
    void testWriteUsesEnumName() {
        assertThat(converter.convertToDatabaseColumn(CarStatus.Inactive)).isEqualTo("Inactive");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("Giá trị hoàn toàn không tồn tại -> báo lỗi rõ ràng")
    void testUnknownValueThrows() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Broken"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Broken");
    }
}
