/**
 * 
 */
package co.sblock.Sblock;

import java.lang.annotation.*;

/**
 * Tag to place on CommandExecutors indicating that the command should be registered.
 * @author FireNG
 *
 */
@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.TYPE)
public @interface CommandHandler
{
	/**
	 * Name of the command that this executor will handle.
	 */
	

	public String name();
}
