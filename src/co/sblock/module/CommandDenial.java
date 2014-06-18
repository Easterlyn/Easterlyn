package co.sblock.module;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify an alternate denial of access message for a CustomCommand.
 * <p>
 * If the user does not have the correct permission, the message specified will be displayed instead
 * of the default.
 * 
 * @author Jikoo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandDenial {
	public abstract String value() default "<&4Lil Hal&f> &cI'm sorry Dirk, I'm afraid I can't do that.";
}
