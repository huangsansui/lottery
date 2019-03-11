package top.huangsansui.lottery.util;

import java.util.Collections;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * redis 连接池
 */

public class RedisPoolsUtil {

    private static Logger log = Logger.getLogger(RedisPoolsUtil.class);
    public static JedisPool jedisPool = null;
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;
    private final static String LOCKER_PREFIX = "lock:";

    /**
     * Redis服务器IP
     */
    private static String IP = PropertiesFileUtil.getInstance("redis").get("master.redis.ip");

    /**
     * Redis的端口号
     */
    private static int PORT = PropertiesFileUtil.getInstance("redis").getInt("master.redis.port");

    /**
     * 访问密码
     */
    private static String PASSWORD = PropertiesFileUtil.getInstance("redis").get("master.redis.password");

    /**
     * 可用连接实例的最大数目，默认值为8；
     * 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
     */
    private static int MAX_ACTIVE = PropertiesFileUtil.getInstance("redis").getInt("master.redis.max_active");

    /**
     * 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
     */
    private static int MAX_IDLE = PropertiesFileUtil.getInstance("redis").getInt("master.redis.max_idle");

    /**
     * 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
     */
    private static int MAX_WAIT = PropertiesFileUtil.getInstance("redis").getInt("master.redis.max_wait");

    /**
     * 超时时间
     */
    private static int TIMEOUT = PropertiesFileUtil.getInstance("redis").getInt("master.redis.timeout");

    /**
     * 存储环境
     */
    private static String RUNTIME = PropertiesFileUtil.getInstance("redis").get("master.redis.run_time");

    /**
     * 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
     */
    private static boolean TEST_ON_BORROW = false;

    /**
     * redis过期时间,以秒为单位
     */
    // 一小时
    public final static int EXRP_HOUR = 60 * 60;
    // 一天
    public final static int EXRP_DAY = 60 * 60 * 24;
    // 一个月
    public final static int EXRP_MONTH = 60 * 60 * 24 * 30;

    /**
     * 初始化Redis连接池
     */
    private static void initialPool() {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);
            // jedisPool = new JedisPool(config, IP, PORT, TIMEOUT);
            if (IP.contains("127.0.0.1")) {
                jedisPool = new JedisPool(config, IP, PORT, TIMEOUT);
            } else {
                jedisPool = new JedisPool(config, IP, PORT, TIMEOUT, PASSWORD);
            }
        } catch (Exception e) {
            log.error("First create JedisPool error : " + e);
        }
    }

    /**
     * 在多线程环境同步初始化
     */
    private static synchronized void poolInit() {
        if (null == jedisPool) {
            initialPool();
        }
    }

    /**
     * 同步获取Jedis实例
     *
     * @return Jedis
     */
    public static Jedis getJedis() {
        poolInit();
        Jedis jedis = null;
        try {
            if (null != jedisPool) {
                jedis = jedisPool.getResource();
                try {
                    jedis.auth(PASSWORD);
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            log.error("Get jedis error : " + e);
        }
        return jedis;
    }

    /**
     * 得到Key
     *
     * @param key
     * @return
     */
    public static String buildKey(String key) {
        return new StringBuilder().append(RUNTIME).append(":").append(key).toString();
    }

    public static void close(Jedis jedis) {
        if (jedis != null) jedis.close();
    }

    public static void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(buildKey(key), value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(jedis);
        }
    }


    public static void del(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(buildKey(key));
        } catch (Exception e) {

        } finally {
            close(jedis);
        }
    }

    public static String get(String key) {
        Jedis jedis = null;
        String value = "";
        try {
            jedis = getJedis();
            value = jedis.get(buildKey(key));
        } catch (Exception e) {
            return value;
        } finally {
            close(jedis);
        }
        return value;
    }

    /**
     * 缓存中保存对象
     *
     * @param key
     * @param value
     * @param seconds 秒为单位
     */
    public static void setex(String key, String value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.setex(buildKey(key), seconds, value);
        } catch (Exception e) {
        } finally {
            close(jedis);
        }
    }


    public static void setex(String key, Object value, int seconds) {
        setex(key, JSON.toJSONString(value), seconds);
    }

    /**
     * 缓存中保存对象
     *
     * @param key
     * @param valueObject
     * @author rfk
     * @date 2014-11-22上午10:23:33
     */
    public static void set(String key, Object valueObject) {
        String value = JSON.toJSONString(valueObject);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(buildKey(key), value);
        } catch (Exception e) {

        } finally {
            close(jedis);
        }
    }

    public static String getCounter(String key) {
        Jedis jedis = null;
        String value = "01";
        try {
            String buildKey = buildKey(key);
            jedis = getJedis();
            if (keyIsExsit(buildKey)) {//存在
                value = jedis.get(buildKey);
                int value_ = Integer.parseInt(value);
                if (value_ < 9) {
                    value = "0" + (value_ + 1);
                }
            }
    		/*LocalDateTime nowTime = LocalDateTime.now();
    		nowTime.
    		LocalDate now = LocalDate.now();
    		LocalDate tomorrow = now.plusDays(1L);
    		tomorrow.get*/
            jedis.set(buildKey, value);
        } catch (Exception e) {

        } finally {
            close(jedis);
        }
        return value;
    }

    /**
     * 从缓存中获取value 转化为对象
     *
     * @param key
     * @param resultClass
     * @return
     * @author rfk
     * @date 2014-11-22上午10:23:52
     */
    public static <T> T get(String key, Class<T> resultClass) {
        log.info("get '" + key + "' data from cache");
        Jedis jedis = null;
        String value = "";
        try {
            jedis = getJedis();
            value = jedis.get(buildKey(key));
        } catch (Exception e) {
            return null;
        } finally {
            close(jedis);
        }
        return JSON.parseObject(value, resultClass);
    }

    /**
     * 缓存计数器
     *
     * @param key
     * @param value
     * @return true:存在  false:
     * @author rfk
     * @date 2014-11-22下午3:12:09
     */
    public static long updateCounter(String key, long value) {
        Jedis jedis = null;
        long count = 0;
        try {
            jedis = getJedis();
            count = jedis.incrBy(buildKey(key), value);
            jedis.expire(buildKey(key), 60 * 60 * 24);
        } catch (Exception e) {
            return 0;
        } finally {
            close(jedis);
        }
        return count;
    }

    /**
     * 计数器
     *
     * @param key
     * @param value
     * @return
     */
    public static long incrBy(String key, long value) {
        Jedis jedis = null;
        long count = 0;
        try {
            jedis = getJedis();
            count = jedis.incrBy(buildKey(key), value);
        } catch (Exception e) {
            return 0;
        } finally {
            close(jedis);
        }
        return count;
    }

    /**
     * 原子减
     *
     * @param key
     * @param value
     * @return
     */
    public static long decrBy(String key, long value) {
        Jedis jedis = null;
        long count = 0;
        try {
            jedis = getJedis();
            count = jedis.decrBy(buildKey(key), value);
        } catch (Exception e) {
            return 0;
        } finally {
            close(jedis);
        }
        return count;
    }


    /**
     * 带过期时间计数
     *
     * @param key
     * @param value
     * @param exp
     * @return
     */
    public static long incrBy(String key, long value, int exp) {
        Jedis jedis = null;
        long count = 0;
        try {
            jedis = getJedis();
            count = jedis.incrBy(buildKey(key), value);
            jedis.expire(key, exp);
        } catch (Exception e) {
            return 0;
        } finally {
            close(jedis);
        }
        return count;
    }

    /**
     * 设置过期时间
     *
     * @param key
     * @param exp 秒为单位
     * @return
     */
    public static boolean expire(String key, int exp) {
        log.info("Is has key:" + key);
        Jedis jedis = null;
        boolean temp = false;
        try {
            jedis = getJedis();
            jedis.expire(buildKey(key), exp);
        } catch (Exception e) {
            return temp;
        } finally {
            close(jedis);
        }
        return temp;
    }


    /**
     * 判断键值是否存在
     *
     * @author rfk
     * @date 2014-11-22下午3:26:11
     */
    public static boolean keyIsExsit(String key) {
        log.info("Is has key:" + key);
        Jedis jedis = null;
        boolean temp = false;
        try {
            jedis = getJedis();
            temp = jedis.exists(buildKey(key));
        } catch (Exception e) {
            return temp;
        } finally {
            close(jedis);
        }
        return temp;
    }

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {

        Jedis jedis = null;

        try {
            lockKey = LOCKER_PREFIX + lockKey;
            jedis = getJedis();
            String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            log.error("尝试获取分布式锁错误", e);
        } finally {
            close(jedis);
        }
        return false;
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(String lockKey, String requestId) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            lockKey = LOCKER_PREFIX + lockKey;
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            log.error("释放分布式锁错误", e);
        } finally {
            close(jedis);
        }
        return false;

    }

    /**
     * 测试hash中指定的存储是否存在
     *
     * @param key
     * @param fieid 存储的名字
     * @return 1存在，0不存在
     */
    public static boolean hexists(String key, String fieid) {
        //ShardedJedis sjedis = getShardedJedis();  
        Jedis sjedis = getJedis();
        boolean s = sjedis.hexists(key, fieid);
        close(sjedis);
        return s;
    }

    /**
     * 返回hash中指定存储位置的值
     *
     * @param key
     * @param fieid 存储的名字
     * @return 存储对应的值
     */
    public static String hget(String key, String fieid) {
        Jedis sjedis = getJedis();
        String s = sjedis.hget(key, fieid);
        close(sjedis);
        return s;
    }


    /**
     * 存储hash值 key加前缀
     *
     * @param key
     * @param fieid
     * @param value
     * @return
     */
    public static Long hset(String key, String fieid, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            key = buildKey(key);
            return jedis.hset(key, fieid, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }

    /**
     * 在列表中的尾部添加一个个值，返回列表的长度
     *
     * @param key
     * @param value
     * @return Long
     */
    public static Long rpush(String key, String value) {
        Jedis jedis = getJedis();
        key = buildKey(key);
        Long length = jedis.rpush(key, value);
        jedis.close();
        return length;
    }

    /**
     * 在列表中的尾部添加多个值，返回列表的长度
     *
     * @param key
     * @param values
     * @return Long
     */
    public static Long rpush(String key, String[] values) {
        Jedis jedis = getJedis();
        key = buildKey(key);
        Long length = jedis.rpush(key, values);
        jedis.close();
        return length;
    }

    /**
     * 向集合添加一个或多个成员，返回添加成功的数量
     *
     * @param key
     * @param members
     * @return Long
     */
    public static Long sadd(String key, String... members) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            key = buildKey(key);
            return jedis.sadd(key, members);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }


    /**
     * 返回集合中元素的数量
     *
     * @param key
     * @return
     */
    public static Long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            key = buildKey(key);
            return jedis.scard(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }


    /**
     * 向有序集合添加一个成员(已存在则更新分数)，返回添加成功的数量
     *
     * @param key
     * @param member
     * @param score
     * @return long
     */
    public static Long zadd(String key, String member, double score) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            key = buildKey(key);
            return jedis.zadd(key, score, member);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }

    /**
     * 返回有序set 中介于两个分数之间的成员数量(包含首尾分数)
     *
     * @param key
     * @return
     */
    public static Long zcount(String key, double min_score, double max_score) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            key = buildKey(key);
            return jedis.zcount(key, min_score, max_score);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }
   /* public static void main(String[] args) {
    	zadd("test", "key1", 22);
    	zadd("test", "key2", 24);
    	System.out.println(scount("test",22,24));
	}*/
    /*public static void main(String args[]) {
        Jedis jedis = null;
        try {
            System.out.println(updateCounter("20180608m", 0));
			*//*jedis = getInstance().getResource();
			jedis.set("name", "riyunzhu");
//			
//			jedis.setex("qyyd_verify_123",10*60,"123456");
//			System.out.println(jedis.get("name"));
//			System.out.println(jedis.get("qyyd_verify_123"));
			System.out.println(jedis.get("name"));
			jedis.del("qyyd_upderror_15268180490");*//*
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                close(jedis);
            }
        }
    }*/

}

