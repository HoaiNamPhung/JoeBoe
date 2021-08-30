package discord.joeboe;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

public class ChatFilterController {
	
	// Common database keys.
	final static String SERVER_ID = "server_id";
	final static String USER_ID = "user_id";
	final static String DEFAULT_REPLACEMENT_WORD =  "default_replacement_word";
	final static String CHAT_FILTER_MAP = "chat_filter_map";
	final static String SHAME_WORDS = "shame_words";
	final static String SHAME_COUNT = "shame_count";
	final static String MEDIA_LINKS = "media_links";
	
	
	Database db = null;
	
	public ChatFilterController() {
		db = Database.getDatabaseInstance();
	}

	
	/* ============== CHAT FILTER =============== */
	/**
	 * Adds a word to our database of chat-filtered words for a given server.
	 * A replacement word can optionally be mapped to the filtered word. To default, leave as null.
	 * @param serverId The id of our target discord server.
	 * @param word The word to add to our chat filter.
	 * @param replacementWord The word to replace said filtered word.
	 * @return Returns true if word is successfully added.
	 */
	public boolean addWordToChatFilter(String serverId, String word, String replacementWord) {
		word = word.toLowerCase();	// Ignore capitalization.
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = CHAT_FILTER_MAP;
		return addToDatabaseAttribute(serverId, word, replacementWord, TABLE_NAME, ATTR_NAME);
	}
	
	/**
	 * Removes a word from our database of chat-filtered words for a given server.
	 * @param serverId The id of our target discord server.
	 * @param word The word to remove from our chat filter.
	 * @return Returns true if word is successfully removed. Returns false if word doesn't exist or could not be removed.
	 */
	public boolean removeWordFromChatFilter(String serverId, String word) {
		word = word.toLowerCase();	// Ignore capitalization.
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = CHAT_FILTER_MAP;
		return removeFromDatabaseAttribute(serverId, word, TABLE_NAME, ATTR_NAME);
	}
	
	/**
	 * Retrieves our database of chat-filtered words and their replacement word mappings for a given server.
	 * @param serverId The id of our target discord server.
	 * @return Returns the map of chat-filtered words and their replacement words.
	 */
	public Map<String, String> getChatFilter(String serverId) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = CHAT_FILTER_MAP;
		Item dbItem = db.getItem(TABLE_NAME, SERVER_ID, serverId);
		// Given server does not yet exist in database.
		if (dbItem == null) {
			return null;
		}
		return dbItem.getMap(ATTR_NAME);
	}
	
	
	/* ============== SHAME WORDS =============== */
	/**
	 * Adds a word to our database of shame words for a given server.
	 * A replacement word can optionally be mapped to the filtered word. To default, leave as null.
	 * @param serverId The id of our target discord server.
	 * @param word The word to add to our shame words.
	 * @return Returns true if word is successfully added.
	 */
	public boolean addWordToShameWords(String serverId, String word) {
		word = word.toLowerCase();	// Ignore capitalization.
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = SHAME_WORDS;
		return addToDatabaseAttribute(serverId, word, null, TABLE_NAME, ATTR_NAME);
	}
	
	/**
	 * Removes a word from our database of shame words for a given server.
	 * @param serverId The id of our target discord server.
	 * @param word The word to remove from our shame words.
	 * @return Returns true if word is successfully removed. Returns false if word doesn't exist or could not be removed.
	 */
	public boolean removeWordFromShameWords(String serverId, String word) {
		word = word.toLowerCase();	// Ignore capitalization.
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = SHAME_WORDS;
		return removeFromDatabaseAttribute(serverId, word, TABLE_NAME, ATTR_NAME);
	}
	
	/**
	 * Retrieves our database of shame words for a given server.
	 * @param serverId The id of our target discord server.
	 * @return Returns the map of shame words.
	 */
	public Set<String> getShameWords(String serverId) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = SHAME_WORDS;
		return getDatabaseAttribute(serverId, TABLE_NAME, ATTR_NAME).keySet();
	}

	
	/* ============== MEDIA LINKS =============== */
	/**
	 * Adds a word to our database of chat-filtered words for a given server.
	 * A replacement word can optionally be mapped to the filtered word. To default, leave as null.
	 * @param serverId The id of our target discord server.
	 * @param word The word to add to our chat filter.
	 * @param replacementWord The word to replace said filtered word.
	 * @return Returns true if word is successfully added.
	 */
	public boolean addMediaLink(String serverId, String link, String key) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = MEDIA_LINKS;
		return addToDatabaseAttribute(serverId, key, link, TABLE_NAME, ATTR_NAME);
	}
	
	/**
	 * Removes a word from our database of chat-filtered words for a given server.
	 * @param serverId The id of our target discord server.
	 * @param word The word to remove from our chat filter.
	 * @return Returns true if word is successfully removed. Returns false if word doesn't exist or could not be removed.
	 */
	public boolean removeMediaLink(String serverId, String key) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = MEDIA_LINKS;
		return removeFromDatabaseAttribute(serverId, key, TABLE_NAME, ATTR_NAME);
	}
	
	/**
	 * Retrieves our database of chat-filtered words and their replacement word mappings for a given server.
	 * @param serverId The id of our target discord server.
	 * @return Returns the map of chat-filtered words and their replacement words.
	 */
	public Map<String, String> getMediaLinks(String serverId) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = MEDIA_LINKS;
		return getDatabaseAttribute(serverId, TABLE_NAME, ATTR_NAME);
	}
	
	/* ============== CUSTOM CRUD IMPLEMENTATIONS =============== */
	/**
	 * Updates the current default replacement word for the given server.
	 * @param serverId The id of our target discord server.
	 * @param word The default replacement word to use.
	 * @return Returns true if word successfully updated.
	 */
	public boolean setDefaultReplacementWord(String serverId, String word) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = DEFAULT_REPLACEMENT_WORD;
		return db.setAttribute(TABLE_NAME, SERVER_ID, serverId, ATTR_NAME, word);
	}	
	
	/**
	 * Gets the current default replacement word for the given server.
	 * @param serverId The id of our target discord server.
	 * @return Returns the default replacement word. If there is none, returns null.
	 */
	public String getDefaultReplacementWord(String serverId) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = DEFAULT_REPLACEMENT_WORD;
		return db.getAttribute(TABLE_NAME, SERVER_ID, serverId, ATTR_NAME);
	}
	
	public boolean initializeChatFilter(String serverId) {
		// n-words in ASCII.
		String n1 = String.valueOf((char)110)+String.valueOf((char)105)+String.valueOf((char)103)+String.valueOf((char)103)+String.valueOf((char)97);
		String n2 = String.valueOf((char)110)+String.valueOf((char)105)+String.valueOf((char)103)+String.valueOf((char)103)+String.valueOf((char)101)+String.valueOf((char)114);
		// Add default filtered words to chat filter and shame words.
		boolean rv = addWordToChatFilter(serverId, n1, null);
		rv &= addWordToChatFilter(serverId, n2, null);
		rv &= addWordToShameWords(serverId, n1);
		rv &= addWordToShameWords(serverId, n2);
		return rv;
	}
	
	public int getShameCount(String serverId, String userId) {
		final String TABLE_NAME = Database.USERS_TABLE_NAME;  
		final String ATTR_NAME = SHAME_COUNT;
		Item dbItem = db.getItem(TABLE_NAME, SERVER_ID, serverId, USER_ID, userId);
		if (dbItem == null) {
			return 0;
		}
		return Integer.parseInt(dbItem.getString(ATTR_NAME));
	}
	
	public int incrementShameCount(String serverId, String userId, int amount) {
		final String TABLE_NAME = Database.USERS_TABLE_NAME;  
		final String ATTR_NAME = SHAME_COUNT;
		int count = 0;
		Item dbItem = db.getItem(TABLE_NAME, USER_ID, userId, SERVER_ID, serverId);
		// First shame word ever; initialize counter.
		if (dbItem == null) {
			count = amount;
			db.putItem(TABLE_NAME, SERVER_ID, serverId, USER_ID, userId, SHAME_COUNT, count + "");
		}
		// Increment normally.
		else {
			count = Integer.parseInt(dbItem.getString(ATTR_NAME));
			count += amount;
			db.setAttribute(TABLE_NAME, SERVER_ID, serverId, USER_ID, userId, SHAME_COUNT, count + "");
		}
		return count;
	}
	
	
	/* ============== GENERIC CRUD IMPLEMENTATIONS =============== */
	/**
	 * Adds an entry (key-value pair) to a table given a specified attribute (column) within said table.
	 * @param serverId The id of our target discord server.
	 * @param key The key of the new entry.
	 * @param val The value of the new entry.
	 * @param tableName That destination table's name.
	 * @param attrName The destination attribute's name.
	 * @return Returns true if media is successfully added.
	 */
	public boolean addToDatabaseAttribute(String serverId, String key, String val, String tableName, String attrName) {
		Item dbItem = db.getItem(tableName, SERVER_ID, serverId);
		Map<String, String> map = new HashMap<>(); 
		
		// Retrieve existing map from table to update it, if exists.
		if (!(dbItem == null) && !(dbItem.getMap(attrName) == null)) {
			map = dbItem.getMap(attrName);
		}

		// Add the new media link.
		map.put(key, val);
		return setDatabaseAttribute(serverId, map, tableName, attrName);
	}
	
	/**
	 * Removes a mapping from an attribute for a given table.
	 * @param serverId The id of our target discord server.
	 * @param key The key of the entry to remove from the attribute's mappings.
	 * @param tableName That desired table's name.
	 * @param attrName The desired attribute's name.
	 * @return Returns true if mapping is successfully removed. Returns false if key doesn't exist or mapping could not be removed.
	 */
	public boolean removeFromDatabaseAttribute(String serverId, String key, String tableName, String attrName) {
		Item dbItem = db.getItem(tableName, SERVER_ID, serverId);
		// Given server does not have any mappings for given attribute yet; nothing to remove.
		if (dbItem == null) {
			return false; 
		}
		// Update the attribute.
		else {
			Map<String, String> map = dbItem.getMap(attrName);
			// Check if key exists in attribute's existing mappings.
			boolean result = map.containsKey(key);
			if (!result) {
				return false;
			}
			// Remove the mapping from the attribute.
			map.remove(key);
			return setDatabaseAttribute(serverId, map, tableName, attrName);
		}
	}
	
	private boolean setDatabaseAttribute(String serverId, Map<String, String> map, String tableName, String attrName) {
		Table table = db.getTable(tableName);
		UpdateItemSpec spec = new UpdateItemSpec().withPrimaryKey(SERVER_ID, serverId)
				.withUpdateExpression("set " + attrName + " = :m")
	            .withValueMap(new ValueMap().withMap(":m", map))
	            .withReturnValues(ReturnValue.UPDATED_NEW);
        try {
            System.out.println("Attempting to update the item...");
            UpdateItemOutcome outcome = table.updateItem(spec);
            System.out.println("UpdateItem succeeded: " + outcome.getItem().toJSONPretty());
            return true;
        }
        catch (Exception e) {
            System.err.println("Unable to update item: " + serverId + " " + attrName);
            System.err.println(e.getMessage());
        }
        return false;
	}
	
	/**
	 * Gets the mappings for an attribute from a given table.
	 * @param serverId The id of our target discord server.
	 * @param tableName That desired table's name.
	 * @param attrName The desired attribute's name.
	 * @return Returns the set of mappings for the given attribute.
	 */
	public Map<String, String> getDatabaseAttribute(String serverId, String tableName, String attrName) {
		Item dbItem = db.getItem(tableName, SERVER_ID, serverId);
		// Given server does not yet exist in database.
		if (dbItem == null) {
			return null;
		}
		return dbItem.getMap(attrName);
	}
}
