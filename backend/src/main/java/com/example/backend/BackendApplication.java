package com.example.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {
        "com.example.backend.admin.mapper",
        "com.example.backend.chat.mapper",
        "com.example.backend.demand.mapper",
        "com.example.backend.notification.mapper",
        "com.example.backend.user.mapper"
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
