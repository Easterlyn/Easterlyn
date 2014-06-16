package co.sblock.module;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag to place on executing methods indicating that the command should be registered.
 * <p>
 * 
 * About the command system: Command handlers are placed into a class implementing the
 * CommandListener interface, and must be registered in the module through
 * registerCommands(CommandListener). Each command handler must be a public method that returns a
 * boolean indicating that the command is well-formed and can be run to completion. The method must
 * be named after the command that it handles and tagged with this annotation. The first parameter
 * is the CommandSender object for who is issuing the command, and the second is a String[]
 * containing all command arguments. Additional annotations can be used to provide supplemental
 * details when forming the command.
 * <p>
 * 
 * An example method for executing a ban command in this format: /ban (player) (message)
 * <p>
 * 
 * <pre>
 * // in a class implementing CommandListener
 * &#064;CommandDescription(&quot;Ban a player.&quot;)
 * &#064;CommandPermission(&quot;group.horrorterror&quot;)
 * &#064;CommandUsage(&quot;/ban (player)&quot;)
 * &#064;SblockCommand(consoleFriendly = true)
 * public boolean ban(CommandSender sender, String[] args) {
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

}
