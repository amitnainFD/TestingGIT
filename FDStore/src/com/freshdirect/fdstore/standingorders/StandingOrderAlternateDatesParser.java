package com.freshdirect.fdstore.standingorders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Category;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.freshdirect.framework.util.log.LoggerFactory;

public class StandingOrderAlternateDatesParser {

	private static Category LOGGER = LoggerFactory.getInstance(StandingOrderAlternateDatesParser.class);
	public StandingOrderAlternateDatesParser() {
		super();
		this.exceptionList = new ArrayList<String>();
	}

	private List<String> exceptionList = null;
	
	public List<FDStandingOrderAltDeliveryDate> parseFile(File file) {
		List<FDStandingOrderAltDeliveryDate> list = null;
		try{
			POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(FileUtils.readFileToByteArray(file)));
			HSSFWorkbook workbook = new HSSFWorkbook(fs);
			HSSFDataFormatter formatter = new HSSFDataFormatter();
			formatter.setDefaultNumberFormat(new DecimalFormat());
			HSSFSheet sheet = workbook.getSheetAt(0);
			
			int rowCount = sheet.getLastRowNum() + 1;

			Row firstRow = sheet.getRow(2);
			if (firstRow == null) {
				this.exceptionList.add("Header row not found (maybe empty)");
			}
			list = new ArrayList<FDStandingOrderAltDeliveryDate>();
			for (int iCurrentRow = 2; iCurrentRow < rowCount; iCurrentRow++) {
				FDStandingOrderAltDeliveryDate altDate = new FDStandingOrderAltDeliveryDate();
				HSSFRow row = sheet.getRow(iCurrentRow);					
				if(row == null)	{
					this.exceptionList.add("Row #"+ row.getRowNum() + " is empty");
				} else {						
					int cellIndex = 0;
					short noOfCells = 9;//row.getLastCellNum();//PhysicalNumberOfCells();	
					String[] cellValues = new String[noOfCells]; 
					short firstCellNum = row.getFirstCellNum();
					short lastCellNum = row.getLastCellNum()<=noOfCells?row.getLastCellNum():noOfCells;

					if (firstCellNum >= 0 && lastCellNum >= 0) {
						for (short iCurrent = firstCellNum; iCurrent < lastCellNum; iCurrent++) {
							Cell cell = row.getCell(iCurrent);
							if (cell == null || Cell.CELL_TYPE_BLANK ==cell.getCellType()) {
								if(iCurrent ==0 || (iCurrent == 1 && cellValues[8]!=null) || iCurrent == 8){
									this.exceptionList.add("Empty data found at Row #"+ (row.getRowNum()+1) + ", Cell #"+ (iCurrent +1)+" . It can't be empty.");
								}
								cellIndex++;
								continue;
							} else {									
								cellValues[cellIndex] = getCellStringValue(cell, cell.getCellType());
 								cellIndex++;
							}
						}
						if(null != cellValues[0]){
							altDate.setOrigDate(HSSFDateUtil.getJavaDate(Double.parseDouble(cellValues[0])));
						}
						if(null != cellValues[1]){
							altDate.setAltDate(HSSFDateUtil.getJavaDate(Double.parseDouble(cellValues[1])));
						}
						altDate.setSoId(cellValues[2]);
						altDate.setDescription(cellValues[3]);
						if(null != cellValues[4]){
							altDate.setOrigStartTime(HSSFDateUtil.getJavaDate(Double.parseDouble(cellValues[4])));
						}
						if(null != cellValues[5]){
							altDate.setOrigEndTime(HSSFDateUtil.getJavaDate(Double.parseDouble(cellValues[5])));
						}
						if(null != cellValues[6]){
							altDate.setAltStartTime(HSSFDateUtil.getJavaDate(Double.parseDouble(cellValues[6])));
						}
						if(null != cellValues[7]){
							altDate.setAltEndTime(HSSFDateUtil.getJavaDate(Double.parseDouble(cellValues[7])));
						}
						altDate.setActionType(cellValues[8]);
						list.add(altDate);	
						}
					
					}	
				
				exceptionList =FDStandingOrderAlternateDateUtil.validate(altDate, exceptionList,row.getRowNum()+1);
				} 
		}catch(Exception e){
			LOGGER.error("Error while uploading: "+e);
			exceptionList.add("Error while uploading: "+e);
		}
		exceptionList =FDStandingOrderAlternateDateUtil.validate(list, exceptionList);
		return list;
	}
	
	private String getCellStringValue(Cell cell, int type) {
		String stringValue = "";
		switch (type) {
		case Cell.CELL_TYPE_BLANK:
			break;
		case Cell.CELL_TYPE_STRING:
			stringValue = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			stringValue = Boolean.toString(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			double value = cell.getNumericCellValue();
			if (HSSFDateUtil.isCellDateFormatted(cell))	{
				if (HSSFDateUtil.isValidExcelDate(value)) {																		
					stringValue = Double.toString(cell.getNumericCellValue());
				} else {
					this.exceptionList.add("Invalid Date value found at row # "+ (cell.getRow().getRowNum()+1)	+ " and column # "+ (cell.getColumnIndex()+1));
				}
			} else {
				stringValue = Integer.toString((int)cell.getNumericCellValue());
			}			
			break;
		case Cell.CELL_TYPE_ERROR:
			this.exceptionList.add("Error in row " + (cell.getRow().getRowNum()+1) + " at cell " + (cell.getColumnIndex()+1)+ ": error type: " + Byte.toString(cell.getErrorCellValue()));
			break;
		default:
			this.exceptionList.add(("Error in row " + (cell.getRow().getRowNum()+1) + " at cell " + (cell.getColumnIndex()+1)+ ": unknown data type: " + cell.getCellType() + ", upgrade POI"));
		}
		return stringValue;
	}

	public List<String> getExceptionList() {
		return exceptionList;
	}
	
	public boolean isParseSuccessful(){
		return (null == this.getExceptionList() || this.getExceptionList().isEmpty());
	}
	

}
	