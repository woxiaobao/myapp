package org.start.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.start.app.util.RedisUtil;
import org.start.app.util.RedisLockUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Tag(name = "Redis示例", description = "Redis各种数据类型使用示例")
@RestController
@RequestMapping("/redis")
public class RedisController {

    private final RedisUtil redisUtil;
    private final RedisLockUtil redisLockUtil;

    public RedisController(RedisUtil redisUtil, RedisLockUtil redisLockUtil) {
        this.redisUtil = redisUtil;
        this.redisLockUtil = redisLockUtil;
    }

    // ====== String类型示例 ======

    @Operation(summary = "设置验证码", description = "String示例1：设置60秒过期的验证码")
    @PostMapping("/string/verifyCode")
    public String setVerifyCode(@RequestParam String phone, @RequestParam String code) {
        redisUtil.setEx("verify_code:" + phone, code, 60, TimeUnit.SECONDS);
        return "验证码已发送";
    }

    @Operation(summary = "文章点赞", description = "String示例2：文章点赞计数")
    @PostMapping("/string/like")
    public Long likeArticle(@RequestParam String articleId) {
        return redisUtil.increment("article_likes:" + articleId, 1);
    }

    // ====== Hash类型示例 ======

    @Operation(summary = "更新用户信息", description = "Hash示例1：存储用户信息")
    @PostMapping("/hash/userInfo")
    public String updateUserInfo(@RequestParam String userId, @RequestParam String field, @RequestParam String value) {
        redisUtil.hSet("user:" + userId, field, value);
        return "用户信息已更新";
    }

    @Operation(summary = "获取用户信息", description = "Hash示例2：获取用户完整信息")
    @GetMapping("/hash/userInfo/{userId}")
    public Map<Object, Object> getUserInfo(@PathVariable String userId) {
        return redisUtil.hGetAll("user:" + userId);
    }

    // ====== List类型示例 ======

    @Operation(summary = "添加操作日志", description = "List示例1：记录用户操作日志")
    @PostMapping("/list/opLog")
    public Long addOperationLog(@RequestParam String userId, @RequestParam String operation) {
        return redisUtil.lPush("user_op_log:" + userId, operation);
    }

    @Operation(summary = "获取最近操作", description = "List示例2：获取用户最近的10条操作记录")
    @GetMapping("/list/opLog/{userId}")
    public List<Object> getRecentOperations(@PathVariable String userId) {
        return redisUtil.lRange("user_op_log:" + userId, 0, 9);
    }

    // ====== Set类型示例 ======

    @Operation(summary = "关注用户", description = "Set示例1：用户关注功能")
    @PostMapping("/set/follow")
    public Long followUser(@RequestParam String userId, @RequestParam String... followIds) {
        return redisUtil.sAdd("user_follows:" + userId, (Object[]) followIds);
    }

    @Operation(summary = "获取关注列表", description = "Set示例2：获取用户的所有关注")
    @GetMapping("/set/follow/{userId}")
    public Set<Object> getFollowList(@PathVariable String userId) {
        return redisUtil.sMembers("user_follows:" + userId);
    }

    // ====== Sorted Set类型示例 ======

    @Operation(summary = "商品购买", description = "Sorted Set示例1：记录商品销量")
    @PostMapping("/zset/purchase")
    public Boolean purchaseProduct(@RequestParam String productId, @RequestParam Integer quantity) {
        return redisUtil.zAdd("product_sales", productId, quantity);
    }

    @Operation(summary = "热销商品", description = "Sorted Set示例2：获取销量最高的5个商品")
    @GetMapping("/zset/hotProducts")
    public Set<Object> getHotProducts() {
        return redisUtil.zTopN("product_sales", 5);
    }

    @Operation(summary = "分布式锁示例", description = "使用分布式锁执行秒杀操作")
    @PostMapping("/lock/seckill")
    public String seckill(@RequestParam String productId, @RequestParam String userId) {
        return redisLockUtil.executeWithLock(
            "seckill:" + productId,  // 锁的key
            () -> {
                // 检查库存
                String stockKey = "product:stock:" + productId;
                Integer stock = (Integer) redisUtil.get(stockKey);
                if (stock == null || stock <= 0) {
                    return "商品已售罄";
                }

                // 检查用户是否已购买
                String boughtKey = "product:bought:" + productId;
                if (Boolean.TRUE.equals(redisUtil.sIsMember(boughtKey, userId))) {
                    return "您已购买过该商品";
                }

                // 扣减库存
                redisUtil.decrement(stockKey, 1);
                // 记录购买用户
                redisUtil.sAdd(boughtKey, userId);
                
                return "抢购成功";
            }
        );
    }

    @Operation(summary = "初始化商品库存", description = "初始化商品库存，用于测试分布式锁")
    @PostMapping("/lock/init-stock")
    public String initStock(@RequestParam String productId, @RequestParam Integer stock) {
        String stockKey = "product:stock:" + productId;
        redisUtil.set(stockKey, stock);
        return "库存初始化成功";
    }
} 