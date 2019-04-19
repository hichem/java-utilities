package com.example.openldapexample;

import java.io.Serializable;

public class LdapEntry implements Serializable {
	
	private static final long serialVersionUID = 10278590390598034L;
	private String value1;
	private int value2;
	
	public LdapEntry() {
		
	}
	
	public LdapEntry(String ip, int port) {
		value1 = ip;
		value2 = port;
	}
	
	public String getIP() {
		return value1;
	}
	
	public int getPort() {
		return value2;
	}
	
	public void setIP(String ip) {
		value1 = ip;
	}
	
	public void setPort(int port) {
		value2 = port;
	}
	
	@Override
	public String toString() {
		return String.format("%s:%d", value1, value2);
	}
}