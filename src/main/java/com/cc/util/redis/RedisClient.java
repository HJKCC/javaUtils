package com.cc.util.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Set;

/**
 * 封装Jedis接口，便于在JedisPool、ShardedJedisPool切换
 * 
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午6:17:51
 */
public interface RedisClient extends JedisCommands {

    public String setObject(String key, Object value);

    public String setObject(String key, Object value, int time);

    public String set(String key, String value, int time);

    public Object getObject(String key);

    public Long del(String... keys);

    public List<String> mget(String... keys);

    public String mset(String... keysvalues);

    public Long msetnx(String... keysvalues);

    public String rpoplpush(String srckey, String dstkey);

    public Set<String> sdiff(String... keys);

    public Long sdiffstore(String dstkey, String... keys);

    public Set<String> sinter(String... keys);

    Long sinterstore(String dstkey, String... keys);

    Set<String> sunion(String... keys);

    public Long sunionstore(String dstkey, String... keys);

    public Long smove(String srckey, String dstkey, String member);

    public Set<String> keys(String pattern);

    public void destroy(JedisPool pool);

	public Jedis getResource();

	public void returnResource(Jedis jedis);
}
