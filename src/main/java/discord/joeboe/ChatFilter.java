package discord.joeboe;

import java.awt.Color;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public class ChatFilter {
	
	/* Adjustable Values */
	static String[] cool = {"bitch"};
	final static String DEFAULT_REPLACEMENT_WORD = "panda";
	final static String SHAME_ROLE_NAME = "may be racist???";
	final static Color SHAME_ROLE_COLOR = Color.getHSBColor(.08f, .48f, .53f);
	
	/*-------------------*/

	/**
	 * Replaces the slurs within a message right after it is sent.
	 * @param event The message's creation event.
	 */
	public static void replaceSlurs(MessageCreateEvent event) {
	
		// Get the relevant data of the event.
		String serverId = event.getServer().get().getId() + "";
		Message msg = event.getMessage();
		String content = event.getMessageContent();
		
		// Ignore links to media files.
		if (content.startsWith("http") && 
				(content.endsWith(".mp4") || content.endsWith(".jpg") || content.endsWith(".png") || content.endsWith(".gif") || content.endsWith(".webm"))) {
			return;
		}
		
		// Query database for filtered words.
		ChatFilterController cfc = new ChatFilterController();
		Map<String, String> replacementWordMap = cfc.getChatFilter(serverId);
		Set<String> filteredWords = replacementWordMap.keySet();

		// Check for filtered words.
		boolean triggered = filteredWords.stream().anyMatch(content.toLowerCase()::contains);
		
		// Make sure an actual user wrote the message to be filtered, and not the bot itself. (Otherwise, recursion may happen!)
		if (triggered && !msg.getAuthor().isBotUser()) {
			
			// Check if user said a shame word.
			boolean isShameful = assignShameRole(event, cfc);

			// Replace filtered words with their assigned replacement words.
			String replacementWord = cfc.getDefaultReplacementWord(serverId);
			if (replacementWord == null) {
				replacementWord = DEFAULT_REPLACEMENT_WORD;
			}
			String newContent = filterString(content, replacementWordMap, replacementWord);
			
			// Send the filtered message through the bot.
			EmbedBuilder embedMsg = formatMessageAsEmbed(msg, newContent, isShameful);
			event.getChannel().sendMessage(embedMsg);
			
			// Send any remaining attachments that weren't sent through the embed.
			boolean isImage = false;
			if (msg.getAttachments().size() > 0) {
				isImage = msg.getAttachments().get(0).isImage();
			}
			if (!isImage) {
				msg.toMessageBuilder().setContent("").send(event.getChannel());
			}

			// Delete the original message. (MUST BE AFTER FILTERED MESSAGE IS FINISHED, OR
			// WE WILL NOT ABLE TO RETRIEVE ORIGINAL MESSAGE DATA.)
			event.getChannel().deleteMessages(event.getMessage().getId());
		}
	}

	
	/** 
	 * Gives a user a shame role if their message contains any 'shame words'.
	 * @return Returns true if a shame word was used in the message.
	 */
	public static boolean assignShameRole(MessageCreateEvent event, ChatFilterController cfc) {	
		// Get the relevant data of the event.
		String serverId = event.getServer().get().getId() + "";
		Message msg = event.getMessage();
		String content = event.getMessageContent();
		Server thisServer = event.getServer().get();
		
		// Query database for shame words.
		Set<String> shameWords = cfc.getShameWords(serverId);
		
		// Check for shame words.
		boolean triggered = shameWords.stream().anyMatch(content.toLowerCase()::contains);
		
		if (triggered) {
			try {
				User user = msg.getUserAuthor().get();
				Role role = null;
				// Check if role needs to be created first.
				if (thisServer.getRolesByName(SHAME_ROLE_NAME).isEmpty()) {
					role = RoleManager.createRole(thisServer, SHAME_ROLE_NAME, SHAME_ROLE_COLOR, true);
				}
				else {
					role = thisServer.getRolesByName(SHAME_ROLE_NAME).get(0);
				}
				// Check if the user already has the role before giving it to them.
				if (!RoleManager.hasRole(thisServer, SHAME_ROLE_NAME, user)) {
					thisServer.addRoleToUser(user, role);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Builds a decorated, embedded message using the filtered contents of a given discord message.
	 * @param oldMsg The discord message that the embed message is referencing.
	 * @param newContent The filtered contents of the discord message that is being referenced.
	 * @param isShameful Whether or not the original message contained a 'shame word'.
	 * @return Returns a decorated, embedded message with filtered contents.
	 */
	private static EmbedBuilder formatMessageAsEmbed(Message oldMsg, String newContent, boolean isShameful) {
		// Get message data.
		MessageAuthor author = oldMsg.getAuthor();
		MessageAttachment attachment = null;
		if (oldMsg.getAttachments().size() > 0) {
			attachment = oldMsg.getAttachments().get(0);
		}

		// Create an embed containing the filtered contents of the deleted message.
		EmbedBuilder embed = new EmbedBuilder()
				.setAuthor(author)
				.setDescription(newContent);

		// Set the embed's image to the original message's attachment image, if it exists.
		if (attachment != null) {
			if (attachment.isImage()) {
				try {
					embed.setImage(attachment.downloadAsInputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Set the embed's border color based on the author's role color, if it exists.
		if (author.getRoleColor().isPresent()) {
			embed.setColor(author.getRoleColor().get());
		}
		
		// Update header to show if it's user's first time saying a shame word.
		if (!RoleManager.hasRole(oldMsg.getServer().get(), SHAME_ROLE_NAME, author.asUser().get())) {
			
			// Check for shame word.
			if (isShameful) {
				String customEmoji = "🎊";
				embed.setFooter(customEmoji + " " + author.getDisplayName() + " has finally said the n-word! " + customEmoji);
			}
		}	
		return embed;
	}
	
	/**
	 * Filters a word by converting it to its corresponding replacement word if said word is a filtered word.
	 * @param inputStr The word that will be passed through the filter.
	 * @param replacementWordMap A mapping of filtered words to their replacement words.
	 * @return Returns the word after passing through the filter.
	 */
	private static String filterString(String inputStr, Map<String, String> replacementWordMap, String defaultReplacementWord) {
		Set<String> filteredWords = replacementWordMap.keySet();
		for (String filteredWord : filteredWords) {
			// If no possible replacement word found, use default replacement word specified outside of database.
			String replacementWord = replacementWordMap.get(filteredWord);
			if (replacementWord == null) {	
				replacementWord = defaultReplacementWord;
			}
			inputStr = inputStr.replaceAll("(?i)"+Pattern.quote(filteredWord), replacementWord); // (?i) = regex for case insensitive.
		}
		return inputStr;
	}
}
