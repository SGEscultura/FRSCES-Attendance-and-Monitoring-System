import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection 
{
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static 
    {
        try 
        {
            // Always looks for config.properties next to the JAR file
            String jarDir = new java.io.File(DatabaseConnection.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI())
                .getParent();

            String configPath = jarDir + "/config.properties";
            
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(configPath);
            props.load(fis);
            fis.close();

            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String name = props.getProperty("db.name");

            URL      = "jdbc:postgresql://" + host + ":" + port + "/" + name;
            USER     = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } 
        catch (Exception e) 
        {
            System.err.println("config.properties not found! Using localhost.");
            URL      = "jdbc:postgresql://localhost:5432/frsces_db";
            USER     = "postgres";
            PASSWORD = "admin";
        }
    }

    public static Connection getConnection() throws SQLException 
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}