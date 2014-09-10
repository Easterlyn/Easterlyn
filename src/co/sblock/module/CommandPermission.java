package co.sblock.module;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a required permission to a CustomCommand.
 * <p>
 * The permission specified is required for the CustomCommand to execute. If the user does not have
 * the correct permission, the command's denial of permission message will be displayed.
 * 
 * @author Jikoo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandPermission {
	public abstract String value();
}
