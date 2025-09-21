package com.byrski.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Excel工具类
 * 
 * @author ByrSki
 * @since 2024-01-01
 */
@Slf4j
@Component
public class ExcelUtils {

    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 验证是否为有效的Excel文件
     * 
     * @param fileName 文件名
     * @return 是否为有效Excel文件
     */
    public boolean isValidExcelFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".xlsx") || lowerFileName.endsWith(".xls");
    }

    /**
     * 从Base64编码的Excel文件中解析手机号
     * 
     * @param base64Data Base64编码的Excel文件数据
     * @param phoneColumnName 手机号列名
     * @return 手机号列表
     * @throws IOException 文件读取异常
     */
    public List<String> parsePhoneNumbersFromBase64(String base64Data, String phoneColumnName) throws IOException {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new IllegalArgumentException("Excel文件数据不能为空");
        }

        // 解码Base64数据
        byte[] excelData = java.util.Base64.getDecoder().decode(base64Data);
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelData)) {
            return parsePhoneNumbersFromStream(bis, phoneColumnName);
        }
    }

    /**
     * 从输入流中解析手机号
     * 
     * @param inputStream Excel文件输入流
     * @param phoneColumnName 手机号列名
     * @return 手机号列表
     * @throws IOException 文件读取异常
     */
    private List<String> parsePhoneNumbersFromStream(java.io.InputStream inputStream, String phoneColumnName) throws IOException {
        List<String> phoneNumbers = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            
            // 查找手机号列
            int phoneColumnIndex = findPhoneColumnIndex(sheet, phoneColumnName);
            if (phoneColumnIndex == -1) {
                log.warn("未找到手机号列: {}", phoneColumnName);
                return phoneNumbers;
            }
            
            // 从第二行开始读取数据（第一行是标题）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                Cell cell = row.getCell(phoneColumnIndex);
                if (cell == null) {
                    continue;
                }
                
                String phoneNumber = getCellValueAsString(cell);
                if (isValidPhoneNumber(phoneNumber)) {
                    phoneNumbers.add(phoneNumber);
                } else {
                    log.warn("第{}行手机号格式无效: {}", rowIndex + 1, phoneNumber);
                }
            }
        }
        
        log.info("从Excel文件中解析到{}个有效手机号", phoneNumbers.size());
        return phoneNumbers;
    }

    /**
     * 查找手机号列的索引
     * 
     * @param sheet 工作表
     * @param phoneColumnName 手机号列名
     * @return 列索引，未找到返回-1
     */
    private int findPhoneColumnIndex(Sheet sheet, String phoneColumnName) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return -1;
        }
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String cellValue = getCellValueAsString(cell);
                if (phoneColumnName.equals(cellValue)) {
                    return i;
                }
            }
        }
        
        return -1;
    }

    /**
     * 获取单元格的字符串值
     * 
     * @param cell 单元格
     * @return 字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 处理数字格式的手机号
                double numericValue = cell.getNumericCellValue();
                return String.valueOf((long) numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 验证手机号格式
     * 
     * @param phoneNumber 手机号
     * @return 是否为有效手机号
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }
}
