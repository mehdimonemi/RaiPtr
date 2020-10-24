package company.backend1;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;


/**
 * Created by Monemi_M on 05/05/2018.
 */
public class ExcelSetValue {
    public static XSSFCell cell;

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

    public static void setCell(XSSFSheet sheet, XSSFRow row, int columnId, String value, XSSFCellStyle style, XSSFColor color) {
        if (color!=null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(color);
        }
        cell = row.createCell(columnId);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public static void setCell(XSSFSheet sheet, XSSFRow row, int columnId, Double value, XSSFCellStyle style, XSSFColor color) {
        if (color!=null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(color);
        }
        cell = row.createCell(columnId);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public static void sumColumn(XSSFRow row, int columnId, int firstRow, int SecondRow, String operator, CellStyle style) {
        cell = row.createCell(columnId);
        cell.setCellType(CellType.FORMULA);
        cell.setCellFormula(operator + "(" + CellReference.convertNumToColString(cell.getColumnIndex()).toString() + firstRow + ":"
                + CellReference.convertNumToColString(cell.getColumnIndex()) + SecondRow + ")");
        cell.setCellStyle(style);
    }
}
