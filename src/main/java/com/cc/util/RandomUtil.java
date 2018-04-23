package com.cc.util;

import java.util.Random;

/**
 * 随机数工具类
 *
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午5:20:58
 */
public class RandomUtil {

    /**
     * 生成一个在数值以内的随机数
     * @return
     */
    public static int randomNumber(int num) {
        return randomNumber(0, num);
    }

    /**
     * 生成一个在最大数和最小数之间的随机数。会出现最小数，但不会出现最大数。
     * @param minNum 最小数
     * @param maxNum 最大数
     * @return
     */
    public static int randomNumber(int minNum, int maxNum) {
        if (maxNum <= minNum) {
            throw new RuntimeException("maxNum必须大于minNum!");
        }
        // 计算出来差值
        int subtract = maxNum - minNum;
        Double ranDouble = Math.floor(Math.random() * subtract);

        return ranDouble.intValue() + minNum;
    }

}
