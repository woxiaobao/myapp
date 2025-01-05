package org.start.app.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/demo")
public class DemoController {


    @Value("${app.config.valueInfo}")
    private String value;
    /**
     * demo
     * @return
     */
    @GetMapping("get")
    public Boolean getDemo() {
        System.out.println(value);
        return Boolean.TRUE;
    }
}
