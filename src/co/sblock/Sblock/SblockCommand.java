/**
 * 
 */
package co.sblock.Sblock;

import java.lang.annotation.*;

/**
 * Tag to place on executing methods indicating that the command should be
 * registered.
 * <p>
 * 
 * About the command system: Command handlers are placed into a class
 * implementing the CommandListener interface, and must be registered in the
 * module through registerCommands(CommandListener).Each command handler must be
 * a public method that returns a <code>boolean</code> indicating that the
 * command is well-formed and can be run. (<code>false</code> will cause the
 * usage message to be displayed to the player.) The method must be named after
 * the command that it handles and tagged with this annotation. The first
 * parameter is the CommandSender object for who is issuing the command, and the
 * remaining parameters must be strings, one for each argument in the command.
 * <p>
 * 
 * 
 * Each command (As of now at least) must still be defined in plugin.yml, and
 * its usage message and permissions should still be placed there.
 * <p>
 * 
 * An example method for executing a ban command in this format: /ban (player)
 * (message)
 * <p>
 * 
 * <pre>
 * // in a class implementing CommandListener
 * &#064;SblockCommand(mergeLast = true)
 * public boolean ban(String executor, String message) {
 * 	// Ban code goes here...
 * }
 * </pre>
 * 
 * @author FireNG
 * 
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface SblockCommand {

	/**
	 * (Optional, defaults to false)
	 * <p>
	 * 
	 * Indicates that the last argument in this command method should receive
	 * all of the remaining words the player types, allowing commands to contain
	 * multi-word messages.
	 */
	public boolean mergeLast() default false;

	/**
	 * (Optional, defaults to false)
	 * <p>
	 * 
	 * Indicates that this command may accept input from the console. If the
	 * console executes this command, <code>null</code> will be passed to the
	 * player parameter.
	 * 
	 * @return
	 */
	public boolean consoleFriendly() default false;
}
