/**
 * 
 */
package co.sblock.Sblock.Chat.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.ColorDef;

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
	CROCKERJANE("Jane", "gutsyGumshoe", "b", "pestering"),

	DAMARA("Damara", null, "4", "trolling"),
	RUFIOH("Rufioh", null, "6", "trolling"),
	MITUNA("Mituna", null, "e", "trolling"),
	KANKRI("Kankri", null, "4", "trolling"),
	MEULIN("Meulin", null, "2", "trolling"),
	PORRIM("Porrim", null, "2", "trolling"),
	LATULA("Latula", null, "3", "trolling"),
	ARENEA("Aranea", null, "9", "trolling"),
	HORUSS("Horuss", null, "1", "trolling"),
	KURLOZ("Kurloz", null, "5", "trolling"),
	CRONUS("Cronus", null, "5", "trolling"),
	MEENAH("Meenah", null, "5", "trolling"),

	CALLIOPE("Calliope", "uranianUmbra", "7", "cheering"),
	CALIBORN("Caliborn", "undyingUmbrage", "8", "jeering"),
	SERKITFEATURE("L" + ChatColor.MAGIC + "o" + ChatColor.GREEN + "rd English",
			"", "a", "paying attention to");


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

//	Nakodile: Red text ALL CAPS NAK NAK NAK THE GLASSES ARE TALKING
//	Salamander: Yellow text, all lower case glub glub
//	MomLalonde: Same Quirk and color as roxy
//	DadLalonde: White text, perfect grammar and syntext
//	Bro strider: same quirk and color as Dirk
//	Grandpa Harley: Same color/quirk as jake
//	ANCESTORS: same color quirks as their descendants, Cept Condy cus s)(e speak all ghetto
//	Damara: I totally can fake the japanese stuff
//	Aranea: Quirk and color as vriska
//	Betty Crocker: White text, Condy's quirk
//	LilCal: Orange, HEE HEE HEE HAA HAA HAA HOOO HOOO HOOO CAPS
//	Jack Noir: absolutely no punctuation and no niceness he ain that fuckin sweet sugar
//	Clubs Deuce:
//	Hearts Boxcars:
//	Diamonds Droog://AKA Draconian Dignitary
//	ALL OF THE FELT
//	AutoResponder: Dirks quirk, red text
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

	public String applyQuirk(String s) {
		switch (this) {
		case ARADIA:
			break; // future
		case ARENEA:
			break; // future
		case AUTORESPONDER:
			return this.color + s.replace("robot", "brobot");
			// future more bropuns
		case CALIBORN:
			break; // future
		case CALLIOPE:
			break; // future
		case CROCKERJANE:
			return ChatColor.DARK_RED + s;
		case CRONUS:
			break; // future
		case DAMARA:
			return this.color + ancestral(s);
		case DAVE:
			return this.color + mixedToLowerCase(s).replaceAll("\\.{1,2}", "")
					.replaceAll("\\.{4}", "").replaceAll("[\\W&&[^\\Q.!?\\E]]", "");
		case DIRK:
			break; // future bropuns
		case EQUIUS:
			break; // future
		case ERIDAN:
			break; // future
		case FEFERI:
			break; // future
		case GAMZEE:
			break; // future
		case HORUSS:
			break; // future
		case JADE:
			break; // future
		case JAKE:
			if (s.length() > 1) {
				s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
			} else {
				s = s.toLowerCase();
			}
			return this.color + s.toLowerCase();
		case JANE:
			break; // Done.
		case JOHN:
			break; // future
		case KANAYA:
			break; // future
		case KANKRI:
			return this.color + s.replaceAll("[oO]", "9").replaceAll("[bB]", "6");
		case KARKAT:
			return this.color + s.toUpperCase();
		case KURLOZ:
			break; // future
		case LATULA:
			break; // future
		case LILHALJUNIOR:
			String[] responses = {"Hmm.", "Yes.", "Interesting."};
			return this.color + responses[(int) (Math.random() * 3)];
		case MEENAH:
			return this.color + s.replaceAll("[;:]([dDbBpPL\\Q)(][\\E])", "38$1")
					.replaceAll("([^8])[\\W&&[^\\s]]", "$1").replaceAll("H", ")(")
					.replaceAll("E", "-E").replaceAll("\\b(.*in)g\\b", "$1");
		case MEULIN:
			break; // future
		case MITUNA:
			break; // future
		case NEPETA:
			break; // future
		case PORRIM:
			return this.color + s.replaceAll("o", "o+");
		case ROXY:
			s = mixedToLowerCase(s).replaceAll("\\b(.*in)g\\b", "$1");
		case ROSE:
			return this.color + randShuffle(s);
		case RUFIOH:
			break; // future
		case SOLLUX:
			break; // future
		case TAVROS:
			return this.color + invertCase(s).replaceAll(".", ",");
		case TEREZI:
			break; // future
		case VRISKA:
			return this.color + s.replaceAll(":*([;:])+([dDbBpPL\\Q)(][\\E])", ":::$1$2")
					.replaceAll("([\\.!?])+", "$1$1$1$1$1$1$1$1");
		case SERKITFEATURE:
			return serkitFeature(s);
		default:
			break;
		}
		return this.color + s;
	}

	@SuppressWarnings("unused")
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
				if (nick.equals(n.name)) {
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
					int next = (int) Math.random() * s1.length();
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
				for (int i = 0; i < m.group().length();) {
					for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
						if (i >= m.group().length())
							break;
						String next = m.group().substring(i, i + 1);
						if (next.equals("O")) {
							sb.append(ColorDef.RAINBOW[j]).append(ChatColor.MAGIC).append(next);
						} else {
							sb.append(ColorDef.RAINBOW[j]).append(next);
						}
						i++;
					}
				}
			} else {
				sb.append(m.group().toUpperCase());
			}
			end = m.end();
		}
		return sb.toString();
	}

	// http://unicode-table.com/en/#cjk-unified-ideographs-extension-a
	// 3440-9FFF, maximum fast calc range = 4000-8FFF (9FA0 last row without odd box chars)
	private String ancestral(String s) {
		StringBuilder sb = new StringBuilder();
		String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
		for (int i = 0; i < s.length(); i++) {
			String randChar = new StringBuilder()
					.append(hex[(int) Math.random() * 5 + 4])
					.append(hex[(int) Math.random() * hex.length])
					.append(hex[(int) Math.random() * hex.length])
					.append(hex[(int) Math.random() * hex.length]).toString();
			sb.append((char) Integer.parseInt(randChar, 16));
			if (Math.random() > 0.75) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
}
