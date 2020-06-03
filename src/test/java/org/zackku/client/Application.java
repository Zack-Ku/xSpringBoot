package org.zackku.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Zack
 * @date 2020/6/3
 */
@SpringBootApplication(scanBasePackages = "org.zackku")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
