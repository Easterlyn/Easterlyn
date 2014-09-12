package co.sblock.data.redis;

import com.tmathmeyer.jadis.async.CallBackLogger;
import co.sblock.utilities.Log;

/**
 * 
 * @author ted
 *
 */
public class ExceptionLogger implements CallBackLogger<Throwable> {

	static final ExceptionLogger log = new ExceptionLogger();
	
	/**
	 * @return the logger instance
	 */
	public static ExceptionLogger getEL() {
		return log;
	}
	
	@Override
	public void Log(Throwable e, Class<?> clazz, String message) {
		Log.getLog("ExceptionLogger").criticalErr(e);
		Log.getLog("ExceptionLogger").severe(message);
		Log.getLog("ExceptionLogger").severe(clazz.getSimpleName());
	}

}
