package daaa.qdscraper.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypt and decrypt using a key stored in a file.
 * Code modified from http://www.rgagnon.com/javadetails/java-0400.html
 * @author daaa
 */
public class CryptoUtils {

	public static final String AES = "AES";

	/**
	 * encrypt a value
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static String encrypt(String value) throws GeneralSecurityException,
			IOException {
		SecretKeySpec sks = getSecretKeySpec();
		Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
		cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
		byte[] encrypted = cipher.doFinal(value.getBytes());
		return byteArrayToHexString(encrypted);
	}

	/**
	 * decrypt a value
	 * 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static String decrypt(String message)
			throws GeneralSecurityException, IOException {
		SecretKeySpec sks = getSecretKeySpec();
		Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
		cipher.init(Cipher.DECRYPT_MODE, sks);
		byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
		return new String(decrypted);
	}

	private static SecretKeySpec getSecretKeySpec()
			throws NoSuchAlgorithmException, IOException {
		byte[] key = readKeyFile();
		SecretKeySpec sks = new SecretKeySpec(key, CryptoUtils.AES);
		return sks;
	}

	private static byte[] readKeyFile() throws IOException {
		String key = QDUtils.loadClasspathFile("key");
		Scanner scanner = new Scanner(key);
		scanner.useDelimiter("\\Z");
		String keyValue = scanner.next();
		scanner.close();
		return hexStringToByteArray(keyValue);
	}

	private static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	private static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

}