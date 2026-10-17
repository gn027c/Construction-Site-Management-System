package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Xử lý ký tự UTF-8 BOM ở dòng đầu tiên nếu có
                if (isFirstLine) {
                    if (line.startsWith(UTF8_BOM)) {
                        line = line.substring(1);
                    }
                    isFirstLine = false;
                }
                lines.add(line);
            }
        } catch (Exception e) {
            // Khi gặp lỗi hoặc tệp chưa tồn tại, trả về danh sách rỗng an toàn
            return new ArrayList<>();
        }
        return lines;
    }

    /**
     * Ghi danh sách các dòng văn bản ra tệp CSV kèm tiền tố UTF-8 BOM để Excel hiển thị không lỗi font.
     *
     * @param filePath đường dẫn tệp tin CSV
     * @param lines    danh sách các dòng văn bản cần ghi
     * @return true nếu ghi thành công, false nếu phát sinh lỗi I/O
     */
    public static boolean writeAllLines(String filePath, List<String> lines) {
        if (lines == null) {
            return false;
        }
        try {
            java.io.File parent = new java.io.File(filePath).getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
        } catch (Exception ignored) {
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            
            // Ghi tiền tố UTF-8 BOM ở đầu tệp
            writer.write(UTF8_BOM);
            
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                if (i < lines.size() - 1) {
                    writer.newLine();
                }
            }
            writer.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Bao bọc và thoát ký tự đặc biệt cho một ô dữ liệu CSV nếu chứa dấu phẩy hoặc nháy kép.
     *
     * @param field giá trị chuỗi của ô dữ liệu
     * @return chuỗi đã được chuẩn hóa an toàn theo chuẩn RFC 4180
     */
    public static String escapeSpecialCharacters(String field) {
        if (field == null) {
            return "";
        }
        String escapedField = field;
        
        // Theo chuẩn RFC 4180: Nếu chuỗi chứa dấu nháy kép, thay thế bằng cặp dấu nháy kép ""
        if (escapedField.contains("\"")) {
            escapedField = escapedField.replace("\"", "\"\"");
        }
        
        // Nếu chứa dấu phẩy, nháy kép, hoặc xuống dòng thì bao bọc cả ô bằng dấu nháy kép
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            escapedField = "\"" + escapedField + "\"";
        }
        
        return escapedField;
    }
}
