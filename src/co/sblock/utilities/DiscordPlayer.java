package co.sblock.utilities;

import org.bukkit.permissions.Permission;


/**
 * 
 * 
 * @author Jikoo
 */
public class DiscordPlayer extends DummyPlayer {

	private final String name;
	private String[] groups;

	public DiscordPlayer(String name, String... groups) {
		this.name = name;
		if (groups == null) {
			this.groups = new String[] { "hero" };
		} else {
			this.groups = new String[groups.length + 1];
			System.arraycopy(groups, 0, this.groups, 1, groups.length);
			this.groups[0] = "hero";
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void sendMessage(String arg0) {
		// FIXME REPLY TO DISCORD - command response, etc.
	}

	@Override
	public void sendMessage(String[] arg0) {
		// FIXME see above
	}

	// FIXME Fixed UUID?

	@Override
	public boolean hasPermission(String arg0) {
		PermissionBridge bridge = PermissionBridge.getInstance();
		for (String group : groups) {
			if (bridge.hasPermission(group, arg0)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		return this.hasPermission(arg0.getName());
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

}
