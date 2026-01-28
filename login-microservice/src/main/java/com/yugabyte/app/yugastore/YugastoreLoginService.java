package com.yugabyte.app.yugastore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.yugabyte.app.yugastore", "security"})
@EntityScan(basePackages = {"entities"})
@EnableJpaRepositories(basePackages = {"repositories"})
public class YugastoreLoginService /*extends SpringBootServletInitializer */{
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(WebApplication.class);
//    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(YugastoreLoginService.class, args);
    }
}
