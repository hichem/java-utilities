package com.example.sslserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLSocket;


public class ClientSocket implements Runnable {

	public enum ErrorCode {
		SUCCESS,
		ERROR,
		ERROR_SCREEN_ID,
		ERROR_CLIENT_DISCONNECTED,
		PING
	}

	//private final int 		CLIENT_READ_TIMEOUT		= 15000;	//15000 ms
	private final int 		SSL_HANDSHAKE_TIMEOUT				= 15000;	//15000 ms

	//Variables
	private Socket			m_clientSocket		= null;
	private MyLogger		m_logger				= null;
	private BufferedReader 	m_inReader = null;
	private BufferedWriter 	m_outWriter = null;


	public ClientSocket(Socket clientSocket) {
		m_logger = MyLogger.getSharedLogger();

		if(m_logger.isDebugEnabled()) {
			m_logger.logDebug(String.format("[ClientSocket::ClientSocket] Received client connection from IP: %s", clientSocket.getRemoteSocketAddress()));
		}
		m_clientSocket = clientSocket;
	}

	public ClientSocket() {
		m_logger = MyLogger.getSharedLogger();
	}

	public void initSocket(Socket clientSocket) {

		if(m_logger.isDebugEnabled()) {
			m_logger.logDebug(String.format("[ClientSocket::initSocket] Received client connection from IP: %s", clientSocket.getRemoteSocketAddress()));
		}
		m_clientSocket = clientSocket;
		try {
			m_clientSocket.setTcpNoDelay(true);
		} catch (SocketException e) {
			m_logger.logWarn(String.format("[ClientSocket::initSocket] Failed to set Socket TCP No Delay Parameter. Erro: %s", e.getMessage()));
		}
	}


	public Socket getTerminalSocket() {
		return m_clientSocket;
	}


	@Override
	public void run() {

		if(m_logger.isDebugEnabled()) {
			m_logger.logDebug("[ClientSocket::run] Terminal Job started");
		}

		//ErrorCode code;

		boolean onHandshakeFailed = false;

		//Perform SSL Handshake if SSL is enabled
		if(GlobalConfig.getInstance().getSSL() == true) {
			//Make handshake synchronous
			try {

				//Configure socket read timeout for terminal ssl handshake
				m_clientSocket.setSoTimeout(SSL_HANDSHAKE_TIMEOUT);

				//Start TLS Handshake
				((SSLSocket)m_clientSocket).startHandshake();

			} catch (IOException e) {
				if(m_logger.isDebugEnabled()) {
					m_logger.logDebug(String.format("[ClientSocket::run] SSL Handshake failed. Exception: %s", e.getMessage()));
				}
				onHandshakeFailed = true;
			}
		}

		if(onHandshakeFailed == false) {

			//process client request
			processClientRequest();

		}

		logDebug("[ClientSocket::run] Client Socket  exit");
	}


	private ErrorCode processClientRequest() {
		m_logger.logDebug("[ClientSocket::processClientRequest]");

		ErrorCode result = ErrorCode.SUCCESS;
		String line = null;

		try {

			//Configure socket read timeout
			//m_terminalSocket.setSoTimeout(CLIENT_READ_TIMEOUT);

			m_inReader 	= new BufferedReader(new InputStreamReader(m_clientSocket.getInputStream()));
			m_outWriter 	= new BufferedWriter(new OutputStreamWriter(m_clientSocket.getOutputStream()));

			while(true) {
				line = m_inReader.readLine();

				if(line != null) {

					logInfo("Received: " + line);
					
					m_outWriter.write(line + "\n");
					m_outWriter.flush();

				} else {
					logDebug(String.format("[ClientSocket::processClientRequest] Client Disconnected. Remote IP: %s", m_clientSocket.getRemoteSocketAddress()));
					result = ErrorCode.ERROR_CLIENT_DISCONNECTED;
					break;
				}
			}

		} catch (SocketTimeoutException e) {
			logDebug("[ClientSocket::processClientRequest] Request timeout");
			result = ErrorCode.ERROR;
		} catch (IOException e) {
			logDebug(String.format("[ClientSocket::processClientRequest] Client disconnected. Exception: %s", e.getMessage()));
			result = ErrorCode.ERROR;
		}

		//Resource Clean Up
		try {
			if(m_clientSocket != null) {
				m_clientSocket.close();
			}
		} catch (IOException e) {
			logError(String.format("[ClientSocket::processClientRequest] Error when closing the terminal connection: %s", e.getMessage()));
		}

		return result;
	}


	private void logInfo(String log) {
		m_logger.logInfo(log);
	}

	private void logDebug(String log) {
		if(m_logger.isDebugEnabled()) {
			m_logger.logDebug(log);
		}
	}

	@SuppressWarnings("unused")
	private void logWarn(String log) {
		m_logger.logWarn(log);
	}

	private void logError(String log) {
		m_logger.logError(log);
	}
}
