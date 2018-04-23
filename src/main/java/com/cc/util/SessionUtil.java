package com.cc.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.cc.util.redis.RedisClient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 获取有效session工具类,更方便更安全
 *
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午6:20:31
 */
public class SessionUtil {

    private static final String User   = "User";
    private static final int    EXPIRE = 7 * 24 * 60 * 60;

    private static SessionUtil sessionUtil;
    @Autowired
    private        RedisClient redisClient;

    public void init() {
        sessionUtil = this;
        sessionUtil.redisClient = this.redisClient;
    }

    /**
     * 获取用户ID
     *
     * @return
     */
    static public String getUserID() {
    	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        return (String) request.getSession().getAttribute("username");
    }

    /**
     * 获取Ip地址
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取session属性
     *
     * @return
     */
    public static Object getAttribute(String key) {
    	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    	HttpSession shiroSession = (HttpSession) request.getSession();
        return shiroSession.getAttribute(key);
    }
    public static void setAttribute(String key, Object object) {
    	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    	HttpSession shiroSession = (HttpSession) request.getSession();
    	shiroSession.setAttribute(key, object);
    }
    public static void removeAttribute(String key) {
    	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    	HttpSession shiroSession = (HttpSession) request.getSession();
    	shiroSession.removeAttribute(key);
    }
}
