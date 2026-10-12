package util;

import java.util.ArrayList;
import java.util.List;

/**
 * TIỆN ÍCH XỬ LÝ TỆP TIN CSV (PERSISTENCE HELPER)
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461) - Nhóm trưởng
 * MÔ TẢ:
 * - Module tiện ích hỗ trợ đọc và ghi tệp tin phẳng CSV theo chuẩn RFC 4180.
 * - Xử lý tiền tố UTF-8 BOM (\uFEFF) để Microsoft Excel trên Windows hiển thị đúng tiếng Việt có dấu.
 * - Thoát ký tự đặc biệt (dấu phẩy, dấu nháy kép, dấu ngắt dòng) khi xuất dòng CSV.
 * - Clean Skeleton Stub cho Workshop 5.
 */
public class CsvHelper {

    /**
     * Tiền tố Byte Order Mark (UTF-8 BOM) để Excel tự động nhận diện bảng mã UTF-8.
     */
    public static final String UTF8_BOM = "\uFEFF";

    /**
     * Đọc toàn bộ các dòng văn bản từ tệp CSV, tự động loại bỏ ký tự UTF-8 BOM ở dòng đầu nếu có.
     *
     * @param filePath đường dẫn tệp tin CSV
     * @return danh sách các dòng văn bản đọc được từ tệp
     */
    public static List<String> readAllLines(String filePath) {
        // TODO: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461) cài đặt đọc tệp và xử lý UTF-8 BOM (Workshop 5)
        return new ArrayList<>();
    }

    /**
     * Ghi danh sách các dòng văn bản ra tệp CSV kèm tiền tố UTF-8 BOM để Excel hiển thị không lỗi font.
     *
     * @param filePath đường dẫn tệp tin CSV
     * @param lines    danh sách các dòng văn bản cần ghi
     * @return true nếu ghi thành công, false nếu phát sinh lỗi I/O
     */
    public static boolean writeAllLines(String filePath, List<String> lines) {
        // TODO: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461) cài đặt ghi tệp kèm UTF-8 BOM (Workshop 5)
        return false;
    }

    /**
     * Bao bọc và thoát ký tự đặc biệt cho một ô dữ liệu CSV nếu chứa dấu phẩy hoặc nháy kép.
     *
     * @param field giá trị chuỗi của ô dữ liệu
     * @return chuỗi đã được chuẩn hóa an toàn theo chuẩn RFC 4180
     */
    public static String escapeSpecialCharacters(String field) {
        // TODO: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461) cài đặt xử lý escape chuỗi CSV (Workshop 5)
        return field != null ? field : "";
    }
}
