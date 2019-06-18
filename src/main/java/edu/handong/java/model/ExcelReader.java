package edu.handong.java.model;

import java.io.FileNotFoundException; 
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.text.DateFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.DateFormatConverter;

public class ExcelReader {
	private ArrayList<String> rawData;
	
	public ExcelReader(InputStream in, int cellCount){
		rawData = new ArrayList<String>();
		
		try {
		    Workbook wb = WorkbookFactory.create(in);
		    Sheet sheet = wb.getSheetAt(0);
    		DataFormatter formatter = new DataFormatter();
    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
		
		    Iterator<Row> it = sheet.rowIterator();
		    boolean headerJump = false;
		    
		    while (it.hasNext()){
		    	Row row = it.next();
		    	if ((int)row.getLastCellNum() != cellCount){
		    		continue;
		    	}if (!headerJump){
		    		headerJump = true;
		    		continue;
		    	}
		    	
		    	String rowString = "";
		    	
		    	for(int cn=0; cn<row.getLastCellNum(); cn++){
		    		Cell cell = row.getCell(cn);
		    		
		    		String value = "";
		    		
		    		if (cell != null){
						if (cell.getCellType() == CellType.NUMERIC){ // 누머릭 타입 검사
							if (DateUtil.isCellDateFormatted(cell))		// 날짜 검사 
								value += dateFormat.format(cell.getDateCellValue());
							else{
								value += formatter.formatCellValue(cell);
								if (Double.parseDouble(value) > 1900.0){	// 고의 날짜 변
									value = dateFormat.format(cell.getDateCellValue());
								}
							}
						}
						else{
						    value += formatter.formatCellValue(cell);
						}
			            value = value.replaceAll("\n", "");
			            value = value.replaceAll("\"", "\"\"");
			            value = value.replaceAll("\'", "\'\'");
		    		}
			        rowString += ", \"" + value + "\"";
			    }
		    	rawData.add(rowString);
		    }
		    
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}public ArrayList<String> getRawData(){
		return rawData;
	}
}
