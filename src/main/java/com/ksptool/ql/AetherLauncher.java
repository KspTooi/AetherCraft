package com.ksptool.ql;

import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.commons.H2Server;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
@EntityScan(basePackages = {"com.ksptool.ql.biz.model"})
@EnableJpaRepositories(basePackages = {"com.ksptool.ql.biz.mapper"})
public class AetherLauncher {

    public static void main(String[] args) throws SQLException {

        if(!isPortInUse(30001)){
            H2Server h2 = new H2Server(30001);
            h2.start();
        }

        SpringApplication.run(AetherLauncher.class, args);
    }

    public static boolean isPortInUse(int port) {
        try {
            new ServerSocket(port).close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }
    
    /**
     * 应用启动时检查全局配置
     */
    @Bean
    public ApplicationRunner configInitializer(GlobalConfigService globalConfigService) {
        return args -> {
            // 检查是否存在allow.install.wizard配置
            String allowInstallWizard = globalConfigService.getValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey());
            
            // 如果配置不存在，则添加默认值false
            if (StringUtils.isBlank(allowInstallWizard)) {
                globalConfigService.setValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey(), "false");
                System.out.println("初始化配置: " + GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey() + " = false");
            }
        };
    }
}