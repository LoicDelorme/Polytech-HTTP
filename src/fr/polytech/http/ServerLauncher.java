package fr.polytech.http;

import java.io.File;
import java.io.IOException;

/**
 * This class represents a server launcher.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class ServerLauncher
{
	/**
	 * The number of connection.
	 */
	public static final int NB_CONNECTIONS = 6;

	/**
	 * The default directory.
	 */
	public static final File DEFAULT_DIRECTORY = new File("D:/Downloads/TEMP/server/");

	public static void main(String[] args) throws IOException
	{
		final HttpServer server = new HttpServer(NB_CONNECTIONS, DEFAULT_DIRECTORY);
		server.run();
	}
}