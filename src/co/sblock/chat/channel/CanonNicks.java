package co.sblock.chat.channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

import co.sblock.chat.ColorDef;

/**
 * @author Jikoo
 *
 */
public enum CanonNicks {
	JOHN("John", "ectoBiologist", "1", "pestering"),
	ROSE("Rose", "tentacleTherapist", "d", "pestering"),
	DAVE("Dave", "turntechGodhead", "4", "pestering"),
	JADE("Jade", "gardenGnostic", "a", "pestering"),

	DAD("Dad Egbert", "pipefan413", "8", "discussing matters of great import in"),
	DADCROCKER("Dad Crocker", "pipefan413", "8", "discussing matters of great import in"),
	// This is fabricated based Dad's header: http://www.mspaintadventures.com/?s=6&p=002167

	ARADIA("Aradia", "apocalypseArisen", "4", "trolling"),
	TAVROS("Tavros", "adiosToreador", "6", "trolling"),
	SOLLUX("Sollux", "twinArmageddons", "e", "trolling"),
	KARKAT("Karkat", "carcinoGeneticist", "7", "trolling"),
	NEPETA("Nepeta", "arsenicCatnip", "2", "trolling"),
	KANAYA("Kanaya", "grimAuxiliatrix", "2", "trolling"),
	TEREZI("Terezi", "gallowsCalibrator", "3", "trolling"),
	VRISKA("Vriska", "arachnidsGrip", "9", "trolling"),
	EQUIUS("Equius", "centaursTesticle", "1", "trolling"),
	GAMZEE("Gamzee", "terminallyCapricious", "5", "trolling"),
	ERIDAN("Eridan", "caligulasAquarium", "5", "trolling"),
	FEFERI("Feferi", "cuttlefishCuller", "5", "trolling"),

	JAKE("Jake", "golgothasTerror", "2", "pestering"),
	ROXY("Roxy", "tipsyGnostalgic", "d", "pestering"),
	DIRK("Dirk", "timaeusTestified", "6", "pestering"),
	AUTORESPONDER("Auto-Responder", "timaeusTestified", "4", "pestering"),
	AUTORESPONDER2("Lil Hal", "timaeusTestified", "4", "pestering"),
	LILHALJUNIOR("Lil Hal Junior", "timaeusTestified", "6", "pestering"),
	JANE("Jane", "gutsyGumshoe", "b", "pestering"),
	CROCKERJANE(ChatColor.AQUA + "Jane", "gutsyGumshoe", "4", "pestering"),

	DAMARA("Damara", null, "4", "trolling"),
	RUFIOH("Rufioh", null, "6", "trolling"),
	MITUNA("Mituna", null, "e", "trolling"),
	KANKRI("Kankri", null, "4", "trolling"),
	MEULIN("Meulin", null, "2", "trolling"),
	PORRIM("Porrim", null, "2", "trolling"),
	LATULA("Latula", null, "3", "trolling"),
	ARANEA("Aranea", null, "9", "trolling"),
	HORUSS("Horuss", null, "1", "trolling"),
	KURLOZ("Kurloz", null, "5", "trolling"),
	CRONUS("Cronus", null, "5", "trolling"),
	MEENAH("Meenah", null, "5", "trolling"),

	CALLIOPE("Calliope", "uranianUmbra", "7", "cheering"),
	CALIBORN("Caliborn", "undyingUmbrage", "8", "jeering"),
	SERKITFEATURE(ChatColor.GREEN + "L" + ChatColor.MAGIC + "o" + ChatColor.GREEN + "rd English",
			null, "a", "paying attention to");

	private String name;
	private String chumHandle;
	private ChatColor color;
	private String pester;

	private CanonNicks(String name, String chumHandle, String colorCode, String pester) {
		this.name = name;
		this.chumHandle = chumHandle;
		this.color = ChatColor.getByChar(colorCode);
		this.pester = pester;
	}

	// Nakodile: Red text ALL CAPS NAK NAK NAK THE GLASSES ARE TALKING
	// Salamander: Yellow text, all lower case glub glub
	// MomLalonde: Same Quirk and color as roxy
	// DadLalonde: White text, perfect grammar and syntext
	// Bro strider: same quirk and color as Dirk
	// Grandpa Harley: Same color/quirk as jake
	// ANCESTORS: same color quirks as their descendants, Cept Condy cus s)(e speak all ghetto
	// Betty Crocker: White text, Condy's quirk
	// LilCal: Orange, HEE HEE HEE HAA HAA HAA HOOO HOOO HOOO CAPS
	// Jack Noir: absolutely no punctuation and no niceness he ain that fuckin sweet sugar
	// Clubs Deuce:
	// Hearts Boxcars:
	// Diamonds Droog://AKA Draconian Dignitary
	// ALL OF THE FELT
	// AutoResponder: Dirks quirk, red text
	// ErisolSprite: Eridan and Sollux's quirk, green text
	// Fefeta Sprite: Feferi/nepeta, roxy pink. (I'll do the full quirks for the sprites later)
	// ARQuiusSprite: Not sure how to handle the glasses arrow, we'll think of something
	// NannaSprite
	// TavriskaSprite
	// JasperSprite
	// JadeSprite
	// BecSprite: Again &k ALL TEXT except Woof or Bark
	// Snowman:
	// DaveSprite:
	// Trickster?

	public String applyQuirk(String s) {
		switch (this) {
		case ARADIA:
			return this.color
					+ s.toLowerCase().replace("o", "0").replace("0.0", "0_0")
							.replaceAll("[\\W&&[^\\s]]", "");
		case AUTORESPONDER:
			return this.color + s.replace("robot", "brobot");
			// bropuns
		case CALIBORN:
			return this.color + s.toUpperCase().replace("U", "u");
		case CALLIOPE:
			return this.color + s.toLowerCase().replace("u", "U");
		case CROCKERJANE:
			break;
		case CRONUS: // fix ISSUE wvITH ALLCAPS
			return this.color
					+ s.replaceAll("[vV]", "vw").replaceAll("([^vV]|\\b)([wW])", "$1wv")
							.replace("B", "8");
		case DAMARA:
			return this.color + ancestral(s);
		case DAVE:
			return this.color
					+ mixedToLowerCase(s).replaceAll("([^\\.])\\.{1,2}([^\\.])", "$1$2")
							.replaceAll("\\.+", "...").replaceAll("[\\W&&[^\\s\\.!\\?]]", "");
		case DIRK:
			break; // bropuns
		case EQUIUS:
			return this.color
					+ "D --> "
					+ s.replaceAll("[xX]", "%")
							.replaceAll("[lL](([uU][eE])|([eE][uU])|([oO]{2,}))", "100")
							.replaceAll("(([uU][eE])|([eE][uU])|([oO]{2,}))[lL]", "001")
							.replaceAll("[sS]+[tT]+[rR]+[oO]+[nN]+[gG]+", "STRONG")
							.replace("nay", "neigh").replaceAll("[nN][aA][yY]", "NEIGH");
		case ERIDAN:
			return this.color
					+ s.replaceAll("([vwVW])", "$1$1").replaceAll("\\ban\\b", "a")
							.replaceAll("\\band\\b", "an").replaceAll("\\b(.*in)g\\b", "$1");
		case FEFERI:
			return this.color
					+ s.replaceAll("[;:]([dDbBpPL\\Q)(][\\E])", "38$1")
							.replaceAll("([^8])[\\W&&[^\\s]]", "$1").replaceAll("[hH]", ")(")
							.replace("E", "-E");
		case GAMZEE:
			return this.color + alternateCase(s);
		case HORUSS:
			return applyQuirk(s, EQUIUS).replaceAll("[iI]", "\\*").replaceFirst("D -->", "8=D <");
		case JADE:
			return this.color + s.toLowerCase().replace("'", "");
		case JAKE:
			if (s.length() > 1) {
				s = Character.toUpperCase(s.charAt(0)) + mixedToLowerCase(s.substring(1));
			}
			return this.color + s.replace("'", "");
		case JANE:
			break; // Done.
		case JOHN:
			if (s.length() > 1) {
				s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
			} else {
				s = s.toLowerCase();
			}
			return this.color + s.toLowerCase();
		case KANAYA:
			return this.color + hellaAnnoying(s.replaceAll("[\\W&&[^\\s]]", ""));
		case KANKRI:
			return this.color + s.replaceAll("[oO]", "9").replaceAll("[bB]", "6");
		case KARKAT:
			return this.color + s.toUpperCase();
		case KURLOZ:
			if (s.length() == 0 || s.charAt(0) != '#') {
				s = "";
			}
			return this.color + s;
		case LATULA:
			return this.color + s.replace("A", "4").replace("E", "3").replace("I", "1");
		case LILHALJUNIOR:
			String[] responses = { "Hmm.", "Yes.", "Interesting." };
			return this.color + responses[(int) (Math.random() * 3)];
		case MEENAH:
			return this.color
					+ s.replaceAll("[;:]([dDbBpPL\\Q)(][\\E])", "38$1")
							.replaceAll("([^8])[\\W&&[^\\s]]", "$1").replaceAll("[hH]", ")(")
							.replace("E", "-E").replaceAll("\\b(.*in)g\\b", "$1");
		case MEULIN: // emoticons
			return this.color + s.toUpperCase().replace("EE", "33");
		case MITUNA:
			return this.color
					+ s.toUpperCase().replace("A", "4").replace("B", "8").replace("E", "3")
							.replace("I", "1").replace("O", "0").replace("S", "5")
							.replace("T", "7");
		case NEPETA:
			return this.color + ":33 < "
					+ s.replaceAll("[eE]{2},", "33").replace("ver", "fur").replace("pos", "paws");
		case PORRIM:
			return this.color + s.replaceAll("o", "o+");
		case ROXY:
			s = mixedToLowerCase(s).replaceAll("\\b(.*in)g\\b", "$1");
		case ROSE:
			return this.color + randShuffle(s);
		case RUFIOH:
			return this.color
					+ mixedToLowerCase(s).replaceAll("[iI]", "1").replaceAll(
							"([;:])([dDbBpPL\\Q)(][\\E])", "}$1$2");
		case SOLLUX:
			return this.color
					+ s.toLowerCase().replace("s", "2").replaceAll("i+", "ii")
							.replaceAll("to+", "two");
		case TAVROS:
			return this.color
					+ invertCase(s).replace(".", ",").replaceAll("([;:])([dDbBpPL\\Q)(][\\E])",
							"}$1$2");
		case TEREZI:
			return this.color
					+ s.toUpperCase().replace("A", "4").replace("E", "3").replace("I", "1")
							.replace("'", "").replaceAll("([;:])([dDbBpPL\\Q)(][\\E])", ">$1$2")
							.replaceAll(">([:;])\\(", ">$1[").replaceAll(">([:;])\\)", ">$1]")
							.replaceAll("\\.{1,2}", "").replaceAll("\\.{4}", "...");
		case ARANEA:
		case VRISKA:
			return this.color
					+ s.replaceAll(":*([;:])+([dDbBpPL\\Q)(][\\E])", ":::$1$2").replaceAll(
							"([\\.!?])+", "$1$1$1$1$1$1$1$1");
		case SERKITFEATURE:
			return serkitFeature(s);
		default:
			break;
		}
		return this.color + s;
	}

	private String applyQuirk(String s, CanonNicks n) {
		// This is for quirks that make use of multiple existing quirks.
		// Mostly will be used for combo sprites.
		return n.applyQuirk(s);
	}

	public String getPester() {
		return this.pester;
	}

	public String getName() {
		return this.name;
	}

	public ChatColor getColor() {
		return this.color;
	}

	public String getHandle() {
		return this.color + this.chumHandle;
	}

	/**
	 * @param string
	 * @return
	 */
	public static CanonNicks getNick(String nick) {
		try {
			return CanonNicks.valueOf(nick.toUpperCase());
		} catch (IllegalArgumentException | IllegalStateException e) {
			for (CanonNicks n : CanonNicks.values()) {
				if (nick.equals(n.color + n.name) || nick.equals(n.name)) {
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
					sb.append(ColorDef.RAINBOW[(int) (Math.random() * ColorDef.RAINBOW.length)]);
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
	// translates to 16384-40959, 40783 last useful
	private String ancestral(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			sb.append((char) (int) (Math.random() * 24399 + 16384));
			if (Math.random() > 0.8) {
				sb.append('\u0020');
			}
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
