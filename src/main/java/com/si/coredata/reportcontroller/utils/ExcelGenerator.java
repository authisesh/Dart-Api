package com.si.coredata.reportcontroller.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class ExcelGenerator {

    @Value("${data.normalisechars.tokeep}")
    String normaliseChars;

    @Autowired
    private CoreDataProcessor coreDataProcessor;

    public String generateExcelRecord(String[] dataLines, String tableName, HttpServletResponse response) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(1);
        String status;
        Sheet sheet = workbook.createSheet(tableName);

        String columns = dataLines[0];
        columns = columns.replaceAll("[^\\\\.A-Za-z0-9|]", "");
        columns = columns.replace(".", "_");
        columns = columns.replace("|", ",");
        int columnsCount = columns.split(",").length;
        columns = coreDataProcessor.formatColumns(columns);
        String[] excelColumns = columns.split("\\,");

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < excelColumns.length - 1; i++) { // Assuming 10 columns in the header
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(excelColumns[i]);
        }
        int totalRows = dataLines.length;
        for (int i = 1; i < totalRows; i++) {
            Row dataRow = sheet.createRow(i);
            String array[][] = new String[totalRows - 1][columnsCount];
            String row = (String) dataLines[i];
            String coulmns[] = row.split("\\|");
            for (int j = 0; j < coulmns.length; j++) {
                String value = coulmns[j];
                String Normalizedvalue = value.replaceAll("[^\\\\.A-Za-z0-9 " + normaliseChars + "]", "");
                Cell dataCell = dataRow.createCell(j);
                dataCell.setCellValue(Normalizedvalue);
            }
        }

        //  try (FileOutputStream outputStream = new FileOutputStream("//Users//authis//Tables.xlsx")) {
        // workbook.write(outputStream);
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String contentDisposition = "attachment; filename=" + tableName + ".xlsx";

            response.setHeader("Content-Disposition", contentDisposition);

// Write the SXSSFWorkbook to the response output stream
            workbook.write(response.getOutputStream());
            workbook.dispose();
            response.getOutputStream().flush();
// Close the SXSSFWorkbook and flush the response

            System.out.println("Excel file created successfully!");
            status = "SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
            status = "FAILURE";
        }


        return status;

    }
}
