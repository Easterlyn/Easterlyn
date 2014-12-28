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
		input = input.toLowerCase().replace('x', '*');
		try {
			double ans = eval.evaluate(input);
			String answer;
			// Remove trailing zeroes without precision loss
			if (ans == (long) ans) {
				answer = String.format("%d", (long) ans);
			} else {
				answer = String.format("%s", ans);
			}
			return input + " = " + answer;
		} catch (IllegalArgumentException e) {
			if (input.matches("\\A\\s*m(y|e|ah?).*((di|co)c?k|penis|(we(i|e)n|(schl|d)ong)(er)?|willy|(trouser )?snake|lizard)\\s*\\Z")) {
				return "Sorry, your equation is too tiny for me to read.";
			} else if (input.matches("\\A.*life.*universe.*everything*\\Z")) {
				return input + " = 42";
			} else {
				return "Sorry, I can't read that equation!";
			}
		}
	}
}
