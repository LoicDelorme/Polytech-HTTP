package fr.polytech.http;

import java.io.File;

/**
 * This class represents a server launcher.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class ServerLauncher
{
	/**
	 * The default port.
	 */
	public static final int DEFAULT_PORT = 1026;

	/**
	 * The default number of connection.
	 */
	public static final int DEFAULT_NB_CONNECTIONS = 6;

	/**
	 * The default directory.
	 */
	public static final File DEFAULT_DIRECTORY = new File("D:/Downloads/TEMP/server/");

	/**
	 * The entry of the application.
	 * 
	 * @param args
	 *            Some arguments.
	 */
	public static void main(String[] args)
	{
		final HttpServer server = new HttpServer(DEFAULT_PORT, DEFAULT_NB_CONNECTIONS, DEFAULT_DIRECTORY);
		server.run();
	}
}