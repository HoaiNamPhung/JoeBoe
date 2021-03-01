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
					
					else if (actualMsg.startsWith("roll")) {
						String rollMsg = actualMsg.substring(4).replaceAll("\\s", "");
						if (rollMsg.length() < 2) {
							Command.getRollHelp(event);
						}
						else if ((rollMsg.charAt(0) == '#') && (rollMsg.charAt(2) == '#')) {
							Command.getRollHelp(event);
						}
						else {
							DiceRoller.rollDice(event, rollMsg);
						}
					}

					else if (actualMsg.equals("invitebot") || actualMsg.equals("invite bot")) {
						Command.getInvite(event, api);
					}
					
					else if (actualMsg.startsWith("rm role") || actualMsg.startsWith("remove role")) {
						String roleName = "";
						if (actualMsg.startsWith("rm role")) {
							if (actualMsg.equals("rm role")) {
								Command.getRemoveRoleHelp(event);
							}
							else {
								roleName = actualMsg.substring(8);
							}
						}
						else {
							if (actualMsg.equals("remove role")) {
								Command.getRemoveRoleHelp(event);
							}
							else {
								roleName = actualMsg.substring(12);
							}
						}
						Command.removeRole(event, roleName);
					}
				}
			}
		});
	}
}
