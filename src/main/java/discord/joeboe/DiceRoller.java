package discord.joeboe;

import java.awt.Color;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class DiceRoller {

	/**
	 * Rolls a given number of dice with a given number of faces and displays the results on screen.
	 * @param event The message event.
	 * @param rollMsg The message written, after the command.
	 */
	public static void rollDice(MessageCreateEvent event, String rollMsg) {
		// Parse the input message.
		String[] parsedMsg = parseMsgFormat(rollMsg);
		if (parsedMsg == null) {
			return;
		}
		int numOfDie = Integer.parseInt(parsedMsg[0]);
		int numOfFaces = Integer.parseInt(parsedMsg[1]);
		String modifier = parsedMsg[2];
		System.out.println(numOfDie + " | " + numOfFaces + " | " + modifier);
		
		// Roll generation.
		int[] rolls = new int[numOfDie];
		int max = 0;
		int total = 0;
		for (int i = 0; i < numOfDie; i++) {
			rolls[i] = (int) (Math.random() * numOfFaces) + 1;
			if (rolls[i] > max) {
				max = rolls[i];
			}
			total += rolls[i];
		}
		
		// Add modifiers to the rolls.
		int moddedMax = max;
		if (!modifier.equals("")) {
			try {
				String[] numerics = modifier.split("[+-]");
				int i = 1;
				CharacterIterator iter = new StringCharacterIterator(modifier);
				while (iter.current() != CharacterIterator.DONE) {
					if (iter.current() == '+') {
						moddedMax = moddedMax + Integer.parseInt(numerics[i]);
						total = total + Integer.parseInt(numerics[i]);
						i++;
					}
					else if (iter.current() == '-') {
						moddedMax = moddedMax - Integer.parseInt(numerics[i]);
						total = total - Integer.parseInt(numerics[i]);
						i++;
					}
					iter.next();
				}
			}
			catch (Exception e) {
				System.out.println("Something went wrong with adding modifier to roll \'" + rollMsg + "\'");
			}
		}
		
		// Return results in a decorated format.
		event.getChannel().sendMessage(formatMessageAsEmbed(event, rollMsg, numOfFaces, rolls, max, moddedMax, total));
	}
	
	/**
	 * Builds a decorated, embedded dice roll result message.
	 * @param event The roller's query message's creation event.
	 * @param rollMsg The original roll query message.
	 * @param numOfFaces The number of faces used for the roll query.
	 * @param rolls An array containing individual dice roll results.
	 * @param bestRoll The best roll out of the roller's results, before modification.
	 * @param moddedBestRoll The best roll out of the roller's results, after modification.
	 * @param total The total of the roller's results, after modification.
	 * @return Returns a decorated, embedded dice roll result message.
	 */
	private static EmbedBuilder formatMessageAsEmbed(MessageCreateEvent event, String rollMsg, int numOfFaces, int[] rolls, int bestRoll, int moddedBestRoll, int total) {
		
		// Get roller's name.
		MessageAuthor author = event.getMessageAuthor();

		// Create an embed.
		EmbedBuilder embed = new EmbedBuilder()
				.setAuthor(author)
				.setDescription("*...rolled for " + rollMsg + " and got:*\n\n" +
								"**Rolls:** " + Arrays.toString(rolls) + "\n" +
								"**Best:** " + moddedBestRoll + "\n" +
								"**Total:** " + total);

		// Set the embed's border color based on roll results.
		// HSB varies from red to green based on hue from 0~120. (Using 0~.33 | .83 | .75)
		final Color MAGENTA = Color.getHSBColor(.90f, .85f, .43f);
		final Color DIAMOND = Color.getHSBColor(.47f, .40f, 1f);
		final Color WHITE = Color.getHSBColor(1, 0, 1);
		if (numOfFaces == 1) {
			embed.setColor(WHITE);
			embed.setFooter("Why are you even rolling a dice for this?");
		}
		else if (bestRoll == 1) {
			embed.setColor(MAGENTA);
			embed.setFooter("Critical Fail!");
		}
		else if (bestRoll == numOfFaces) {
			embed.setColor(DIAMOND);
			embed.setFooter("Critical Hit!");
		}
		else {
			float hue =  (.33f / (numOfFaces - 2)) * (bestRoll - 2);	// Red -> Yellow -> Green color
			embed.setColor(Color.getHSBColor(hue, .83f, .75f));
		}
		return embed;
	}
	
	/**
	 * Parses the roll message into three parts: number of dice, number of faces, and the arithmetic modifier (i.e: +1).
	 * Returns those three parts as an array of strings. Invalid inputs result in null being returned instead.
	 * Note that while number of dices and number of faces are also checked for valid input, the modifier is not.
	 * @param rollMsg The roll message to be parsed.
	 * @return Returns an array containing string values of the number of dice, the number of faces, and the modifier.
	 */
	private static String[] parseMsgFormat(String rollMsg) {
		
		// Check if rollMsg is valid. If not, do nothing.
		if (rollMsg.length() >= 2) {
			
			String numOfDie = "";
			String numOfFaces = "";
			String modifier = "";
			
			// If no "d" in the roll message, invalid command.
			int dIndex = rollMsg.indexOf("d");
			if (dIndex == -1) {
				return null;
			}
			
			// Check if a modifier exists in the roll.
			String modSymbol = "";
			if (rollMsg.contains("+")) {
				modSymbol = "+";
			}
			else if (rollMsg.contains("-")) {
				modSymbol = "-";
			}
			boolean modifierExists = !modSymbol.equals("");

			// Get number of dice. Default to 1 die if no amount given.
			numOfDie = rollMsg.substring(0, dIndex);
			if (numOfDie.length() < 1) {
				numOfDie = "1";
			}
			else if (!isNumeric(numOfDie) || numOfDie.contains("-") || numOfDie.contains("+")) {
				return null;
			}
			
			// Get number of faces. Do nothing if no amount given.
			String substringPastD = rollMsg.substring(dIndex + 1);
			if (modifierExists) {
				numOfFaces = substringPastD.substring(0, substringPastD.indexOf(modSymbol));
				modifierExists = true;
			}
			else {
				numOfFaces = substringPastD;
			}
			if (numOfFaces.length() < 1) {
				return null;
			}
			else if (!isNumeric(numOfFaces) || numOfFaces.contains("-") || numOfFaces.contains("+")) {
				return null;
			}
			
			// Get arithmetical modifiers to be added post-dice roll.
			if (modifierExists) {
				modifier = substringPastD.substring(substringPastD.indexOf(modSymbol));
			}
			// Create an array containing the 3 value substrings and return it.
			String[] rv = {numOfDie, numOfFaces, modifier};
			return rv;
		}
		return null;
	}
	
	/**
	 * Checks if a string is numeric.
	 * @param strNum The string to check for numericity.
	 * @return Returns true if the string is a number. Else, false.
	 */
	private static boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        int number = Integer.parseInt(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
}
