package com.easterlyn.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Material;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;

import static org.junit.Assert.fail;

public class ItemNameTest {

	@Test
	public void testMaterialsPresent() {
		HashMap<String, String> items = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/items.csv"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] row = line.split(",");
				if (row.length < 2) {
					fail("Row does not match format: " + line);
				}
				items.put(row[0], row[1]);
			}
		} catch (IOException e) {
			fail("Could not load items from items.csv!");
		}
		EnumSet<Material> missing = EnumSet.noneOf(Material.class);

		JsonObject element;
		try {
			element = new JsonParser().parse(new BufferedReader(new FileReader(new File("src/test/resources/en_us.json")))).getAsJsonObject();
		} catch (FileNotFoundException e) {
			element = null;
			e.printStackTrace();
		}

		for (Material material : Material.values()) {
			if (material.name().startsWith("LEGACY_")) {
				continue;
			}
			String name = items.get(material.name());
			if (name == null) {
				missing.add(material);
				System.out.println(material.name() + ',' + getFriendlyName(material, element));
			}
		}
		if (!missing.isEmpty()) {
			fail("Missing material data for " + missing.toString() + " in items.csv");
		}
	}

	private static String getFriendlyName(Material material, JsonObject lang) {
		if (lang == null) {
			return TextUtils.getFriendlyName(material) + ",AUTO-GENERATED";
		}

		JsonElement element = lang.get("item.minecraft." + material.name().toLowerCase());

		if (element == null) {
			element = lang.get("block.minecraft." + material.name().toLowerCase());
		}

		return element != null ? element.getAsString() : TextUtils.getFriendlyName(material) + ",AUTO-GENERATED";
	}

}
