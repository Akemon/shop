package com.hk.shop.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author 何康
 * @date 2018/10/30 17:52
 */
public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    /***
     * 创建一个GUAUA缓存对象，初始容量是1000，最大容量是10000，有效期为12个小时
     * 当超出最大容量是，这种缓存会使用LRU(最近最少使用)算法将缓存移除
     */
    private static LoadingCache<String,String> localCache =
            CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000)
            .expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //这个方法是当缓存对象找不到key时的返回值
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    /***
     * 加入缓存
     * @param key
     * @param value
     */
    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    /***
     * 获取缓存
     * @param key
     * @return
     */
    public static String getValue(String key){
        String value = null;
        try{
            value = localCache.get(key);
            if("null".equals(value)) return null;
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }

}
