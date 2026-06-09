package com.subpilot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan({
        "com.subpilot.module.user.mapper",
        "com.subpilot.module.category.mapper",
        "com.subpilot.module.subscription.mapper"
})
@SpringBootApplication
public class SubPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubPilotApplication.class, args);
    }
}
