package com.cc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 常用方法汇总
 *
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午4:47:27
 */
public final class CommonUtil<E> {
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static boolean isEmpty(List list) {
		return (list == null || list.size() == 0);
	}

	public static boolean isEmpty(Map map) {
		return (map == null || map.size() == 0);
	}

	public static boolean isEmpty(Set set) {
		return (set == null) || (set.size() == 0);
	}

	public static boolean isEmpty(String value) {
		return (value == null || "".equals(value.trim()));
	}

	public static boolean isEmpty(Object value) {
		return (value == null);
	}

	public static boolean isEmpty(Long value) {
		return (value == null);
	}

	public static boolean isEmpty(Integer value) {
		return (value == null);
	}

	public static boolean isEmpty(String[] arrValue) {
		return (arrValue == null || arrValue.length == 0);
	}

	public static boolean isEmpty(Object[] arrObject) {
		return (arrObject == null || arrObject.length == 0);
	}

	public static boolean isNotEmpty(List list) {
		return !isEmpty(list);
	}

	public static boolean isNotEmpty(Map map) {
		return !isEmpty(map);
	}

	public static boolean isNotEmpty(Set set) {
		return !isEmpty(set);
	}

	public static boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}

	public static boolean isNotEmpty(Object value) {
		return !isEmpty(value);
	}

	public static boolean isNotEmpty(Long value) {
		return !isEmpty(value);
	}

	public static boolean isNotEmpty(Integer value) {
		return !isEmpty(value);
	}

	public static boolean isNotEmpty(String[] arrValue) {
		return !isEmpty(arrValue);
	}

	public static boolean isNotEmpty(Object[] arrObject) {
		return !isEmpty(arrObject);
	}

	public static boolean isNotZero(Integer aNum) {
		if (null == aNum || 0 == aNum) {
			return false;
		} else {
			return true;
		}
	}

	public static String getStringValue(Object value) {
		if (isEmpty(value)) {
			return "";
		} else {
			return value.toString();
		}
	}

	/**
	 * 获取今日日期，"yyyy-MM-dd"
	 * 
	 * @return
	 */
	public static String getDate() {
		Date date = new Date();
		SimpleDateFormat frm = new SimpleDateFormat("yyyy-MM-dd");
		String today = frm.format(date);
		return today;
	}

	/**
	 * 按指定样式返回当前时间
	 * 
	 * @param pattern "yyyy-MM-dd HH:mm:ss"
	 */
	public static String formatDateString(String pattern) {
		SimpleDateFormat frm = new SimpleDateFormat(pattern);
		Calendar calendar = Calendar.getInstance();
		String dateStr = frm.format(calendar.getTime());
		
		return dateStr;
	}

	/**
	 * MD5 加密
	 */
	public static String getMD5Str(String str) {
		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			return "";
		} catch (UnsupportedEncodingException e) {
			return "";
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}

		return md5StrBuff.toString();
	}

	public static boolean isLong(String str) {
		try {
			new Long(str);

			return true;
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * 英文双引号转为中文双引号
	 */
	public static String enStringToZhString(String str) {
		return str.replaceAll("\"", "”");
	}

	/**
	 * 计算一个日期N天之前的日期
	 *
	 * @param intervalDays
	 * @return
	 */
	public static Date getTodayBefore(int intervalDays, String dateFormat) {
		Calendar calendar = Calendar.getInstance();
		
		calendar.add(Calendar.DATE, -intervalDays);
		if ("Date".equals(dateFormat)) {
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}
		Date date = calendar.getTime();
		return date;
	}

	/**
	 * 计算一个日期N天之前的日期
	 *
	 * @param intervalDays
	 * @return
	 */
	public static Date getToday2Before(int intervalDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -intervalDays);
		Date date = calendar.getTime();
		return date;
	}

	/**
	 * 计算一个日期N天之前的日期
	 *
	 * @param intervalDays
	 * @return
	 */
	public static String getTodayBefore(int intervalDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -intervalDays);
		Date date = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String dayBefore = format.format(date);
		return dayBefore;
	}

	/**
	 * 计算一个日期N天之前的日期
	 *
	 * @param intervalDays
	 * @return
	 */
	public static Date getTodayDateBefore(int intervalDays, String dateFormat) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -intervalDays);
		Date date = calendar.getTime();
		return date;
	}

	/**
	 * 计算一个日期X分钟前的日期
	 *
	 * @return
	 */
	public static Date getMinuteBefore(Date date, int intervalMinute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -intervalMinute);
		Date result = calendar.getTime();
		return result;
	}

	/**
	 * 计算一个日期N天之后的日期
	 *
	 * @param intervalDays
	 * @return
	 */
	public static String getTodayAfter(int intervalDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, intervalDays);
		Date date = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String dayAfter = format.format(date);
		return dayAfter;
	}

	/**
	 * 计算一个日期N天之后的日期
	 *
	 * @param intervalDays
	 * @return
	 */
	public static Date getToday2After(int intervalDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, intervalDays);
		Date date = calendar.getTime();
		return date;
	}

	public static Date getTodayBeforeHour(int afterHuor) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -afterHuor);
		Date date = calendar.getTime();

		return date;
	}

	/**
	 * 获取指定时间
	 * 
	 * @param dateTime
	 * @param field
	 * @param amount
	 * @return
	 */
	public static Date getDateTime(Date dateTime, int field, int amount) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateTime);
		calendar.add(field, amount);
		Date date = calendar.getTime();

		return date;
	}
	
	/**
	 * 将 "时:分:秒" 这种格式的时间长度转换为秒数 如"00:22:30" 转换为 "1350"。
	 * 
	 * @param runtime "00:22:30"
	 */
	public static int time2Second(String runtime) {
		int timeCount = 0;
		try {
			String[] celltime = runtime.split(":");
			int hour = Integer.parseInt(celltime[0]);
			int minute = Integer.parseInt(celltime[1]);
			int second = Integer.parseInt(celltime[2]);
			timeCount = 3600 * hour + 60 * minute + second;
		} catch (Exception e) {
			int temptime = 0;
			try {
				temptime = Integer.parseInt(runtime);

			} catch (Exception e2) {
				return 0;
			}
			return temptime;
		}
		return timeCount;
	}

	/**
	 * 获取服务器IP地址
	 */
	public static String getServerIp() {
		String sIP = "";
		InetAddress ip = null;
		try {
			boolean bFindIP = false;
			Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				if (bFindIP) {
					break;
				}
				NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					ip = (InetAddress) ips.nextElement();
					if (!ip.isLoopbackAddress() && ip.getHostAddress().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
						bFindIP = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != ip) {
			sIP = ip.getHostAddress();
		}
		return sIP;
	}

	/**
	 * 指定key=value 
	 * @param key 字符串变量名
	 * @param value 值
	 */
	public static void set(String key, String value) {
		if (isEmpty(key) || !key.equals(value)) {
			key = value;
		}
	}
	
	/**
	 * 获取32位唯一序列号
	 * @return
	 */
	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString(); 
		//去掉“-”符号 
		return uuid.replaceAll("-", "");
	}
	
	/**
	 * 判断Excel文件是否为2003以前的版本
	 * @return
	 */
	public static boolean isExcel2003(String filePath) {   
        return filePath.matches("^.+\\.(?i)(xls)$");    
    } 
	
	/**
	 * 判断Excel文件是否为2007的版本
	 * @return
	 */
	public static boolean isExcel2007(String filePath) {    
        return filePath.matches("^.+\\.(?i)(xlsx)$");    
    }
	
	/**
	 * 四舍五入
	 * @param value
	 * @return
	 */
	public static String formatDouble(double value) {    
		BigDecimal bg = new BigDecimal(value).setScale(1, RoundingMode.HALF_UP);
		
        return bg.toString();
    }
	
	/**
	 * 解析日志等级,5-->101
	 * @return
	 */
	public static String parseLogLevel(String logLevel) {   
		if(CommonUtil.isEmpty(logLevel)) {
			return "000";
		}
		int level = Integer.parseInt(logLevel);
		int warnFlag = (level / 4) % 2;
		int debugFlag = (level / 2) % 2;
		int runFlag = level % 2;
		logLevel = "" + warnFlag + debugFlag + runFlag;
		
        return logLevel;
    }
	
	/**
	 * 格式化前端参数，若参数为null，则返回“default description”
	 * @return
	 */
	public static String parseDescription(String pageParam) {
		if(isEmpty(pageParam)) {
			return "default description";
		}
		return pageParam;
    }
	
	/**
	 * m进制转为n进制
	 * @param radixStr 原字符串
	 * @param m 原进制
	 * @param n 目的进制
	 * @return
	 */
	public static String m2n(String radixStr, int m, int n) {
		return new BigInteger(radixStr, m).toString(n);
	}
	
	public static String saveExcel(String radixStr, int m, int n) {
		return new BigInteger(radixStr, m).toString(n);
	}
	
	/**
	 * 
	 * @param targetList 目标排序List
	 * @param sortField 排序字段(实体类属性名)
	 * @param sortMode 排序方式（asc or desc），默认为asc正序
	 */
	public void sort(List<E> targetList, final String sortField, final String sortMode) {	      
        Collections.sort(targetList, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                int retVal = 0;
                try {
                    //首字母转大写  
                    String newStr=sortField.substring(0, 1).toUpperCase() + sortField.replaceFirst("\\w","");
                    String methodStr = "get" + newStr;
                      
                    Method method1 = ((E)obj1).getClass().getMethod(methodStr, null);
                    Method method2 = ((E)obj2).getClass().getMethod(methodStr, null);
                    if (sortMode != null && "desc".equals(sortMode)) {
                        retVal = method2.invoke(((E) obj2), null).toString().compareTo(method1.invoke(((E) obj1), null).toString()); // 倒序  
                    } else {
                        retVal = method1.invoke(((E) obj1), null).toString().compareTo(method2.invoke(((E) obj2), null).toString()); // 正序  
                    }
                } catch (Exception e) {
                    throw new RuntimeException();
                }  
                return retVal;
            }
        });  
    }  
	
	public static void main(String[] args) {
//		System.out.println(getDate());
		System.out.println(formatDateString("yyyy-MM-dd HH:mm:ss"));
//		System.out.println(m2n("101", 2, 10));
	}
}
