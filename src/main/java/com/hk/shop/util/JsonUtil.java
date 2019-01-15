package com.hk.shop.util;

import com.hk.shop.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 何康
 * @date 2019/1/12 11:13
 */
@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //设置默认的日期格式
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));
        //将非空的字段加入序列化
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
        //取消timestamp的默认转换
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        //忽略：空bean异常
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        //忽略:Json存在，对象不存在，反序列化失败
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T string2Object(String str, Class<T> clazz) {
        if (str == null || clazz == null) return null;
        try {
            return objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            log.error("deserializa fail error:{}", e);
        }
        return null;
    }

    public static <T> String object2String(T obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            log.error("serializa fail error:{}", e);
            return null;
        }
    }

    public static <T> T string2Object(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return  objectMapper.readValue(str, typeReference);
        } catch (Exception e) {
            log.error("deserializa fail error:{}", e);
            return null;
        }
    }

    public static void main(String[] args) {
        User user = new User();
        user.setPassword("111");
        user.setUsername("test");
        User user2 = new User();
        user2.setPassword("222");
        user2.setUsername("test2");
        String str = object2String(user);
        System.out.println("serializa object:"+str);
        User deserializaUser = string2Object(str,User.class);
        System.out.println("deserializa object:"+deserializaUser);
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);
        String arrStr = object2String(users);
        System.out.println("serializa array:"+arrStr);
        List<User> deserializaArray = string2Object(arrStr, new TypeReference<List<User>>() {});
        System.out.println("deserializa array:"+deserializaArray);
    }


}
