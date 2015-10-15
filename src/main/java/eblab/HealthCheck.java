package eblab;


import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.Tables;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * Endpoint that perform the health check.
 * Components that are not properly initialised are created.
 */
@Path("health")
public class HealthCheck {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String check(@Context final ServletContext servletContext, @Context final HttpServletResponse response) {

        Config config = (Config) servletContext.getAttribute("config");

        //Check if the table is active, if not we create it
        Table t = config.db.getTable(config.tableName);
        String tableStatus = "unknown";
        if (!Tables.doesTableExist(config.dbClient, config.tableName)) {
            initTable(config);
            tableStatus = "creating";
        }

        TableDescription tDesc = t.describe();

        if (tDesc != null) {
            tableStatus = tDesc.getTableStatus();
        }

        //Reporting
        StringBuilder statusMsg = new StringBuilder();
        statusMsg.append("Table '").append(config.tableName).append("': ").append(tableStatus);
        if (!TableStatus.ACTIVE.toString().equals(tDesc)) {
            Response.serverError().build();
        }
        return statusMsg.toString();
    }

    //Create the table
    private void initTable(Config cfg) {
        ArrayList<KeySchemaElement> keySchema = new ArrayList<>();

        ArrayList<AttributeDefinition> attrs = new ArrayList<>();
        attrs.add(new AttributeDefinition().withAttributeName("productID").withAttributeType("N"));
        keySchema.add(new KeySchemaElement().withAttributeName("productID").withKeyType(KeyType.HASH));


        CreateTableRequest request = new CreateTableRequest()
                .withTableName(cfg.tableName)
                .withAttributeDefinitions(attrs)
                .withKeySchema(keySchema)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(1L)
                        .withWriteCapacityUnits(1L));
        cfg.db.createTable(request);
    }
}
