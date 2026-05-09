package com.greendam.birdhelp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.greendam.birdhelp.mapper")
public class BirdHelpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BirdHelpApplication.class, args);
    }

}
