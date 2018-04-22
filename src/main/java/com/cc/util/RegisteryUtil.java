package com.cc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.*;  

/**
 * 注册表工具类:
 * 1、利用Preferences类 设置或读取指定路径下的注册表。
 *    在windows中：
 *    Preferences.userNodeForPackage代表得到 HKEY_CURRENT_USER\Software\JavaSoft\Prefs下的相对路径
 *    Preferences.systemNodeForPackage代表得到   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs下的相对路径
 * 2、利用Process类 调用命令行指令 操作任意指定路径下的注册表。
 * 
 * @author chencheng0816@gmail.com 
 * @date 2018年4月22日 下午2:38:20
 */
public class RegisteryUtil { 	
	/**
     * 写 HKEY_LOCAL_MACHINE\Software\JavaSoft\Prefs\javaplayer 下的注册表
     *
	 * @param key 属性名
	 * @param value 属性值
	 * @return
	 */
    private void setMachineReg(String key, String value) {  
        Preferences preferences = Preferences.systemRoot().node("/javaplayer");
        preferences.put(key, value); 
    }  
    
    /**
     * 读 HKEY_LOCAL_MACHINE\Software\JavaSoft\Prefs 下的注册表
     *
     * @param key 属性名
     * @return
     */
    private void getMachineReg(String key)
    {
       Preferences preferences = Preferences.systemRoot().node("/javaplayer");
       System.out.println(preferences.get(key,""));
    }
    
    /**
     * 写 HKEY_CURRENT_USER\Software\JavaSoft\Prefs 下的注册表
     *
	 * @param key 属性名
	 * @param value 属性值
	 * @return
	 */
    private void setUserReg(String key, String value)
    {
       Preferences preferences = Preferences.userNodeForPackage(getClass());
       preferences.put(key, value);
    }

    /**
     * 读 HKEY_CURRENT_USER\Software\JavaSoft\Prefs 下的注册表
     *
     * @param key 属性名
     * @return 返回注册表属性值
     */
    private void getUserReg(String key)
    {
       Preferences preferences = Preferences.userNodeForPackage(getClass());
       System.out.println(preferences.get(key,""));
    }
    
    /**
     * 操作任意路径下注册表值
     * 
     * @param regQuery cmd命令，String
     */
    private void invokeCMD(String regQuery) {
        try {    
            Process ps = null;    
            ps = Runtime.getRuntime().exec(regQuery);    
            ps.getOutputStream().close();    
            InputStreamReader i = new InputStreamReader(ps.getInputStream());    
            String line;    
            BufferedReader ir = new BufferedReader(i);
            while ((line = ir.readLine()) != null) {    
                System.out.println(line);    
            }    
        } catch (IOException e) {    
            e.printStackTrace();    
        }  
    }
    
    public static void main(String[] args) {  
        RegisteryUtil registeryUtil = new RegisteryUtil();  
//        registery.getReg("reg query HKEY_CURRENT_USER\\Software\\JavaSoft\\Prefs");
//        registery.getReg("reg query HKEY_LOCAL_MACHINE\\Software\\JavaSoft\\Prefs\\javaplayer");
        registeryUtil.invokeCMD("reg add HKEY_CLASSES_ROOT\\spice\\shell\\open\\command\\test /v testAdd /d testAdd");
//        registery.getUserReg("aa");
//        registery.setUserReg("aa", "bb");
//        registery.setMachineReg("aa", "bb");
    }  
}