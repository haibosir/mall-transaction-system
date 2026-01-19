package com.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 商城交易系统主应用类
 *
 * @author mall
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MallTransactionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallTransactionSystemApplication.class, args);
    }
}
