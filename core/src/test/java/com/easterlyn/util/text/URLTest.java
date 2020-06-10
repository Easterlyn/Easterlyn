package com.easterlyn.util.text;

import com.easterlyn.util.StringUtil;
import java.util.regex.Matcher;
import org.junit.Test;

import static org.junit.Assert.fail;

public class URLTest {

	@Test
	public void testURLMatching() {
		checkURL("http://outdated_format.easterlyn.com/example", "outdated_format.easterlyn.com");
		checkURL("easterlyn.com", "easterlyn.com");
		checkURL("steam://idkhowtheseactuallywork.example.com", "idkhowtheseactuallywork.example.com");
		checkURL("not!aurl.com/:)", null);
		checkURL("format.'", null);
	}

	private void checkURL(String url, String expectedDomain) {
		Matcher matcher = StringUtil.URL_PATTERN.matcher(url);
		if (expectedDomain == null) {
			if (matcher.find()) {
				fail("Improperly matched URL: " + url);
			}
			return;
		} else if (!matcher.find()) {
			fail("Failed to match URL: " + url);
		}

		String domain = matcher.group(3);
		if (!expectedDomain.equals(domain)) {
			fail("Expected \"" + expectedDomain + "\" but received \"" + (domain == null ? "null" : domain) + "\"");
		}
	}

}
