package edu.handong.java.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

public class Util {
	public static String getExtension(String path){
		return path.substring(path.lastIndexOf(".")+1);
	}
	public static String getPathNoExt(String path){
		return path.substring(0, path.lastIndexOf("."));
	}
	
	/**
	 * source: https://javafreak.tistory.com/226
	 * 	       https://stackoverflow.com/questions/15667125/read-content-from-files-which-are-inside-zip-file
	 * 		   https://madplay.github.io/post/java-file-zip
	 * 		   https://dantes.kr/100
	 * @param dir
	 * @throws IOException
	 */
	private static void ensureDestDir(File dir) throws IOException{
		if( ! dir.exists()) {
			dir.mkdirs(); 
		}
	}
	
	/**
	 * source : https://javafreak.tistory.com/226
	 *  	    https://stackoverflow.com/questions/15667125/read-content-from-files-which-are-inside-zip-file
	 * @param zip
	 * @param destDir
	 * @throws IOException
	 */
	public static void unzip(File zip, File destDir) throws IOException{
		InputStream is = new FileInputStream(zip);
		String encoding = Charset.defaultCharset().name();
		
		ZipArchiveInputStream zis ;
		ZipArchiveEntry entry ;
		String name ;
		File target ;
		
		int nWritten = 0;
		BufferedOutputStream bos ;
		byte []buf = new byte[1024 * 8];

		ensureDestDir(destDir);
		
		zis = new ZipArchiveInputStream(is, encoding, false);
		
		while ((entry = zis.getNextZipEntry()) != null ){
			name = entry.getName();
			target = new File (destDir, name);
			
			if( entry.isDirectory() ){
				ensureDestDir(target);
			}else{
				target.createNewFile();
				bos = new BufferedOutputStream(new FileOutputStream(target));
				
				while ((nWritten = zis.read(buf)) >= 0 ){
					bos.write(buf, 0, nWritten);
				}
				bos.close();
			}
		}
		zis.close();
	}
	
}