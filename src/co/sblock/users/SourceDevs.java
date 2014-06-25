package co.sblock.users;

/**
 * @author tmathmeyer
 *
 * Just a fun little thing to keep track of our usernames / UUID's
 * IT'S NOT USED ANYWHERE DONT WORRY
 */
public enum SourceDevs {
	TMATHMEYER("ce35dadbebcf47d3bb1e14a950013bbe", "Ted"),
	JIKOO("40028b1ab4d74feb8f663b82511ecdd6", "Adam"),
	DUBLEK("95d6edf8487949fbb0088be4f570ec52", "Keiko");

	@SuppressWarnings("unused")
	private final String UUID, name;

	private SourceDevs(String id, String irl) {
		UUID = id;
		name = irl;
	}
}
