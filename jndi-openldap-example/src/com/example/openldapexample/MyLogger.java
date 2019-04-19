package com.example.openldapexample;

import java.nio.file.Paths;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MyLogger {
	
	//Shared Logger Instance
	private static MyLogger g_sharedLogger = null;
	
	//Member Variables
	private Logger 	m_logger 			= null;
	private boolean m_isDebugEnabled	= false;
	private String  m_logConfigFilePath	= null;
	
	//Constants
	private final String	LOG_CONFIG_FILE_NAME		= "log4j.properties";
	
	
	private MyLogger() {
		//Initialize log4j Logger
		m_logger = Logger.getLogger("PosServer");
		m_logConfigFilePath = Paths.get(LOG_CONFIG_FILE_NAME).toString();
		PropertyConfigurator.configure(m_logConfigFilePath);
		m_isDebugEnabled = m_logger.isDebugEnabled();
	}
	
	
	public void reloadConfiguration() {
		PropertyConfigurator.configure(m_logConfigFilePath);
		m_isDebugEnabled = m_logger.isDebugEnabled();
		logInfo(String.format("[TMLogger::reloadConfiguration] Log Level: %s", m_logger.getEffectiveLevel().toString()));
	}
	
	public String getLogConfigurationFileName() {
		return LOG_CONFIG_FILE_NAME;
	}
	
	
	public void logDebug(String message) {
		m_logger.debug(message);
	}
	
	public void logInfo(String message) {
		m_logger.info(message);
	}
	
	public void logWarn(String message) {
		m_logger.warn(message);
	}

	public void logError(String message) {
		m_logger.error(message);
	}
	
	static public synchronized MyLogger getSharedLogger() {
		if (g_sharedLogger == null) {
			g_sharedLogger = new MyLogger();
		}
		
		return g_sharedLogger;
	}
	
	public boolean isDebugEnabled() {
		return m_isDebugEnabled;
	}
}
