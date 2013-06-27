package co.sblock.Sblock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author Brandon Dadosky
 * 
 */
public class TestModule extends Module {

	/*
	 * (non-Javadoc)
	 * 
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		this.registerCommands(new CommandListener() {
			@CommandHandler(name = "test")
			class TestCommandHandler implements CommandExecutor {

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command
				 * .CommandSender, org.bukkit.command.Command, java.lang.String,
				 * java.lang.String[])
				 */
				@Override
				public boolean onCommand(CommandSender sender, Command command,
						String label, String[] args) {
					sender.sendMessage("Hello there!");
					return true;
				}

			}
		});

		this.registerEvents(new Listener() {
			@EventHandler
			public void playerChats(AsyncPlayerChatEvent event) {
				TestModule.this.getLogger().info("It works!");
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		// org.bukkit.event.HandlerList.unregisterAll(this.getListener());
		// for each registered command:
		// setExecutor(null)
	}

}
