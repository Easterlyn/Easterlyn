package com.easterlyn.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of useful string manipulation functions.
 *
 * @author Jikoo
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class StringUtil {

	public static final Pattern IP_PATTERN = Pattern.compile("([0-9]{1,3}\\.){3}[0-9]{1,3}");
	public static final Pattern URL_PATTERN = Pattern.compile("^(([^:/?#]+)://)?([^/?#]+\\.[^/?#]+)([^?#]*)(\\?([^#]*))?(#(.*))?$");
	private static final Pattern ENUM_NAME_PATTERN = Pattern.compile("(?<=(?:\\A|_)([A-Z]))([A-Z]+)");
	public static final Simplifier TO_LOWER_CASE = s -> s.toLowerCase(Locale.ENGLISH);
	public static final Simplifier STRIP_URLS = s -> trimExtraWhitespace(URL_PATTERN.matcher(s).replaceAll(" "));
	public static final Simplifier NORMALIZE = s -> Normalizer.normalize(s, Normalizer.Form.NFD);
	private static final Set<Function<String, WordMatcher>> WORD_HANDLERS = new HashSet<>();

	static {
		WORD_HANDLERS.add(string -> new WordMatcher(URL_PATTERN, string) {
			@Override
			protected TextComponent[] handleMatch(TextComponent previousComponent) {
				if ("d.va".equalsIgnoreCase(string)) {
					return null;
				}
				// Matches, but main group is somehow empty.
				if (getMatcher().group(3) == null || getMatcher().group(3).isEmpty()) {
					return null;
				}
				String url = string;
				// Correct missing protocol
				if (getMatcher().group(1) == null ||getMatcher().group(1).isEmpty()) {
					url = "http://" + url;
				}
				TextComponent component = new TextComponent();
				component.setText('[' + getMatcher().group(3).toLowerCase(Locale.ENGLISH) + ']');
				component.setColor(Colors.WEB_LINK);
				component.setUnderlined(true);
				TextComponent[] hover = { new TextComponent(url) };
				hover[0].setColor(Colors.WEB_LINK);
				hover[0].setUnderlined(true);
				component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
				component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
				return new TextComponent[] {component};
			}
		});
	}

	private static BiMap<String, String> items;

	public static void addWordHandler(Function<String, WordMatcher> function) {
		WORD_HANDLERS.add(function);
	}

	public static TextComponent[] fromLegacyText(String message) {
		ArrayList<TextComponent> components = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		TextComponent component = new TextComponent();
		Stream<WordMatcher> wordMatcherStream = WORD_HANDLERS.stream().map(function -> function.apply(message));

		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			if (c == ChatColor.COLOR_CHAR) {
				i++;
				c = message.charAt(i);
				if (c >= 'A' && c <= 'Z') {
					c += 32;
				}
				ChatColor format = ChatColor.getByChar(c);
				if (format == null) {
					continue;
				}
				if (builder.length() > 0) {
					TextComponent old = component;
					component = new TextComponent(old);
					old.setText(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}
				switch (format) {
					case BOLD:
						component.setBold(true);
						break;
					case ITALIC:
						component.setItalic(true);
						break;
					case UNDERLINE:
						component.setUnderlined(true);
						break;
					case STRIKETHROUGH:
						component.setStrikethrough(true);
						break;
					case MAGIC:
						component.setObfuscated(true);
						break;
					case RESET:
						format = ChatColor.WHITE;
					default:
						component = new TextComponent();
						component.setColor(format);
						break;
				}
				continue;
			}
			int pos = message.indexOf(' ', i);
			if (pos == -1) {
				pos = message.length();
			}

			int start = i;
			int end = pos;
			TextComponent previousComponent = component;
			if (wordMatcherStream.anyMatch(wordMatcher -> {
				TextComponent[] special = wordMatcher.consumeSection(start, end, previousComponent);
				if (special != null) {
					components.addAll(Arrays.asList(special));
					return true;
				}
				return false;
			})) {
				if (builder.length() > 0) {
					TextComponent old = component;
					component = new TextComponent(old);
					old.setText(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}
				i = pos - 1;
				continue;
			}
			builder.append(c);
		}

		if (builder.length() > 0) {
			component.setText(builder.toString());
			components.add(component);
		}

		// The client will crash if the array is empty
		if (components.isEmpty()) {
			components.add(new TextComponent(""));
		}

		return components.toArray(new TextComponent[0]);
	}

	@NotNull
	public static TextComponent getItemComponent(ItemStack itemStack) {
		boolean named = itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName();
		TextComponent component = new TextComponent(fromLegacyText(named ? itemStack.getItemMeta().getDisplayName() : getItemName(itemStack)));
		for (int i = 0; i < component.getExtra().size(); i++) {
			BaseComponent baseExtra = component.getExtra().get(i);
			if (baseExtra.hasFormatting()) {
				break;
			}
			baseExtra.setColor(ChatColor.AQUA);
			if (named) {
				baseExtra.setItalic(true);
			}
		}
		component.setHoverEvent(getItemHover(itemStack));
		return component;
	}

	@NotNull
	public static String getItemText(@NotNull ItemStack itemStack) {
		return CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).toString();
	}

	@NotNull
	private static HoverEvent getItemHover(@NotNull ItemStack itemStack) {
		return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[] { new TextComponent(getItemText(itemStack)) });
	}

	/**
	 * Trims additional spaces, including ones surrounding chat colors.
	 *
	 * @param s the String to trim
	 *
	 * @return the trimmed String
	 */
	@NotNull
	public static String trimExtraWhitespace(@NotNull String s) {
		// Strips useless codes and any spaces between them. Reset negates all prior colors and formatting.
		s = s.replaceAll("((([" + ChatColor.COLOR_CHAR + "&][0-9a-fk-orA-FK-OR])+)\\s+?)+([" + ChatColor.COLOR_CHAR + "&][rR])", "$4");
		// Strips useless codes and any spaces between them. Colors reset prior colors and formatting.
		s = s.replaceAll("((([" + ChatColor.COLOR_CHAR + "&][0-9a-fk-orA-FK-OR])+)\\s+?)([" + ChatColor.COLOR_CHAR + "&][0-9a-fA-F])", "$4");
		// Strip all spaces between chat colors - actually strips about 1/2 per iteration
		s = s.replaceAll("\\s+(([" + ChatColor.COLOR_CHAR + "&][0-9a-fk-orA-FK-OR])+)\\s+", " $1");
		// Strip all spaces that appear to be at start
		s = s.replaceAll("(\\A|\\s+)((([" + ChatColor.COLOR_CHAR + "&][0-9a-fk-orA-FK-OR])+)?\\s+?)", " $3");
		return s.trim();
	}

	/**
	 * Checks if a String is nothing but ChatColors and whitespace.
	 *
	 * @param s the String to check
	 *
	 * @return true if the String will appear empty to the client
	 */
	public static boolean appearsEmpty(@NotNull String s) {
		return s.replaceAll("(\\s|[" + ChatColor.COLOR_CHAR + "&][0-9a-fk-rA-FK-R])", "").isEmpty();
	}

	/**
	 * Returns a more user-friendly version of standard Enum names.
	 *
	 * @param e the Enum to prettify
	 *
	 * @return the user-friendly version of the name
	 */
	@NotNull
	public static String getFriendlyName(@NotNull Enum<?> e) {
		return getFriendlyName(e.name());
	}

	/**
	 * Returns a more user-friendly version of standard Enum names.
	 *
	 * @param name the name to prettify
	 *
	 * @return the user-friendly version of the name
	 */
	@NotNull
	public static String getFriendlyName(@NotNull String name) {
		Matcher matcher = ENUM_NAME_PATTERN.matcher(name);
		StringBuilder builder = new StringBuilder();
		while (matcher.find()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(matcher.group(1)).append(matcher.group(2).toLowerCase());
		}
		return builder.toString();
	}

	@NotNull
	public static String stripEndPunctuation(@NotNull String word) {
		if (word.length() == 0) {
			return word;
		}
		char character = word.charAt(word.length() - 1);
		if (character < '0' || character > '9' && character < 'A' || character > 'Z'
				&& character != '_' && character < 'a' || character > 'z') {
			return word.substring(0, word.length() - 1);
		}
		return word;
	}

	@NotNull
	public static String stripNonAlphanumerics(@NotNull String word) {
		StringBuilder sb = new StringBuilder();
		for (char character : word.toCharArray()) {
			if (character < '0' || character > '9' && character < 'A' || character > 'Z'
					&& character != '_' && character < 'a' || character > 'z') {
				continue;
			}
			sb.append(character);
		}
		return sb.toString();
	}

	public static boolean isOnlyAscii(@NotNull String string) {
		// Also no tildes because I can
		for (char character : string.toCharArray()) {
			if (character < ' ' || character > '}') {
				return false;
			}
		}
		return true;
	}

	@NotNull
	public static String getTrace(@NotNull Throwable throwable) {
		return getTrace(throwable, 50);
	}

	@NotNull
	public static String getTrace(@NotNull Throwable throwable, int limit) {
		StringBuilder trace = new StringBuilder(throwable.toString());
		StackTraceElement[] elements = throwable.getStackTrace();
		for (int i = 0; i < elements.length && i < limit; i++) {
			trace.append("\n\tat ").append(elements[i].toString());
		}
		if (throwable.getCause() != null) {
			trace.append("\nCaused by: ").append(throwable.getCause().toString());
			for (int i = 0; i < elements.length && i < limit; i++) {
				trace.append("\n\tat ").append(elements[i].toString());
			}
		}
		return trace.toString();
	}

	@NotNull
	public static String join(@NotNull Object[] args, char separator) {
		return join(args, String.valueOf(separator));
	}

	@NotNull
	public static String join(@NotNull Object[] args, String separator) {
		return join(args, separator, 0, args.length);
	}

	@NotNull
	public static String join(@NotNull Object[] array, char separator, int startIndex, int endIndex) {
		return join(array, String.valueOf(separator), startIndex, endIndex);
	}

	@NotNull
	public static String join(@NotNull Object[] array, String separator, int startIndex, int endIndex) {
		int totalCount = endIndex - startIndex;
		if (totalCount <= 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder(totalCount * 16);

		for(int i = startIndex; i < endIndex; ++i) {
			if (i > startIndex) {
				builder.append(separator);
			}

			if (array[i] != null) {
				builder.append(array[i]);
			}
		}

		return builder.toString();
	}

	@NotNull
	private static BiMap<String, String> getItems() {
		if (items != null) {
			return items;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Easterlyn")).getResource("items.csv"))))) {
			items = HashBiMap.create();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] row = line.split(",");
				items.put(row[0], row[1]);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load items from items.csv!", e);
		}
		return items;
	}

	@NotNull
	public static String getItemName(@NotNull ItemStack item) {
		Material material = item.getType();
		String name = getItems().get(material.name());
		if (name == null) {
			// Even special-cased materials should have an entry.
			name = getFriendlyName(material);
		}
		if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION
				|| material == Material.TIPPED_ARROW) {
			if (!item.hasItemMeta()) {
				return name;
			}
			ItemMeta meta = item.getItemMeta();
			if (meta instanceof PotionMeta) {
				return getFriendlyName(material) + " of " + getPotionName((PotionMeta) meta);
			}
			return name;
		}
		return name;
	}

	@NotNull
	private static String getPotionName(@NotNull PotionMeta meta) {
		PotionData base;
		try {
			base = meta.getBasePotionData();
		} catch (IllegalArgumentException e) {
			// This can be thrown by Spigot when converting a valid potion with odd data values.
			return "Questionable Validity";
		}
		if (base.getType() != PotionType.UNCRAFTABLE) {
			StringBuilder name = new StringBuilder();
			if (base.isExtended()) {
				name.append("Extended ");
			}
			if (meta.getCustomEffects().size() > 0) {
				name.append("Custom ");
			}
			name.append(getFriendlyName(base.getType()));
			if (base.isUpgraded()) {
				name.append(" II");
			}
			return name.toString();
		}
		if (!meta.hasCustomEffects()) {
			return "No Effect";
		}
		if (meta.getCustomEffects().size() > 1) {
			return "Multiple Effects";
		}
		PotionEffect effect = meta.getCustomEffects().get(0);
		StringBuilder name = new StringBuilder(getFriendlyName(effect.getType().getName()));
		if (effect.getAmplifier() > 0) {
			// Effect power is 0-indexed
			name.append(' ').append(NumberUtil.romanFromInt(effect.getAmplifier() + 1));
		}
		return name.toString();
	}

	public static boolean isMisleadinglyNamed(@NotNull String name, @NotNull Material material) {
		String materialName = getItems().inverse().get(name);
		return materialName != null && !materialName.equals(material.name());
	}

	@Nullable
	public static Material matchMaterial(@NotNull String search) {
		String searchMaterialName = search.toUpperCase().replace(' ', '_');

		try {
			return Material.valueOf(searchMaterialName);
		} catch (IllegalArgumentException ignored) {}

		String searchFriendlyName = search.replace('_', ' ');

		// TODO ignoreCase
		String materialName = getItems().inverse().get(searchFriendlyName);
		if (materialName != null) {
			return Material.valueOf(materialName);
		}

		Material material = null;

		float matchLevel = 0F;
		search = TO_LOWER_CASE.apply(NORMALIZE.apply(searchFriendlyName));
		for (Map.Entry<String, String> entry : getItems().entrySet()) {
			float current = compare(search, TO_LOWER_CASE.apply(entry.getValue()));
			if (current > matchLevel) {
				matchLevel = current;
				material = Material.getMaterial(entry.getKey());
			}
			if (current == 1F) {
				return material;
			}
		}

		if (material == null) {
			return null;
		}

		// Allow more fuzziness for longer named items
		if (matchLevel > (.7F - (1F / material.name().length()))) {
			return material;
		}
		return null;
	}

	/**
	 * Check if a String starts with a prefix, ignoring case.
	 *
	 * @param string the String to check
	 * @param prefix the prefix of the String
	 * @return true if the String starts with the prefix
	 */
	public static boolean startsWithIgnoreCase(String string, String prefix) {
		if (prefix.length() > string.length()) {
			return false;
		}
		for (int i = 0; i < prefix.length(); ++i) {
			if (Character.toLowerCase(string.charAt(i)) != Character.toLowerCase(prefix.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Jaro-Winkler string comparison implementation.
	 *
	 * @param a the first String
	 * @param b the second String
	 * @param simplifiers Simplification functions to apply to the String before comparing
	 * @return similarity score where 0 is no match and 1 is 100% match
	 */
	public static float compare(@NotNull String a, @NotNull String b, Simplifier... simplifiers) {
		for (Function<String, String> simplifier : simplifiers) {
			a = simplifier.apply(a);
			b = simplifier.apply(b);
		}
		final float jaroScore = compareJaro(a, b);

		if (jaroScore < (float) 0.7) {
			return jaroScore;
		}

		return jaroScore + (Math.min(commonPrefix(a, b).length(), 4) * (float) 0.1 * (1.0f - jaroScore));

	}

	private static float compareJaro(@NotNull String a, @NotNull String b) {
		if (a.isEmpty() && b.isEmpty()) {
			return 1.0f;
		}

		if (a.isEmpty() || b.isEmpty()) {
			return 0.0f;
		}

		final int[] charsA = a.codePoints().toArray();
		final int[] charsB = b.codePoints().toArray();

		// Intentional integer division to round down.
		final int halfLength = Math.max(0, Math.max(charsA.length, charsB.length) / 2 - 1);

		final int[] commonA = getCommonCodePoints(charsA, charsB, halfLength);
		final int[] commonB = getCommonCodePoints(charsB, charsA, halfLength);

		// commonA and commonB will always contain the same multi-set of
		// characters. Because getCommonCharacters has been optimized, commonA
		// and commonB are -1-padded. So in this loop we count transposition
		// and use commonCharacters to determine the length of the multi-set.
		float transpositions = 0;
		int commonCharacters = 0;
		for (int length = commonA.length; commonCharacters < length
				&& commonA[commonCharacters] > -1; commonCharacters++) {
			if (commonA[commonCharacters] != commonB[commonCharacters]) {
				transpositions++;
			}
		}

		if (commonCharacters == 0) {
			return 0.0f;
		}

		float aCommonRatio = commonCharacters / (float) charsA.length;
		float bCommonRatio = commonCharacters / (float) charsB.length;
		float transpositionRatio = (commonCharacters - transpositions / 2.0f) / commonCharacters;

		return (aCommonRatio + bCommonRatio + transpositionRatio) / 3.0f;
	}

	/*
	 * Returns an array of code points from a within b. A character in b is
	 * counted as common when it is within separation distance from the position
	 * in a.
	 */
	private static int[] getCommonCodePoints(final int[] charsA, final int[] charsB, final int separation) {
		final int[] common = new int[Math.min(charsA.length, charsB.length)];
		final boolean[] matched = new boolean[charsB.length];

		// Iterate of string a and find all characters that occur in b within
		// the separation distance. Mark any matches found to avoid
		// duplicate matchings.
		int commonIndex = 0;
		for (int i = 0, length = charsA.length; i < length; i++) {
			final int character = charsA[i];
			final int index = indexOf(character, charsB, i - separation, i
					+ separation + 1, matched);
			if (index > -1) {
				common[commonIndex++] = character;
				matched[index] = true;
			}
		}

		if (commonIndex < common.length) {
			common[commonIndex] = -1;
		}

		// Both invocations will yield the same multi-set terminated by -1, so
		// they can be compared for transposition without making a copy.
		return common;
	}

	/*
	 * Search for code point in buffer starting at fromIndex to toIndex - 1.
	 *
	 * Returns -1 when not found.
	 */
	private static int indexOf(int character, int[] buffer, int fromIndex, int toIndex, boolean[] matched) {

		// compare char with range of characters to either side
		for (int j = Math.max(0, fromIndex), length = Math.min(toIndex, buffer.length); j < length; j++) {
			// check if found
			if (buffer[j] == character && !matched[j]) {
				return j;
			}
		}

		return -1;
	}

	@NotNull
	private static String commonPrefix(@NotNull CharSequence a, @NotNull CharSequence b) {
		int maxPrefixLength = Math.min(a.length(), b.length());

		int p;

		p = 0;
		while (p < maxPrefixLength && a.charAt(p) == b.charAt(p)) {
			++p;
		}

		if (validSurrogatePairAt(a, p - 1) || validSurrogatePairAt(b, p - 1)) {
			--p;
		}

		return a.subSequence(0, p).toString();
	}

	private static boolean validSurrogatePairAt(@NotNull CharSequence string, int index) {
		return index >= 0 && index <= string.length() - 2 && Character.isHighSurrogate(string.charAt(index)) && Character.isLowSurrogate(string.charAt(index + 1));
	}

	public interface Simplifier extends Function<String, String> {}

	public static abstract class WordMatcher {
		private Matcher matcher;
		public WordMatcher(Pattern pattern, String match) {
			matcher = pattern.matcher(match);
		}
		@Nullable
		public final TextComponent[] consumeSection(int start, int end, TextComponent previousComponent) {
			return matcher.region(start, end).find() ? handleMatch(previousComponent) : null;
		}
		protected final Matcher getMatcher() {
			return matcher;
		}
		protected abstract TextComponent[] handleMatch(TextComponent previousComponent);
	}

}
