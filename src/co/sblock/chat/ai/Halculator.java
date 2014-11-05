package co.sblock.chat.ai;

/**
 * 
 * 
 * @author Jikoo
 */
public class Halculator {

	private com.fathzer.soft.javaluator.DoubleEvaluator eval;

	public Halculator() {
		eval = new com.fathzer.soft.javaluator.DoubleEvaluator();
	}

	public String evhaluate(String input) {
		input = input.toLowerCase();
		try {
			 return input + " = " + eval.evaluate(input);
		} catch (IllegalArgumentException e) {
			if (input.matches("\\A\\s*m(y|e|ah?).*((di|co)c?k|penis|(we(i|e)n|(schl|d)ong)(er)?|willy|(trouser )?snake|lizard)\\s*\\Z")) {
				return "Sorry, your equation is too tiny for me to read.";
			} else {
				return "Sorry, I can't read that equation!";
			}
		}
	}
}
