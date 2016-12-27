package daaa.qdscraper.services;

/**
 * Manager for syserr and sysout able to display temporary progress
 * @author daaa
 */
public class Console {

	private Console(){} // do not instanciate
	
	private static boolean isProgressing = false;
	private static int nbDots = 0;
	
	private static void stopProgress() {
		if(isProgressing) {
			isProgressing = false;
			// doesn't work in Eclipse Juno, but ok in a Mac Terminal at least
			for(int i=0; i<nbDots; i++)
			{
				System.out.print("\b");
			}
			nbDots = 0;
			System.out.print("\r");
		}
	}
	
	/**
	 * @see System.out.println
	 * @param message
	 */
	public static void println(String message) {
		stopProgress();
		System.out.println(message);
	}
	
	/**
	 * @see System.out.println
	 * @param message
	 */
	public static void println() {
		stopProgress();
		System.out.println();
	}
	
	/**
	 * System.err.println
	 * @param message
	 */
	public static void printErr(String message) {
		stopProgress();
		System.err.println(message);
	}
	
	/**
	 * @see Exception.printStackTrace
	 * @param e
	 */
	public static void printErr(Exception e) {
		stopProgress();
		//System.err.println(e.getClass().getSimpleName());
		e.printStackTrace(System.err);
	}
	
	/**
	 * Adds one tick of progress
	 */
	public static void doProgress() {
//		if(!isProgressing) {
//			System.out.println();
//		}
		System.out.print(".");
		nbDots ++;
		isProgressing = true;
	}
}
