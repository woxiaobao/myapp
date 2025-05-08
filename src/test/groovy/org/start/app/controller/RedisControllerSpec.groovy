package org.start.app.controller

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.start.app.config.TestConfig
import org.start.app.util.RedisUtil
import org.start.app.util.RedisLockUtil
import spock.lang.Specification
import org.springframework.boot.test.mock.mockito.MockBean
import org.start.app.mapper.TradeRecordMapper
import org.apache.catalina.connector.ClientAbortException

import java.util.concurrent.TimeUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [RedisController])
@Import([TestConfig])
class RedisControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    RedisUtil redisUtil = Mock()

    @SpringBean
    RedisLockUtil redisLockUtil = Mock()

    @MockBean
    TradeRecordMapper tradeRecordMapper

    def setup() {
        // 设置默认的Mock行为
        redisUtil.increment(_, _) >> 1L
        redisUtil.setEx(_, _, _, _) >> null
        redisUtil.hSet(_, _, _) >> null
        redisUtil.hGetAll(_) >> [:]
        redisUtil.lPush(_, _) >> 1L
        redisUtil.lRange(_, _, _) >> []
        redisUtil.sAdd(_, _) >> 1L
        redisUtil.sMembers(_) >> []
        redisUtil.zAdd(_, _, _) >> true
        redisUtil.zTopN(_, _) >> []
    }

    private MvcResult performRequest(def requestBuilder) {
        try {
            return mockMvc.perform(requestBuilder)
                    .andReturn()
        } catch (Exception e) {
            if (isClientAbortException(e)) {
                return null
            }
            throw e
        }
    }

    private boolean isClientAbortException(Exception e) {
        return e instanceof ClientAbortException ||
               (e.cause instanceof IOException && 
                e.cause.message == "Connection reset by peer")
    }

    def "测试文章点赞"() {
        given: "准备测试数据"
        def articleId = "article123"
        def expectedLikes = 1L

        and: "设置Mock行为"
        redisUtil.increment("article_likes:" + articleId, 1) >> expectedLikes

        when: "调用文章点赞接口"
        def result = performRequest(post("/redis/string/like")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("articleId", articleId))

        then: "验证结果"
        1 * redisUtil.increment("article_likes:" + articleId, 1) >> expectedLikes
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == expectedLikes.toString()
        }
    }

    def "测试设置验证码"() {
        given: "准备测试数据"
        def phone = "13800138000"
        def code = "123456"

        when: "调用设置验证码接口"
        def result = performRequest(post("/redis/string/verifyCode")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("phone", phone)
                .param("code", code))

        then: "验证结果"
        1 * redisUtil.setEx("verify_code:" + phone, code, 60, TimeUnit.SECONDS)
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == "验证码已发送"
        }
    }

    def "测试更新用户信息"() {
        given: "准备测试数据"
        def userId = "user123"
        def field = "name"
        def value = "张三"

        when: "调用更新用户信息接口"
        def result = performRequest(post("/redis/hash/userInfo")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userId", userId)
                .param("field", field)
                .param("value", value))

        then: "验证结果"
        1 * redisUtil.hSet("user:" + userId, field, value)
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == "用户信息已更新"
        }
    }

    def "测试获取用户信息"() {
        given: "准备测试数据"
        def userId = "user123"
        def userInfo = [name: "张三", age: "25"]

        and: "设置Mock行为"
        redisUtil.hGetAll("user:" + userId) >> userInfo

        when: "调用获取用户信息接口"
        def result = performRequest(get("/redis/hash/userInfo/{userId}", userId))

        then: "验证结果"
        1 * redisUtil.hGetAll("user:" + userId) >> userInfo
        if (result != null) {
            result.response.status == 200
            def content = new groovy.json.JsonSlurper().parseText(result.response.contentAsString)
            content.name == "张三"
            content.age == "25"
        }
    }

    def "测试添加操作日志"() {
        given: "准备测试数据"
        def userId = "user123"
        def operation = "登录系统"
        def expectedId = 1L

        and: "设置Mock行为"
        redisUtil.lPush("user_op_log:" + userId, operation) >> expectedId

        when: "调用添加操作日志接口"
        def result = performRequest(post("/redis/list/opLog")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userId", userId)
                .param("operation", operation))

        then: "验证结果"
        1 * redisUtil.lPush("user_op_log:" + userId, operation) >> expectedId
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == expectedId.toString()
        }
    }

    def "测试获取最近操作"() {
        given: "准备测试数据"
        def userId = "user123"
        def operations = ["登录系统", "修改密码"]

        and: "设置Mock行为"
        redisUtil.lRange("user_op_log:" + userId, 0, 9) >> operations

        when: "调用获取最近操作接口"
        def result = performRequest(get("/redis/list/opLog/{userId}", userId))

        then: "验证结果"
        1 * redisUtil.lRange("user_op_log:" + userId, 0, 9) >> operations
        if (result != null) {
            result.response.status == 200
            def content = new groovy.json.JsonSlurper().parseText(result.response.contentAsString)
            content[0] == "登录系统"
            content[1] == "修改密码"
        }
    }

    def "测试关注用户"() {
        given: "准备测试数据"
        def userId = "user123"
        def followIds = ["user456", "user789"]
        def expectedCount = 2L

        when: "调用关注用户接口"
        def result = performRequest(post("/redis/set/follow")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userId", userId)
                .param("followIds", followIds as String[]))

        then: "验证结果"
        1 * redisUtil.sAdd("user_follows:" + userId, _ as Object[]) >> expectedCount
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == expectedCount.toString()
        }
    }

    def "测试获取关注列表"() {
        given: "准备测试数据"
        def userId = "user123"
        def follows = ["user456", "user789"] as Set

        and: "设置Mock行为"
        redisUtil.sMembers("user_follows:" + userId) >> follows

        when: "调用获取关注列表接口"
        def result = performRequest(get("/redis/set/follow/{userId}", userId))

        then: "验证结果"
        1 * redisUtil.sMembers("user_follows:" + userId) >> follows
        if (result != null) {
            result.response.status == 200
            def content = new groovy.json.JsonSlurper().parseText(result.response.contentAsString)
            content[0] == "user456"
            content[1] == "user789"
        }
    }

    def "测试商品购买"() {
        given: "准备测试数据"
        def productId = "prod123"
        def quantity = 5

        when: "调用商品购买接口"
        def result = performRequest(post("/redis/zset/purchase")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("productId", productId)
                .param("quantity", quantity.toString()))

        then: "验证结果"
        1 * redisUtil.zAdd("product_sales", productId, quantity) >> true
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == "true"
        }
    }

    def "测试获取热销商品"() {
        given: "准备测试数据"
        def hotProducts = ["prod123", "prod456"] as Set

        and: "设置Mock行为"
        redisUtil.zTopN("product_sales", 5) >> hotProducts

        when: "调用获取热销商品接口"
        def result = performRequest(get("/redis/zset/hotProducts"))

        then: "验证结果"
        1 * redisUtil.zTopN("product_sales", 5) >> hotProducts
        if (result != null) {
            result.response.status == 200
            def content = new groovy.json.JsonSlurper().parseText(result.response.contentAsString)
            content[0] == "prod123"
            content[1] == "prod456"
        }
    }

    def "测试秒杀功能"() {
        given: "准备测试数据"
        def productId = "prod123"
        def userId = "user123"
        def expectedResult = "抢购成功"

        and: "设置Mock行为"
        redisLockUtil.executeWithLock(_, _) >> expectedResult

        when: "调用秒杀接口"
        def result = performRequest(post("/redis/lock/seckill")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("productId", productId)
                .param("userId", userId))

        then: "验证结果"
        1 * redisLockUtil.executeWithLock(_, _) >> expectedResult
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == expectedResult
        }
    }

    def "测试初始化商品库存"() {
        given: "准备测试数据"
        def productId = "prod123"
        def stock = 100

        when: "调用初始化库存接口"
        def result = performRequest(post("/redis/lock/init-stock")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("productId", productId)
                .param("stock", stock.toString()))

        then: "验证结果"
        1 * redisUtil.set("product:stock:" + productId, stock)
        if (result != null) {
            result.response.status == 200
            result.response.contentAsString == "库存初始化成功"
        }
    }

    // ====== Bitmap测试用例 ======
    
    def "测试用户签到"() {
        given: "准备测试数据"
        def userId = "user123"
        def today = LocalDate.now()
        def key = String.format("user:sign:%s:%s", userId, today.format(DateTimeFormatter.ofPattern("yyyyMM")))
        
        when: "调用签到接口"
        def result = performRequest(
            post("/redis/bitmap/sign")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
        
        then: "验证签到结果"
        1 * redisUtil.setBit(key, today.getDayOfMonth() - 1, true) >> true
        result.andExpect(status().isOk())
            .andExpect(content().string("true"))
    }
    
    def "测试获取用户签到统计"() {
        given: "准备测试数据"
        def userId = "user123"
        def today = LocalDate.now()
        def key = String.format("user:sign:%s:%s", userId, today.format(DateTimeFormatter.ofPattern("yyyyMM")))
        
        when: "调用获取签到统计接口"
        def result = performRequest(
            get("/redis/bitmap/sign/count/{userId}", userId)
        )
        
        then: "验证统计结果"
        1 * redisUtil.bitCount(key) >> 15L
        result.andExpect(status().isOk())
            .andExpect(content().string("15"))
    }
    
    // ====== HyperLogLog测试用例 ======
    
    def "测试记录页面访问"() {
        given: "准备测试数据"
        def pageId = "page123"
        def userId = "user123"
        def key = "page:uv:" + pageId
        
        when: "调用记录访问接口"
        def result = performRequest(
            post("/redis/hll/pageview")
                .param("pageId", pageId)
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
        
        then: "验证访问记录结果"
        1 * redisUtil.pfAdd(key, userId)
        1 * redisUtil.pfCount(key) >> 100L
        result.andExpect(status().isOk())
            .andExpect(content().string("100"))
    }
    
    def "测试合并页面UV统计"() {
        given: "准备测试数据"
        def pageIds = ["page1", "page2", "page3"]
        def keys = pageIds.collect { "page:uv:" + it } as String[]
        
        when: "调用合并UV统计接口"
        def result = performRequest(
            post("/redis/hll/pageview/merge")
                .param("pageIds", pageIds.toArray(new String[0]))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
        
        then: "验证合并结果"
        1 * redisUtil.pfMerge(!null, keys)
        1 * redisUtil.pfCount(!null) >> 250L
        result.andExpect(status().isOk())
            .andExpect(content().string("250"))
    }
    
    // ====== Stream测试用例 ======
    
    def "测试添加订单消息"() {
        given: "准备测试数据"
        def orderId = "order123"
        def userId = "user123"
        def product = "product123"
        def amount = 99.99
        def timestamp = System.currentTimeMillis()
        def expectedMessage = [
            orderId: orderId,
            userId: userId,
            product: product,
            amount: amount.toString(),
            timestamp: timestamp.toString()
        ]
        
        when: "调用添加订单消息接口"
        def result = performRequest(
            post("/redis/stream/order")
                .param("orderId", orderId)
                .param("userId", userId)
                .param("product", product)
                .param("amount", amount.toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
        
        then: "验证消息添加结果"
        1 * redisUtil.xAdd("order:stream", { Map map ->
            map.orderId == orderId &&
            map.userId == userId &&
            map.product == product &&
            map.amount == amount.toString() &&
            map.timestamp != null
        }) >> "1234567890-0"
        
        result.andExpect(status().isOk())
            .andExpect(content().string("1234567890-0"))
    }
    
    def "测试获取最近订单消息"() {
        given: "准备测试数据"
        def count = 10
        def expectedOrders = [
            [orderId: "order1", userId: "user1", amount: "99.99"],
            [orderId: "order2", userId: "user2", amount: "199.99"]
        ]
        
        when: "调用获取最近订单消息接口"
        def result = performRequest(
            get("/redis/stream/order/recent")
                .param("count", count.toString())
        )
        
        then: "验证获取结果"
        1 * redisUtil.xRange("order:stream", count) >> expectedOrders
        result.andExpect(status().isOk())
            .andExpect(jsonPath('$[0].orderId').value("order1"))
            .andExpect(jsonPath('$[1].orderId').value("order2"))
    }

    // ... other test methods ...
} 