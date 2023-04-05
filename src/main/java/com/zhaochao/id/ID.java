package com.zhaochao.id;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


/**
 * 基本Redis生成ID
 */
@Component
public class ID {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static Long REDIS_ID;
    /**
     * redis KEY 不同项目需要修改
     */
    @Value("${rid.redisKey}")
    private String ID_REDIS_KEY = "RID";
    /**
     * 起始时间戳
     */
    @Value("${rid.startStamp}")
    private Long startStamp = 1577808000000L;

    /**
     * 机器id所占的位数 最多机器节点2^5=32个
     */
    @Value("${rid.workerIdBits}")
    private final long workerIdBits = 5L;
    /**
     * 序列号所占的位数 决定单个容器每毫秒生成速度，默认每毫秒生成 2^7=128
     */
    @Value("${rid.sequenceBits}")
    private final long sequenceBits = 7L;
    /**
     * 时间戳位数 从startStamp开始可以  2^41/(1000606024365)=69，大概可以使用69年。
     */
    private final long timeStampBits = 41L;

    private Long workerId;

    @PostConstruct
    void init() {
        REDIS_ID = stringRedisTemplate.opsForValue().increment(ID_REDIS_KEY) % maxWorkerId;
    }


    /**
     * 时间戳最大值
     */
    private final long maxTimeStamp = ~(-1L << timeStampBits);
    /**
     * 机器id的最大值
     */
    private final long maxWorkerId = ~(-1L << workerIdBits);
    /**
     * 序列号的最大值
     */
    private final long maxSequence = ~(-1L << sequenceBits);


    private long sequence = 0L;
    private long lastTimeStamp = -1L;


    public synchronized long id() {
        long currentTimeStamp = timeGen();
        if (currentTimeStamp < lastTimeStamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimeStamp - currentTimeStamp));
        }
        if (lastTimeStamp == currentTimeStamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                currentTimeStamp = tilNextMillis(lastTimeStamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimeStamp = currentTimeStamp;
        return ((currentTimeStamp - startStamp) & maxTimeStamp) << (sequenceBits + workerIdBits) | (workerId << sequenceBits) | sequence;
    }


    private long tilNextMillis(long lastTimeStamp) {
        long timeStamp = timeGen();
        while (timeStamp <= lastTimeStamp) {
            timeStamp = timeGen();
        }
        return timeStamp;
    }


    private long timeGen() {
        return System.currentTimeMillis();
    }


    private static class SingletonHolder {
        private static ID ID;

        static {
            ID = new ID(REDIS_ID);
        }
    }

    public static ID getInstance() {
        return SingletonHolder.ID;
    }


    private ID() {
    }

    private ID(long workerId) {
        this.workerId = workerId;
    }
}
