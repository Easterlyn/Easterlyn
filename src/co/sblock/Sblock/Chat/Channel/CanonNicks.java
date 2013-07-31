/**
 * 
 */
package co.sblock.Sblock.Chat.Channel;

/**
 * @author Jikoo
 *
 */
public enum RPNick {
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

	DAMARA("Damara", "", "4", "trolling"),
	RUFIOH("Rufioh", "", "6", "trolling"),
	MITUNA("Mituna", "", "e", "trolling"),
	KANKRI("Kankri", "", "8", "trolling"),
	MEULIN("Meulin", "", "2", "trolling"),
	PORRIM("Porrim", "", "3", "trolling"),
	LATULA("Latula", "", "3", "trolling"),
	ARENEA("Aranea", "", "9", "trolling"),
	HORUSS("Horuss", "", "1", "trolling"),
	KURLOZ("Kurloz", "", "5", "trolling"),
	CRONUS("Cronus", "", "5", "trolling"),
	MEENAH("Meenah", "", "5", "trolling"),

	CALLIOPE("Calliope", "uranianUmbra", "7", "cheering"),
	CALIBORN("Caliborn", "undyingUmbrage", "8", "jeering"),

	CUSTOM("Report seeing this please!", "Report seeing this please!", "2", "pestering");


	private final char SECTION_SIGN = '\u00A7';
	
	private String name;
	private String chumHandle;
	private String color;
	private String pester;


	private RPNick(String name, String chumHandle, String colorCode, String pester) {
		this.name = name;
		this.chumHandle = chumHandle;
		this.color = SECTION_SIGN + colorCode;
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
		case CUSTOM:
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

	private String applyQuirk(String s, RPNick n) {
		// This is for quirks that make use of multiple existing quirks.
		// Mostly will be used for combo sprites.
		return n.applyQuirk(s);
	}

	public String getPester() {
		return this.pester;
	}

	public String getName() {
		return this.color + this.name;
	}

	/**
	 * @return
	 */
	private String getColor() {
		return this.color;
	}

	public RPNick customize(String name, String chumHandle) {
		this.name = name;
		this.chumHandle = chumHandle;
		return this;
	}
}
