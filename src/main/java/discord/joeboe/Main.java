package discord.joeboe;

import java.io.FileReader;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.UserStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {

    public static void main(String[] args) {
    	
    	String token = null;
    	
    	// Try the local config file for the token.
        JSONParser parser = new JSONParser();
        try {     
            Object obj = parser.parse(new FileReader("config.json"));
            JSONObject jsonObj =  (JSONObject) obj;
            token = (String) jsonObj.get("token");
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
        // Try the cloud config file for the token.
    	//token = System.getenv("BOT_TOKEN");
    	
    	// Set the token.
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        
        // Replace n-word and other slurs with fun stuff.
        ChatListener.listenToChat(api);
        
        // Change bot's activity status.
        String statusMsg = "Use 'jb help' for commands."; 
        api.updateActivity(ActivityType.PLAYING, statusMsg);
        
        // Print the invite url of your bot
        final int PERMISSIONS = 825752640;
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite() + PERMISSIONS);
    }

}