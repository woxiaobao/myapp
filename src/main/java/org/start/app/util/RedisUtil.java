package org.start.app.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * Redis工具类
 */
@Component
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ====== String类型操作 ======
    
    /**
     * 设置带过期时间的缓存
     */
    public void setEx(String key, Object value, long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 计数器
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ====== Hash类型操作 ======
    
    /**
     * 设置Hash字段
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取Hash所有字段
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // ====== List类型操作 ======
    
    /**
     * 从左侧插入列表
     */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 获取指定范围的列表元素
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ====== Set类型操作 ======
    
    /**
     * 添加Set元素
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取Set所有成员
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // ====== Sorted Set类型操作 ======
    
    /**
     * 添加分数
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取排名前N的元素
     */
    public Set<Object> zTopN(String key, long n) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, n - 1);
    }

    /**
     * 获取值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 递减
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 判断Set中是否存在某个值
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    // ====== Bitmap操作 ======

    /**
     * 设置位图中指定位置的值
     *
     * @param key    键
     * @param offset 偏移量
     * @param value  值
     * @return 是否设置成功
     */
    public Boolean setBit(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    /**
     * 获取位图中指定位置的值
     *
     * @param key    键
     * @param offset 偏移量
     * @return 该位置的值
     */
    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 统计位图中值为1的个数
     *
     * @param key 键
     * @return 值为1的个数
     */
    public Long bitCount(String key) {
        return redisTemplate.execute((connection) -> 
            connection.bitCount(key.getBytes()), true);
    }

    // ====== HyperLogLog操作 ======

    /**
     * 添加指定元素到 HyperLogLog
     *
     * @param key    键
     * @param value  值
     * @return 是否添加成功
     */
    public Long pfAdd(String key, Object... values) {
        return redisTemplate.opsForHyperLogLog().add(key, values);
    }

    /**
     * 获取 HyperLogLog 的基数估算值
     *
     * @param key 键
     * @return 基数估算值
     */
    public Long pfCount(String key) {
        return redisTemplate.opsForHyperLogLog().size(key);
    }

    /**
     * 将多个 HyperLogLog 合并为一个
     *
     * @param destKey 目标键
     * @param sourceKeys 源键数组
     * @return 合并后的基数估算值
     */
    public Long pfMerge(String destKey, String... sourceKeys) {
        return redisTemplate.opsForHyperLogLog().union(destKey, sourceKeys);
    }

    // ====== Stream操作 ======

    /**
     * 添加消息到Stream
     *
     * @param key 键
     * @param message 消息内容
     * @return 消息ID
     */
    public String xAdd(String key, Map<String, String> message) {
        Map<String, Object> objectMap = new HashMap<>();
        message.forEach((k, v) -> objectMap.put(k, (Object) v));
        
        MapRecord<String, String, Object> record = StreamRecords.newRecord()
            .in(key)
            .ofMap(objectMap);
        return redisTemplate.opsForStream().add(record).getValue();
    }

    /**
     * 获取Stream中的消息列表
     *
     * @param key 键
     * @param count 获取的消息数量
     * @return 消息列表
     */
    public List<Map<String, String>> xRange(String key, int count) {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
            .range(key, Range.unbounded());
        
        return records.stream()
            .limit(count)
            .map(record -> {
                Map<Object, Object> value = record.getValue();
                Map<String, String> result = new HashMap<>();
                value.forEach((k, v) -> result.put(k.toString(), v.toString()));
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 创建消费者组
     *
     * @param key 键
     * @param groupName 消费者组名称
     * @return 是否创建成功
     */
    public String xGroupCreate(String key, String groupName) {
        try {
            redisTemplate.opsForStream().createGroup(key, ReadOffset.from("0-0"), groupName);
            return groupName;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 读取消费者组中的消息
     *
     * @param key 键
     * @param groupName 消费者组名称
     * @param consumerName 消费者名称
     * @param count 获取的消息数量
     * @return 消息列表
     */
    public List<Map<String, String>> xReadGroup(String key, String groupName, 
                                              String consumerName, int count) {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
            .read(Consumer.from(groupName, consumerName),
                StreamOffset.create(key, ReadOffset.lastConsumed()));
        
        return records.stream()
            .limit(count)
            .map(record -> {
                Map<Object, Object> value = record.getValue();
                Map<String, String> result = new HashMap<>();
                value.forEach((k, v) -> result.put(k.toString(), v.toString()));
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 确认消息已处理
     *
     * @param key 键
     * @param groupName 消费者组名称
     * @param recordIds 消息ID列表
     * @return 确认的消息数量
     */
    public Long xAck(String key, String groupName, String... recordIds) {
        return redisTemplate.opsForStream().acknowledge(key, groupName, recordIds);
    }
} 