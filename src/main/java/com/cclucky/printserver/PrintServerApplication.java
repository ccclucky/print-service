package com.cclucky.printserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
//@ComponentScan(value = {"com.cclucky.printserver.utils", "com.cclucky.printserver.config"})
public class PrintServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrintServerApplication.class, args);
    }

}
