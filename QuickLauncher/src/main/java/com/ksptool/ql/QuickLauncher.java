package com.ksptool.ql;

import com.ksptool.ql.commons.H2Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
public class QuickLauncher {

    public static void main(String[] args) throws SQLException {

        if(!isPortInUse(30001)){
            H2Server h2 = new H2Server(30001);
            h2.start();
        }

        SpringApplication.run(QuickLauncher.class, args);
    }

    public static boolean isPortInUse(int port) {
        try {
            new ServerSocket(port).close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}