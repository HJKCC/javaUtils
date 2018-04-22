package com.cc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.*;  

/**
 * ע�������:
 * 1������Preferences�� ���û��ȡָ��·���µ�ע���
 *    ��windows�У�
 *    Preferences.userNodeForPackage����õ� HKEY_CURRENT_USER\Software\JavaSoft\Prefs�µ����·��
 *    Preferences.systemNodeForPackage����õ�   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs�µ����·��
 * 2������Process�� ����������ָ�� ��������ָ��·���µ�ע���
 * 
 * @author chencheng0816@gmail.com 
 * @date 2018��4��22�� ����2:38:20
 */
public class RegisteryUtil { 	
	/**
     * д HKEY_LOCAL_MACHINE\Software\JavaSoft\Prefs\javaplayer �µ�ע���
     *
	 * @param key ������
	 * @param value ����ֵ
	 * @return
	 */
    private void setMachineReg(String key, String value) {  
        Preferences preferences = Preferences.systemRoot().node("/javaplayer");
        preferences.put(key, value); 
    }  
    
    /**
     * �� HKEY_LOCAL_MACHINE\Software\JavaSoft\Prefs �µ�ע���
     *
     * @param key ������
     * @return
     */
    private void getMachineReg(String key)
    {
       Preferences preferences = Preferences.systemRoot().node("/javaplayer");
       System.out.println(preferences.get(key,""));
    }
    
    /**
     * д HKEY_CURRENT_USER\Software\JavaSoft\Prefs �µ�ע���
     *
	 * @param key ������
	 * @param value ����ֵ
	 * @return
	 */
    private void setUserReg(String key, String value)
    {
       Preferences preferences = Preferences.userNodeForPackage(getClass());
       preferences.put(key, value);
    }

    /**
     * �� HKEY_CURRENT_USER\Software\JavaSoft\Prefs �µ�ע���
     *
     * @param key ������
     * @return ����ע�������ֵ
     */
    private void getUserReg(String key)
    {
       Preferences preferences = Preferences.userNodeForPackage(getClass());
       System.out.println(preferences.get(key,""));
    }
    
    /**
     * ��������·����ע���ֵ
     * 
     * @param regQuery cmd���String
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