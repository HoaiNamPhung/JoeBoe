package discord.joeboe;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

public class Main {

    public static void main(String[] args) {

    	ChatFilterController CFC = new ChatFilterController();
    	String[] serverId = {"bigLads", "demil", "shrimpen"};
    	String[] word = {"nigga", "nigger", "cringe", "based", "Tiananmen Square", "NIGga"};
    	String[] replacementWord = {"panda", "pandaz", "sonaminominom", ""};
    	
    	/*
    	CFC.addWordToChatFilter(serverId[0], word[0], replacementWord[3]);
    	CFC.addWordToChatFilter(serverId[0], word[1], replacementWord[3]);
    	CFC.addWordToChatFilter(serverId[1], word[1], replacementWord[1]);
    	CFC.addWordToChatFilter(serverId[1], word[1], replacementWord[0]);
    	CFC.addWordToShameWords(serverId[0], word[0]);
    	CFC.addWordToShameWords(serverId[0], word[1]);
    	CFC.addWordToShameWords(serverId[1], word[2]);
    	CFC.addWordToShameWords(serverId[2], word[0]);
    	CFC.addWordToShameWords(serverId[2], word[1]);
    	CFC.addWordToChatFilter(serverId[0], word[5], null);
    	*/
    	
    	// Retrieve the API using the bot's official discord token. Retrieve the token from Heroku.
    	String token = BotToken.getToken(BotToken.CLOUD);
    	if (token == null) {
    		token = BotToken.getToken(BotToken.LOCAL);
    	}
        DiscordApi api = BotToken.getApi(token);

        // TODO: Instantiate and create a cache of the database to avoid expensive CRUD queries.
        
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