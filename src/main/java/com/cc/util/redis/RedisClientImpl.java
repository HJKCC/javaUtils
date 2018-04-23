package com.cc.util.redis;

import redis.clients.jedis.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.cc.util.SerializeUtils;

/**
 * JedisPool 通用工具实现类
 * 
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午6:18:29
 */
public class RedisClientImpl implements RedisClient {
	@Autowired
	private JedisPoolConfig config;
    private JedisPool pool = null;

    /**
     * 传入ip和端口号构建redis连接池
     *
     * @param ip   ip
     * @param port 端口
     */
    public RedisClientImpl(String ip, int port) {
        if (pool == null) {
//            JedisPoolConfig config = new JedisPoolConfig();
//            // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
//            config.setMaxIdle(5);
//            // 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
//            config.setMaxWaitMillis(1000 * 10);
//            // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
//            config.setTestOnBorrow(true);
//            // pool = new JedisPool(config, "192.168.0.121", 6379, 100000);
            pool = new JedisPool(config, ip, port, 100000);
        }
    }

    /**
     * 通过配置对象ip端口构建连接池
     *
     * @param config 配置对象
     * @param ip     ip
     * @param port   端口
     */
    public RedisClientImpl(JedisPoolConfig config, String ip, int port) {
        if (pool == null) {
            pool = new JedisPool(config, ip, port, 10000);
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 通过配置对象ip端口超时时间构建连接池
     *
     * @param config  配置对象
     * @param ip      ip
     * @param port    端口
     * @param timeout 超时时间
     */
    public RedisClientImpl(JedisPoolConfig config, String ip, int port, int timeout) {
        if (pool == null) {
            pool = new JedisPool(config, ip, port, timeout);
        }
    }
    
    /**
     * 通过配置对象ip端口超时时间构建连接池
     *
     * @param config  配置对象
     * @param ip      ip
     * @param port    端口
     * @param timeout 超时时间
     * @param pass 密码
     */
    public RedisClientImpl(JedisPoolConfig config, String ip, int port, int timeout, String pass) {
        if (pool == null) {
            pool = new JedisPool(config, ip, port, timeout, pass);
        }
    }

    /**
     * 通过配置对象ip端口超时时间构建连接池
     *
     * @param config  配置对象
     * @param ip      ip
     * @param port    端口
     * @param timeout 超时时间
     * @param pass 密码
     */
    public RedisClientImpl(JedisPoolConfig config, String ip, int port, String pass) {
        if (pool == null) {
            pool = new JedisPool(config, ip, port, 10000, pass);
        }
    }
    
    /**
     * 通过连接池对象构建一个连接池
     *
     * @param pool 连接池对象
     */
    public RedisClientImpl(JedisPool pool) {
        if (this.pool == null) {
            this.pool = pool;
        }
    }


    /**
     * 向redis存入key和value,并释放连接资源
     * 如果key已经存在 则覆盖
     *
     * @param key
     * @param value
     * @return 成功 返回OK 失败返回 0
     */
    @Override
    public String set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.set(key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String setObject(String key, Object value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.set(key.getBytes(), SerializeUtils.serialize((Serializable) value));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String setObject(String key, Object value, int time) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String result = jedis.set(key.getBytes(), SerializeUtils.serialize((Serializable) value));
            jedis.expire(key, time);
            return result;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 向redis存入key和value,设置失效时间并释放连接资源
     * 如果key已经存在 则覆盖
     *
     * @param key
     * @param value
     * @param time  失效时间, 单位：秒
     * @return 成功 返回OK 失败返回 0
     */
    @Override
    public String set(String key, String value, int time) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            //nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在是才进行set，如果取XX，则只有当key已经存在时才进行set
            //expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
            String result = jedis.set(key, value);
            jedis.expire(key, time);
            return result;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 存储数据到缓存中，并制定过期时间和当Key存在时是否覆盖,并释放连接资源。
     *
     * @param key
     * @param value
     * @param nxxx  nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在是才进行set，如果取XX，则只有当key已经存在时才进行set
     * @param expx  expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒。
     * @param time  过期时间，单位是expx所代表的单位。
     * @return 成功 返回OK 失败返回 0
     */
    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.set(key, value, nxxx, expx, time);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /**
     * 通过key获取储存在redis中的value
     * 并释放连接
     *
     * @param key
     * @return 成功返回value 失败返回null
     */
    @Override
    public String get(String key) {
        Jedis jedis = null;
        String value = null;
        try {
            jedis = pool.getResource();
            value = jedis.get(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
    }


    @Override
    public Object getObject(String key) {
        Jedis jedis = null;
        Object value = null;
        try {
            jedis = pool.getResource();
            value = SerializeUtils.deserialize(jedis.get(key.getBytes()));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
    }


    /**
     * 判断key是否存在
     *
     * @param key
     * @return true OR false
     */
    @Override
    public Boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long persist(String s) {
        return null;
    }

    /**
     * 通过key判断值得类型
     *
     * @param key
     * @return
     */
    @Override
    public String type(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.type(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Long expire(String s, int i) {
        return null;
    }

    @Override
    public Long pexpire(String s, long l) {
        return null;
    }

    @Override
    public Long expireAt(String s, long l) {
        return null;
    }

    @Override
    public Long pexpireAt(String s, long l) {
        return null;
    }

    @Override
    public Long ttl(String s) {
        return null;
    }

    @Override
    public Boolean setbit(String s, long l, boolean b) {
        return null;
    }

    @Override
    public Boolean setbit(String s, long l, String s1) {
        return null;
    }

    @Override
    public Boolean getbit(String s, long l) {
        return null;
    }


    /**
     * 通过key 和offset 从指定的位置开始将原先value替换
     * 下标从0开始,offset表示从offset下标开始替换
     * 如果替换的字符串长度过小则会这样
     * example:
     * value : bigsea@zto.cn
     * str : abc
     * 从下标7开始替换  则结果为
     * RES : bigsea.abc.cn
     *
     * @param key
     * @param offset 下标位置
     * @param str
     * @return 返回替换后value的长度
     */
    @Override
    public Long setrange(String key, long offset, String str) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.setrange(key, offset, str);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 通过下标和key获取指定下标位置的value
     *
     * @param key
     * @param startOffset 开始位置从0开始负数表示从右边开始截取
     * @param endOffset
     * @return 如果没有返回null
     */
    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.getrange(key, startOffset, endOffset);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 设置key的值,并返回一个旧值
     *
     * @param key
     * @param value
     * @return 旧值如果key不存在则返回null
     */
    @Override
    public String getSet(String key, String value) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.getSet(key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 设置key value,如果key已经存在则返回0,nx==> not exist
     *
     * @param key
     * @param value
     * @return 成功返回1, 如果存在和发生异常返回null
     */
    @Override
    public Long setnx(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.setnx(key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 设置key value并制定这个键值的有效期
     *
     * @param key
     * @param value
     * @param seconds 单位:秒
     * @return 成功返回OK, 失败和异常返回null
     */
    @Override
    public String setex(String key, int seconds, String value) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.setex(key, seconds, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 减去指定的值
     *
     * @param key
     * @param integer
     * @return
     */
    @Override
    public Long decrBy(String key, long integer) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.decrBy(key, integer);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 对key的值做减减操作,如果key不存在,则设置key为-1
     *
     * @param key
     * @return
     */
    @Override
    public Long decr(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.decr(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给指定的value加值,如果key不存在,则这是value为该值
     *
     * @param key
     * @param integer
     * @return
     */
    @Override
    public Long incrBy(String key, long integer) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.incrBy(key, integer);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Double incrByFloat(String s, double v) {
        return null;
    }

    /**
     * 通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1
     *
     * @param key
     * @return 加值后的结果
     */
    @Override
    public Long incr(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.incr(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }


    /**
     * 通过key向指定的value值追加值
     *
     * @param key
     * @param str
     * @return 成功返回添加后value的长度, 失败返回添加的value的长度, 异常返回null
     */
    @Override
    public Long append(String key, String str) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.append(key, str);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public String substr(String s, int i, int i1) {
        return null;
    }

    /**
     * 通过key给field设置指定的值,如果key不存在,则先创建
     *
     * @param key
     * @param field 字段
     * @param value
     * @return 如果存在返回0 异常返回null
     */
    @Override
    public Long hset(String key, String field, String value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hset(key, field, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key 和 field 获取指定的value
     *
     * @param key
     * @param field
     * @return 没有返回null
     */
    @Override
    public String hget(String key, String field) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hget(key, field);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    @Override
    public Long hsetnx(String key, String field, String value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hsetnx(key, field, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }


    /**
     * 批量的设置key:value,可以一个
     * example:
     * obj.mset(new String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回OK, 失败/异常返回null
     */
    @Override
    public String mset(String... keysvalues) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.mset(keysvalues);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 批量的设置key:value,可以一个,如果key已经存在则会失败,操作会回滚
     * example:
     * obj.msetnx(new String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回1, 失败返回0
     */
    @Override
    public Long msetnx(String... keysvalues) {
        Jedis jedis = null;
        Long res = 0L;
        try {
            jedis = pool.getResource();
            res = jedis.msetnx(keysvalues);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key同时设置 hash的多个field
     *
     * @param key
     * @param hash
     * @return 返回OK 异常返回null
     */
    @Override
    public String hmset(String key, Map<String, String> hash) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hmset(key, hash);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过批量的key获取批量的value
     *
     * @param keys string数组 也可以是一个key
     * @return 成功返回value的集合, 失败返回null的集合, 异常返回空
     */
    @Override
    public List<String> mget(String... keys) {
        Jedis jedis = null;
        List<String> values = null;
        try {
            jedis = pool.getResource();
            values = jedis.mget(keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return values;
    }

    /**
     * 通过key 和 fields 获取指定的value 如果没有对应的value则返回null
     *
     * @param key
     * @param fields 可以使 一个String 也可以是 String数组
     * @return
     */
    @Override
    public List<String> hmget(String key, String... fields) {
        Jedis jedis = null;
        List<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hmget(key, fields);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key给指定的field的value加上给定的值
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    @Override
    public Long hincrBy(String key, String field, long value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hincrBy(key, field, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key和field判断是否有指定的value存在
     *
     * @param key
     * @param field
     * @return
     */
    @Override
    public Boolean hexists(String key, String field) {
        Jedis jedis = null;
        Boolean res = false;
        try {
            jedis = pool.getResource();
            res = jedis.hexists(key, field);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key 删除指定的field
     *
     * @param key
     * @param fields 可以是 一个 field 也可以是 一个数组
     * @return
     */
    @Override
    public Long hdel(String key, String... fields) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hdel(key, fields);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回field的数量
     *
     * @param key
     * @return
     */
    @Override
    public Long hlen(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hlen(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回所有的field
     *
     * @param key
     * @return
     */
    @Override
    public Set<String> hkeys(String key) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hkeys(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回所有和key有关的value
     *
     * @param key
     * @return
     */
    @Override
    public List<String> hvals(String key) {
        Jedis jedis = null;
        List<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hvals(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取所有的field和value
     *
     * @param key
     * @return
     */
    @Override
    public Map<String, String> hgetAll(String key) {
        Jedis jedis = null;
        Map<String, String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.hgetAll(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key向list尾部添加字符串
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    @Override
    public Long rpush(String key, String... strs) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.rpush(key, strs);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key向list头部添加字符串
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    @Override
    public Long lpush(String key, String... strs) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.lpush(key, strs);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回list的长度
     *
     * @param key
     * @return
     */
    @Override
    public Long llen(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.llen(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取list指定下标位置的value
     * 如果start 为 0 end 为 -1 则返回全部的list中的value
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<String> lrange(String key, long start, long end) {
        Jedis jedis = null;
        List<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.lrange(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key保留list中从strat下标开始到end下标结束的value值
     *
     * @param key
     * @param start
     * @param end
     * @return 成功返回OK
     */
    @Override
    public String ltrim(String key, long start, long end) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.ltrim(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取list中指定下标位置的value
     *
     * @param key
     * @param index
     * @return 如果没有返回null
     */
    @Override
    public String lindex(String key, long index) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.lindex(key, index);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key设置list指定下标位置的value
     * 如果下标超过list里面value的个数则报错
     *
     * @param key
     * @param index 从0开始
     * @param value
     * @return 成功返回OK
     */
    @Override
    public String lset(String key, long index, String value) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.lset(key, index, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key从对应的list中删除指定的count个 和 value相同的元素
     *
     * @param key
     * @param count 当count为0时删除全部
     * @param value
     * @return 返回被删除的个数
     */
    @Override
    public Long lrem(String key, long count, String value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.lrem(key, count, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key从list的头部删除一个value,并返回该value
     *
     * @param key
     * @return
     */
    @Override
    public String lpop(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.lpop(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key从list尾部删除一个value,并返回该元素
     *
     * @param key
     * @return
     */
    @Override
    public String rpop(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.rpop(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value
     * 如果第一个list为空或者不存在则返回null
     *
     * @param srckey
     * @param dstkey
     * @return
     */
    @Override
    public String rpoplpush(String srckey, String dstkey) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.rpoplpush(srckey, dstkey);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key向指定的set中添加value
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 添加成功的个数
     */
    @Override
    public Long sadd(String key, String... members) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sadd(key, members);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取set中所有的value
     *
     * @param key
     * @return
     */
    @Override
    public Set<String> smembers(String key) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.smembers(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key删除set中对应的value值
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 删除的个数
     */
    @Override
    public Long srem(String key, String... members) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.srem(key, members);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key随机删除一个set中的value并返回该值
     *
     * @param key
     * @return
     */
    @Override
    public String spop(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.spop(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Set<String> spop(String s, long l) {
        return null;
    }

    /**
     * 通过key获取set中value的个数
     *
     * @param key
     * @return
     */
    @Override
    public Long scard(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.scard(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key判断value是否是set中的元素
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Boolean sismember(String key, String member) {
        Jedis jedis = null;
        Boolean res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sismember(key, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取set中随机的value,不删除元素
     *
     * @param key
     * @return
     */
    @Override
    public String srandmember(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = pool.getResource();
            res = jedis.srandmember(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public List<String> srandmember(String s, int i) {
        return null;
    }

    /**
     * 通过key获取value值的长度
     *
     * @param key
     * @return 失败返回null
     */
    @Override
    public Long strlen(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.strlen(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取set中的差集
     * 以第一个set为标准
     *
     * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    @Override
    public Set<String> sdiff(String... keys) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sdiff(keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取set中的差集并存入到另一个key中
     * 以第一个set为标准
     *
     * @param dstkey 差集存入的key
     * @param keys   可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    @Override
    public Long sdiffstore(String dstkey, String... keys) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sdiffstore(dstkey, keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取指定set中的交集
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Set<String> sinter(String... keys) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sinter(keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取指定set中的交集 并将结果存入新的set中
     *
     * @param dstkey
     * @param keys   可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Long sinterstore(String dstkey, String... keys) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sinterstore(dstkey, keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回所有set的并集
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Set<String> sunion(String... keys) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sunion(keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回所有set的并集,并存入到新的set中
     *
     * @param dstkey
     * @param keys   可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Long sunionstore(String dstkey, String... keys) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.sunionstore(dstkey, keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key将set中的value移除并添加到第二个set中
     *
     * @param srckey 需要移除的
     * @param dstkey 添加的
     * @param member set中的value
     * @return
     */
    @Override
    public Long smove(String srckey, String dstkey, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.smove(srckey, dstkey, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的
     * 如果该value已经存在则根据score更新元素
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    @Override
    public Long zadd(String key, double score, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zadd(key, score, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的
     * 如果该value已经存在则根据score更新元素
     *
     * @param key
     * @param scoreMembers
     * @return
     */
    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zadd(key, scoreMembers);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Set<String> zrange(String s, long l, long l1) {
        return null;
    }

    /**
     * 通过key删除在zset中指定的value
     *
     * @param key
     * @param members 可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Long zrem(String key, String... members) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zrem(key, members);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key增加该zset中value的score的值
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    @Override
    public Double zincrby(String key, double score, String member) {
        Jedis jedis = null;
        Double res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zincrby(key, score, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回zset中value的排名
     * 下标从小到大排序
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Long zrank(String key, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zrank(key, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回zset中value的排名
     * 下标从大到小排序
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Long zrevrank(String key, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zrevrank(key, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key将获取score从start到end中zset的value
     * socre从大到小排序
     * 当start为0 end为-1时返回全部
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zrevrange(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Set<Tuple> zrangeWithScores(String s, long l, long l1) {
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String s, long l, long l1) {
        return null;
    }

    /**
     * 通过key返回zset中的value个数
     *
     * @param key
     * @return
     */
    @Override
    public Long zcard(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zcard(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key获取zset中value的score值
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Double zscore(String key, String member) {
        Jedis jedis = null;
        Double res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zscore(key, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public List<String> sort(String s) {
        return null;
    }

    @Override
    public List<String> sort(String s, SortingParams sortingParams) {
        return null;
    }

    @Override
    public Long zcount(String s, double v, double v1) {
        return null;
    }

    /**
     * 返回指定区间内zset中value的数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    @Override
    public Long zcount(String key, String min, String max) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zcount(key, min, max);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    @Override
    public Set<String> zrangeByScore(String key, double max, double min) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zrevrangeByScore(key, max, min);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    @Override
    public Set<String> zrangeByScore(String key, String max, String min) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zrevrangeByScore(key, max, min);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Set<String> zrevrangeByScore(String s, double v, double v1) {
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String s, double v, double v1, int i, int i1) {
        return null;
    }

    @Override
    public Set<String> zrevrangeByScore(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String s, String s1, String s2, int i, int i1) {
        return null;
    }

    @Override
    public Set<String> zrevrangeByScore(String s, double v, double v1, int i, int i1) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v1) {
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, double v, double v1) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v1, int i, int i1) {
        return null;
    }

    @Override
    public Set<String> zrevrangeByScore(String s, String s1, String s2, int i, int i1) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, String s1, String s2, int i, int i1) {
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, double v, double v1, int i, int i1) {
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, String s1, String s2, int i, int i1) {
        return null;
    }

    /**
     * 返回满足pattern表达式的所有key
     * keys(*)
     * 返回所有的key
     *
     * @param pattern
     * @return
     */
    @Override
    public Set<String> keys(String pattern) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = pool.getResource();
            res = jedis.keys(pattern);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key删除给定区间内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zremrangeByRank(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    /**
     * 通过key删除指定score内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.zremrangeByScore(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Long zremrangeByScore(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Long zlexcount(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Set<String> zrangeByLex(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Set<String> zrangeByLex(String s, String s1, String s2, int i, int i1) {
        return null;
    }

    @Override
    public Set<String> zrevrangeByLex(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Set<String> zrevrangeByLex(String s, String s1, String s2, int i, int i1) {
        return null;
    }

    @Override
    public Long zremrangeByLex(String s, String s1, String s2) {
        return null;
    }

    /**
     * 通过key在list指定的位置之前或者之后添加字符串元素
     *
     * @param key
     * @param where LIST_POSITION枚举类型
     * @param pivot list里面的value
     * @param value 添加的value
     * @return
     */
    @Override
    public Long linsert(String key, BinaryClient.LIST_POSITION where,
                        String pivot, String value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.linsert(key, where, pivot, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public Long lpushx(String s, String... strings) {
        return null;
    }

    @Override
    public Long rpushx(String key, String... strs) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = pool.getResource();
            res = jedis.rpushx(key, strs);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public List<String> blpop(String s) {
        return null;
    }

    @Override
    public List<String> blpop(int i, String s) {
        return null;
    }

    @Override
    public List<String> brpop(String s) {
        return null;
    }

    @Override
    public List<String> brpop(int i, String s) {
        return null;
    }

    @Override
    public Long del(String s) {
        return null;
    }

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param keys 一个key  也可以使 string 数组
     * @return 返回删除成功的个数
     */
    @Override
    public Long del(String... keys) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.del(keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String echo(String s) {
        return null;
    }

    @Override
    public Long move(String s, int i) {
        return null;
    }

    @Override
    public Long bitcount(String s) {
        return null;
    }

    @Override
    public Long bitcount(String s, long l, long l1) {
        return null;
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String s, int i) {
        return null;
    }

    @Override
    public ScanResult<String> sscan(String s, int i) {
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(String s, int i) {
        return null;
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String s, String s1) {
        return null;
    }

    @Override
    public ScanResult<String> sscan(String s, String s1) {
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(String s, String s1) {
        return null;
    }

    @Override
    public Long pfadd(String s, String... strings) {
        return null;
    }

    @Override
    public long pfcount(String s) {
        return 0;
    }

    /**
     * 销毁连接池
     *
     * @param pool
     */
    @Override
    public void destroy(JedisPool pool) {
        pool.destroy();
    }

    /**
     * 获取连接池
     */
    @Override
	public Jedis getResource() {
		return pool.getResource();
	}

	/**
     * 关闭连接池
     */
	@Override
	public void returnResource(Jedis jedis) {
		if (jedis != null) {
            jedis.close();
        }		
	}
}
