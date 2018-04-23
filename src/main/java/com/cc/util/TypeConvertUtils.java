package com.cc.util;

/**
* java类型转换工具类
* @author cc_2471082434@qq.com 
* @date 2016年10月28日 上午11:16:10
 */
public class TypeConvertUtils {
	
	/**
     * 将 Little-Endian 的字节数组转为 int 类型的数据
     * Little-Endian 表示高位字节在高位索引中
     * @param bys       字节数组
     * @param start     需要转换的开始索引位数
     * @param len       需要转换的字节数量
     * @return  指定开始位置和长度以 LE 方式表示的 int 数值
     */
    public static int bytes2IntLE(byte[] bys, int start, int len) {
        return bytes2Int(bys, start, len, false);
    }
  
    public static int bytes2IntLE(byte[] bys) {
        return bytes2Int(bys, 0, bys.length, false);
    }
  
    /**
     * 将 Big-Endian 的字节数组转为 int 类型的数据
     * Big-Endian 表示高位字节在低位索引中
     * @param bys       字节数组
     * @param start     需要转换的开始索引位数
     * @param len       需要转换的字节数量
     * @return  指定开始位置和长度以 BE 方式表示的 int 数值
     */
    public static int bytes2IntBE(byte[] bys, int start, int len) {
        return bytes2Int(bys, start, len, true);
    }
  
    public static int bytes2IntBE(byte[] bys) {
        return bytes2Int(bys, 0, bys.length, true);
    }
    
    /**
     * 将字节数组转为 Java 中的 int 数值
     * @param bys           字节数组
     * @param start         需要转换的起始索引点
     * @param len           需要转换的字节长度
     * @param isBigEndian   是否是 BE（true -- BE 序，false -- LE 序）
     * @return
     */ 
    private static int bytes2Int(byte[] bys, int start, int len, 
            boolean isBigEndian) {
        int n = 0;
        for(int i = start, k = start + len % (Integer.SIZE / Byte.SIZE + 1); i < k; i++) {
            n |= (bys[i] & 0xff) << ((isBigEndian ? (k - i - 1) : i) * Byte.SIZE);
        }
        return n;
    }
    
    public static byte[] int2byte(int res) {  
   	 byte[] targets = new byte[4];  
   	   
   	 targets[0] = (byte) (res & 0xff);// 最低位   
   	 targets[1] = (byte) ((res >> 8) & 0xff);// 次低位   
   	 targets[2] = (byte) ((res >> 16) & 0xff);// 次高位   
   	 targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。   
   	 return targets;   
   }   
    
    public static int byte2int(byte[] res) {   
   	// 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000   
   	int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或   
   	| ((res[2] << 24) >>> 8) | (res[3] << 24);   
   	return targets;   
   } 
}