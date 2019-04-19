package com.example.openldapexample;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class OpenLdapSSLSocketFactory extends SocketFactory {
    private SSLSocketFactory sf;

    static String trustStoreLocation = null;
    static String truststorePassword = null;
    
    public static SocketFactory getDefault() {
    	return new OpenLdapSSLSocketFactory();
    }

    
    public static void setLdapTruststore(String name, String password) {
    	trustStoreLocation = name;
    	truststorePassword = password;
    }
    
    
    public OpenLdapSSLSocketFactory() {
    	
    	try {
    		KeyStore keyStore = KeyStore.getInstance("JKS");

    		InputStream readStream = new FileInputStream(trustStoreLocation);
    		if (readStream != null) {
    			keyStore.load(readStream, truststorePassword.toCharArray());
    			readStream.close();
    			TrustManagerFactory tmf = TrustManagerFactory
    					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    			tmf.init(keyStore);
    			SSLContext ctx = SSLContext.getInstance("SSL");
    			ctx.init(null, tmf.getTrustManagers(), null);
    			sf = ctx.getSocketFactory();
    		}
    	} catch (NoSuchAlgorithmException e) {
			MyLogger.getSharedLogger().logError(String.format("[OpenLdapSSLSocketFactory::OpenLdapSSLSocketFactory] NoSuchAlgorithmException Exception: %s", e.getMessage()));
		} catch (KeyStoreException e) {
			MyLogger.getSharedLogger().logError(String.format("[OpenLdapSSLSocketFactory::OpenLdapSSLSocketFactory] KeyStoreException Exception: %s", e.getMessage()));
		} catch (CertificateException e) {
			MyLogger.getSharedLogger().logError(String.format("[OpenLdapSSLSocketFactory::OpenLdapSSLSocketFactory] CertificateException Exception: %s", e.getMessage()));
		} catch (KeyManagementException e) {
			MyLogger.getSharedLogger().logError(String.format("[OpenLdapSSLSocketFactory::OpenLdapSSLSocketFactory] KeyManagementException Exception: %s", e.getMessage()));
		} catch (UnknownHostException e) {
			MyLogger.getSharedLogger().logError(String.format("[OpenLdapSSLSocketFactory::OpenLdapSSLSocketFactory] UnknownHostException Exception: %s", e.getMessage()));
		} catch (IOException e) {
			MyLogger.getSharedLogger().logError(String.format("[OpenLdapSSLSocketFactory::OpenLdapSSLSocketFactory] IOException Exception: %s", e.getMessage()));
		}
    }

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
		return sf.createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return sf.createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		return sf.createSocket(arg0, arg1, arg2, arg3);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
		return sf.createSocket(arg0, arg1, arg2, arg3);
	}

    
}