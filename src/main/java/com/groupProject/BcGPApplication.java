package com.groupProject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baizhi.dao")
public class BcGPApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcGPApplication.class, args);
    }

}
