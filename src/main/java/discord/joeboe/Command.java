package discord.joeboe;

import java.util.Map;
import java.util.Set;

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
	
	/**
	 * Returns server info with regards to filtered words, shame words, privileges, etc.
	 * @param event The nessage event.
	 */
	public static void getInfo(MessageCreateEvent event) {
		if (checkAdminPrivileges(event)) {
			// Retrieve database info.
			String serverId = event.getServer().get().getIdAsString();
			ChatFilterController cfc = new ChatFilterController();
			Map<String, String> filteredWords = cfc.getChatFilter(serverId);
			Set<String> shameWords = cfc.getShameWords(serverId);
			String defaultReplacementWord = cfc.getDefaultReplacementWord(serverId);
			if (defaultReplacementWord == null) {
				defaultReplacementWord = ChatFilter.DEFAULT_REPLACEMENT_WORD;
			}
			
			EmbedBuilder embed = new EmbedBuilder()
				.setDescription("**Filtered words:**\n" + 
								"```" +
								filteredWords +
								"```\n" +
								"**Shame words:**\n" +
								"```" +
								shameWords +
								"```\n" +
								"**Default replacement word:**\n" +
								"```" +
								defaultReplacementWord +
								"```\n" +
								"———-——-—-—-——-———\n" +
								"Any filtered words mapped to *null* will be replaced with the default replacement word, *" 
								+ defaultReplacementWord + "*.")
				.setFooter("Feel free to send Asyrium#2101 dumb suggestions.")
				.setTitle("Info");
			event.getChannel().sendMessage(embed);
		}
	}
	
	public static void getHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("Type commands in lowercase, exactly one space after *jb*.\n" + 
								"```" +
								"• roll *X*d*Y*\n" +
								"• shame count\n" +
								"• bot invite\n" +
								"• help\n" +
								"• admin help\n" +
								"```" +
								"———-——-—-—-——-———\n" +
								"Also comes with a passive n-word filter.")
				.setFooter("Feel free to send Asyrium#2101 dumb suggestions.")
				.setTitle("Available Commands");
		event.getChannel().sendMessage(embed);
	}
	
	public static void getAdminHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("Type commands in lowercase, exactly one space after *jb*.\n" + 
								"```" +
								"• filter word ___\n" +
								"• filter word ___ [___]\n" +
								"• remove filter word ___\n" +
								"• default replacement word ___\n" +
								"• shame word ___\n" +
								"• remove shame word ___\n" +
								"• remove role ___\n" +
								"• info\n" +
								"```" +
								"———-——-—-—-——-———\n" +
								"Type a command as-is without '___' to get more info.")
				.setFooter("Feel free to send Asyrium#2101 dumb suggestions.")
				.setTitle("Available Admin Commands");
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
								"• jb remove role *rolename*")
				.setTitle("remove role");
		event.getChannel().sendMessage(embed);
	}
	
	/**
	 * Add a word to the chat filter, along with the word to replace it.
	 * @param event The message event.
	 * @param word The filtered word.
	 * @param replacementWord The replacement word.
	 */
	public static void addFilterWord(MessageCreateEvent event, String word, String replacementWord) {
		if (checkAdminPrivileges(event)) {
			new ChatFilterController().addWordToChatFilter(event.getServer().get().getIdAsString(), word, replacementWord);
		}
	}
	
	/**
	 * Add a word to the chat filter.
	 * @param event The message event.
	 * @param word The filtered word.
	 */
	public static void addFilterWord(MessageCreateEvent event, String word) {
		addFilterWord(event, word, null);
	}
	
	/**
	 * Remove a word from the chat filter. This also removes it from the list of shame words, if applicable.
	 * @param event The message event.
	 * @param word The filtered word.
	 */
	public static void removeFilterWord(MessageCreateEvent event, String word) {
		if (checkAdminPrivileges(event)) {
			ChatFilterController cfc = new ChatFilterController();
			String serverId = event.getServer().get().getIdAsString();
			cfc.removeWordFromChatFilter(serverId, word);
			cfc.removeWordFromShameWords(serverId, word);
			
		}
	}
	
	/**
	 * Returns instructions on how to query to add a word to the chat filter.
	 * @param event The message event.
	 */
	public static void getFilterWordHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("This bot will replace filtered words found in messages that contain them.\n\n" +
								"By default, variations of the n-word are filtered and replaced with 'panda' to ensure a family friendly environment!\n\n" +
								"Type the following to add a word to the server's chat filter.\n\n" +
								"• jb filter word *filterword*\n\n" +
								"Optionally, a replacement word can also be mapped to a filtered word by wrapping it with brackets, as follows.\n\n" +
								"• jb filter word *filterword* [*replacementword*]\n\n" +
								"Type the following to remove a word from the server's chat filter, along with its list of shame words.\n\n" +
								"• jb remove filter word *filterword*")
				.setTitle("filter word");
		event.getChannel().sendMessage(embed);
	}
	
	/**
	 * Add a word to the list of shame words.
	 * @param event The message event.
	 * @param word The shame word.
	 */
	public static void addShameWord(MessageCreateEvent event, String word) {
		if (checkAdminPrivileges(event)) {
			new ChatFilterController().addWordToShameWords(event.getServer().get().getIdAsString(), word);
		}
	}
	
	/**
	 * Remove a word from the list of shame words.
	 * @param event The message event.
	 * @param word The filtered word.
	 */
	public static void removeShameWord(MessageCreateEvent event, String word) {
		if (checkAdminPrivileges(event)) {
			new ChatFilterController().removeWordFromShameWords(event.getServer().get().getIdAsString(), word);
			
		}
	}
	
	/**
	 * Returns instructions on how to query to add a word to the list of shame words.
	 * @param event The message event.
	 */
	public static void getShameWordHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("This bot will track the number of times a user has said a shame word.\n\n" +
								"By default, variations of the n-word are considered shame words.\n\n" +
								"The first time a user says a shame word, they will be given a shame role as punishment.\n\n" +
								"Type the following to add a word to the server's list of shame words.\n\n" +
								"• jb shame word *shameword*\n\n" +
								"Type the following to remove a word from the server's list of shame words.\n\n" +
								"• jb remove shame word *shameword*")
				.setTitle("shame word");
		event.getChannel().sendMessage(embed);
	}
	
	/**
	 * Update the default replacement word.
	 * @param event The message event.
	 * @param word The default replacement word.
	 */
	public static void setDefaultReplacementWord(MessageCreateEvent event, String word) {
		if (checkAdminPrivileges(event)) {
			new ChatFilterController().setDefaultReplacementWord(event.getServer().get().getIdAsString(), word);
		}
	}
	
	/**
	 * Returns instructions on how to query to add a word to the list of shame words.
	 * @param event The message event.
	 */
	public static void getDefaultReplacementWordHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("In the case that a filtered word has no user-given replacement word, " +
								"it will default to the default replacement word.\n\n" +
								"By default, the default replacement word is 'panda'.\n\n" +
								"Type the following to update the default replacement word.\n\n" +
								"• jb default replacement word *defaultreplacementword*\n\n")
				.setTitle("default replacement word");
		event.getChannel().sendMessage(embed);
	}
	
	/**
	 * Returns instructions on how to query for a dice roll.
	 * @param event The message event.
	 */
	public static void getRollHelp(MessageCreateEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription("Type the following to roll *X* number of *Y*-sided die.\n\n" +
								"• jb roll *X*d*Y*\n\n" +
								"You can also add or subtract a number *Z* from the dice rolls.\n\n" +
								"• jb roll *X*d*Y* + Z\n" +
								"• jb roll *2*d*20*+2+2-3\n" + 
								"———-——-—-—-——-———\n" +
								"Note that you only actually need a space after jb; nothing else should be space-sensitive.")
				.setTitle("roll");
		event.getChannel().sendMessage(embed);
	}
	
	/**
	 * Returns querier's shame count.
	 * @param event The message event.
	 */
	public static void getShameCount(MessageCreateEvent event) {
		// Get shame count from database.
		ChatFilterController cfc = new ChatFilterController();
		String serverId = event.getServer().get().getIdAsString();
		String userId = event.getMessageAuthor().getIdAsString();
		int shameCount = cfc.getShameCount(serverId, userId);
		
		// Return message.
		String userName = event.getMessageAuthor().getDisplayName();
		String customEmoji = "🐼";
		EmbedBuilder embed = new EmbedBuilder()
				.setDescription(customEmoji + " Panda count: " + shameCount + " " + customEmoji)
				.setTitle(userName);
		event.getChannel().sendMessage(embed);
	}
	
	public static boolean checkAdminPrivileges(MessageCreateEvent event) {
		if (event.getMessageAuthor().isServerAdmin()) {
			return true;
		}
		else {
			EmbedBuilder embed = new EmbedBuilder()
					.setDescription("You do not have access to this command on this server.\n");
			event.getChannel().sendMessage(embed);
			return false;
		}
	}
}
