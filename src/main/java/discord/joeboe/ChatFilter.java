package discord.joeboe;

import java.awt.Color;
import java.util.Arrays;

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
	static String[] filteredWords = {"nigga", "nigger", "cringe", "Nigga", "Nigger", "Cringe"};
	static String[] replacementWords = { "blackpilled", "frogpilled", "pewdiepilled", "redditpilled", "based", "micropilled",
			"coompilled", "pandapilled", "dogpilled", "abelpilled", "dementiapilled", "suspilled",
			"darwinpilled", "junpilled", "sonampilled", "natepilled", "kadenpilled", "kanyepilled",
			"hoaipilled", "brianpilled", "moylanpilled", "durdlepilled" };
	static String DEFAULT_REPLACEMENT_WORD = "panda";
	static String SHAME_ROLE_NAME = "may be racist???";
	static Color SHAME_ROLE_COLOR = Color.getHSBColor(.08f, .48f, .53f);
	
	/*-------------------*/

	/**
	 * Replaces the slurs within a message right after it is sent.
	 * @param event The message's creation event.
	 */
	public static void replaceSlurs(MessageCreateEvent event) {
	
		// Get the relevant data of the event.
		Message msg = event.getMessage();
		String content = event.getMessageContent();

		// Check for filtered words.
		boolean triggered = Arrays.stream(filteredWords).anyMatch(content::contains);
		
		// Ignore links to media files.
		if (content.startsWith("http") && 
				(content.endsWith(".mp4") || content.endsWith(".jpg") || content.endsWith(".png") || content.endsWith(".gif") || content.endsWith(".webm"))) {
			// Do nothing.
		}
		
		// Make sure an actual user wrote the message to be filtered, and not the bot itself. (Otherwise, recursion may happen!)
		else if (triggered && !msg.getAuthor().isBotUser()) {

			// Replace filtered words with random words or panda.
			String newContent = filterString(content, filteredWords, replacementWords);
			
			// Send the filtered message through the bot.
			EmbedBuilder embedMsg = formatMessageAsEmbed(msg, newContent);
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
			
			// Give user the "has said the n-word" role to brand them for life in shame.
			Server thisServer = event.getServer().get();
			String[] nWords = {"nigga", "nigger", "Nigga", "Nigger"};
			boolean nTriggered = Arrays.stream(nWords).anyMatch(content::contains);
			if (nTriggered) {
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
			}
		}
	}

	/**
	 * Builds a decorated, embedded message using the filtered contents of a given discord message.
	 * @param oldMsg The discord message that the embed message is referencing.
	 * @param newContent The filtered contents of the discord message that is being referenced.
	 * @return Returns a decorated, embedded message with filtered contents.
	 */
	private static EmbedBuilder formatMessageAsEmbed(Message oldMsg, String newContent) {
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
		
		// Update header to show if it's user's first time saying the n-word.
		if (!RoleManager.hasRole(oldMsg.getServer().get(), SHAME_ROLE_NAME, author.asUser().get())) {
			
			// Check for n-word.
			if (newContent.contains(DEFAULT_REPLACEMENT_WORD)) {
				String customEmoji = "🎊";
				embed.setFooter(customEmoji + " " + author.getDisplayName() + " has finally said the n-word! " + customEmoji);
			}
		}	
		return embed;
	}
	
	/**
	 * Filters a word by converting it to its corresponding replacement word if said word is a filtered word.
	 * @param inputStr The word that will be passed through the filter.
	 * @param filteredWords An array of words to filter for.
	 * @param replacementWords An array of replacement words that can replace a given filtered word.
	 * @return Returns the word after passing through the filter.
	 */
	private static String filterString(String inputStr, String[] filteredWords, String[] replacementWords) {

		// Check if filter triggered.
		boolean triggered = Arrays.stream(filteredWords).anyMatch(inputStr::contains);
		if (triggered) {
			// Generate replacement words.
			int randIndex = (int) (Math.random() * replacementWords.length);
			
			// Replace words (currently hard-coded).
			inputStr = inputStr.replace("nigga", DEFAULT_REPLACEMENT_WORD)
				.replace("Nigga", toProperNoun(DEFAULT_REPLACEMENT_WORD))
				.replace("nigger", DEFAULT_REPLACEMENT_WORD)
				.replace("Nigger", toProperNoun(DEFAULT_REPLACEMENT_WORD))
				.replace("cringe", replacementWords[randIndex])
				.replace("Cringe", toProperNoun(replacementWords[randIndex]));
		}
		// Return the filtered string.
		return inputStr;
	}
	
	/** 
	 * Converts a string to proper noun form.
	 * @param inputStr The string to convert
	 * @return Returns the string as a proper noun.
	 */
	private static String toProperNoun(String inputStr) {
		return inputStr.substring(0, 1).toUpperCase() + inputStr.substring(1);
	}
	

}
