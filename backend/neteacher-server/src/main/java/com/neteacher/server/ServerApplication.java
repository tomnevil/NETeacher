package com.neteacher.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 主应用入口：单进程运行，聚合所有业务模块（模块化单体）。
 * 统一扫描 com.neteacher 下的组件、实体与 JPA 仓库。
 */
@SpringBootApplication
@ComponentScan("com.neteacher")
@EntityScan("com.neteacher")
@EnableJpaRepositories("com.neteacher")
@EnableJpaAuditing
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
