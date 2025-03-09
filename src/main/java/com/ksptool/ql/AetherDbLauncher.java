package com.ksptool.ql;

import com.ksptool.ql.commons.H2Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

public class AetherDbLauncher {

    public static void main(String[] args) throws SQLException {
        if(!isPortInUse(30001)){
            H2Server h2 = new H2Server(30001);
            h2.start();
        }
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
