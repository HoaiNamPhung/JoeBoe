package discord.joeboe;

import java.awt.Color;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.RoleBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class RoleManager {

	/**
	 * Creates a role if it doesn't exist.
	 * @param server The current server.
	 * @param name The name of the role.
	 * @param color The color of the role.
	 * @param mentionable Whether the role is mentionable.
	 * @return Returns the role, regardless of whether or not it was freshly created.
	 */
	public static Role createRole(Server server, String name, Color color, boolean mentionable) {
		// Make sure the role doesn't already exist.
		for (Role role : server.getRoles()) {
			if (role.getName().equals(name)) {
				return role;
			}
		}
		// Create the role and return it.
		try {
			Role newRole = new RoleBuilder(server)
					.setName(name)
					.setColor(color)
					.setMentionable(mentionable)
					.create().get();
			return newRole;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Checks if a user already has a given role.
	 * @param server The current server.
	 * @param name The name of the role.
	 * @param user The user we are checking.
	 * @return Returns whether the user already has the role.
	 */
	public static boolean hasRole(Server server, String name, User user) {
		for (Role role : user.getRoles(server)) {
			if (role.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes the given role from a user.
	 * @param server The current server.
	 * @param name The name of the role.
	 * @param user The user we are modifying the roles of.
	 */
	public static void removeRole(Server server, String name, User user) {
		for (Role role : user.getRoles(server)) {
			if (role.getName().equals(name)) {
				user.removeRole(role);
			}
		}
	}
}
