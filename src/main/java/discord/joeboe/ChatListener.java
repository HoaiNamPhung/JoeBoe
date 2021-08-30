package discord.joeboe;

import org.javacord.api.DiscordApi;

public class ChatListener {

	public static void listenToChat(DiscordApi api) {
		// Listen for a message.
		api.addMessageCreateListener(event -> {
			String msg = event.getMessageContent();

			// Initialize database, if necessary.
			Database.initializeServerTableContents(event.getServer().get().getId() + "");

			// Check if something should be done based on the message.
			ChatFilter.replaceSlurs(event);
			
			// Check if message is an existing command.
			if (Command.isCommand(msg)) {
				// Get rid of the trigger and the space following it from the message.
				if (msg.length() > 3) {
					String actualMsg = msg.substring(Command.TRIGGER.length() + 1);

					// Set modifier words to their acronyms if they preface the entire command.
					if (actualMsg.startsWith("remove")) {
						actualMsg = actualMsg.replaceFirst("remove", "rm");
					}
					else if (actualMsg.startsWith("delete")) {
						actualMsg = actualMsg.replaceFirst("delete", "rm");
					}
					else if (actualMsg.startsWith("add ")) {
						actualMsg = actualMsg.replaceFirst("add ", "");
					}
					// Consolidate and shorten equivalent command inputs.
					if (actualMsg.startsWith("filter word")) {
						actualMsg = actualMsg.replaceFirst("filter word", "filter");
					}
					else if (actualMsg.startsWith("rm filter word")) {
						actualMsg = actualMsg.replaceFirst("rm filter word", "rm filter");
					}
					else if (actualMsg.startsWith("default replacement word")) {
						actualMsg = actualMsg.replaceFirst("default replacement word", "default replacement");
					}
					else if (actualMsg.startsWith("shame word")) {
						actualMsg = actualMsg.replaceFirst("shame word", "shame");
					}
					else if (actualMsg.startsWith("rm shame word")) {
						actualMsg = actualMsg.replaceFirst("rm shame word", "rm shame");
					}
					else if (actualMsg.startsWith("panda count")) {
						actualMsg = actualMsg.replaceFirst("panda count", "shame count");
					}
					else if (actualMsg.startsWith("save media")) {
						actualMsg = actualMsg.replaceFirst("save media", "save");
					}
					else if (actualMsg.startsWith("load media")) {
						actualMsg = actualMsg.replaceFirst("load media", "load");
					}
					else if (actualMsg.startsWith("rm media")) {
						actualMsg = actualMsg.replaceFirst("rm media", "rm");
					}			
						
					/* General Settings and Info */
					if (actualMsg.equals("info")) {
						Command.getInfo(event);
					}
					else if (actualMsg.equals("help")) {
						Command.getHelp(event);
					}
					else if (actualMsg.equals("admin help")) {
						Command.getAdminHelp(event);
					}
					else if (actualMsg.startsWith("roll")) {
						String rollMsg = actualMsg.substring("roll".length()).replaceAll("\\s", "");
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
					else if (actualMsg.equals("bot invite")) {
						Command.getInvite(event, api);
					}	
					/* Chat Filtering */	
					if (actualMsg.startsWith("filter")) {
						if (actualMsg.equals("filter")) {
							Command.getFilterWordHelp(event);
						}
						else {
							int b1 = actualMsg.length() + 1;
							int b2 = actualMsg.length() + 1;
							String replacementWord = null;
							if (actualMsg.contains("[") && actualMsg.contains("]")) {
								b1 = actualMsg.lastIndexOf("[");
								b2 = actualMsg.lastIndexOf("]");
								replacementWord = actualMsg.substring(b1 + 1, b2);
							}
							String shameWord = actualMsg.substring("filter".length() + 1, b1 - 1);
							Command.addFilterWord(event, shameWord, replacementWord);
						}
					}
					else if (actualMsg.startsWith("rm filter")) {
						if (actualMsg.equals("rm filter")) {
							Command.getFilterWordHelp(event);
						}
						else {
							String filterWord = actualMsg.substring("rm filter".length() + 1);
							Command.removeFilterWord(event, filterWord);
						}
					}	
					else if (actualMsg.startsWith("default replacement")) {
						if (actualMsg.equals("default replacement")) {
							Command.getDefaultReplacementWordHelp(event);
						}
						else {
							String filterWord = actualMsg.substring("default replacement".length() + 1);
							Command.setDefaultReplacementWord(event, filterWord);
						}
					}			
					else if (actualMsg.startsWith("shame")) {
						if (actualMsg.equals("shame count")) {
							Command.getShameCount(event);
						}
						else if (actualMsg.equals("shame")) {
							Command.getShameWordHelp(event);
						}
						else {
							String shameWord = actualMsg.substring("shame".length() + 1);
							Command.addShameWord(event, shameWord);
						}
					}				
					else if (actualMsg.startsWith("rm shame")) {
						if (actualMsg.equals("rm shame")) {
							Command.getShameWordHelp(event);
						}
						else {
							String shameWord = actualMsg.substring("rm shame".length() + 1);
							Command.removeShameWord(event, shameWord);
						}
					}
					else if (actualMsg.startsWith("rm role")) {
						if (actualMsg.equals("rm role")) {
							Command.getRemoveRoleHelp(event);
						}
						else {
							String roleName = actualMsg.substring("rm role".length() + 1);
							Command.removeRole(event, roleName);
						}
					}
					/* Medialink Storage */
					else if (actualMsg.startsWith("save")) {
						if (actualMsg.equals("save")) {
							Command.getMediaHelp(event);
						}
						else if (actualMsg.contains("[") && actualMsg.contains("]")) {
							int b1 = actualMsg.lastIndexOf("[");
							int b2 = actualMsg.lastIndexOf("]");
							String key = actualMsg.substring(b1 + 1, b2);
							String mediaName = actualMsg.substring("save".length() + 1, b1 - 1);
							Command.saveMedia(event, mediaName, key);
						}
					}
					else if (actualMsg.startsWith("load")) {
						if (actualMsg.equals("load")) {
							Command.getMediaHelp(event);
						}
						else {
							String key = actualMsg.substring("load".length() + 1);
							Command.loadMedia(event, key);
						}
					}
					else if (actualMsg.startsWith("rm")) {
						if (actualMsg.equals("rm")) {
							Command.getMediaHelp(event);
						}
						else {
							String key = actualMsg.substring("rm".length() + 1);
							Command.removeMedia(event, key);
						}
					}
					else if (actualMsg.equals("media")) {
						Command.getMediaInfo(event);
					}
				}
			}
		});
	}
}
