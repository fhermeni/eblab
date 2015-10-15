package eblab;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application parameter.
 * It has been declared as a listener in web.xml
 */
@WebListener
public class Config implements ServletContextListener {

    final String tableName = "products";
    AmazonDynamoDBClient dbClient;
    DynamoDB db;
    Region region;

    /*
     Initialise AWS stuff when the servlet is initialised and makes it available through the
     {{@value "config"}} servlet context attribute
     */
    public void contextInitialized(ServletContextEvent event) {

        AWSCredentialsProvider cred = new DefaultAWSCredentialsProviderChain();
        dbClient = new AmazonDynamoDBClient(cred);
        region = Region.getRegion(Regions.EU_WEST_1);
        dbClient.setRegion(region);

        db = new DynamoDB(dbClient);
        dbClient.listTables().getTableNames().stream().forEach(t -> System.err.println(t));
        event.getServletContext().setAttribute("config", this);
        System.err.println("Config registered");
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
