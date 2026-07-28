package com.smarttravel.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(scanBasePackages = "com.smarttravel")
@MapperScan(basePackages = {
        "com.smarttravel.user.mapper",
        "com.smarttravel.scenic.mapper",
        "com.smarttravel.route.mapper",
        "com.smarttravel.social.mapper",
        "com.smarttravel.travel.mapper",
        "com.smarttravel.trajectory.mapper",
        "com.smarttravel.ticket.mapper"
})
@EnableElasticsearchRepositories(basePackages = {
        "com.smarttravel.scenic.repository",
        "com.smarttravel.travel.repository"
})
public class SmartTravelApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTravelApplication.class, args);
    }

}