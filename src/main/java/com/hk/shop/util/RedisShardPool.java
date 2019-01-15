package com.hk.shop.util;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Arrays;
import java.util.List;

/**
 * @author 何康
 * @date 2019/1/12 11:19
 */
public class RedisShardPool {
    private static ShardedJedisPool shardedJedisPool;

    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "20"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "20"));

    private static boolean testBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    private static boolean testReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String ip1 = PropertiesUtil.getProperty("redis1.ip");
    private static Integer port1 = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static String ip2 = PropertiesUtil.getProperty("redis2.ip");
    private static Integer port2 = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    private static void initPool(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnBorrow(testBorrow);
        jedisPoolConfig.setTestOnReturn(testReturn);
        jedisPoolConfig.setBlockWhenExhausted(true);//连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。
        JedisShardInfo shardInfo1 = new JedisShardInfo(ip1,port1);
        JedisShardInfo shardInfo2 = new JedisShardInfo(ip2,port2);
        List<JedisShardInfo> jedisShardInfoList = Arrays.asList(shardInfo1,shardInfo2);
        shardedJedisPool = new ShardedJedisPool(jedisPoolConfig,jedisShardInfoList);
    }
    static {initPool();}


    public static ShardedJedis getJedis(){
        return shardedJedisPool.getResource();
    }
    public static void returnJedis(ShardedJedis shardedJedis){
        shardedJedisPool.returnResource(shardedJedis);
    }
    public static void returnBrokenJedi(ShardedJedis shardedJedis){
        shardedJedisPool.returnBrokenResource(shardedJedis);
    }

    public static void main(String[] args) {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        for(int i = 0 ;i<10;i++){
            shardedJedis.set("key:"+i,"value:"+i);
        }
        returnJedis(shardedJedis);
        System.out.println("propram is end");
    }
}
