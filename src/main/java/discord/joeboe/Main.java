package discord.joeboe;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

public class Main {

    public static void main(String[] args) {

    	// Retrieve the API using the bot's official discord token. Retrieve the token from Heroku.
    	String token = BotToken.getToken(BotToken.CLOUD);
    	if (token == null) {
    		token = BotToken.getToken(BotToken.LOCAL);
    	}
        DiscordApi api = BotToken.getApi(token);

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