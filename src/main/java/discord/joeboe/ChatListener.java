package discord.joeboe;

import org.javacord.api.DiscordApi;

public class ChatListener {

	public static void listenToChat(DiscordApi api) {
		// Listen for a message.
		api.addMessageCreateListener(event -> {
			String msg = event.getMessageContent();

			// Check if something should be done based on the message.
			ChatFilter.replaceSlurs(event);

			// Check if message is a command. If it is, check if it is one of JoeBoe's
			// existing commands.
			if (Command.isCommand(msg)) {
				
				// Get rid of the trigger and the space following it from the message.
				if (msg.length() > 3) {
					String actualMsg = msg.substring(Command.TRIGGER.length() + 1);

					if (actualMsg.equals("help")) {
						Command.getHelp(event);
					}

					if (actualMsg.length() >= 8 && actualMsg.startsWith("roll")) {
						String rollMsg = actualMsg.substring(5);
						Command.rollDice(event, rollMsg);
					}

					if (actualMsg.equals("invite")) {
						Command.getInvite(event, api);
					}
				}
			}
		});
	}
}
