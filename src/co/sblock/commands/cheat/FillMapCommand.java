package co.sblock.commands.cheat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockAsynchronousCommand;

import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockDirt;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.BlockStone;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.ItemWorldMap;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.MaterialMapColor;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldMap;

/**
 * Basically ripped straight out of ItemWorldMap.
 * 
 * @author Jikoo
 */
public class FillMapCommand extends SblockAsynchronousCommand {

	public FillMapCommand(Sblock plugin) {
		super(plugin, "fillmap");
		setPermissionLevel("denizen");
		setUsage("/fillmap <map id>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		if (!sender.hasPermission("ask.adam.before.using")) {
			sender.sendMessage("Incomplete. Don't use this.");
			return true;
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
		Renderer render = new Renderer(view);
		render.runTaskTimer(getPlugin(), 0, 1);
		return true;
	}

	private class Renderer extends BukkitRunnable {

		private final ItemWorldMap mapItem;
		private final World world;
		private final WorldMap map;
		private final int increment;
		private final int x;
		private final int z;
		private int dX = 0;
		private int dZ = 0;

		@SuppressWarnings("deprecation")
		public Renderer(MapView view) {
			mapItem = Items.FILLED_MAP;
			world = ((CraftWorld) view.getWorld()).getHandle();
			map = mapItem.getSavedMap(new ItemStack(mapItem, 1, view.getId()), world);
			increment = 16 >> map.scale;
			dX = dZ = increment * 63;
			x = dX + map.centerX;
			z = dZ + map.centerZ;
		}

		@Override
		public void run() {
			int i = 1 << map.scale;
			int j = map.centerX;
			int k = map.centerZ;
			int l = MathHelper.floor(x - dX - j) / i + 64;
			int i1 = MathHelper.floor(z - dZ - k) / i + 64;
			int j1 = 128 / i;

			System.out.println(x + " " + z + " rendering at " + l + " " + i1 + " offsets " + dX + " and " + dZ);

			if (world.worldProvider.o()) {
				j1 /= 2;
			}

			for (int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
				double d0 = 0.0D;

				for (int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
					if ((k1 >= 0) && (l1 >= -1) && (k1 < 128) && (l1 < 128)) {
						int i2 = k1 - l;
						int j2 = l1 - i1;
						boolean flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
						int k2 = (j / i + k1 - 64) * i;
						int l2 = (k / i + l1 - 64) * i;
						HashMultiset<MaterialMapColor> hashmultiset = HashMultiset.create();
						Chunk chunk = world.getChunkAtWorldCoords(new BlockPosition(k2, 0, l2));

						if (!(chunk.isEmpty())) {
							int i3 = k2 & 0xF;
							int j3 = l2 & 0xF;
							int k3 = 0;
							double d1 = 0.0D;

							if (world.worldProvider.o()) {
								int l3 = k2 + l2 * 231871;

								l3 = l3 * l3 * 31287121 + l3 * 11;
								if ((l3 >> 20 & 0x1) == 0)
									hashmultiset.add(
											Blocks.DIRT.g(Blocks.DIRT.getBlockData().set(
													BlockDirt.VARIANT,
													BlockDirt.EnumDirtVariant.DIRT)), 10);
								else {
									hashmultiset.add(
											Blocks.STONE.g(Blocks.STONE.getBlockData().set(
													BlockStone.VARIANT,
													BlockStone.EnumStoneVariant.STONE)), 100);
								}

								d1 = 100.0D;
							} else {
								BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

								for (int i4 = 0; i4 < i; ++i4) {
									for (int j4 = 0; j4 < i; ++j4) {
										int k4 = chunk.b(i4 + i3, j4 + j3) + 1;
										IBlockData iblockdata = Blocks.AIR.getBlockData();

										if (k4 > 1) {
											do {
												--k4;
												iblockdata = chunk
														.getBlockData(blockposition_mutableblockposition
																.c(i4 + i3, k4, j4 + j3));
											} while ((iblockdata.getBlock().g(iblockdata) == MaterialMapColor.b)
													&& (k4 > 0));

											if ((k4 > 0)
													&& (iblockdata.getBlock().getMaterial()
															.isLiquid())) {
												int l4 = k4 - 1;
												Block block;
												do {
													block = chunk
															.getTypeAbs(i4 + i3, l4--, j4 + j3);
													++k3;
												} while ((l4 > 0)
														&& (block.getMaterial().isLiquid()));
											}
										}

										d1 += k4 / i * i;
										hashmultiset.add(iblockdata.getBlock().g(iblockdata));
									}
								}
							}

							k3 /= i * i;
							double d2 = (d1 - d0) * 4.0D / (i + 4) + ((k1 + l1 & 0x1) - 0.5D)
									* 0.4D;
							byte b0 = 1;

							if (d2 > 0.6D) {
								b0 = 2;
							}

							if (d2 < -0.6D) {
								b0 = 0;
							}

							MaterialMapColor materialmapcolor = Iterables
									.getFirst(Multisets.copyHighestCountFirst(hashmultiset),
											MaterialMapColor.b);

							if (materialmapcolor == MaterialMapColor.n) {
								d2 = k3 * 0.1D + (k1 + l1 & 0x1) * 0.2D;
								b0 = 1;
								if (d2 < 0.5D) {
									b0 = 2;
								}

								if (d2 > 0.9D) {
									b0 = 0;
								}
							}

							d0 = d1;
							if ((l1 >= 0) && (i2 * i2 + j2 * j2 < j1 * j1)
									&& (((!(flag1)) || ((k1 + l1 & 0x1) != 0)))) {
								byte b1 = map.colors[(k1 + l1 * 128)];
								byte b2 = (byte) (materialmapcolor.M * 4 + b0);

								if (b1 != b2) {
									map.colors[(k1 + l1 * 128)] = b2;
									map.flagDirty(k1, l1);
								}
							}
						}
					}
				}
			}

			dX -= increment;
			if (dX < increment * -64) {
				dX = increment * 63;
				dZ -= increment;
			}
			if (dZ < increment * -64) {
				cancel();
			}

		}

	}

}
