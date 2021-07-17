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
	final static String DEFAULT_REPLACEMENT_WORD = "default_replacement_word";
	final static String CHAT_FILTER_MAP = "chat_filter_map";
	final static String SHAME_WORDS = "shame_words";
	
	Database db = null;
	
	public ChatFilterController() {
		db = Database.getDatabaseInstance();
	}

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
		
		Item dbItem = db.getItem(TABLE_NAME, SERVER_ID, serverId);
		
		Map<String, String> chatFilterMap = new HashMap<>();
		// Shame words already for given server; retrieve them to update it.
		if (!(dbItem == null) && !(dbItem.getMap(ATTR_NAME) == null)) {
			chatFilterMap = dbItem.getMap(ATTR_NAME);
		}
		
		// Add the new word to the chat filter.
		chatFilterMap.put(word, replacementWord);
		return setChatFilter(serverId, chatFilterMap);
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
		Item dbItem = db.getItem(TABLE_NAME, SERVER_ID, serverId);
		
		// Chat filter does not exist in database for given server, thus word does not exist in chat filter.
		if (dbItem == null) {
			return false; 
		}
		// Update existing chat filter.
		else {
			Map<String, String> chatFilterMap = dbItem.getMap(ATTR_NAME);
			// Check if word exists in chat filter.
			boolean result = chatFilterMap.containsKey(word);
			if (!result) {
				return false;
			}
			// Remove the word from the map.
			chatFilterMap.remove(word);
			return setChatFilter(serverId, chatFilterMap);
		}
	}
	
	private boolean setChatFilter(String serverId, Map<String, String> chatFilterMap) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = CHAT_FILTER_MAP;
		Table table = db.getTable(TABLE_NAME);
		UpdateItemSpec spec = new UpdateItemSpec().withPrimaryKey(SERVER_ID, serverId)
				.withUpdateExpression("set " + ATTR_NAME + " = :m")
	            .withValueMap(new ValueMap().withMap(":m", chatFilterMap))
	            .withReturnValues(ReturnValue.UPDATED_NEW);
        try {
            System.out.println("Attempting to update the item...");
            UpdateItemOutcome outcome = table.updateItem(spec);
            System.out.println("UpdateItem succeeded: " + outcome.getItem().toJSONPretty());
            return true;
        }
        catch (Exception e) {
            System.err.println("Unable to update item: " + serverId + " " + ATTR_NAME);
            System.err.println(e.getMessage());
        }
        return false;
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
		Item dbItem = db.getItem(TABLE_NAME, SERVER_ID, serverId);
		
		Map<String, String> shameWords = new HashMap<>(); 
		
		// Shame words already for given server; retrieve them to update it.
		if (!(dbItem == null) && !(dbItem.getMap(ATTR_NAME) == null)) {
			shameWords = dbItem.getMap(ATTR_NAME);
		}

		// Add the new shame word.
		shameWords.put(word, null);
		return setShameWords(serverId, shameWords);
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
		Item dbItem = db.getItem(TABLE_NAME, SERVER_ID, serverId);
		
		// Chat filter does not exist in database for given server, thus word does not exist in chat filter.
		if (dbItem == null) {
			return false; 
		}
		// Update existing chat filter.
		else {
			Map<String, String> shameWords = dbItem.getMap(ATTR_NAME);
			// Check if word exists in shameWords.
			boolean result = shameWords.containsKey(word);
			if (!result) {
				return false;
			}
			// Remove the word from the map.
			shameWords.remove(word);
			return setShameWords(serverId, shameWords);
		}
	}
	
	private boolean setShameWords(String serverId, Map<String, String> shameWords) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = SHAME_WORDS;
		Table table = db.getTable(TABLE_NAME);
		UpdateItemSpec spec = new UpdateItemSpec().withPrimaryKey(SERVER_ID, serverId)
				.withUpdateExpression("set " + ATTR_NAME + " = :m")
	            .withValueMap(new ValueMap().withMap(":m", shameWords))
	            .withReturnValues(ReturnValue.UPDATED_NEW);
        try {
            System.out.println("Attempting to update the item...");
            UpdateItemOutcome outcome = table.updateItem(spec);
            System.out.println("UpdateItem succeeded: " + outcome.getItem().toJSONPretty());
            return true;
        }
        catch (Exception e) {
            System.err.println("Unable to update item: " + serverId + " " + ATTR_NAME);
            System.err.println(e.getMessage());
        }
        return false;
	}
	
	public Set<String> getShameWords(String serverId) {
		final String TABLE_NAME = Database.SERVERS_TABLE_NAME;
		final String ATTR_NAME = SHAME_WORDS;
		Item dbItem = db.getItem(TABLE_NAME, SERVER_ID, serverId);
		// Given server does not yet exist in database.
		if (dbItem == null) {
			return null;
		}
		return dbItem.getMap(ATTR_NAME).keySet();
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
}
