package com.example.pgpexample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.util.io.Streams;


//File downloaded from the following Github Project: https://github.com/bcgit/bc-java/blob/master/pg/src/main/java/org/bouncycastle/openpgp/examples/KeyBasedFileProcessor.java

/**
 * A simple utility class that encrypts/decrypts public key based
 * encryption files.
 * <p>
 * To encrypt a file: KeyBasedFileProcessor -e [-a|-ai] fileName publicKeyFile.<br>
 * If -a is specified the output file will be "ascii-armored".
 * If -i is specified the output file will be have integrity checking added.
 * <p>
 * To decrypt: KeyBasedFileProcessor -d fileName secretKeyFile passPhrase.
 * <p>
 * Note 1: this example will silently overwrite files, nor does it pay any attention to
 * the specification of "_CONSOLE" in the filename. It also expects that a single pass phrase
 * will have been used.
 * <p>
 * Note 2: if an empty file name has been specified in the literal data object contained in the
 * encrypted packet a file with the name filename.out will be generated in the current working directory.
 */
public class KeyBasedFileProcessor
{
	public static void decryptFile(
			String inputFileName,
			String keyFileName,
			char[] passwd,
			String defaultFileName)
					throws IOException, NoSuchProviderException, PGPException, IllegalArgumentException
	{
		MyLogger.getSharedLogger().logInfo(String.format("[KeyBasedFileProcessor::decryptFile] File: %s", inputFileName));
		InputStream in = new BufferedInputStream(new FileInputStream(inputFileName));
		InputStream keyIn = new BufferedInputStream(new FileInputStream(keyFileName));
		decryptFile(in, keyIn, passwd, defaultFileName);
		keyIn.close();
		in.close();
	}

	/**
	 * decrypt the passed in message stream
	 */
	private static void decryptFile(
			InputStream in,
			InputStream keyIn,
			char[]      passwd,
			String      defaultFileName)
					throws IOException, NoSuchProviderException, PGPException, IllegalArgumentException
	{
		in = PGPUtil.getDecoderStream(in);

		JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
		PGPEncryptedDataList    enc;

		Object                  o = pgpF.nextObject();
		//
		// the first object might be a PGP marker packet.
		//
		if (o instanceof PGPEncryptedDataList)
		{
			enc = (PGPEncryptedDataList)o;
		}
		else
		{
			enc = (PGPEncryptedDataList)pgpF.nextObject();
		}

		//
		// find the secret key
		//
		@SuppressWarnings("rawtypes")
		Iterator                    it = enc.getEncryptedDataObjects();
		PGPPrivateKey               sKey = null;
		PGPPublicKeyEncryptedData   pbe = null;
		PGPSecretKeyRingCollection  pgpSec = new PGPSecretKeyRingCollection(
				PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

		while ((sKey == null) && it.hasNext())
		{
			pbe = (PGPPublicKeyEncryptedData)it.next();

			sKey = PGPUtility.findSecretKey(pgpSec, pbe.getKeyID(), passwd);
		}

		if (sKey == null)
		{
			throw new IllegalArgumentException("secret key for message not found.");
		}

		InputStream         clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey));

		JcaPGPObjectFactory    plainFact = new JcaPGPObjectFactory(clear);

		Object              message = plainFact.nextObject();

		if (message instanceof PGPCompressedData)
		{
			PGPCompressedData   cData = (PGPCompressedData)message;
			JcaPGPObjectFactory    pgpFact = new JcaPGPObjectFactory(cData.getDataStream());

			message = pgpFact.nextObject();
		}


		if (message instanceof PGPLiteralData)
		{
			PGPLiteralData ld = (PGPLiteralData)message;

			//String outFileName = ld.getFileName();
			//if (outFileName.length() == 0)
			//{
			//	outFileName = defaultFileName;
			//}

			String outFileName = defaultFileName;

			InputStream unc = ld.getInputStream();
			OutputStream fOut = new BufferedOutputStream(new FileOutputStream(outFileName));

			Streams.pipeAll(unc, fOut);

			fOut.close();
		}
		else if (message instanceof PGPOnePassSignatureList)
		{
			throw new PGPException("encrypted message contains a signed message - not literal data.");
		}
		else
		{
			throw new PGPException("message is not a simple encrypted file - type unknown.");
		}

		if (pbe.isIntegrityProtected())
		{
			if (!pbe.verify())
			{
				//System.err.println("message failed integrity check");
				MyLogger.getSharedLogger().logError("[KeyBasedFileProcessor::decryptFile] Message failed integrity check");
			}
			else
			{
				//System.err.println("message integrity check passed");
				MyLogger.getSharedLogger().logDebug("[KeyBasedFileProcessor::decryptFile] Message integrity check passed");
			}
		}
		else
		{
			//System.err.println("no message integrity check");
			MyLogger.getSharedLogger().logInfo("[KeyBasedFileProcessor::decryptFile] No Message Integrity Check");
		}


	}


	public static void decryptAndVerifyFile(
			String inputFileName,
			String secRingFileName,
			String pubRingFileName,
			char[] secRingPasswd,
			char[] pubRingPasswd,
			String defaultFileName)
					throws IOException, NoSuchProviderException, PGPException, IllegalArgumentException
	{
		MyLogger.getSharedLogger().logInfo(String.format("[KeyBasedFileProcessor::decryptFile] File: %s", inputFileName));
		InputStream in = new BufferedInputStream(new FileInputStream(inputFileName));
		InputStream secRingIn = new BufferedInputStream(new FileInputStream(secRingFileName));
		InputStream pubRingIn = new BufferedInputStream(new FileInputStream(pubRingFileName));
		decryptAndVerifyFile(in, secRingIn, pubRingIn, secRingPasswd, pubRingPasswd, defaultFileName);
		secRingIn.close();
		pubRingIn.close();
		in.close();
	}

	/**
	 * decrypt and verify signature of the passed in message stream
	 */
	private static void decryptAndVerifyFile(
			InputStream in,
			InputStream secKeyringIn,
			InputStream pubKeyringIn,
			char[]      passwdSecKeyring,
			char[]      passwdPubKeyring,
			String      defaultFileName)
					throws IOException, NoSuchProviderException, PGPException, IllegalArgumentException
	{	
		in = PGPUtil.getDecoderStream(in);

		JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
		PGPEncryptedDataList    enc;

		Object                  o = pgpF.nextObject();
		//
		// the first object might be a PGP marker packet.
		//
		if (o instanceof PGPEncryptedDataList)
		{
			enc = (PGPEncryptedDataList)o;
		}
		else
		{
			enc = (PGPEncryptedDataList)pgpF.nextObject();
		}

		//
		// find the secret key
		//
		@SuppressWarnings("rawtypes")
		Iterator                    it = enc.getEncryptedDataObjects();
		PGPPrivateKey               sKey = null;
		PGPPublicKeyEncryptedData   pbe = null;
		PGPSecretKeyRingCollection  pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(secKeyringIn), new JcaKeyFingerprintCalculator());

		while ((sKey == null) && it.hasNext())
		{
			pbe = (PGPPublicKeyEncryptedData)it.next();

			sKey = PGPUtility.findSecretKey(pgpSec, pbe.getKeyID(), passwdSecKeyring);
		}

		if (sKey == null)
		{
			throw new IllegalArgumentException("secret key for message not found.");
		}

		InputStream         clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey));

		JcaPGPObjectFactory    plainFact = new JcaPGPObjectFactory(clear);

		Object message = null;
		PGPOnePassSignatureList onePassSignatureList = null;
		PGPSignatureList signatureList = null;
		PGPOnePassSignature onePassSignature = null;
		//OutputStream fOut = null;
		ByteArrayOutputStream fOut = new ByteArrayOutputStream();
		
		//Get next object in the factory
		message = plainFact.nextObject();
		do {

			if (message instanceof PGPCompressedData)
			{
				PGPCompressedData   cData = (PGPCompressedData)message;
				plainFact = new JcaPGPObjectFactory(cData.getDataStream());

				message = plainFact.nextObject();
			}


			if (message instanceof PGPLiteralData)
			{
				PGPLiteralData ld = (PGPLiteralData)message;
				InputStream unc = ld.getInputStream();
				//fOut = new BufferedOutputStream(new FileOutputStream(outFileName));
				Streams.pipeAll(unc, fOut);
				
				fOut.close();
			}
			else if (message instanceof PGPOnePassSignatureList)
			{
				onePassSignatureList = (PGPOnePassSignatureList) message;
				//throw new PGPException("encrypted message contains a signed message - not literal data.");
			}
			else if (message instanceof PGPSignatureList)
			{
				signatureList = (PGPSignatureList) message;
				//throw new PGPException("encrypted message contains a signed message - not literal data.");
			}
			else
			{
				throw new PGPException("message is not a simple encrypted file - type unknown.");
			}

			//Get next object in the factory
			message = plainFact.nextObject();

		} while (message != null);	

		//Verify Signature
		if((onePassSignatureList != null) || (signatureList != null)) {

			//Get the one pass signature - we assume only one signature is in the package
			onePassSignature = onePassSignatureList.get(0);

			//Get the public key of the sender to verify the signature of the package
			PGPPublicKeyRingCollection  pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(pubKeyringIn), new JcaKeyFingerprintCalculator());
			PGPPublicKey                pubKey = pgpRing.getPublicKey(onePassSignature.getKeyID());
			
			//Get output buffer
			byte[] output = fOut.toByteArray();

			if(pubKey != null) {
				onePassSignature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), pubKey);
				onePassSignature.update(output);
				
				//Verify the signature
				PGPSignature signature = signatureList.get(0);
				if(onePassSignature.verify(signature) == true) {

					//Print signing entities
					Iterator<?> userIds = pubKey.getUserIDs();
					while (userIds.hasNext()) {
						String userId = (String) userIds.next();
						MyLogger.getSharedLogger().logInfo(String.format("[KeyBasedFileProcessor::decryptAndVerifyFile] Package signed by: %s", userId));
					}

				} else {
					throw new PGPException("Signature verification failed");
				}
			}
		} else {
			throw new PGPException("No signature found in the package");
		}

		if (pbe.isIntegrityProtected())
		{
			if (!pbe.verify())
			{
				//System.err.println("message failed integrity check");
				MyLogger.getSharedLogger().logError("[KeyBasedFileProcessor::decryptAndVerifyFile] Message failed integrity check. Payment DLL will not be loaded");
			}
			else
			{
				//System.err.println("message integrity check passed");
				MyLogger.getSharedLogger().logDebug("[KeyBasedFileProcessor::decryptAndVerifyFile] Message integrity check passed");
				
				//Write Payment DLL Package
				OutputStream outFile = new FileOutputStream(defaultFileName);
				fOut.writeTo(outFile);
				fOut.flush();
				fOut.close();
			}
		}
		else
		{
			//System.err.println("no message integrity check");
			MyLogger.getSharedLogger().logError("[KeyBasedFileProcessor::decryptAndVerifyFile] No Message Integrity Check. Payment DLL will not be loaded");
		}


	}

	
	public static void encryptFile(
			String          outputFileName,
			String          inputFileName,
			String          encKeyFileName,
			boolean         armor,
			boolean         withIntegrityCheck)
					throws IOException, NoSuchProviderException, PGPException
	{
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFileName));
		PGPPublicKey encKey = PGPUtility.readPublicKey(encKeyFileName);
		encryptFile(out, inputFileName, encKey, armor, withIntegrityCheck);
		out.close();
	}

	private static void encryptFile(
			OutputStream    out,
			String          fileName,
			PGPPublicKey    encKey,
			boolean         armor,
			boolean         withIntegrityCheck)
					throws IOException, NoSuchProviderException
	{
		if (armor)
		{
			out = new ArmoredOutputStream(out);
		}

		try
		{
			byte[] bytes = PGPUtility.compressFile(fileName, CompressionAlgorithmTags.ZIP);

			PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
					new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).setWithIntegrityPacket(withIntegrityCheck).setSecureRandom(new SecureRandom()).setProvider("BC"));

			encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));

			OutputStream cOut = encGen.open(out, bytes.length);

			cOut.write(bytes);
			cOut.close();

			if (armor)
			{
				out.close();
			}
		}
		catch (PGPException e)
		{
			System.err.println(e);
			if (e.getUnderlyingException() != null)
			{
				e.getUnderlyingException().printStackTrace();
			}
		}
	}



}