package co.sblock.chat.channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.sblock.chat.Color;

import net.md_5.bungee.api.ChatColor;

/**
 * An enum representing all supported nicknames in RPChannels.
 * 
 * @author Jikoo
 */
public enum CanonNick {
	JOHN("John", "John", '1'),
	DRUNKROSE("drunk!Rose", "Rose", 'd'),
	SOBERROSE("sober!Rose", "Rose", 'd'),
	DAVE("Dave", "Dave", '4'),
	JADE("Jade", "Jade", 'a'),

	DAD("Dad Egbert", "Dad Egbert", '8'),
	DADCROCKER("Dad Crocker", "Dad Crocker", '8'),

	ARADIA("Aradia", "Aradia", '4'),
	TAVROS("Tavros", "Tavros", '6'),
	SOLLUX("Sollux", "Sollux", 'e'),
	KARKAT("Karkat", "Karkat", '7'),
	NEPETA("Nepeta", "Nepeta", '2'),
	KANAYA("Kanaya", "Kanaya", '2'),
	TEREZI("Terezi", "Terezi", '3'),
	VRISKA("Vriska", "Vriska", '9'),
	EQUIUS("Equius", "Equius", '1'),
	PRESGRUBGAMZEE("pre-sgrub!Gamzee", "Gamzee", '5'),
	SGRUBGAMZEE("sgrub!Gamzee", "Gamzee", '5'),
	ERIDAN("Eridan", "Eridan", '5'),
	FEFERI("Feferi", "Feferi", '5'),

	JAKE("Jake", "Jake", '2'),
	ROXY("Roxy", "Roxy", 'd'),
	DIRK("Dirk", "Dirk", '6'),
	AUTORESPONDER("Auto-Responder", "Auto-Responder", '4'),
	LILHAL("Lil Hal", "Lil Hal", '4'),
	LILHALJUNIOR("Lil Hal Junior", "Lil Hal Junior", '6'),
	JANE("Jane", "Jane", 'b'),
	CROCKERJANE("crocker!Jane", "Jane", '4'),

	DAMARA("Damara", "Damara", '4'),
	RUFIOH("Rufioh", "Rufioh", '6'),
	MITUNA("Mituna", "Mituna", 'e'),
	KANKRI("Kankri", "Kankri", '4'),
	MEULIN("Meulin", "Meulin", '2'),
	PORRIM("Porrim", "Porrim", '2'),
	LATULA("Latula", "Latula", '3'),
	ARANEA("Aranea", "Aranea", '9'),
	HORUSS("Horuss", "Horuss", '1'),
	KURLOZ("Kurloz", "Kurloz", '5'),
	CRONUS("Cronus", "Cronus", '5'),
	MEENAH("Meenah", "Meenah", '5'),

	CALLIOPE("Calliope", "Calliope", '7'),
	TAKEOVERCALLIOPE("takeover!Calliope", "Calliope", '7'),
	CALIBORN("Caliborn", "Caliborn", '8'),
	TAKEOVERCALIBORN("takeover!Caliborn", "Caliborn", '8'),
	BLOODYTAKEOVERCALIBORN("bloodytakeover!Caliborn", "Caliborn", 'a'),
	SERKITFEATURE(ChatColor.DARK_RED + ":3 :3 :3", ChatColor.GREEN + "L" + ChatColor.MAGIC + "o" + ChatColor.GREEN + "rd English", 'a');

	private String id;
	private String name;
	private ChatColor color;
	private CanonNick(String id, String name, char colorCode) {
		this.id = id;
		this.name = name;
		this.color = ChatColor.getByChar(colorCode);
	}

//	Nakodile: Red text ALL CAPS NAK NAK NAK THE GLASSES ARE TALKING
//	Salamander: Yellow text, all lower case glub glub
//	MomLalonde: Same Quirk and color as roxy
//	DadLalonde: White text, perfect grammar and syntext
//	Bro strider: same quirk and color as Dirk
//	Grandpa Harley: Same color/quirk as jake
//	Betty Crocker: White text, Condy's quirk
//	LilCal: Orange, HEE HEE HEE HAA HAA HAA HOOO HOOO HOOO CAPS
//	Jack Noir: absolutely no punctuation and no niceness he ain that fuckin sweet sugar
//	Clubs Deuce:
//	Hearts Boxcars:
//	Diamonds Droog://AKA Draconian Dignitary
//	ALL OF THE FELT
//	ErisolSprite: Eridan and Sollux's quirk, green text
//	Fefeta Sprite: Feferi/nepeta, roxy pink. (I'll do the full quirks for the sprites later)
//	ARQuiusSprite: Not sure how to handle the glasses arrow, we'll think of something
//	NannaSprite
//	TavriskaSprite
//	JasperSprite
//	JadeSprite
//	BecSprite: Again &k ALL TEXT except Woof or Bark
//	Snowman:
//	DaveSprite:
//	Trickster?

	public String getPrefix() {
		switch (this) {
		case EQUIUS:
			return "D --> ";
		case HORUSS:
			return "8=D < ";
		case NEPETA:
			return ":33 < ";
		default:
			return null;
		}
	}

	public String applyQuirk(String s) {
		switch (this) {
		case ARADIA:
			return s.toLowerCase().replace("o", "0").replace("0.0", "0_0")
					.replaceAll("[\\W&&[^\\s]]", "");
		case AUTORESPONDER:
		case DIRK:
			return s.replaceAll("([^bB]|\\b)ro", "$1bro").replaceAll("([^bB]|\\b)Ro", "$1Bro")
					.replaceAll("([^bB]|\\b)[rR][oO]", "$1BRO");
		case BLOODYTAKEOVERCALIBORN:
		case TAKEOVERCALIBORN:
		case KARKAT:
			return s.toUpperCase();
		case CALIBORN:
			return s.toUpperCase().replace("U", "u");
		case TAKEOVERCALLIOPE:
			return s.toLowerCase();
		case CALLIOPE:
			return s.toLowerCase().replace("u", "U");
		case CRONUS:
			return s.replace("v", "vw").replace("V", "VW").replaceAll("([^vV]|\\b)w", "$1wv")
					.replaceAll("([^vV]|\\b)W", "$1WV").replace("B", "8");
		case DAMARA:
			return ancestral(s);
		case DAVE:
			return mixedToLowerCase(s).replaceAll("([^\\.])\\.{1,2}([^\\.])", "$1$2")
					.replaceAll("\\.+", "...").replaceAll("[\\W&&[^\\s\\.!\\?]]", "");
		case EQUIUS:
			return s.replaceAll("[xX]", "%")
					.replaceAll("[lL](([uU][eE])|([eE][uU]|[wW])|([oO]{2,}))", "100")
					.replaceAll("(([uU][eE])|([eE][uU]|[wW])|([oO]{2,}))[lL]", "001")
					.replaceAll("[sS]+[tT]+[rR]+[oO]+[nN]+[gG]+", "STRONG")
					.replace("nay", "neigh").replaceAll("[nN][aA][yY]", "NEIGH");
		case ERIDAN:
			return s.replaceAll("([vwVW])", "$1$1").replaceAll("\\ban\\b", "a")
					.replaceAll("\\band\\b", "an").replaceAll("\\b(.*in)g\\b", "$1");
		case FEFERI:
			return s.replaceAll("[;:]([dDbBpPL\\Q)(][\\E])", "38$1")
					.replaceAll("([^8])[\\W&&[^\\s]]", "$1").replaceAll("[hH]", ")(")
					.replace("E", "-E");
		case PRESGRUBGAMZEE:
			return alternateCaseAlphabetical(s);
		case SGRUBGAMZEE:
			return alternateCase(s);
		case HORUSS:
			return EQUIUS.applyQuirk(s).replaceAll("[iI]", "\\*");
		case JADE:
			return s.toLowerCase().replace("'", "");
		case JAKE:
			if (s.length() > 1) {
				s = Character.toUpperCase(s.charAt(0)) + mixedToLowerCase(s.substring(1));
			}
			return s.replace("'", "");
		case JOHN:
			if (s.length() > 1) {
				s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
			} else {
				s = s.toLowerCase();
			}
			return s.toLowerCase();
		case KANAYA:
			return hellaAnnoying(s.replaceAll("[\\W&&[^\\s]]", ""));
		case KANKRI:
			return s.replaceAll("[oO]", "9").replaceAll("[bB]", "6");
		case KURLOZ:
			return "...";
		case LATULA:
			return s.replace("A", "4").replace("E", "3").replace("I", "1");
		case LILHALJUNIOR:
			String[] responses = {"Hmm.", "Yes.", "Interesting."};
			return responses[(int) (Math.random() * 3)];
		case MEENAH:
			return s.replaceAll("[;:]([dDbBpPL\\Q)(][\\E])", "38$1")
					.replaceAll("([^8])[\\W&&[^\\s]]", "$1").replaceAll("[hH]", ")(")
					.replace("E", "-E").replaceAll("\\b(.*in)g\\b", "$1");
		case MEULIN: // emoticons
			return s.toUpperCase().replace("EE", "33");
		case MITUNA:
			return s.toUpperCase().replace("A", "4").replace("B", "8")
					.replace("E", "3").replace("I", "1").replace("O", "0")
					.replace("S", "5").replace("T", "7");
		case NEPETA:
			return s.toLowerCase().replaceAll("[e]{2},", "33")
					.replace("ver", "fur").replace("pos", "paws");
		case PORRIM:
			return s.replaceAll("([oO0])", "$1+").replaceAll("[pP][lL][uU][sS]", "+");
		case ROXY:
			s = mixedToLowerCase(s).replaceAll("\\b(.*in)g\\b", "$1");
		case DRUNKROSE:
			return randShuffle(s);
		case RUFIOH:
			return mixedToLowerCase(s).replaceAll("[iI]", "1")
					.replaceAll("([;:])([dDbBpPL\\Q)(][\\E])", "}$1$2");
		case SOLLUX:
			return s.toLowerCase().replace("s", "2")
					.replaceAll("i+", "ii").replaceAll("to+", "two");
		case TAVROS:
			return invertCase(s).replace(".", ",")
					.replaceAll("([;:])([dDbBpPL\\Q)(][\\E])", "}$1$2");
		case TEREZI:
			return s.toUpperCase().replace("A", "4")
					.replace("E", "3").replace("I", "1").replace("'", "")
					.replaceAll("([;:])([dDbBpPL\\Q)(][\\E])", ">$1$2")
					.replaceAll(">([:;])\\(", ">$1[").replaceAll(">([:;])\\)", ">$1]")
					.replaceAll("\\.{1,2}", "").replaceAll("\\.{4}", "...");
		case ARANEA:
		case VRISKA:
			return s.replaceAll(":*([;:])+([dDbBpPL\\Q)(][\\E])", ":::$1$2")
					.replaceAll("([\\.!?])+", "$1$1$1$1$1$1$1$1").replaceAll("[bB]", "8")
					.replaceAll("[aA]([iI][tT]|[tT][eE])", "8")
					.replaceAll("[aA][tT]([iI][nN][gG])\\b", "8$1");
		case SERKITFEATURE:
			return serkitFeature(s);
		default:
			return s;
		}
	}

	public String getId() {
		return this.id;
	}

	public String getDisplayName() {
		return this.name;
	}

	public ChatColor getNameColor() {
		switch (this) {
		case CROCKERJANE:
			return ChatColor.AQUA;
		default:
			return this.getColor();
		}
	}

	public ChatColor getColor() {
		return this.color;
	}

	public static CanonNick getNick(String nick) {
		try {
			return CanonNick.valueOf(nick.toUpperCase());
		} catch (IllegalArgumentException | IllegalStateException e) {
			for (CanonNick n : CanonNick.values()) {
				if (nick.equals(n.id) || nick.equals(n.name)) {
					return n;
				}
			}
		}
		return null;
	}

	private String randShuffle(String s) {
		Matcher m = Pattern.compile("\\b\\w+\\b").matcher(s);
		StringBuilder sb = new StringBuilder();
		int end = 0;
		while (m.find()) {
			sb.append(s.substring(end, m.start()));
			if (Math.random() > 0.75) {
				StringBuilder shuffle = new StringBuilder();
				String s1 = m.group();
				while (s1.length() != 0) {
					int next = (int) (Math.random() * s1.length());
					shuffle.append(s1.charAt(next));
					s1 = s1.substring(0, next) + s1.substring(next + 1);
				}
				sb.append(shuffle.toString());
			} else {
				sb.append(m.group());
			}
			end = m.end();
		}
		return sb.toString();
	}

	private String invertCase(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (Character.isLowerCase(s.charAt(i))) {
				sb.append(Character.toUpperCase(s.charAt(i)));
			} else if (Character.isUpperCase(s.charAt(i))) {
				sb.append(Character.toLowerCase(s.charAt(i)));
			} else {
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}

	private String mixedToLowerCase(String s) {
		Matcher m = Pattern.compile("\\w+").matcher(s);
		StringBuilder sb = new StringBuilder();
		int end = 0;
		while (m.find()) {
			sb.append(s.substring(end, m.start()));
			if (m.group().equals(m.group().toUpperCase())) {
				sb.append(m.group());
			} else {
				sb.append(m.group().toLowerCase());
			}
			end = m.end();
		}
		if (end < s.length()) {
			sb.append(s.substring(end));
		}
		return sb.toString();
	}

	private String serkitFeature(String s) {
		Matcher m = Pattern.compile("\\w*").matcher(s);
		StringBuilder sb = new StringBuilder();
		int end = 0;
		while (m.find()) {
			sb.append(s.substring(end, m.start()));
			if (m.group().equals(m.group().toUpperCase())) {
				for (int i = 0; i < m.group().length(); i++) {
					String next = m.group().substring(i, i + 1);
					sb.append(Color.RAINBOW[(int) (Math.random() * Color.RAINBOW.length)]);
					if (next.equals("O")) {
						sb.append(ChatColor.MAGIC);
					}
					sb.append(next);
				}
			} else {
				sb.append(ChatColor.GREEN).append(m.group().toUpperCase());
			}
			end = m.end();
		}
		return sb.toString();
	}

	// http://unicode-table.com/en/#cjk-unified-ideographs-extension-a
	// 3440-9FFF, maximum fast calc range = 4000-8FFF (9FA0 last row without odd box chars)
	// translates to 16384-40959,  40783 last useful
	private String ancestral(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length() && i < 20; i++) {
			sb.append((char) (int) (Math.random() * 24399 + 16384));
			if (Math.random() > 0.7) {
				sb.append('\u0020');
			}
		}
		return sb.toString();
	}

	private String alternateCaseAlphabetical(String s) {
		boolean upper = true;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char charAt = s.charAt(i);
			if (!Character.isAlphabetic(charAt)) {
				sb.append(charAt);
				continue;
			}
			if (upper) {
				sb.append(Character.toUpperCase(charAt));
				upper = false;
				continue;
			}
			sb.append(Character.toLowerCase(charAt));
		}
		return sb.toString();
	}

	private String alternateCase(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (i % 2 == 0) {
				sb.append(Character.toUpperCase(s.charAt(i)));
			} else {
				sb.append(Character.toLowerCase(s.charAt(i)));
			}
		}
		return sb.toString();
	}

	private String hellaAnnoying(String s) {
		StringBuilder sb = new StringBuilder();
		for (String s1 : s.split(" ")) {
			if (s1.length() > 1) {
				sb.append(Character.toUpperCase(s1.charAt(0))).append(s1.substring(1));
			} else {
				sb.append(s1.toUpperCase());
			}
			sb.append('\u0020');
		}
		return sb.substring(0, sb.length() - 1).toString();
	}
}
