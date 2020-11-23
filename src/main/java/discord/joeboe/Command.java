package discord.joeboe;

import java.util.Arrays;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public class Command {
	
	public static final String TRIGGER = "jb";
	
	/**
	 * Returns whether or not the given string starts with the designated trigger format.
	 * @param str The string being parsed for the trigger.
	 * @return Returns true if the trigger format is followed, thus making the string a command.
	 */
	public static boolean isCommand(String str) {
		if (str.toLowerCase().startsWith(TRIGGER)) {
			return true;
			}
        return false;
	}
	
	public static void getHelp(MessageCreateEvent event) {
		event.getChannel().sendMessage("```" + "Existing commands:\n" + 
				"help\n" +
				"roll #d#\n" +
				"invite\n" +
				"rm roll\n" +
				"\n" + 
				"Make sure commands are typed in lowercase and are one space after " + TRIGGER + "```");
	}
	
	// TODO: Parse based on before or after "d" instead. Add + and - functionality.
	/**
	 * Rolls a given number of dice with a given number of faces and displays the results on screen.
	 * @param event The message event.
	 * @param rollMsg The message written, after the command.
	 */
	public static void rollDice(MessageCreateEvent event, String rollMsg) {
		// Check if rollMsg is valid.
		if (rollMsg.length() < 3) {
			return;
		}
		
		// Calculate the rolls.
		int numOfDie = 0;
		int numOfFaces = 0;
		try {
			int dIndex = rollMsg.indexOf("d");
			if (dIndex == -1) {
				return;
			}
			numOfDie = Integer.parseInt(rollMsg.substring(0, dIndex));
			numOfFaces = Integer.parseInt(rollMsg.substring(dIndex + 1));
		}
		catch (NumberFormatException e) {
			return;
		}
		int[] rolls = new int[numOfDie];
		int total = 0;
		for (int i = 0; i < numOfDie; i++) {
			rolls[i] = (int) (Math.random() * numOfFaces) + 1;
			total += rolls[i];
		}
		
		// Return the results.
		String roller = event.getMessageAuthor().getDisplayName();
		event.getChannel().sendMessage("```" + roller + " rolled " + rollMsg + " and got:\n" +
				"Rolls: " + Arrays.toString(rolls) + "\n" +
				"Total: " + total + "```");
	}
	
	/**
	 * Grabs the bot invite link and displays it on the screen.
	 * @param event The message event.
	 * @param api The discord api.
	 */
	public static void getInvite(MessageCreateEvent event, DiscordApi api) {
        final int PERMISSIONS = 825752640;
		event.getChannel().sendMessage("```" + "You can invite the bot by using the following url:```\n" +
				api.createBotInvite() + PERMISSIONS);
	}
	
	/**
	 * Remove a role. This cannot be undone.
	 * @param event The message event.
	 * @param roleName The role being removed.
	 */
	public static void removeRole(MessageCreateEvent event, String roleName) {
		User user = event.getMessageAuthor().asUser().get();
		RoleManager.removeRole(event.getServer().get(), roleName, user);
	}
}
