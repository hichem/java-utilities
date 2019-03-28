package com.example.sslserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class SslServer {

	
	public enum ErrorCode {
		SUCCESS,
		UNKNOWN_ERROR,
		ERROR_PORT_ALREADY_USED,
		ERROR_CONNECTION_ACCEPT,
		ERROR_SSL_CONFIG
	}
	
	//Singleton
	private static SslServer g_sslServer = null;
	
	//Constants
	final int SERVER_SOCKET_BACKLOG	= 2000;
	
	//Variables
	private int 			m_portNumber		= 0;
	private boolean			m_serverStarted		= false;
	private ServerSocket	m_serverSocket		= null;
	
	//Public constructor
	public static synchronized SslServer getInstance() {
		if(g_sslServer == null) {
			g_sslServer = new SslServer();
		}
		
		return g_sslServer;
	}
	
	//Private constructor
	private SslServer() {
		MyLogger.getSharedLogger().logDebug("[SslServer::SslServer]");
	}
	
	
	public void setPortNumber(int portNumber) {
		MyLogger.getSharedLogger().logDebug(String.format("[SslServer::setPortNumber] Port: %d", portNumber));
		m_portNumber = portNumber;
	}
	
	public void stopServer() {
		MyLogger.getSharedLogger().logDebug("[SslServer::stopServer]");
		m_serverStarted = false;
	}
	
	
	public ErrorCode startServer() {
		MyLogger.getSharedLogger().logDebug("[SslServer::startServer]");
		ErrorCode result = ErrorCode.SUCCESS;
		
		if(m_serverStarted == true) {
			MyLogger.getSharedLogger().logError(String.format("[%s] %s", "SslServer::startServer", "Server already started"));
		} else {
			
			//Get the bind ip address
			InetAddress bindAddr = null;
			try {
				bindAddr = InetAddress.getByName(GlobalConfig.getInstance().getBindIpAddress());
			} catch (UnknownHostException e1) {
				MyLogger.getSharedLogger().logWarn(String.format("[SslServer::startServer] Failed to parse bind address. Default will be used. Exception: %s", e1.getMessage()));
				bindAddr = null;
			}
			
			// Create a ServerSocket to listen for connections with
		    try {
		    	//Check if SSL is enabled
		    	if(GlobalConfig.getInstance().getSSL() == true) {
		    		MyLogger.getSharedLogger().logDebug("[SslServer::startServer] Using Secure Sockets");

		    		try {
		    			//Initialize the key manager
		    			X509TrustManager customTm = null;
		    			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		    			FileInputStream fin = new FileInputStream(GlobalConfig.getInstance().getKeystoreName());
		    			KeyStore ks = KeyStore.getInstance("JKS");
		    			ks.load(fin, GlobalConfig.getInstance().getKeystorePassword().toCharArray());
		    			kmf.init(ks, GlobalConfig.getInstance().getKeystorePassword().toCharArray());
		    			fin.close();
		    			
		    			if(GlobalConfig.getInstance().getSSLMutualAuth() == true) {
		    				//Initialize the trust manager
			    			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			    			FileInputStream instream = new FileInputStream(GlobalConfig.getInstance().getTruststoreName());
			    			KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			    			myTrustStore.load(instream, GlobalConfig.getInstance().getTruststorePassword().toCharArray());
			    			tmf.init(myTrustStore);
			    			instream.close();
			    			
			    			// Get hold of the default trust manager
			    			X509TrustManager myTm = null;
			    			for (TrustManager tm : tmf.getTrustManagers()) {
			    			    if (tm instanceof X509TrustManager) {
			    			        myTm = (X509TrustManager) tm;
			    			        break;
			    			    }
			    			}

			    			// Wrap it in your own class.
			    			//final X509TrustManager finalDefaultTm = defaultTm;
			    			final X509TrustManager finalMyTm = myTm;
			    			customTm = new X509TrustManager() {
			    			    @Override
			    			    public X509Certificate[] getAcceptedIssuers() {
			    			        // If you're planning to use client-cert auth,
			    			        // merge results from "defaultTm" and "myTm".
			    			        //return finalDefaultTm.getAcceptedIssuers();
			    			    	return finalMyTm.getAcceptedIssuers();
			    			    }

			    			    @Override
			    			    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			    			    	//MyLogger.getSharedLogger().logDebug("[SslServer::X509TrustManager::checkServerTrusted]");
			    			        try {
			    			            finalMyTm.checkServerTrusted(chain, authType);
			    			        } catch (CertificateException e) {
			    			        	MyLogger.getSharedLogger().logDebug("[SslServer::X509TrustManager::checkServerTrusted] Exception: " + e.getMessage());
			    			        }
			    			    }

			    			    @Override
			    			    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			    			    	//MyLogger.getSharedLogger().logDebug("[SslServer::X509TrustManager::checkClientTrusted]");
			    			    	// If you're planning to use client-cert auth,
			    			        // do the same as checking the server.
			    			        //finalDefaultTm.checkClientTrusted(chain, authType);
			    			    	finalMyTm.checkClientTrusted(chain, authType);
			    			    }
			    			};
		    			}

		    			
		    			//Initialize the SSL context
		    			SSLContext context = SSLContext.getInstance("TLS");
		    			
		    			if(GlobalConfig.getInstance().getSSLMutualAuth() == true) {
		    				context.init(kmf.getKeyManagers(), new TrustManager[] { customTm }, null);
		    				MyLogger.getSharedLogger().logInfo("[SslServer::startServer] SSL Mutual Authentication Enabled");
		    			} else {
		    				context.init(kmf.getKeyManagers(), null, null);
		    				MyLogger.getSharedLogger().logInfo("[SslServer::startServer] SSL Simple Authentication Enabled");
		    			}
		    			
		    			SSLServerSocketFactory sslserversocketfactory = context.getServerSocketFactory();
		    			m_serverSocket = sslserversocketfactory.createServerSocket(m_portNumber, SERVER_SOCKET_BACKLOG, bindAddr);
		    			
		    			//Enable / Disable mutual authentication
		    			((SSLServerSocket)m_serverSocket).setNeedClientAuth(GlobalConfig.getInstance().getSSLMutualAuth());
		    			
		    			//Set enabled ssl protcols
		    			((SSLServerSocket)m_serverSocket).setEnabledProtocols(new String[]{"TLSv1.2"}); //"SSLv3"
		    			
		    			//Set the server as started
		    			m_serverStarted = true;

		    			MyLogger.getSharedLogger().logInfo(String.format("[SslServer::startServer] Server started on %s:%d", m_serverSocket.getInetAddress().getHostAddress(), m_portNumber));

		    		} catch (NoSuchAlgorithmException e) {
		    			MyLogger.getSharedLogger().logError(String.format("[%s] Exception: %s", "SslServer::startServer", e.getMessage()));
		    			result = ErrorCode.ERROR_SSL_CONFIG;
		    		} catch (KeyStoreException e) {
		    			MyLogger.getSharedLogger().logError(String.format("[%s] Exception: %s", "SslServer::startServer", e.getMessage()));
		    			result = ErrorCode.ERROR_SSL_CONFIG;
		    		} catch (CertificateException e) {
		    			MyLogger.getSharedLogger().logError(String.format("[%s] Exception: %s", "SslServer::startServer", e.getMessage()));
		    			result = ErrorCode.ERROR_SSL_CONFIG;
		    		} catch (UnrecoverableKeyException e) {
		    			MyLogger.getSharedLogger().logError(String.format("[%s] Exception: %s", "SslServer::startServer", e.getMessage()));
		    			result = ErrorCode.ERROR_SSL_CONFIG;
		    		} catch (KeyManagementException e) {
		    			MyLogger.getSharedLogger().logError(String.format("[%s] Exception: %s", "SslServer::startServer", e.getMessage()));
		    			result = ErrorCode.ERROR_SSL_CONFIG;
		    		}
		    	} else {
		    		MyLogger.getSharedLogger().logInfo("[SslServer::startServer] Using Non Secure Sockets");
		    		m_serverSocket = new ServerSocket(m_portNumber, SERVER_SOCKET_BACKLOG, bindAddr);

		    		//Set the server as started
				    m_serverStarted = true;
				    MyLogger.getSharedLogger().logInfo(String.format("[SslServer::startServer] Server started on %s:%d", m_serverSocket.getInetAddress().getHostAddress(), m_portNumber));

		    	}
		    	
			
		    	ExecutorService executor = Executors.newCachedThreadPool();
		    	
			    do {
			    	ClientSocket job = new ClientSocket();
			    	
				    //Wait for a client to connect
				    try {
						job.initSocket(m_serverSocket.accept());
						
						//Create a thread for client
						executor.execute(job);
						
					} catch (IOException e) {
						result = ErrorCode.ERROR_CONNECTION_ACCEPT;
						MyLogger.getSharedLogger().logWarn(String.format("[SslServer::startServer] Failed to Accept Connection. Exception: %s", e.getMessage()));
					}
				    
			    } while (m_serverStarted);
			    
			    //Close server socket
			    m_serverSocket.close();
			    m_serverSocket = null;
			    
			} catch (IOException e) {
				result = ErrorCode.ERROR_PORT_ALREADY_USED;
				MyLogger.getSharedLogger().logError(String.format("[SslServer::startServer] Failed to run server. Exception: %s", e.getMessage()));
			}
		    
			
		}
		
		return result;
	}
}
