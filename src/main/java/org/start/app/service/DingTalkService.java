package org.start.app.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.start.app.config.DingTalkConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DingTalkService {

    @Autowired
    private DingTalkConfig dingTalkConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送钉钉消息
     *
     * @param content 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String content) {
        try {
            long timestamp = System.currentTimeMillis();
            String sign = generateSign(timestamp);

            // 构建请求URL
            String url = dingTalkConfig.getWebhookUrl() + "&timestamp=" + timestamp + "&sign=" + sign;

            // 构建消息内容
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "text");
            Map<String, String> text = new HashMap<>();
            text.put("content", content);
            message.put("text", text);

            // 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(message), headers);
            
            restTemplate.postForObject(url, request, String.class);
            return true;
        } catch (Exception e) {
            log.error("发送钉钉消息失败", e);
            return false;
        }
    }

    /**
     * 生成签名
     *
     * @param timestamp 时间戳
     * @return 签名字符串
     */
    private String generateSign(long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + dingTalkConfig.getSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(dingTalkConfig.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
    }
}