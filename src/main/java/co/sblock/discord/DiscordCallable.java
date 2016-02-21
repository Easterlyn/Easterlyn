package co.sblock.discord;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.util.HTTP429Exception;

/**
 * A simple abstraction allowing for easier handling of Discord API calls.
 * 
 * @author Jikoo
 */
public abstract class DiscordCallable {

	public abstract void call() throws DiscordException, HTTP429Exception, MissingPermissionsException;

	public abstract boolean retryOnException();

}
