package org.start.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.start.app.service.DingTalkService;

@Tag(name = "钉钉机器人接口", description = "钉钉机器人消息发送相关接口")
@RestController
@RequestMapping("/api/dingtalk")
public class DingTalkController {

    @Autowired
    private DingTalkService dingTalkService;

    @Operation(summary = "发送钉钉消息")
    @PostMapping("/send")
    public boolean sendMessage(@RequestParam String content) {
        return dingTalkService.sendMessage(content);
    }
}