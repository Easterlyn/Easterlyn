package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.github.jikoo.planarwrappers.util.Generics;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("lore")
@Description("{@@sink.module.lore.description}")
@CommandPermission("easterlyn.command.lore")
public class LoreCommand extends BaseCommand {

  @Dependency EasterlynCore core;

  @Subcommand("clearmeta")
  @Description("{@@sink.module.lore.clearmeta.description}")
  @Syntax("")
  @CommandCompletion("")
  public void clearMeta(@NotNull @Flags(CoreContexts.SELF) Player player) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    hand.setItemMeta(null);

    core.getLocaleManager().sendMessage(player, "sink.module.lore.clearmeta.success");
  }

  @CommandAlias("head")
  @Subcommand("owner")
  @Description("{@@sink.module.lore.owner.description}")
  @Syntax("<player>")
  @CommandCompletion("@player")
  public void owner(
      @NotNull @Flags(CoreContexts.SELF) Player player,
      @Nullable @Single @Optional String owner) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if ("head".equals(getExecCommandLabel())) {
      if (hand.getType() != Material.PLAYER_HEAD) {
        hand.setType(Material.PLAYER_HEAD);
      }
    } else {
      if (handLacksMeta(player, hand)) {
        return;
      }

      if (hand.getType() != Material.PLAYER_HEAD) {
        core.getLocaleManager().sendMessage(player, "sink.module.lore.error.not_head");
        return;
      }
    }

    Generics.consumeAs(
        SkullMeta.class,
        hand.getItemMeta(),
        skullMeta -> {
          // TODO custom heads - null UUID, name, data
          skullMeta.setOwnerProfile(
              owner == null
                  ? null
                  : core.getServer().createPlayerProfile(owner));
          hand.setItemMeta(skullMeta);
        });
    if (owner != null) {
      core.getLocaleManager()
          .sendMessage(player, "sink.module.lore.owner.success", "{value}", owner);
    } else {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.delete.owner.success");
    }
  }

  @Subcommand("texture")
  @Description("{@@sink.module.lore.texture.description}")
  @Syntax("<hex string>")
  @CommandCompletion("")
  public void texture(
      @NotNull @Flags(CoreContexts.SELF) Player player,
      @NotNull @Single String hex) {
    ItemStack hand = player.getInventory().getItemInMainHand();
    if (handLacksMeta(player, hand)) {
      return;
    }
    if (hand.getType() != Material.PLAYER_HEAD) {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.error.not_head");
      return;
    }

    String format = "{SkullOwner:{Id:\"[I;%s,%s,%s,%s]\",Properties:{textures:[{Value:\"%s\"}]}}}";
    UUID uuid = UUID.nameUUIDFromBytes(hex.getBytes());
    long mostSigBits = uuid.getMostSignificantBits();
    long leastSigBits = uuid.getLeastSignificantBits();

    Bukkit.getUnsafe().modifyItemStack(
        hand,
        String.format(
            format,
            mostSigBits >> 32,
            mostSigBits & Integer.MAX_VALUE,
            leastSigBits >> 32,
            leastSigBits & Integer.MAX_VALUE,
            hex));
  }

  @Subcommand("author")
  @Description("{@@sink.module.lore.author.description}")
  @Syntax("<author>")
  @CommandCompletion("@player")
  public void author(
      @NotNull @Flags(CoreContexts.SELF) Player player,
      @Nullable @Optional String args) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.error.not_book");
      return;
    }
    String author = args == null ? null : ChatColor.translateAlternateColorCodes('&', args);
    Generics.consumeAs(
        BookMeta.class,
        hand.getItemMeta(),
        bookMeta -> {
          bookMeta.setAuthor(author);
          hand.setItemMeta(bookMeta);
        });
    if (author != null) {
      core.getLocaleManager()
          .sendMessage(player, "sink.module.lore.author.success", "{value}", author);
    } else {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.delete.author.success");
    }
  }

  @Subcommand("title")
  @Description("{@@sink.module.lore.title.description}")
  @Syntax("<title>")
  @CommandCompletion("Sample Text")
  public void title(
      @NotNull @Flags(CoreContexts.SELF) Player player,
      @Nullable @Optional String args) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.error.not_book");
      return;
    }
    String title = args == null ? null : ChatColor.translateAlternateColorCodes('&', args);
    Generics.consumeAs(
        BookMeta.class,
        hand.getItemMeta(),
        bookMeta -> {
          bookMeta.setTitle(title);
          hand.setItemMeta(bookMeta);
        });
    if (title != null) {
      core.getLocaleManager()
          .sendMessage(player, "sink.module.lore.title.success", "{value}", title);
    } else {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.delete.title.success");
    }
  }

  @Subcommand("name")
  @Description("{@@sink.module.lore.name.description}")
  @Syntax("<name>")
  @CommandCompletion("Sample Text")
  public void name(
      @NotNull @Flags(CoreContexts.SELF) Player player,
      @Nullable @Optional String args) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    String name = args == null ? null : ChatColor.translateAlternateColorCodes('&', args);
    Generics.consumeAs(
        ItemMeta.class,
        hand.getItemMeta(),
        bookMeta -> {
          bookMeta.setDisplayName(name);
          hand.setItemMeta(bookMeta);
        });
    if (name != null) {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.name.success", "{value}", name);
    } else {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.delete.name.success");
    }
  }

  @Subcommand("delete owner")
  @Description("{@@sink.module.lore.delete.owner.description}")
  @Syntax("")
  @CommandCompletion("")
  public void deleteOwner(@NotNull @Flags(CoreContexts.SELF) Player player) {
    owner(player, null);
  }

  @Subcommand("delete author")
  @Description("{@@sink.module.lore.delete.author.description}")
  @Syntax("")
  @CommandCompletion("")
  public void deleteAuthor(@NotNull @Flags(CoreContexts.SELF) Player player) {
    author(player, null);
  }

  @Subcommand("delete title")
  @Description("{@@sink.module.lore.delete.title.description}")
  @Syntax("")
  @CommandCompletion("")
  public void deleteTitle(@NotNull @Flags(CoreContexts.SELF) Player player) {
    title(player, null);
  }

  @Subcommand("delete name")
  @Description("{@@sink.module.lore.delete.name.description}")
  @Syntax("")
  @CommandCompletion("")
  public void deleteName(@NotNull @Flags(CoreContexts.SELF) Player player) {
    name(player, null);
  }

  @Subcommand("delete")
  @Description("{@@sink.module.lore.delete.description}")
  @Syntax("<line>")
  @CommandCompletion("@integer") // TODO range for lore
  public void delete(@NotNull @Flags(CoreContexts.SELF) Player player, int line) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    Generics.consumeAs(
        ItemMeta.class,
        hand.getItemMeta(),
        meta ->
            handleLore(
                player,
                line,
                "",
                hand,
                meta,
                (lore, newLine) -> {
                  lore.remove(line - 1);
                  return "sink.module.lore.delete.success";
                }));
  }

  @Subcommand("add")
  @Description("{@@sink.module.lore.add.description}")
  @Syntax("<text>")
  @CommandCompletion("")
  public void add(@NotNull @Flags(CoreContexts.SELF) Player player, @NotNull String text) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    String loreLine = ChatColor.translateAlternateColorCodes('&', text);
    Generics.consumeAs(
        ItemMeta.class,
        hand.getItemMeta(),
        meta -> {
          ArrayList<String> lore = new ArrayList<>();
          if (meta.hasLore()) {
            lore.addAll(Objects.requireNonNull(meta.getLore()));
          }
          lore.add(loreLine);
          meta.setLore(lore);
          hand.setItemMeta(meta);
        });
    core.getLocaleManager()
        .sendMessage(player, "sink.module.lore.add.success", "{value}", loreLine);
  }

  @Subcommand("set")
  @Description("{@@sink.module.lore.set.description}")
  @Syntax("<line> <text>")
  @CommandCompletion("@integer")
  public void set(
      @NotNull @Flags(CoreContexts.SELF) Player player,
      int line,
      @NotNull String text) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    Generics.consumeAs(
        ItemMeta.class,
        hand.getItemMeta(),
        meta ->
            handleLore(
                player,
                line,
                text,
                hand,
                meta,
                (lore, newLine) -> {
                  lore.set(line - 1, newLine);
                  return "sink.module.lore.set.success";
                }));
  }

  @Subcommand("insert")
  @Description("{@@sink.module.lore.insert.description}")
  @Syntax("<line> <text>")
  @CommandCompletion("@integer")
  private void insert(
      @NotNull @Flags(CoreContexts.SELF) Player player,
      int line,
      @NotNull String text) {
    ItemStack hand = player.getInventory().getItemInMainHand();

    if (handLacksMeta(player, hand)) {
      return;
    }

    Generics.consumeAs(
        ItemMeta.class,
        hand.getItemMeta(),
        meta ->
            handleLore(
                player,
                line,
                text,
                hand,
                meta,
                (lore, newLine) -> {
                  lore.add(line - 1, newLine);
                  return "sink.module.lore.insert.success";
                }));
  }

  private boolean handLacksMeta(@NotNull Player player, @NotNull ItemStack itemStack) {
    if (itemStack.getType() == Material.AIR) {
      core.getLocaleManager().sendMessage(player, "core.common.no_item");
      return true;
    }
    if (!itemStack.hasItemMeta() && itemStack.getItemMeta() == null) {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.error.no_meta_support");
      return true;
    }
    return false;
  }

  private void handleLore(
      @NotNull Player player,
      int line,
      @NotNull String text,
      @NotNull ItemStack hand,
      @NotNull ItemMeta meta,
      @NotNull BiFunction<List<String>, String, String> function) {
    if (!meta.hasLore()) {
      core.getLocaleManager().sendMessage(player, "sink.module.lore.error.no_lore");
      return;
    }
    ArrayList<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
    if (line < 1 || lore.size() < line) {
      core.getLocaleManager()
          .sendMessage(
              player,
              "core.common.number_within",
              "{min}",
              "1",
              "{max}",
              String.valueOf(lore.size()));
      return;
    }
    text = ChatColor.translateAlternateColorCodes('&', text);
    String oldLine = lore.get(line - 1);
    String messageKey = function.apply(lore, text);
    meta.setLore(lore);
    hand.setItemMeta(meta);
    core.getLocaleManager()
        .sendMessage(
            player,
            messageKey,
            "{value}",
            text,
            "{line}",
            String.valueOf(line),
            "{old_value}",
            oldLine);
  }

}
