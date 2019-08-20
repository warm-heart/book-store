package com.book.utils;

import com.book.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author wangqianlong
 * @create 2019-08-09 18:29
 */
public class JsonUtil {

	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 转换为json字符串
	 * @param object
	 * @return
	 */
	public static String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * json转对象
	 * @param string
	 * @param classType
	 * @return
	 */
	public static Object fromJson(String string, Class classType) {
		try {
			return objectMapper.readValue(string, classType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * json转对象
	 * @param string
	 * @param typeReference
	 * @return
	 */
	public static Object fromJson(byte[] string, Class<User> typeReference) {
		try {
			return objectMapper.readValue(string, typeReference);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
