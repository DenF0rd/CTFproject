package com.example.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {

    private static final String REDIS_HOST = "localhost"; // или IP вашего сервера
    private static final int REDIS_PORT = 6379;
    private static final int REDIS_TIMEOUT = 2000; // 2 секунды
    private static final int MAX_TOTAL = 10;
    private static final int MAX_IDLE = 5;
    private static final int MIN_IDLE = 2;

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(MAX_TOTAL);
        poolConfig.setMaxIdle(MAX_IDLE);
        poolConfig.setMinIdle(MIN_IDLE);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT);
        System.out.println("Redis connection pool initialized!");
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void closePool() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            System.out.println("Redis connection pool closed.");
        }
    }

    public static boolean isRedisAvailable() {
        try (Jedis jedis = getJedis()) {
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            System.err.println("Redis is not available: " + e.getMessage());
            return false;
        }
    }
}