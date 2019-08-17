package com.book.dao;


import com.book.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.HashMap;
import java.util.concurrent.TimeUnit;


@Slf4j
public class RedisDaoTest extends StartApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 操作String
     */
    @Test
    public void String() {
        log.info("开始测试");
        User user = new User();
        user.setUserName("cooper");
        user.setUserPassword("密码");
        redisTemplate.opsForValue().set("a", user);
        redisTemplate.opsForValue().set("b", user);
        //redisTemplate.opsForValue().set("testt", "test",180,TimeUnit.SECONDS);
        log.info("取出数据的数据为 {}", redisTemplate.opsForValue().get("a"));

    }

    /**
     * 操作hash
     */
    @Test
    public void hash() {
        //添加
        redisTemplate.opsForHash().put("spring-boot-hash", "name", "wql");
        redisTemplate.opsForHash().put("spring-boot-hash", "password", "123");
        System.out.println(redisTemplate.opsForHash().get("spring-boot-hash", "name"));

        //删除
        redisTemplate.opsForHash().delete("spring-boot-hash", "name");


        //添加
        HashMap<String, String> map = new HashMap<>(4);
        map.put("name", "cooper");
        map.put("password", "123");
        redisTemplate.opsForHash().putAll("map", map);

        HashMap map1 = (HashMap) redisTemplate.opsForHash().entries("map");
        System.out.println("name=" + map1.get("name"));

    }

    /**
     * 操作list
     */
    @Test
    public void list() {

        redisTemplate.opsForList().rightPush("list1", "第一条数据");
        redisTemplate.opsForList().rightPush("list1", "第二条数据");
        redisTemplate.opsForList().rightPush("list1", "第三条数据");

        System.out.println(redisTemplate.opsForList().leftPop("list1"));


    }

    /**
     * 操作set
     */
    @Test
    public void set() {
        redisTemplate.opsForSet().add("set1", "asa", "da");
        System.out.println(redisTemplate.opsForSet().pop("set1"));

    }

    /**
     * 操作有序set
     */
    @Test
    public void zset() {
        redisTemplate.opsForZSet().add("key", "value1", 1.0);
        redisTemplate.opsForZSet().add("key", "value2", 5.0);
        redisTemplate.opsForZSet().add("key", "value3", 6.0);
        redisTemplate.opsForZSet().add("key", "value4", 4.0);
        redisTemplate.opsForZSet().add("key", "value5", 5.0);
        redisTemplate.opsForZSet().add("key", "value6", 6.0);
        redisTemplate.opsForZSet().add("key", "value7", 7.0);
        redisTemplate.opsForZSet().add("key", "value8", 8.0);

        //修改score 实际应用中score可以是积分如类的数据
        redisTemplate.opsForZSet().incrementScore("key", "value1", 9.0);

        //可以做排行榜
        System.out.println(redisTemplate.opsForZSet().rangeByScore("key", 6.0, 10.0));

        //判断value在zset中的排名  zrank
        System.out.println("排名是" + redisTemplate.opsForZSet().rank("key", "value3"));

        //查询value对应的score
        System.out.println(redisTemplate.opsForZSet().score("key", "value3"));


        System.out.println(redisTemplate.opsForZSet().reverseRange("key", 1, 100));


    }


}