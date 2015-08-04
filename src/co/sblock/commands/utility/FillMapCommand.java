package co.sblock.commands.utility;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import co.sblock.chat.Color;
import co.sblock.commands.SblockAsynchronousCommand;

/**
 * 
 * 
 * @author Jikoo
 */
public class FillMapCommand extends SblockAsynchronousCommand {

	public FillMapCommand() {
		super("fillmap");
		setPermissionLevel("denizen");
		setUsage("/fillmap <map id>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		short id;
		try {
			id = Short.valueOf(args[0]);
		} catch (NumberFormatException e) {
			return false;
		}

		@SuppressWarnings("deprecation")
		MapView view = Bukkit.getMap(id);

		if (view == null) {
			sender.sendMessage(Color.BAD + "Invalid map ID!");
			return true;
		}
		Renderer render = new Renderer();
		view.addRenderer(render);
		for (int i = 0; i < 16384; i++) {
			// TODO
		}
		view.removeRenderer(render);
		sender.sendMessage("This function is not actually implemented.");
		return false;
	}

	private class Renderer extends MapRenderer {

		@Override
		public void render(MapView map, MapCanvas canvas, Player player) {
			//map.
			// TODO
		}

	}

}
