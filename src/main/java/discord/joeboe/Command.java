package discord.joeboe;

import java.util.Arrays;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
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
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("Type commands in lowercase, exactly one space after *jb*.\n" + 
								"• roll *#*d*#*\n" +
								"• remove role ___\n " +
								"• invite bot\n" +
								"• help\n" +
								"\n" +
								"———-——-—-—-——-———\n" +
								"Also comes with a passive n-word filter.")
				.setFooter("Feel free to send Asyrium#2101 dumb suggestions.")
				.setTitle("Available Commands");
		event.getChannel().sendMessage(embed);
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
	
	/**
	 * Returns instructions on how to query to remove a role.
	 * @param event The message event.
	 */
	public static void getRemoveRoleHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("Type the following to remove a given role from yourself. In case you're embarrassed of it, or something.\n\n" +
								"• jb remove role *rolename*");
		event.getChannel().sendMessage(embed);
	}
	
	/**
	 * Returns instructions on how to query for a dice roll.
	 * @param event The message event.
	 */
	public static void getRollHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("Type the following to roll *X* number of *Y*-sided die.\n\n" +
								"• jb roll *X*d*Y*\n" +
								"You can also add or subtract a number *Z* from the dice rolls.\n\n" +
								"• jb roll *X*d*Y* + Z\n" +
								"• jb roll*2*d*20*+2+2-3\n" + 
								"———-——-—-—-——-———\n" +
								"Note that you only actually need a space after jb; nothing else should be space-sensitive.");
		event.getChannel().sendMessage(embed);
	}
}
