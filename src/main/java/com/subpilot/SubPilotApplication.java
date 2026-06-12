package com.subpilot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan({
        "com.subpilot.module.user.mapper",
        "com.subpilot.module.category.mapper",
        "com.subpilot.module.subscription.mapper",
        "com.subpilot.module.bill.mapper",
        "com.subpilot.module.notification.mapper",
        "com.subpilot.module.reminder.mapper"
})
@SpringBootApplication
public class SubPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubPilotApplication.class, args);
    }
}
