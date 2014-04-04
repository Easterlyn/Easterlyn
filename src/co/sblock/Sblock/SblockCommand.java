package co.sblock.Sblock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag to place on executing methods indicating that the command should be
 * registered.
 * <p>
 * 
 * About the command system: Command handlers are placed into a class
 * implementing the CommandListener interface, and must be registered in the
 * module through registerCommands(CommandListener).Each command handler must be
 * a public method that returns a boolean indicating that the command is
 * well-formed and can be run. The method must be named after the command that
 * it handles and tagged with this annotation. The first parameter is the
 * CommandSender object for who is issuing the command, and the second is a
 * String[] containing all command arguments.
 * <p>
 * 
 * An example method for executing a ban command in this format: /ban (player)
 * (message)
 * <p>
 * 
 * <pre>
 * // in a class implementing CommandListener
 * &#064;SblockCommand(description = "Ban a player.", usage = "/ban (player)")
 * public boolean ban(String executor, String[] args) {
 * 	// Ban code goes here...
 * }
 * </pre>
 * 
 * @author FireNG, Jikoo
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface SblockCommand {

	/**
	 * (Optional, defaults to false)
	 * <p>
	 * 
	 * Indicates that this command may accept input from the console.
	 * 
	 * @return
	 */
	public boolean consoleFriendly() default false;

	/**
	 * Provides a description for this command in lieu of an entry in the plugin
	 * description file.
	 * 
	 * @return
	 */
	public String description();

	/**
	 * Provides usage for this command in lieu of an entry in the plugin
	 * description file.
	 * 
	 * @return
	 */
	public String usage();

	/**
	 * (Optional, defaults to group.hero)
	 * <p>
	 * 
	 * Provides required permission for this command in lieu of an entry in the
	 * plugin description file.
	 * 
	 * @return
	 */
	public String permission() default "group.hero";
}
