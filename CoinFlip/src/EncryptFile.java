
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class EncryptFile {

	private KeyPair keyPair;
	
	private KeyPair createKeyPair() throws Exception {
		System.out.println("Create KeyPair ...");
		

		Security.addProvider(new BouncyCastleProvider());
		for( @SuppressWarnings("unused") Provider k: Security.getProviders() )
		{
			//System.out.println(k);
		}

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
		keyPairGenerator.initialize(4096, new SecureRandom());
		
		return keyPairGenerator.generateKeyPair();
	}
	
	private static OutputStream encryptOutputStream(OutputStream os, PublicKey publicKey) throws Exception {
		System.out.println("Create Session Key ...");
		
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "BC");
		keyGenerator.init(new SecureRandom());
		Key key = keyGenerator.generateKey();
		
		System.out.println("Crypt session key ...");
		Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
		cipher.init(Cipher.WRAP_MODE, publicKey);
		
		byte [] wrapedKey = cipher.wrap(key);
		
		System.out.println(wrapedKey.length);
		
		os.write(wrapedKey);
		
		cipher = Cipher.getInstance("AES", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		
		return new CipherOutputStream(os, cipher);
	}
	
	private void encryptFile(String sourceFileName, String destinationFileName, PublicKey publicKey) throws Exception {
		File sourceFile = new File(sourceFileName);
		File destFile = new File(destinationFileName);
		
		InputStream in = new FileInputStream(sourceFile);
		OutputStream out = encryptOutputStream(new FileOutputStream(destFile), publicKey);
		
		System.out.println("Encrypt " + sourceFileName + " ...");
		
		byte[] buffer = new byte[128];
		int length;
		
		while ((length = in.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		out.flush();
		out.close();
		in.close();
	}
	
	private static InputStream decryptInputStream(InputStream is, PrivateKey privateKey) throws Exception {
		System.out.println("Read crypted session key ...");
		
		byte[] wrappedKey= new byte[512];
		Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
		is.read(wrappedKey, 0, 512);
		
		System.out.println("Decrypt session key ...");
		cipher.init(Cipher.UNWRAP_MODE, privateKey);
		Key key = cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);		
		
		cipher = Cipher.getInstance("AES", "BC");
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		return new CipherInputStream(is, cipher);
	}
	
	private void decryptFile(String sourceFileName, String destinationFileName, PrivateKey privateKey) throws Exception {
		File sourceFile = new File(sourceFileName);
		File destFile = new File(destinationFileName);
		
		InputStream in = decryptInputStream(new FileInputStream(sourceFile), privateKey);
		OutputStream out = new FileOutputStream (destFile);
		
		System.out.println("Decrpyt " + sourceFileName + " ...");
		
		byte[] buffer = new byte[128];
		int length;
		
		while ((length = in.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		out.flush();
		out.close();
		in.close();
	}
	
	public static void main(String[] args) {
		EncryptFile start = new EncryptFile();
		try {
			start.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception {
		keyPair = createKeyPair();
		
		encryptFile("/home/konstantin/Pictures/img.png", "/home/konstantin/Pictures/encrypted.png", keyPair.getPublic());
		decryptFile("/home/konstantin/Pictures/encrypted.png", "/home/konstantin/Pictures/new.png", keyPair.getPrivate());
		
	}
	
	

}
