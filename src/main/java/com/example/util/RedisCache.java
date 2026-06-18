package com.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

public class RedisCache {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Сохранить объект в Redis с TTL
     */
    public static void put(String key, Object value, long ttlSeconds) {
        if (!RedisConfig.isRedisAvailable()) {
            System.err.println("Redis unavailable, skipping cache put: " + key);
            return;
        }

        try (Jedis jedis = RedisConfig.getJedis()) {
            String json = objectMapper.writeValueAsString(value);
            jedis.setex(key, (int) ttlSeconds, json);
            System.out.println("Redis PUT: " + key + " (TTL: " + ttlSeconds + "s)");
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize value for key: " + key);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Redis error on put: " + key);
            e.printStackTrace();
        }
    }

    /**
     * Сохранить объект в Redis с TTL по умолчанию (30 секунд)
     */
    public static void put(String key, Object value) {
        put(key, value, 30);
    }

    /**
     * Получить объект из Redis
     */
    public static <T> T get(String key, Class<T> clazz) {
        if (!RedisConfig.isRedisAvailable()) {
            return null;
        }

        try (Jedis jedis = RedisConfig.getJedis()) {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }
            System.out.println("Redis GET HIT: " + key);
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to deserialize value for key: " + key);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Redis error on get: " + key);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получить список из Redis (для List<T>)
     */
    public static <T> T get(String key, TypeReference<T> typeRef) {
        if (!RedisConfig.isRedisAvailable()) {
            return null;
        }

        try (Jedis jedis = RedisConfig.getJedis()) {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }
            System.out.println("Redis GET HIT: " + key);
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to deserialize list for key: " + key);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Redis error on get: " + key);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Удалить ключ из Redis
     */
    public static void remove(String key) {
        if (!RedisConfig.isRedisAvailable()) return;

        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.del(key);
            System.out.println("Redis REMOVE: " + key);
        } catch (Exception e) {
            System.err.println("Redis error on remove: " + key);
            e.printStackTrace();
        }
    }

    /**
     * Удалить все ключи по префиксу (с использованием SCAN)
     */
    public static void removeByPrefix(String prefix) {
        if (!RedisConfig.isRedisAvailable()) return;

        try (Jedis jedis = RedisConfig.getJedis()) {
            String cursor = "0";
            do {
                var scanResult = jedis.scan(cursor, new redis.clients.jedis.params.ScanParams().match(prefix + "*"));
                cursor = scanResult.getCursor();
                for (String key : scanResult.getResult()) {
                    jedis.del(key);
                    System.out.println("Redis REMOVE by prefix: " + key);
                }
            } while (!"0".equals(cursor));
        } catch (Exception e) {
            System.err.println("Redis error on removeByPrefix: " + prefix);
            e.printStackTrace();
        }
    }

    /**
     * Проверить существование ключа
     */
    public static boolean exists(String key) {
        if (!RedisConfig.isRedisAvailable()) return false;

        try (Jedis jedis = RedisConfig.getJedis()) {
            return jedis.exists(key);
        } catch (Exception e) {
            System.err.println("Redis error on exists: " + key);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получить оставшееся время жизни ключа (в секундах)
     */
    public static long getTTL(String key) {
        if (!RedisConfig.isRedisAvailable()) return -2;

        try (Jedis jedis = RedisConfig.getJedis()) {
            return jedis.ttl(key);
        } catch (Exception e) {
            System.err.println("Redis error on ttl: " + key);
            e.printStackTrace();
        }
        return -2;
    }

    /**
     * Очистить всю базу Redis
     */
    public static void clear() {
        if (!RedisConfig.isRedisAvailable()) return;

        try (Jedis jedis = RedisConfig.getJedis()) {
            jedis.flushDB();
            System.out.println("Redis FLUSHDB");
        } catch (Exception e) {
            System.err.println("Redis error on clear");
            e.printStackTrace();
        }
    }
}