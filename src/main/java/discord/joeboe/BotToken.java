package discord.joeboe;

import java.io.FileReader;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BotToken {
	public static final int LOCAL = 1;		// A local token is kept in a non-shared, local config.json file.
	public static final int CLOUD = 2;		// A hidden token is stored in our private host, Heroku.
	
	/**
	 * Returns the discord bot's official validation token from a secure, non-public place.
	 * @param flag The location the token will be obtained from, based on the circumstances in which the bot is being run (LOCAL or CLOUD).
	 * @return Returns the bot token, if successful.
	 */
	public static String getToken(int flag) {
		String token = null;
		if (flag == LOCAL) {
	        JSONParser parser = new JSONParser();
	        try {     
	            Object obj = parser.parse(new FileReader("config.json"));
	            JSONObject jsonObj =  (JSONObject) obj;
	            token = (String) jsonObj.get("token");
	        }
	        catch (Exception e) {
	        	e.printStackTrace();
	        }
		}
		else if (flag == CLOUD) {
			token = System.getenv("BOT_TOKEN");
		}
		else {
			System.out.println("Please supply a valid flag for getToken(flag).");
		}
		return token;
	}
	
	/**
	 * Returns the discord API, given that a valid bot token is passed as an argument.
	 * @param token The official token of your bot.
	 * @return Returns the discord API.
	 */
	public static DiscordApi getApi(String token) {
		DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
		return api;
	}
}
