package company.Backend2;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class Exell {

    public static XSSFCell cell;
    public static void setCell(XSSFRow row, int columnId, String value, XSSFCellStyle style, XSSFColor color) {
        if (color != null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(color);
        }
        cell = row.createCell(columnId);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public static void setCell(XSSFRow row, int columnId, float value, XSSFCellStyle style, XSSFColor color) {
        if (color != null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(color);
        }
        cell = row.createCell(columnId);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public static XSSFCellStyle setStyle(XSSFWorkbook workbook, String fontName) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setCharSet(FontCharset.ARABIC);
        font.setFontName(fontName);
        style.setFont(font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }


}
