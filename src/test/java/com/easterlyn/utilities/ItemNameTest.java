package com.easterlyn.utilities;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Material;

import org.junit.Test;

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
				if (row.length != 5) {
					fail("Row does not match format: " + line);
				}
				String id = row[1] + ":" + row[2];
				items.put(id, row[3]);
			}
		} catch (IOException e) {
			fail("Could not load items from items.csv!");
		}
		for (Material material : Material.values()) {
			String name = items.get(material.name() + ":0");
			if (name == null) {
				fail("Missing material data for " + material.name() + " in items.csv");
			}
		}
	}

}
