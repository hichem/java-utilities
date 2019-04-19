package com.example.openldapexample;

import java.util.Hashtable;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;



public class OpenLdapManager {

	
	//Member variables
	private Hashtable<String,String> m_env = new Hashtable<>();
	private DirContext m_dircontext = null;
	private boolean m_sslEnabled = false;
	private String m_connect = null;
	private String m_dn = null;
	private String m_rootDN = null;
	private String m_password = null;
	private String m_truststore = null;
	private String m_truststorePassword = null;
	private MyLogger m_logger = null;
	
	
	//Singleton
	private static OpenLdapManager g_sharedInstance = null;
	
	
	//Public shared constructor
	public static synchronized  OpenLdapManager getInstance() {
		if(g_sharedInstance == null) {
			g_sharedInstance = new OpenLdapManager();
		}
		
		return g_sharedInstance;
	}
	
	
	//Private constructor
	private OpenLdapManager() {
		m_logger = MyLogger.getSharedLogger();
	}
	
	
	public boolean openLdapConnection(String connect, boolean sslEnabled, String dn, String rootDN, String password, String truststore, String truststorePassword) {
		m_logger.logInfo(String.format("[OpenLdapManager::openLdapConnection] Connect: %s, DN: %s, Root DN: %s, SSL: %s, Truststore: %s", connect, dn, rootDN, String.valueOf(sslEnabled), truststore));
		boolean result = true;
		
		m_connect = connect;
		m_dn = dn;
		m_rootDN = rootDN;
		m_password = password;
		m_sslEnabled = sslEnabled;
		m_truststore = truststore;
		m_truststorePassword = truststorePassword;
		
		m_env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		m_env.put("java.naming.ldap.ref.separator", ":");
		
		if(m_sslEnabled == true) {
			
			OpenLdapSSLSocketFactory.setLdapTruststore(m_truststore, m_truststorePassword);
			m_env.put(Context.PROVIDER_URL, String.format("ldaps://%s", m_connect));
			m_env.put(Context.SECURITY_PROTOCOL, "ssl");
			m_env.put("java.naming.ldap.factory.socket", "com.example.openldapexample.OpenLdapSSLSocketFactory");
			
		} else {
			m_env.put(Context.PROVIDER_URL, String.format("ldap://%s", m_connect));
		}
		
		m_env.put(Context.SECURITY_AUTHENTICATION, "simple");
		m_env.put(Context.SECURITY_PRINCIPAL, m_rootDN);
		m_env.put(Context.SECURITY_CREDENTIALS, m_password);
		
		
		try {
			m_dircontext = new InitialDirContext(m_env);
		} catch (NamingException e) {
			m_logger.logError(String.format("[OpenLdapManager::openLdapConnection] Failed to connect to LDAP. Exception: %s", e.getMessage()));
			result = false;
		}
		
		return result;
	}
	
	private boolean reopenLdapConnection() {
		m_logger.logInfo("[OpenLdapManager::reopenLdapConnection]");
		boolean result = true;
		
		m_env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		m_env.put("java.naming.ldap.ref.separator", ":");
		
		if(m_sslEnabled == true) {
			
			OpenLdapSSLSocketFactory.setLdapTruststore(m_truststore, m_truststorePassword);
			m_env.put(Context.PROVIDER_URL, String.format("ldaps://%s", m_connect));
			m_env.put(Context.SECURITY_PROTOCOL, "ssl");
			m_env.put("java.naming.ldap.factory.socket", "com.example.openldapexample.cto.OpenLdapSSLSocketFactory");
			
		} else {
			m_env.put(Context.PROVIDER_URL, String.format("ldap://%s", m_connect));
		}
		
		m_env.put(Context.SECURITY_AUTHENTICATION, "simple");
		m_env.put(Context.SECURITY_PRINCIPAL, m_rootDN);
		m_env.put(Context.SECURITY_CREDENTIALS, m_password);
		
		try {
			m_dircontext = new InitialDirContext(m_env);
			
			m_logger.logInfo("[OpenLdapManager::reopenLdapConnection] Connection Successful");
			
		} catch (NamingException e) {
			m_logger.logError(String.format("[OpenLdapManager::reopenLdapConnection] Failed to connect to LDAP. Exception: %s", e.getMessage()));
			result = false;
		}
		
		return result;
	}
	
	public boolean closeLdapConnection() {
		m_logger.logInfo("[OpenLdapManager::closeLdapConnection]");
		boolean result = true;
		
		if(m_dircontext != null) {
			try {
				m_dircontext.close();
			} catch (NamingException e) {
				m_logger.logError(String.format("[OpenLdapManager::closeLdapConnection] Failed to close LDAP Connection. Exception: %s", e.getMessage()));
				result = false;
			}
		}
		
		return result;
	}
	
	
	public boolean addLdapEntry(String key, LdapEntry entry) {
		m_logger.logDebug(String.format("[OpenLdapManager::addLdapEntry] Terminal: %s, POS IP: %s, POS Port: %d", key, entry.getIP(), entry.getPort()));
		boolean result = true;

		if(m_dircontext != null) {
			try {
				m_dircontext.rebind(String.format("cn=%s,%s", key, m_dn), entry);
			} catch (NamingException e) {
				MyLogger.getSharedLogger().logError(String.format("[OpenLdapManager::addLdapEntry] Failed to add object to LDAP. Exception: %s", e.getMessage()));
				result = false;
			}
		} else {
			//Try reopen openldap connection
			result = false;
			reopenLdapConnection();
		}
		
		return result;
	}
	
	
	public LdapEntry getLdapEntry(String key) {
		LdapEntry entry = null;
		
		if(m_dircontext != null) {
			try {
				entry = (LdapEntry)m_dircontext.lookup(String.format("cn=%s,%s", key, m_dn));
			} catch (Exception e) {
				
				if(e instanceof CommunicationException) {
					m_logger.logWarn(String.format("[OpenLdapManager::getLdapEntry] LDAP Communication Exception. Exception: %s", e.getMessage()));
					
					//Try reopen openldap connection
					if(reopenLdapConnection() == true) {
						try {
							entry = (LdapEntry)m_dircontext.lookup(String.format("cn=%s,%s", key, m_dn));
						} catch (NamingException e1) {
							m_logger.logInfo(String.format("[OpenLdapManager::getLdapEntry] Failed to get object. Exception: %s", e1.getMessage()));
						}
					}
				} else {
					m_logger.logInfo(String.format("[OpenLdapManager::getLdapEntry] Failed to get object. Exception: %s", e.getMessage()));
				}
			}
		} else {
			//Try reopen openldap connection
			reopenLdapConnection();
		}
		
		return entry;
	}
	
	
}
