package com.ksptool.ql;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.commons.H2Server;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
@EntityScan(basePackages = {"com.ksptool.ql.biz.model"})
@EnableJpaRepositories(basePackages = {"com.ksptool.ql.biz.mapper"})
public class AetherLauncher {

    // 从配置文件中读取应用版本号
    private static String applicationVersion;

    @Value("${application.version}")
    public void setApplicationVersion(String version) {
        applicationVersion = version;
    }

    /**
     * 获取应用版本号
     * @return 应用版本号
     */
    public static String getVersion() {
        return applicationVersion;
    }

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
    public ApplicationRunner configInitializer(GlobalConfigService globalConfigService, AuthService authService) {
        return args -> {

            // 检查是否存在allow.install.wizard配置
            String allowInstallWizard = globalConfigService.getValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey());

            // 检查是否存在更新
            String storeVersion = globalConfigService.get(GlobalConfigEnum.APPLICATION_VERSION.getKey(),"1.0A");

            //检查是否在版本落后时允许执行升级向导
            boolean allowWizardWhenUpgraded = globalConfigService.getBoolean(GlobalConfigEnum.ALLOW_INSTALL_WIZARD_UPGRADED.getKey(), true);

            if(!storeVersion.equals(applicationVersion)){

                //允许在版本落后时触发向导进行数据升级
                if(allowWizardWhenUpgraded){
                    log.info("应用程序版本已落后 当前:{} 最新:{},自动运行升级向导。", storeVersion, applicationVersion);
                    globalConfigService.setValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey(),null);
                    allowInstallWizard = "true";
                }

                //不允许版本落后时触发向导升级
                if(!allowWizardWhenUpgraded){
                    log.info("应用程序版本已落后 当前:{} 最新:{},升级向导当前被禁用,请注意数据同步。", storeVersion, applicationVersion);
                    globalConfigService.setValue(GlobalConfigEnum.APPLICATION_VERSION.getKey(), getVersion());
                }

            }

            // 如果配置不存在，则添加默认值true
            if (StringUtils.isBlank(allowInstallWizard) || allowInstallWizard.equals("true")) {
                globalConfigService.setValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey(), "true");
                System.out.println("初始化配置: " + GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey() + " = true");
                
                // 清除所有用户会话，确保安装向导启动时没有用户登录
                authService.clearUserSession();
                System.out.println("已清除所有用户会话，准备启动安装向导");
            }
            
            // 打印应用版本信息
            System.out.println("应用版本: " + getVersion());
        };
    }
}