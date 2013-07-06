/**
 * 
 */
package co.sblock.Sblock;

/**
 * This interface marks a class that contains inner classes that handle
 * commands.
 * 
 * To create commands, create a PUBLIC STATIC inner class that implements CommandExecutor. Tag each
 * class with <code>@CommandHandler(name = {name})</code> above the class declaration,
 * where <code>{name}</code> is the name of the command being handled. To register your listener, call
 * <code>this.registerCommands(new MyCommandListener())</code> in your module's <code>onEnable</code> method.
 * 
 * @author FireNG
 */
public interface CommandListener {

}
