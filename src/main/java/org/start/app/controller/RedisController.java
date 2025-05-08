package org.start.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.start.app.util.RedisUtil;
import org.start.app.util.RedisLockUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag(name = "Redis示例", description = "Redis各种数据类型使用示例")
@RestController
@RequestMapping("/redis")
public class RedisController {
    private static final Logger logger = LoggerFactory.getLogger(RedisController.class);

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
        try {
            logger.debug("Setting verification code for phone: {}", phone);
            redisUtil.setEx("verify_code:" + phone, code, 60, TimeUnit.SECONDS);
            return "验证码已发送";
        } catch (Exception e) {
            logger.error("Error setting verification code for phone: {}", phone, e);
            throw e;
        }
    }

    @Operation(summary = "文章点赞", description = "String示例2：文章点赞计数")
    @PostMapping("/string/like")
    public Long likeArticle(@RequestParam String articleId) {
        try {
            logger.debug("Incrementing likes for article: {}", articleId);
            return redisUtil.increment("article_likes:" + articleId, 1);
        } catch (Exception e) {
            logger.error("Error incrementing likes for article: {}", articleId, e);
            throw e;
        }
    }

    // ====== Hash类型示例 ======

    @Operation(summary = "更新用户信息", description = "Hash示例1：存储用户信息")
    @PostMapping("/hash/userInfo")
    public String updateUserInfo(@RequestParam String userId, @RequestParam String field, @RequestParam String value) {
        try {
            logger.debug("Updating user info for userId: {}, field: {}", userId, field);
            redisUtil.hSet("user:" + userId, field, value);
            return "用户信息已更新";
        } catch (Exception e) {
            logger.error("Error updating user info for userId: {}, field: {}", userId, field, e);
            throw e;
        }
    }

    @Operation(summary = "获取用户信息", description = "Hash示例2：获取用户完整信息")
    @GetMapping("/hash/userInfo/{userId}")
    public Map<Object, Object> getUserInfo(@PathVariable String userId) {
        try {
            logger.debug("Getting user info for userId: {}", userId);
            return redisUtil.hGetAll("user:" + userId);
        } catch (Exception e) {
            logger.error("Error getting user info for userId: {}", userId, e);
            throw e;
        }
    }

    // ====== List类型示例 ======

    @Operation(summary = "添加操作日志", description = "List示例1：记录用户操作日志")
    @PostMapping("/list/opLog")
    public Long addOperationLog(@RequestParam String userId, @RequestParam String operation) {
        try {
            logger.debug("Adding operation log for userId: {}, operation: {}", userId, operation);
            return redisUtil.lPush("user_op_log:" + userId, operation);
        } catch (Exception e) {
            logger.error("Error adding operation log for userId: {}", userId, e);
            throw e;
        }
    }

    @Operation(summary = "获取最近操作", description = "List示例2：获取用户最近的10条操作记录")
    @GetMapping("/list/opLog/{userId}")
    public List<Object> getRecentOperations(@PathVariable String userId) {
        try {
            logger.debug("Getting recent operations for userId: {}", userId);
            return redisUtil.lRange("user_op_log:" + userId, 0, 9);
        } catch (Exception e) {
            logger.error("Error getting recent operations for userId: {}", userId, e);
            throw e;
        }
    }

    // ====== Set类型示例 ======

    @Operation(summary = "关注用户", description = "Set示例1：用户关注功能")
    @PostMapping("/set/follow")
    public Long followUser(@RequestParam String userId, @RequestParam String... followIds) {
        try {
            logger.debug("User {} following users: {}", userId, String.join(", ", followIds));
            return redisUtil.sAdd("user_follows:" + userId, (Object[]) followIds);
        } catch (Exception e) {
            logger.error("Error following users for userId: {}", userId, e);
            throw e;
        }
    }

    @Operation(summary = "获取关注列表", description = "Set示例2：获取用户的所有关注")
    @GetMapping("/set/follow/{userId}")
    public Set<Object> getFollowList(@PathVariable String userId) {
        try {
            logger.debug("Getting follow list for userId: {}", userId);
            return redisUtil.sMembers("user_follows:" + userId);
        } catch (Exception e) {
            logger.error("Error getting follow list for userId: {}", userId, e);
            throw e;
        }
    }

    // ====== Sorted Set类型示例 ======

    @Operation(summary = "商品购买", description = "Sorted Set示例1：记录商品销量")
    @PostMapping("/zset/purchase")
    public Boolean purchaseProduct(@RequestParam String productId, @RequestParam Integer quantity) {
        try {
            logger.debug("Recording purchase for productId: {}, quantity: {}", productId, quantity);
            return redisUtil.zAdd("product_sales", productId, quantity);
        } catch (Exception e) {
            logger.error("Error recording purchase for productId: {}", productId, e);
            throw e;
        }
    }

    @Operation(summary = "热销商品", description = "Sorted Set示例2：获取销量最高的5个商品")
    @GetMapping("/zset/hotProducts")
    public Set<Object> getHotProducts() {
        try {
            logger.debug("Getting hot products");
            return redisUtil.zTopN("product_sales", 5);
        } catch (Exception e) {
            logger.error("Error getting hot products", e);
            throw e;
        }
    }

    @Operation(summary = "分布式锁示例", description = "使用分布式锁执行秒杀操作")
    @PostMapping("/lock/seckill")
    public String seckill(@RequestParam String productId, @RequestParam String userId) {
        try {
            logger.debug("Processing seckill for productId: {}, userId: {}", productId, userId);
            return redisLockUtil.executeWithLock(
                "seckill:" + productId,
                () -> {
                    String stockKey = "product:stock:" + productId;
                    Integer stock = (Integer) redisUtil.get(stockKey);
                    if (stock == null || stock <= 0) {
                        return "商品已售罄";
                    }

                    String boughtKey = "product:bought:" + productId;
                    if (Boolean.TRUE.equals(redisUtil.sIsMember(boughtKey, userId))) {
                        return "您已购买过该商品";
                    }

                    redisUtil.decrement(stockKey, 1);
                    redisUtil.sAdd(boughtKey, userId);
                    
                    return "抢购成功";
                }
            );
        } catch (Exception e) {
            logger.error("Error processing seckill for productId: {}, userId: {}", productId, userId, e);
            throw e;
        }
    }

    @Operation(summary = "初始化商品库存", description = "初始化商品库存，用于测试分布式锁")
    @PostMapping("/lock/init-stock")
    public String initStock(@RequestParam String productId, @RequestParam Integer stock) {
        try {
            logger.debug("Initializing stock for productId: {}, stock: {}", productId, stock);
            String stockKey = "product:stock:" + productId;
            redisUtil.set(stockKey, stock);
            return "库存初始化成功";
        } catch (Exception e) {
            logger.error("Error initializing stock for productId: {}", productId, e);
            throw e;
        }
    }

    // ====== Bitmap类型示例 ======

    @Operation(summary = "用户签到", description = "Bitmap示例1：记录用户每月签到情况")
    @PostMapping("/bitmap/sign")
    public Boolean userSign(@RequestParam String userId) {
        try {
            logger.debug("Recording sign-in for userId: {}", userId);
            LocalDate today = LocalDate.now();
            String key = String.format("user:sign:%s:%s", userId, today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            return redisUtil.setBit(key, today.getDayOfMonth() - 1, true);
        } catch (Exception e) {
            logger.error("Error recording sign-in for userId: {}", userId, e);
            throw e;
        }
    }

    @Operation(summary = "获取用户签到统计", description = "Bitmap示例2：获取用户当月签到次数")
    @GetMapping("/bitmap/sign/count/{userId}")
    public Long getSignCount(@PathVariable String userId) {
        try {
            logger.debug("Getting sign-in count for userId: {}", userId);
            String key = String.format("user:sign:%s:%s", userId, 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
            return redisUtil.bitCount(key);
        } catch (Exception e) {
            logger.error("Error getting sign-in count for userId: {}", userId, e);
            throw e;
        }
    }

    // ====== HyperLogLog类型示例 ======

    @Operation(summary = "记录用户访问", description = "HyperLogLog示例1：统计网页UV（独立访客数）")
    @PostMapping("/hll/pageview")
    public Long recordPageView(@RequestParam String pageId, @RequestParam String userId) {
        try {
            logger.debug("Recording page view - pageId: {}, userId: {}", pageId, userId);
            String key = "page:uv:" + pageId;
            redisUtil.pfAdd(key, userId);
            return redisUtil.pfCount(key);
        } catch (Exception e) {
            logger.error("Error recording page view - pageId: {}, userId: {}", pageId, userId, e);
            throw e;
        }
    }

    @Operation(summary = "合并多个页面UV", description = "HyperLogLog示例2：合并多个页面的UV统计")
    @PostMapping("/hll/pageview/merge")
    public Long mergePageViews(@RequestParam List<String> pageIds) {
        try {
            logger.debug("Merging page views for pages: {}", pageIds);
            String[] keys = pageIds.stream()
                .map(id -> "page:uv:" + id)
                .toArray(String[]::new);
            String mergeKey = "page:uv:merged:" + UUID.randomUUID();
            redisUtil.pfMerge(mergeKey, keys);
            return redisUtil.pfCount(mergeKey);
        } catch (Exception e) {
            logger.error("Error merging page views for pages: {}", pageIds, e);
            throw e;
        }
    }

    // ====== Stream类型示例 ======

    @Operation(summary = "添加订单消息", description = "Stream示例1：记录订单流水信息")
    @PostMapping("/stream/order")
    public String addOrderMessage(@RequestParam String orderId, 
                                @RequestParam String userId,
                                @RequestParam String product,
                                @RequestParam Double amount) {
        try {
            logger.debug("Adding order message - orderId: {}, userId: {}", orderId, userId);
            Map<String, String> message = new HashMap<>();
            message.put("orderId", orderId);
            message.put("userId", userId);
            message.put("product", product);
            message.put("amount", String.valueOf(amount));
            message.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            return redisUtil.xAdd("order:stream", message);
        } catch (Exception e) {
            logger.error("Error adding order message - orderId: {}", orderId, e);
            throw e;
        }
    }

    @Operation(summary = "获取订单消息", description = "Stream示例2：获取最近的订单消息")
    @GetMapping("/stream/order/recent")
    public List<Map<String, String>> getRecentOrders(@RequestParam(defaultValue = "10") int count) {
        try {
            logger.debug("Getting recent {} orders", count);
            return redisUtil.xRange("order:stream", count);
        } catch (Exception e) {
            logger.error("Error getting recent orders", e);
            throw e;
        }
    }
} 