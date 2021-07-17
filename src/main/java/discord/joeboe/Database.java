package discord.joeboe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
public class Database {

	// Constant parameters.
	final static String SERVERS_TABLE_NAME = "joeboe_serverdata";
	final static String USERS_TABLE_NAME = "joeboe_userdata";
	
	// Immediately initializes the one and only database instance at the start of the JVM.
	private static Database myDatabase = new Database();
	private DynamoDB dynamoDB;
	
	/** 
	 * Private default constructor to prevent more than one database instantiation.
	 */
	private Database() {
		this.open();
	}
	
	/**
	 * Retrieves an instance of the singleton, already initialized database.
	 * @return Returns the instance of the database.
	 */
	public static Database getDatabaseInstance() {
		return myDatabase;
	}
	
	/**
	 * Create a connection to databases. If it doesn't exist, it is created.
	 * @return Returns false if failed to create to database.
	 */
	private boolean open() {
		try {
	        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
	            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("dynamodb.us-west-1.amazonaws.com", "us-west-1"))
	            .build();
	        dynamoDB = new DynamoDB(client);
		}
		// Failed to connect to database.
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Initializes the database's tables. Only necessary on first run of database.
	 * @return Returns true if both tables were successfully created.
	 */
	private boolean initializeDatabase() {
		boolean rv1 = createTable(USERS_TABLE_NAME);
		boolean rv2 = createTable(SERVERS_TABLE_NAME);
		if ((rv1 && rv2) == true) {
			return true;
		}
		return false;
	}
	
	/**
	 * Creates the DynamoDB table. Only necessary on first run of database.
	 * @param tableName The name of the table to create.
	 * @return Returns true if table successfully created or already exists.
	 */
	private boolean createTable(String tableName) {
        try {
            System.out.println("Attempting to create table; please wait...");
            Table table = null;
            
            switch (tableName) {
            case USERS_TABLE_NAME:
            	table = dynamoDB.createTable(tableName,
            			Arrays.asList(new KeySchemaElement("server_id", KeyType.HASH),		// Partition key
            					      new KeySchemaElement("user_name", KeyType.RANGE)),	// Sort key
            			Arrays.asList(new AttributeDefinition("server_id", ScalarAttributeType.S),	// Partition value type = (S)tring
            						  new AttributeDefinition("user_name", ScalarAttributeType.S)), // Sort value type = (S)tring
            			new ProvisionedThroughput(5L, 5L));
                table.waitForActive();          	
            	break;
            case SERVERS_TABLE_NAME:
            	table = dynamoDB.createTable(tableName,
            			Arrays.asList(new KeySchemaElement("server_id", KeyType.HASH)),
            			Arrays.asList(new AttributeDefinition("server_id", ScalarAttributeType.S)),
            			new ProvisionedThroughput(5L, 5L));
                table.waitForActive(); 
            	break;
            default:
            	System.out.println("Invalid tableName. Only 'joeboe_userdata' and 'joeboe_serverdata' can be created.");
            	return false;
            }
            System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());

        }
        catch (Exception e) {
        	System.err.println(e.getMessage());
        	return false;
        }
        return true;
	}
	
	/**
	 * Creates and puts an item with a mapping of key/value pairs into the 'mapName' column of the database.
	 * @param tableName The name of the table the item is being placed into.
	 * @param partKey The type of partition key that the item will be placed into.
	 * @param partVal The value of the partition key that the item will be placed into.
	 * @param sortKey The type of sort key that the item will be identified and sorted by.
	 * @param sortVal The value of the sort key that the item will identified and sorted by. 
	 * @param mapName The name of the map.
	 * @param infoMap A map containing key, value information pertaining to given item.
	 * @return Returns true if item is successfully added to the given table.
	 */
	public boolean putItem(String tableName, String partKey, String partVal, String sortKey, String sortVal, String mapName, Map<String, ? extends Object> infoMap) {
		Table table = dynamoDB.getTable(tableName);
		try {
            System.out.println("Adding a new item...");
            PutItemOutcome outcome = (sortKey != null && sortVal != null) ? 
            	table.putItem(new Item().withPrimaryKey(partKey, partVal, sortKey, sortVal).withMap(mapName, infoMap)) :
            	table.putItem(new Item().withPrimaryKey(partKey, partVal).withMap(mapName, infoMap));
            System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
            return true;
        }
        catch (Exception e) {
            System.err.println("Unable to add item: " + partVal + " " + sortVal);
            System.err.println(e.getMessage());
        }	
		return false;
	}
	
	/**
	 * Creates and puts an item with a mapping of key/value pairs into the 'mapName' column of the database.
	 * @param tableName The name of the table the item is being placed into.
	 * @param partKey The type of partition key that the item will be placed into.
	 * @param partVal The value of the partition key that the item will be placed into.
	 * @param mapName The name of the map.
	 * @param infoMap A map containing key, value information pertaining to given item.
	 * @return Returns true if item is successfully added to the given table.
	 */
	public boolean putItem(String tableName, String partKey, String partVal, String mapName, Map<String, ? extends Object> infoMap) {
		return putItem(tableName, partKey, partVal, null, null, mapName, infoMap);
	}
	
	/**
	 * Creates and puts an item into the database with a given attribute.
	 * @param tableName The name of the table the item is being placed into.
	 * @param partKey The type of partition key that the item will be placed into.
	 * @param partVal The value of the partition key that the item will be placed into.
	 * @param sortKey The type of sort key that the item will be identified and sorted by.
	 * @param sortVal The value of the sort key that the item will identified and sorted by. 
	 * @param attrKey The name of the attribute.
	 * @param attrVal The value of said attribute for the current item.
	 * @return Returns true if item is successfully added to the given table.
	 */
	public boolean putItem(String tableName, String partKey, String partVal, String sortKey, String sortVal, String attrKey, String attrVal) {
		Table table = dynamoDB.getTable(tableName);
		try {
            System.out.println("Adding a new item...");
            PutItemOutcome outcome = (sortKey != null && sortVal != null) ? 
            	table.putItem(new Item().withPrimaryKey(partKey, partVal, sortKey, sortVal).withString(attrKey, attrVal)) :
            	table.putItem(new Item().withPrimaryKey(partKey, partVal).withString(attrKey, attrVal));
            System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
            return true;
        }
        catch (Exception e) {
            System.err.println("Unable to add item: " + partVal + " " + sortVal);
            System.err.println(e.getMessage());
        }	
		return false;
	}

	/**
	 * Creates and puts an item into the database with a given attribute.
	 * @param tableName The name of the table the item is being placed into.
	 * @param partKey The type of partition key that the item will be placed into.
	 * @param partVal The value of the partition key that the item will be placed into.
	 * @param attrKey The name of the attribute.
	 * @param attrVal The value of said attribute for the current item.
	 * @return Returns true if item is successfully added to the given table.
	 */
	public boolean putItem(String tableName, String partKey, String partVal, String attrKey, String attrVal) {
		return putItem(tableName, partKey, partVal, null, null, attrKey, attrVal);
	}
	
	/**
	 * Retrieves an item from a table based on a given partition value and sort value.
	 * @param tableName The name of the table the item is being obtained from.
	 * @param partKey The type of partition key of the item.
	 * @param partVal The value of the partition key of the item.
	 * @param sortKey The type of sort key of the item.
	 * @param sortVal The value of the sort key of the item. 
	 * @return Returns the item obtained from the table. If no item found, return null.
	 */
	public Item getItem(String tableName, String partKey, String partVal, String sortKey, String sortVal) {
		Table table = dynamoDB.getTable(tableName);
		GetItemSpec spec = (sortKey != null) ? new GetItemSpec().withPrimaryKey(partKey, partVal, sortKey, sortVal) :
											   new GetItemSpec().withPrimaryKey(partKey, partVal);
        try {
            System.out.println("Attempting to read the item...");
            Item outcome = table.getItem(spec);
            System.out.println("GetItem succeeded: " + outcome);
            return outcome;
        }
        catch (Exception e) {
            System.err.println("Unable to read item: " + partVal + " " + sortVal);
            System.err.println(e.getMessage());
        }
        return null;
	}
	
	/**
	 * Retrieves an item from a table based on a given partition value.
	 * @param tableName The name of the table the item is being obtained from.
	 * @param partKey The type of partition key of the item.
	 * @param partVal The value of the partition key of the item.
	 * @return Returns the item obtained from the table. If no item found, return null.
	 */
	public Item getItem(String tableName, String partKey, String partVal) {
		return getItem(tableName, partKey, partVal, null, null);
	}
	

	/**
	 * Sets the specified attribute of an item to a given string value.
	 * @param tableName The name of the table.
	 * @param partKey The type of partition key of the item.
	 * @param partVal The value of the partition key of the item.
	 * @param sortKey The type of sort key of the item.
	 * @param sortVal The value of the sort key of the item.
	 * @param attrKey The type of attribute key of the item.
	 * @param attrVal The new value of said attribute.
	 * @return Returns true if successfully set.
	 */
	public boolean setAttribute(String tableName, String partKey, String partVal, String sortKey, String sortVal, String attrKey, String attrVal) {
		Table table = dynamoDB.getTable(tableName);
		UpdateItemSpec spec = new UpdateItemSpec().withPrimaryKey(partKey, partVal)
				.withUpdateExpression("set " + attrKey + " = :s")
	            .withValueMap(new ValueMap().withString(":s", attrVal))
	            .withReturnValues(ReturnValue.UPDATED_NEW);
        try {
            System.out.println("Attempting to update the item...");
            UpdateItemOutcome outcome = table.updateItem(spec);
            System.out.println("UpdateItem succeeded: " + outcome.getItem().toJSONPretty());
            return true;
        }
        catch (Exception e) {
            System.err.println("Unable to update item: " + tableName + " " + partKey + " " + sortKey + " " + attrKey);
            System.err.println(e.getMessage());
        }
        return false;
	}
	
	/**
	 * Sets the specified attribute of an item to a given string value.
	 * @param tableName The name of the table.
	 * @param partKey The type of partition key of the item.
	 * @param partVal The value of the partition key of the item.
	 * @param attrKey The type of attribute key of the item.
	 * @param attrVal The new value of said attribute.
	 * @return Returns true if successfully set.
	 */
	public boolean setAttribute(String tableName, String partKey, String partVal, String attrKey, String attrVal) {
		return setAttribute(tableName, partKey, partVal, null, null, attrKey, attrVal);
	}
	
	/**
	 * Gets the specified string attribute of an item.
	 * @param tableName The name of the table.
	 * @param partKey The type of partition key of the item.
	 * @param partVal The value of the partition key of the item.
	 * @param sortKey The type of sort key of the item.
	 * @param sortVal The value of the sort key of the item.
	 * @param attrKey The type of attribute key of the item.
	 * @return Returns the attribute value. If no value found, returns null.
	 */
	public String getAttribute(String tableName, String partKey, String partVal, String sortKey, String sortVal, String attrKey) {
		Item dbItem = getItem(tableName, partKey, partVal, sortKey, sortVal);
		// Given server does not yet exist in database.
		if (dbItem == null) {
			return null;
		}
		return dbItem.getString(attrKey);
	}
	
	/**
	 * Gets the specified string attribute of an item.
	 * @param tableName The name of the table.
	 * @param partKey The type of partition key of the item.
	 * @param partVal The value of the partition key of the item.
	 * @param attrKey The type of attribute key of the item.
	 * @return Returns the attribute value. If no value found, returns null.
	 */
	public String getAttribute(String tableName, String partKey, String partVal, String attrKey) {
		return getAttribute(tableName, partKey, partVal, null, null, attrKey);
	}
	
	
	/**
	 * Retrieves a table from the database.
	 * @param tableName The table being retrieved.
	 * @return The table. Returns null if it doesn't exist.
	 */
	public Table getTable(String tableName) {
		return dynamoDB.getTable(tableName);
	}
	
	/**
	 * Initializes and places the current server into the servers table of the database.
	 * @param serverId The id of our target discord server.
	 * @return Returns false if this server is already initialized in the table. Returns true if initializing successful.
	 */
	public static boolean initializeServerTableContents(String serverId) {
		final String TABLE_NAME = SERVERS_TABLE_NAME;
		Item item = Database.getDatabaseInstance().getItem(TABLE_NAME, ChatFilterController.SERVER_ID, serverId);
		// Server already initialized in server table.
		if (item != null) {
			return false;
		}
		// Create an item for the current server in the servers table and set its default attributes.
		boolean rv1 = new ChatFilterController().initializeChatFilter(serverId);
		return rv1;
	}
	
}
