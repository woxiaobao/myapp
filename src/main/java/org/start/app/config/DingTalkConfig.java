package org.start.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dingtalk.robot")
public class DingTalkConfig {
    /**
     * 钉钉机器人webhook地址
     */
    private String webhookUrl;

    /**
     * 钉钉机器人加签密钥
     */
    private String secret;
}