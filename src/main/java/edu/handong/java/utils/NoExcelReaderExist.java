package edu.handong.java.utils;

public class NoExcelReaderExist extends Exception {
	public NoExcelReaderExist(String fileName)
	{
		super("In zipfile: " + fileName + ", Dont exist Excel file!");
	}
}