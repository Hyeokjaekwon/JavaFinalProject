package edu.handong.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.handong.java.model.ExcelZip;
import edu.handong.java.utils.Util;

public class Collection {
	
	private ArrayList<ExcelZip> zips;
	
	public void run(String args[])
	{
		CommandLineParser parser = new DefaultParser();

		Options options = createOptions();
		
		String rootDirectory = "";
		String resultPath = "";
		
		try{
			CommandLine cmd = parser.parse(options, args);
			
			File file = new File(cmd.getOptionValue("i"));
			resultPath = cmd.getOptionValue("o");
			
			String fileName = file.getName();
			String fileFullPath = file.getCanonicalPath();
			
			if (file.isFile() && Util.getExtension(fileName).equals("zip")) {// if zip file Decompress 
				// unzip.
				Util.unzip(file, file.getParentFile());

				rootDirectory = Util.getPathNoExt(fileFullPath);
				//System.out.println(rootDirectory);
			}else if (file.isDirectory()) {
				rootDirectory = fileFullPath;
			}else{
				throw new Exception("Error! can't resolve " + fileName);
			}
		}catch (ParseException e) {
			printHelp(options);
			System.exit(-1);
		}catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		// Unzip all the zip files in the current folder and load the file in Excel By thread
		File rootDir = new File(rootDirectory);
		
		File[] directoryListing = rootDir.listFiles();
		
		zips = new ArrayList<ExcelZip>();
		
	    for(File child : directoryListing){  // Create a ZipFile object with the compressed files under the directory.
	    	if (Util.getExtension(child.getName()).equals("zip")){
	    		try {
					zips.add(new ExcelZip(child.getCanonicalPath()));
				}catch (IOException e){
					e.printStackTrace();
				}
	    	}
	    }
	    
	    zips.sort(new Comparator<ExcelZip>(){				// Sort by file name
			
	    	@Override
			public int compare(ExcelZip object1, ExcelZip object2) {
				String s1 = object1.getFile().getName();
				String s2 = object2.getFile().getName();
				
				return s1.compareTo(s2);
			}
        });
		
		ArrayList<Thread> threadsForZipFile = new ArrayList<Thread>();
		
		for(ExcelZip runner:zips) {		// All threads started
			
			Thread thread = new Thread(runner);
			//System.out.println(runner.getFile().getName());
			thread.start();
			threadsForZipFile.add(thread);
		}
		// wait.
		try {
			for(Thread runner:threadsForZipFile) {
				runner.join();
			}
		}catch (InterruptedException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		// combine
		saveCSV(resultPath);
	}
	
	public void saveCSV(String targetFile){
		
		Path path = Paths.get(targetFile);
		File parentDir = path.toFile().getParentFile();
		if (!parentDir.exists()){
			try{
				parentDir.mkdirs();
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		try {
			PrintWriter pw1 = new PrintWriter(new FileOutputStream(Util.getPathNoExt(targetFile) + 1 + ".csv"));
			PrintWriter pw2 = new PrintWriter(new FileOutputStream(Util.getPathNoExt(targetFile) + 2 + ".csv"));
			//pw.println("StudentID, TotalNumberOfSemestersRegistered, Semester, NumCoursesTakenInTheSemester");
			
			pw1.println("zip number, 제목, 요약문 (300자 내외), \"핵심어 (keyword,쉽표로 구분)\", 조회날짜, 실제자료조회 출처 (웹자료링크), 원출처 (기관명 등), 제작자 (Copyright 소유처)");
			pw2.println("zip number, 제목(반드시 요약문 양식에 입력한 제목과 같아야 함.), 표/그림 일련번호, \"자료유형(표,그림,…)\", 자료에 나온 표나 그림 설명(캡션), 자료가 나온 쪽번호");
			
			for(ExcelZip zipfile:zips){
				for (String cell:zipfile.getExcelFiles().get(0).getRawData()){
					pw1.print(Util.getPathNoExt(zipfile.getFile().getName()));
					pw1.println(cell);
				}for (String cell:zipfile.getExcelFiles().get(1).getRawData()){
					pw2.print(Util.getPathNoExt(zipfile.getFile().getName()));
					pw2.println(cell);
				}
			}
			pw1.close();
			pw2.close();
		}catch(Exception{
			System.out.println(e.getMessage());
		}
	}
	
	private Options createOptions(){
		Options options = new Options();

		options.addOption(Option.builder("i").longOpt("input")
				.desc("Set an input file path. zip file or directory.")
				.hasArg()
				.argName("Input path")
				.required()
				.build());
		
		options.addOption(Option.builder("o").longOpt("output")
				.desc("Set an input file path. csv file.")
				.hasArg()
				.argName("Output path")
				.required()
				.build());

		options.addOption(Option.builder("h").longOpt("help") // add options by using OptionBuilder
		        .desc("Help")
		        .build());
		
		return options;
	}

	private void printHelp(Options options) {// automatically generate the help statement
		
		HelpFormatter formatter = new HelpFormatter();
		String header = "Java Final HW: 통일한국개론 수집 데이터 합치는 프로그램";
		String footer = "";
		formatter.printHelp("HGUCourseCounter", header, options, footer, true);
	}
}
