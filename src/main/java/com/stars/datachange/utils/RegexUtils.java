package com.stars.datachange.utils;

import java.util.regex.Pattern;

/**
 * 正则工具类
 * @author Hao.
 * @version 1.0
 * @since 2023/2/6 13:26
 */
public class RegexUtils {

	public static Pattern getPattern(String regex){
		return Pattern.compile(regex);
	}

	/**
	 * 是否数字
	 * @author Hao.
	 * @since 2023/3/14 16:18
	 * @param str 值
	 * @return boolean
	 */
	public static boolean isNumber(String str) {
		Pattern pattern = getPattern("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}
}