package co.sblock.module;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a description of command usage to a CustomCommand.
 * <p>
 * Usage will be displayed to a user when the execution of a CustomCommand fails.
 * 
 * @author Jikoo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandUsage {
    public abstract String value();
}
