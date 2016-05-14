package daaa.qdscrapper;

/**
 * All-purposes utils that had no place anywhere else
 * 
 * @author kerndav
 *
 */
public class QDUtils
{
	private QDUtils() {} //do not instanciate
	
	/**
	 * add tabulations before each lines of the input string
	 * @param in the input string to tabulate
	 * @param nb the number of tabulations to add to each line
	 * @return the tabulated string
	 */
	public static String tabulate(String in, int nb) 
	{
		StringBuilder sb = new StringBuilder(in.length());
		String[] lines = in.split("\n");
		
		for(int l=0; l<lines.length; l++)
		{
			String line = lines[l];
			for(int i=0; i<nb; i++) 
			{
				sb.append("\t");
			}
			sb.append(line);
			if(l != lines.length-1) // not the last line
			{
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	
}
