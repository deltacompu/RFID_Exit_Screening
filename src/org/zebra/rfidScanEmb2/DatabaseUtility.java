package org.zebra.rfidScanEmb2;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DatabaseUtility {
    private static DatabaseUtility     datasource;
    private ComboPooledDataSource cpds;

    private DatabaseUtility() throws IOException, SQLException, PropertyVetoException {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.cj.jdbc.Driver"); //loads the jdbc driver
        cpds.setJdbcUrl("jdbc:mysql:aws://10.189.104.67:3306/socka?autoReconnect=true&useUnicode=yes");
        cpds.setUser("userRFID");
        cpds.setPassword("Amazon2022!");

        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setMaxStatements(180);

    }

    public static DatabaseUtility getInstance() throws IOException, SQLException, PropertyVetoException {
        if (datasource == null) {
            datasource = new DatabaseUtility();
            return datasource;
        } else {
            return datasource;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.cpds.getConnection();
    }
}
