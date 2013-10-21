/**
 * 
 */
package co.sblock.Sblock.Chat2.Channel;

import org.bukkit.ChatColor;

/**
 * @author Jikoo
 *
 */
public enum CanonNicks {
	JOHN("John", "ectoBiologist", "1", "pestering"),
	ROSE("Rose", "tentacleTherapist", "d", "pestering"),
	DAVE("Dave", "turntechGodhead", "4", "pestering"),
	JADE("Jade", "gardenGnostic", "a", "pestering"),

	DAD("Dad Egbert", "pipefan413", "8", "discussing matters of great import"),
	DADCROCKER("Dad Crocker", "pipefan413", "8", "discussing matters of great import"),

	ARADIA("Aradia", "apocalypseArisen", "4", "trolling"),
	TAVROS("Tavros", "adiosToreador", "6", "trolling"),
	SOLLUX("Sollux", "twinArmageddons", "e", "trolling"),
	KARKAT("Karkat", "carcinoGeneticist", "8", "trolling"),
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
	KANKRI("Kankri", null, "8", "trolling"),
	MEULIN("Meulin", null, "2", "trolling"),
	PORRIM("Porrim", null, "2", "trolling"),
	LATULA("Latula", null, "3", "trolling"),
	ARENEA("Aranea", null, "9", "trolling"),
	HORUSS("Horuss", null, "1", "trolling"),
	KURLOZ("Kurloz", null, "5", "trolling"),
	CRONUS("Cronus", null, "5", "trolling"),
	MEENAH("Meenah", null, "5", "trolling"),

	CALLIOPE("Calliope", "uranianUmbra", "7", "cheering"),
	CALIBORN("Caliborn", "undyingUmbrage", "8", "jeering");


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
	// This is fabricated based Dad's header: http://www.mspaintadventures.com/?s=6&p=002167


//	Crocker!Jane: Red text, regular jane quirk
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
//	LORD ENGLISH: ALL CAPS BECAUSE HES ALREADY HERE
//	Doc Scratch: White Text, perfect grammar and syntext
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
			break;
		case ARENEA:
			break;
		case AUTORESPONDER:
			break;
		case CALIBORN:
			break;
		case CALLIOPE:
			break;
		case CRONUS:
			break;
		case DAMARA:
			break;
		case DAVE:
			break;
		case DIRK:
			break;
		case EQUIUS:
			break;
		case ERIDAN:
			break;
		case FEFERI:
			break;
		case GAMZEE:
			break;
		case HORUSS:
			break;
		case JADE:
			break;
		case JAKE:
			break;
		case JANE:
			break;
		case JOHN:
			break;
		case KANAYA:
			break;
		case KANKRI:
			break;
		case KARKAT:
			break;
		case KURLOZ:
			break;
		case LATULA:
			break;
		case LILHALJUNIOR:
			String[] responses = {"Hmm.", "Yes.", "Interesting."};
			return responses[(int) (Math.random() * 3)];
		case MEENAH:
			break;
		case MEULIN:
			break;
		case MITUNA:
			break;
		case NEPETA:
			break;
		case PORRIM:
			break;
		case ROSE:
			break;
		case ROXY:
			break;
		case RUFIOH:
			break;
		case SOLLUX:
			break;
		case TAVROS:
			break;
		case TEREZI:
			break;
		case VRISKA:
			break;
		default:
			break;
		}
		return s;
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
		if(CanonNicks.valueOf(nick) != null)	{
			return CanonNicks.valueOf(nick);
		}
		return null;
	}
}
