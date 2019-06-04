/*
 *               Cryptr
 *
 * Cryptr is a java encryption toolset
 * that can be used to encrypt/decrypt files
 * and keys locally, allowing for files to be
 * shared securely over the world wide web
 *
 * Cryptr provides the following functions:
 *	 1. Generating a secret key
 *   2. Encrypting a file with a secret key
 *   3. Decrypting a file with a secret key
 *   4. Encrypting a secret key with a public key
 *   5. Decrypting a secret key with a private key
 *
 */

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

public class Cryptr {
	/**
	 * Generates an 128-bit AES secret key and writes it to a file
	 *
	 * @param  secKeyFile    name of file to store secret key
	 */
	static void generateKey(String secKeyFile) throws Exception{
		// Generate a key
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecretKey skey = kgen.generateKey();

		// Save to file
		try (FileOutputStream out = new FileOutputStream(secKeyFile)) {
			byte[] keyb = skey.getEncoded();
			out.write(keyb);
		}
	}

	/**
	 * Extracts secret key from a file, generates an
	 * initialization vector, uses them to encrypt the original
	 * file, and writes an encrypted file containing the initialization
	 * vector followed by the encrypted file data
	 *
	 * @param  originalFile    name of file to encrypt
	 * @param  secKeyFile      name of file storing secret key
	 * @param  encryptedFile   name of file to write iv and encrypted file data
	 */
	static void encryptFile(String originalFile, String secKeyFile, String encryptedFile) {
		try {
			// load secret key
			byte[] keyb = Files.readAllBytes(Paths.get(secKeyFile));
			SecretKeySpec skey = new SecretKeySpec(keyb, "AES");

			// generates an initialization vector
			SecureRandom srandom = new SecureRandom();
			byte[] iv = new byte[16]; // 128 bits are converted to 16 bytes;
			srandom.nextBytes(iv);
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			// create cipher instance
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skey, ivspec);

			// read plaintext
			List<String> lines = Files.readAllLines(Paths.get(originalFile));
			String plainText = String.join("\n", lines);

			// encrypt
			byte[] encrypted = cipher.doFinal(plainText.getBytes());

			// Save to file
			try (FileOutputStream out = new FileOutputStream(encryptedFile)) {
				// save initail vector
				out.write(iv);
				// save encrypted data
				out.write(encrypted);
			}
		} catch (Exception e) {
			System.err.printf("Error: can't encryptFile! [%s]\n", e.toString());
		}
	}


	/**
	 * Extracts the secret key from a file, extracts the initialization vector
	 * from the beginning of the encrypted file, uses both secret key and
	 * initialization vector to decrypt the encrypted file data, and writes it to
	 * an output file
	 *
	 * @param  encryptedFile    name of file storing iv and encrypted data
	 * @param  secKeyFile	    name of file storing secret key
	 * @param  outputFile       name of file to write decrypted data to
	 */
	static void decryptFile(String encryptedFile, String secKeyFile, String outputFile) {
		try {
			// load secret key
			byte[] keyb = Files.readAllBytes(Paths.get(secKeyFile));
			SecretKeySpec skey = new SecretKeySpec(keyb, "AES");

			// read data & parse
			byte[] allbytes = Files.readAllBytes(Paths.get(encryptedFile));
			byte[] iv = Arrays.copyOfRange(allbytes, 0, 16);
			byte[] encrypted = Arrays.copyOfRange(allbytes, 16, allbytes.length);

			IvParameterSpec ivspec = new IvParameterSpec(iv);

			// create cipher instance
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skey, ivspec);

			// decrypt
			byte[] decrypted = cipher.doFinal(encrypted);

			// save to file
			try (FileOutputStream out = new FileOutputStream(outputFile)) {
				String plainText = new String(decrypted, StandardCharsets.UTF_8);
				out.write(plainText.getBytes());
			}
		} catch (Exception e) {
			System.err.printf("Error: can't decryptFile! [%s]\n", e.toString());
		}
	}

	/**
	 * Extracts secret key from a file, encrypts a secret key file using
     * a public Key (*.der) and writes the encrypted secret key to a file
	 *
	 * @param  secKeyFile    name of file holding secret key
	 * @param  pubKeyFile    name of public key file for encryption
	 * @param  encKeyFile    name of file to write encrypted secret key
	 */
	static void encryptKey(String secKeyFile, String pubKeyFile, String encKeyFile) {
		try {
			// load public key
			byte[] bytes = Files.readAllBytes(Paths.get(pubKeyFile));
			X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pubKey = kf.generatePublic(ks);

			// load secret key
			byte[] keyb = Files.readAllBytes(Paths.get(secKeyFile));

			// create cipher instance
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);

			// encrypt
			byte[] encrypted = cipher.doFinal(keyb);

			// Save to file
			try (FileOutputStream out = new FileOutputStream(encKeyFile)) {
				out.write(encrypted);
			}
		} catch (Exception e) {
			System.err.printf("Error: can't encryptKey! [%s]\n", e.toString());
		}
	}


	/**
	 * Decrypts an encrypted secret key file using a private Key (*.der)
	 * and writes the decrypted secret key to a file
	 *
	 * @param  encKeyFile       name of file storing encrypted secret key
	 * @param  privKeyFile      name of private key file for decryption
	 * @param  secKeyFile       name of file to write decrypted secret key
	 */
	static void decryptKey(String encKeyFile, String privKeyFile, String secKeyFile) {
		try {
			// load private key
			byte[] bytes = Files.readAllBytes(Paths.get(privKeyFile));
			PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey prvKey = kf.generatePrivate(ks);

			// load encrypted key
			byte[] encrypted = Files.readAllBytes(Paths.get(encKeyFile));

			// create cipher instance
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, prvKey);

			// decrypt
			byte[] decrypted = cipher.doFinal(encrypted);

			// Save to file
			try (FileOutputStream out = new FileOutputStream(secKeyFile)) {
				out.write(decrypted);
			}
		} catch (Exception e) {
			System.err.printf("Error: can't decryptKey! [%s]\n", e.toString());
		}
	}


	/**
	 * Main Program Runner
	 */
	public static void main(String[] args) throws Exception{

		String func;

		if(args.length < 1) {
			func = "";
		} else {
			func = args[0];
		}

		switch(func)
		{
			case "generatekey":
				if(args.length != 2) {
					System.out.println("Invalid Arguments.");
					System.out.println("Usage: Cryptr generatekey <key output file>");
					break;
				}
				System.out.println("Generating secret key and writing it to " + args[1]);
				generateKey(args[1]);
				break;
			case "encryptfile":
				if(args.length != 4) {
					System.out.println("Invalid Arguments.");
					System.out.println("Usage: Cryptr encryptfile <file to encrypt> <secret key file> <encrypted output file>");
					break;
				}
				System.out.println("Encrypting " + args[1] + " with key " + args[2] + " to "  + args[3]);
				encryptFile(args[1], args[2], args[3]);
				break;
			case "decryptfile":
				if(args.length != 4) {
					System.out.println("Invalid Arguments.");
					System.out.println("Usage: Cryptr decryptfile <file to decrypt> <secret key file> <decrypted output file>");
					break;
				}
				System.out.println("Decrypting " + args[1] + " with key " + args[2] + " to " + args[3]);
				decryptFile(args[1], args[2], args[3]);
				break;
			case "encryptkey":
				if(args.length != 4) {
					System.out.println("Invalid Arguments.");
					System.out.println("Usage: Cryptr encryptkey <key to encrypt> <public key to encrypt with> <encrypted key file>");
					break;
				}
				System.out.println("Encrypting key file " + args[1] + " with public key file " + args[2] + " to " + args[3]);
				encryptKey(args[1], args[2], args[3]);
				break;
			case "decryptkey":
				if(args.length != 4) {
					System.out.println("Invalid Arguments.");
					System.out.println("Usage: Cryptr decryptkey <key to decrypt> <private key to decrypt with> <decrypted key file>");
					break;
				}
				System.out.println("Decrypting key file " + args[1] + " with private key file " + args[2] + " to " + args[3]);
				decryptKey(args[1], args[2], args[3]);
				break;
			default:
				System.out.println("Invalid Arguments.");
				System.out.println("Usage:");
				System.out.println("  Cryptr generatekey <key output file>");
				System.out.println("  Cryptr encryptfile <file to encrypt> <secret key file> <encrypted output file>");
				System.out.println("  Cryptr decryptfile <file to decrypt> <secret key file> <decrypted output file>");
				System.out.println("  Cryptr encryptkey <key to encrypt> <public key to encrypt with> <encrypted key file> ");
				System.out.println("  Cryptr decryptkey <key to decrypt> <private key to decrypt with> <decrypted key file>");
		}

		System.exit(0);

	}

}
