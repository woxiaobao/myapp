package org.start.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "示例接口", description = "示例接口描述")
@RestController
@RequestMapping("/demo")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DemoController {
    
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Operation(summary = "打招呼", description = "返回问候语")
    @GetMapping("/hello")
    public String hello(
            @Parameter(description = "姓名", required = true) 
            @RequestParam String name) {
        logger.debug("Received hello request with name: {}", name);
        String response = "Hello, " + name + "!";
        logger.info("Returning response: {}", response);
        return response;
    }
}
