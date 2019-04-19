package com.example.openldapexample;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import java.net.URLConnection;

public class CertValidator {

	
	
	
	//Member Variables
	private boolean 	m_sslEnabled 				= true;
	private String 		m_truststoreLocation		= null;
	private String 		m_password					= null;
	private SSLSocketFactory	sslFactory			= null;
	
	
	
	//Default Constructor
	public CertValidator() {
		//Disable SSL by default
		m_sslEnabled 			= false;
	}
	
	//Enable SSL
	public void enableSSL(String trustStoreLocation, String password) throws Exception {
		
		m_sslEnabled			= true;
		m_truststoreLocation	= trustStoreLocation;
		m_password				= password;
		
		//Create the SSL context
		//System.setProperty("jsse.enableSNIExtension", "false");
		KeyStore keyStore = KeyStore.getInstance("JKS");
		
		InputStream readStream = new FileInputStream(m_truststoreLocation);
		if (readStream != null) {
			keyStore.load(readStream, m_password.toCharArray());
			readStream.close();
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, tmf.getTrustManagers(), null);
			sslFactory = ctx.getSocketFactory();
		}
	}
	
	

	public URLConnection getConnection(String myURL) throws Exception {

		HttpURLConnection conn = null;
		URL url = new URL(myURL);
		
		if (m_sslEnabled == false) {
			conn = (HttpURLConnection) url.openConnection();
		} else {

			try {
				
					conn = (HttpsURLConnection) url.openConnection();
					((HttpsURLConnection)conn).setSSLSocketFactory(sslFactory);
				
			} catch (Exception e) {
				MyLogger.getSharedLogger().logError(String.format("[CertValidator::getConnection] Error: %s", e.getMessage()));
			}
		}
		
		return conn;
	}

}
