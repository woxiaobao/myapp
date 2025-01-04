package org.start.app.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {


    /**
     * demo
     * @return
     */
    @GetMapping("get")
    public Boolean getDemo() {
        return Boolean.TRUE;
    }
}
