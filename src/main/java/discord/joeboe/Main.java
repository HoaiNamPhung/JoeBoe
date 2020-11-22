package discord.joeboe;

import java.io.FileReader;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {

    public static void main(String[] args) {
        /*
        JSONParser parser = new JSONParser();
        String token = null;

        try {     
            Object obj = parser.parse(new FileReader("config.json"));
            JSONObject jsonObj =  (JSONObject) obj;
            token = (String) jsonObj.get("token");
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        */
    	
    	String token = System.getenv("BOT_TOKEN");
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        
        // Replace n-word and other slurs with fun stuff.
        ChatListener.listenToChat(api);
       
        
        // Print the invite url of your bot
        final int PERMISSIONS = 825752640;
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite() + PERMISSIONS);
    }

}