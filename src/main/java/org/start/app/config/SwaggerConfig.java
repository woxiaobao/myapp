package org.start.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI springOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("本地环境");

        Server testServer = new Server();
        testServer.setUrl("http://124.221.19.177:8080/");
        testServer.setDescription("测试环境");

        List<Server> servers = new ArrayList<>();
        servers.add(localServer);
        servers.add(testServer);

        return new OpenAPI()
                .info(new Info()
                        .title("项目API文档")
                        .description("API接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com"))
                        .license(new License().name("Apache 2.0")))
                .servers(servers);
    }
} 