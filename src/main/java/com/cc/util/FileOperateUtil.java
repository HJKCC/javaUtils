package com.cc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件操作类
 * 
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午5:19:35
 */
public class FileOperateUtil {
	private final static Logger logger = LoggerFactory.getLogger(FileOperateUtil.class);

	public static String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!=null){
                result.append(System.lineSeparator()+s);
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }
	
	public static void WriteStringToFile(String filePath,String var) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            String s = var+"\n";
            fos.write(s.getBytes());
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
