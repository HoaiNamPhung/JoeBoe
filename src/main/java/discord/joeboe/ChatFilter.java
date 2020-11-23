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

	public static void replaceSlurs(MessageCreateEvent event) {
		// Get the relevant data of the event.
		Message msg = event.getMessage();
		String content = event.getMessageContent();

		// Check for filtered words.
		String[] filteredWords = {"nigga", "nigger", "cringe", "Nigga", "Nigger", "Cringe"};
		boolean triggered = Arrays.stream(filteredWords).anyMatch(content::contains);
		if (triggered && msg.getAuthor().isUser()) {

			// Replace filtered words with random words or panda.
			String[] replacementWords = { "blackpilled", "frogpilled", "pewdiepilled", "redditpilled", "based", "micropilled",
					"coompilled", "pandapilled", "dogpilled",
					"darwinpilled", "junpilled", "sonampilled", "natepilled", "kadenpilled", "kanyepilled",
					"hoaipilled", "brianpilled", "moylanpilled", "durdlepilled" };
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
					Role role = RoleManager.createRole(thisServer, "may be racist???", Color.DARK_GRAY, true);
					if (!RoleManager.hasRole(thisServer, "may be racist???", user)) {
						thisServer.addRoleToUser(user, role);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

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
				.setDescription(newContent)
				.setFooter("todo: track user 'panda' count");

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
		if (!RoleManager.hasRole(oldMsg.getServer().get(), "may be racist???", author.asUser().get())) {
			String customEmoji = "🎊";
			embed.setFooter(customEmoji + " " + author.getDisplayName() + " has finally said the n-word! " + customEmoji);
		}
		
		return embed;
	}
	
	private static String filterString(String inputStr, String[] filteredWords, String[] replacementWords) {

		// Check if filter triggered.
		boolean triggered = Arrays.stream(filteredWords).anyMatch(inputStr::contains);
		if (triggered) {
			
			// Generate replacement words.
			int randIndex = (int) (Math.random() * replacementWords.length);
			final String DEFAULT = "panda";
			
			// Replace words (currently hard-coded).
			inputStr = inputStr.replace("nigga", DEFAULT)
				.replace("Nigga", toProperNoun(DEFAULT))
				.replace("nigger", DEFAULT)
				.replace("Nigger", toProperNoun(DEFAULT))
				.replace("cringe", replacementWords[randIndex])
				.replace("Cringe", toProperNoun(replacementWords[randIndex]));
		}
		// Return the filtered string.
		return inputStr;
	}
	
	private static String toProperNoun(String inputStr) {
		return inputStr.substring(0, 1).toUpperCase() + inputStr.substring(1);
	}
}
