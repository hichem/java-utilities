package com.example.sslserver;

public class Main {

	
	public static void main(String[] args) {
		
		MyLogger.getSharedLogger().logInfo("[Main::main] App started");
		
		//Initialize Global Config
		GlobalConfig.getInstance();
		
		
		//Set SSL Debugging Option
		if(GlobalConfig.getInstance().getSSLDebugging() == true) {
			System.setProperty("javax.net.debug","all");
		}
		//Create SSL Server Thread
		Thread sslServerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				//Start SSL Server
				SslServer sslServer = SslServer.getInstance();
				sslServer.setPortNumber(GlobalConfig.getInstance().getTerminaServerPort());
				sslServer.startServer();
			}
		});
		
		//sslServerThread.setPriority(Thread.MAX_PRIORITY);
		sslServerThread.setName("SslServerThread");
		
		//Start the SSL server thread
		sslServerThread.start();
		
		//Join the SSL server thread
		try {
			sslServerThread.join();
		} catch (InterruptedException e) {
			MyLogger.getSharedLogger().logError(String.format("[Main::main] Error on thread join. Exception: %s", e.getMessage()));
		}
	}

}
