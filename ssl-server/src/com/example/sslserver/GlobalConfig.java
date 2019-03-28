package com.example.sslserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class GlobalConfig {

	
	//Constants
	public static final String CONFIG_DIR_NAME					= "config";
	
	
	private static GlobalConfig g_sharedGlobalConfig 			= null;
	private static String applicationPath						= "";
	
	
	private String CONFIG_FILE_NAME								= "server.config"; 
	private String PROP_SSL										= "SSL";
	private String PROP_SSL_DEBUG								= "SSL_DEBUG";
	private String PROP_SSL_MUTUAL_AUTH							= "SSL_MUTUAL_AUTH";
	private String PROP_KEYSTORE_NAME							= "KEYSTORE_NAME";
	private String PROP_KEYSTORE_PASSWORD						= "KEYSTORE_PASSWORD";
	private String PROP_TRUSTSTORE_NAME							= "TRUSTSTORE_NAME";
	private String PROP_TRUSTSTORE_PASSWORD						= "TRUSTSTORE_PASSWORD";
	private String PROP_SERVER_PORT_EXT							= "SERVER_PORT_EXT";
	private String PROP_BIND_IP_ADDRESS							= "BIND_IP_ADDRESS";
	
	private boolean m_ssl										= false;
	private boolean m_sslDebugging								= false;
	private boolean m_sslMutualAuth								= false;
	private String m_keystoreName								= "TransactionManager_Keystore.jks";
	private String m_keystorePassword							= "";
	private String m_truststoreName								= "TrustStore.jks";
	private String m_truststorePassword							= "";
	private Integer m_serverPortExt								= 8888;
	private String m_bindIpAddress								= "0.0.0.0";
	private MyLogger m_logger									= null;
	
	
	public static GlobalConfig getInstance() {
		if(g_sharedGlobalConfig == null) {
			g_sharedGlobalConfig = new GlobalConfig();
		}
		return g_sharedGlobalConfig;
	}
	

	//Once and for all initialization
	static {
		//Get application folder
		applicationPath = autoDetectApplicationPath();
	}
	

	private static String autoDetectApplicationPath() {

		String path = null;
		try {
			path = new File(".").getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return path;
	}

	public static String getApplicationPath() {
		return applicationPath;
	}
	
	
	private GlobalConfig() {

		//Initialize logger
		m_logger = MyLogger.getSharedLogger();

		//Load app configuration
		loadConfiguration();
	}
	
	
	public void loadConfiguration() {
		m_logger.logInfo("[GlobalConfig::loadConfiguration]");
		
		File configFile = new File(Paths.get(applicationPath, CONFIG_DIR_NAME, CONFIG_FILE_NAME).toString());
		Properties props = new Properties();
		FileOutputStream outstream = null;
		FileInputStream instream = null;

		//Create the default config file if it does not exist
		if(configFile.exists() == false) {
			try {
				configFile.createNewFile();
				
				props.setProperty(PROP_SSL, Boolean.FALSE.toString());
				props.setProperty(PROP_SSL_DEBUG, Boolean.FALSE.toString());
				props.setProperty(PROP_SSL_MUTUAL_AUTH, Boolean.FALSE.toString());
				props.setProperty(PROP_SERVER_PORT_EXT, m_serverPortExt.toString());
				props.setProperty(PROP_KEYSTORE_NAME, m_keystoreName);
				props.setProperty(PROP_KEYSTORE_PASSWORD, m_keystorePassword);
				props.setProperty(PROP_TRUSTSTORE_NAME, m_truststoreName);
				props.setProperty(PROP_TRUSTSTORE_PASSWORD, m_truststorePassword);
				props.setProperty(PROP_BIND_IP_ADDRESS, m_bindIpAddress);
				
				try {
					outstream = new FileOutputStream(configFile);
					props.storeToXML(outstream, null);
				} catch (FileNotFoundException e) {
					m_logger.logError(String.format("[GlobalConfig::loadDefaults] Failed to open file %s. Exception: %s", configFile, e.getMessage()));
				}
				
			} catch (IOException e) {
				m_logger.logError(String.format("[GlobalConfig::loadDefaults] Failed to create file %s. Exception: %s", configFile, e.getMessage()));
			}
			
		} else {	//Load defaults
			try {
				instream = new FileInputStream(configFile);
				
				props.loadFromXML(instream);
				
				//Load properties
				String tmp = null;
				tmp = props.getProperty(PROP_SSL);
				if(tmp != null) {
					m_ssl 					= Boolean.parseBoolean(tmp);
				} else {
					m_ssl 					= true;			//Default to true
				}
				
				tmp = props.getProperty(PROP_SSL_DEBUG);
				if(tmp != null) {
					m_sslDebugging			= Boolean.parseBoolean(tmp);
				} else {
					m_sslDebugging 			= false;	//Default to false
				}
				
				tmp = props.getProperty(PROP_SSL_MUTUAL_AUTH);
				if(tmp != null) {
					m_sslMutualAuth			= Boolean.parseBoolean(tmp);
				} else {
					m_sslMutualAuth			= true;
				}
				
				m_keystoreName			= props.getProperty(PROP_KEYSTORE_NAME);
				m_keystorePassword		= props.getProperty(PROP_KEYSTORE_PASSWORD);
				m_truststoreName		= props.getProperty(PROP_TRUSTSTORE_NAME);
				m_truststorePassword	= props.getProperty(PROP_TRUSTSTORE_PASSWORD);
				m_bindIpAddress 		= props.getProperty(PROP_BIND_IP_ADDRESS);
				String _terminalServerPort = props.getProperty(PROP_SERVER_PORT_EXT);
				if((_terminalServerPort != null) && (!_terminalServerPort.isEmpty())) {
					m_serverPortExt	= Integer.parseInt(_terminalServerPort);
				}
				
			} catch (FileNotFoundException e) {
				m_logger.logError(String.format("[GlobalConfig::loadDefaults] File not found %s. Exception: %s", configFile, e.getMessage()));
			} catch (InvalidPropertiesFormatException e) {
				m_logger.logError(String.format("[GlobalConfig::loadDefaults] Invalid Property File. Exception: %s", e.getMessage()));
			} catch (IOException e) {
				m_logger.logError(String.format("[GlobalConfig::loadDefaults] IO Error for file %s. Exception: %s", configFile, e.getMessage()));
			}
		}
		
		//Close streams
		if(outstream != null) {
			try {
				outstream.close();
			} catch (IOException e) {
				m_logger.logError(String.format("[GlobalConfig::loadDefaults] Failed to close OutputStream. Exception: %s", e.getMessage()));
			}
		}
		
		if(instream != null) {
			try {
				instream.close();
			} catch (IOException e) {
				m_logger.logError(String.format("[GlobalConfig::loadDefaults] Failed to close InputStream. Exception: %s", e.getMessage()));
			}
		}
	}
	
	public boolean getSSL() {
		return m_ssl;
	}
	
	public boolean getSSLDebugging() {
		return m_sslDebugging;
	}
	
	public boolean getSSLMutualAuth() {
		return m_sslMutualAuth;
	}
	
	public Integer getTerminaServerPort() {
		return m_serverPortExt;
	}
	
	public String getKeystoreName() {
		return m_keystoreName;
	}
	
	public String getKeystorePassword() {
		return m_keystorePassword;
	}
	
	public String getTruststoreName() {
		return m_truststoreName;
	}
	
	public String getTruststorePassword() {
		return m_truststorePassword;
	}
	
	public String getConfigDir() {
		return CONFIG_DIR_NAME;
	}
	
	public Path getConfigDirPath() {
		return Paths.get(applicationPath, CONFIG_DIR_NAME);
	}
	
	public String getConfigFileName() {
		return CONFIG_FILE_NAME;
	}
	
	public Path getConfigurationFilePath() {
		return Paths.get(applicationPath, CONFIG_DIR_NAME, CONFIG_FILE_NAME);
	}
	
	public String getBindIpAddress() {
		return m_bindIpAddress;
	}
	
}
