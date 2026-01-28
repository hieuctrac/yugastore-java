package com.yugabyte.app.yugastore.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class YugastoreAdmin {

    public static void main(String[] args) {
        SpringApplication.run(YugastoreAdmin.class, args);
    }
}
