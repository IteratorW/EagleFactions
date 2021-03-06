package io.github.aquerr.eaglefactions.storage.mysql;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import java.sql.*;

public class MySQLConnection
{
    private static MySQLConnection INSTANCE = null;

    private static final String TIME_ZONE_PROPERTY = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    private final String databaseUrl;
    private final String databaseName;
    private final String username;
    private final String password;

    public static MySQLConnection getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
        {
            try
            {
                INSTANCE = new MySQLConnection(eagleFactions);
                return INSTANCE;
            }
            catch(IllegalAccessException | InstantiationException | ClassNotFoundException | SQLException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else return INSTANCE;
    }

    private MySQLConnection(EagleFactions eagleFactions) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException
    {
        //Load MySQL driver
//        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        this.databaseUrl = configFields.getDatabaseUrl();
        this.databaseName = configFields.getDatabaseName();
        this.username = configFields.getStorageUsername();
        this.password = configFields.getStoragePassword();
        if(!databaseExists())
            createDatabase();
    }

    private boolean databaseExists() throws SQLException
    {
//        Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.username + ":" + this.password + "@" + this.databaseUrl + this.databaseName);
//        DataSource dataSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:mysql://" + this.username + ":" + this.password + "@" + this.databaseUrl + this.databaseName);
//        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=sa&password=admin&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", this.username, this.password);
        ResultSet resultSet = connection.getMetaData().getCatalogs();

        while(resultSet.next())
        {
            if(resultSet.getString(1).equalsIgnoreCase(this.databaseName))
                return true;
        }
        resultSet.close();
//        connection.close();
        return false;
    }

    private void createDatabase() throws SQLException
    {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", this.username, this.password);
        Statement statement = connection.createStatement();
        statement.execute("CREATE SCHEMA " + this.databaseName + ";");
        statement.close();
        connection.commit();
        connection.close();
    }

    public Connection openConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:mysql://" + this.databaseUrl + this.databaseName + TIME_ZONE_PROPERTY, this.username, this.password);
    }
}
