package discord.joeboe;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;

public class ChatFilter {

	public static void replaceSlurs(MessageCreateEvent event) {
		String msg = event.getMessageContent();
		MessageAuthor author = event.getMessageAuthor();
		if ((msg.contains("nigga") || msg.contains("nigger") || msg.contains("Nigga") || msg.contains("Nigger") || msg.contains("cringe") || msg.contains("Cringe")) && (author.isUser())) {
			
			// Replace n-word with panda. I do not condone the use of this word.
			String newMsg = msg.replace("nigga", "panda");
			newMsg = newMsg.replace("Nigga", "Panda");
			newMsg = newMsg.replace("nigger", "panda");
			newMsg = newMsg.replace("Nigger", "Panda");

			// Replace cringe with a random word.
			String[] words1 = { "blackpilled", "frogpilled", "pewdiepilled", "redditpilled", "based", "micropilled", "coompilled", "pandapilled", "dogpilled" };
			String[] words2 = { "darwinpilled", "junpilled", "sonampilled", "natepilled", "kadenpilled", "kanyepilled", "hoaipilled", "brianpilled", "moylanpilled", "durdlepilled" };
			int randWordNum1 = (int) (Math.random() * words1.length);
			int randWordNum2 = (int) (Math.random() * words2.length);
			newMsg = newMsg.replace("cringe", words1[randWordNum1]);		
			newMsg = newMsg.replace("Cringe", words2[randWordNum2].substring(0,1).toUpperCase() + words2[randWordNum2].substring(1));
			
			// Instead of editing the message with the slur, we delete it and quote it with the new filter of the word.
			event.getChannel().deleteMessages(event.getMessage().getId());
			event.getChannel().sendMessage("> " + author.getDisplayName() + " said: " + newMsg);
		}
	}
}
