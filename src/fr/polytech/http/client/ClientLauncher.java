package fr.polytech.http.client;

import java.io.File;
import java.net.InetAddress;

/**
 * This class represents an HTTP client launcher.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class ClientLauncher
{
	/**
	 * The default server port.
	 */
	public static final int DEFAULT_SERVER_PORT = 1026;

	/**
	 * The default directory.
	 */
	public static final File DEFAULT_DIRECTORY = new File("D:/Downloads/TEMP/client/");

	/**
	 * The entry of the application.
	 * 
	 * @param args
	 *            Some arguments.
	 * @throws Exception
	 *             If an error occurred.
	 */
	public static void main(String[] args) throws Exception
	{
		final HttpClient client = new HttpClient(DEFAULT_DIRECTORY);
		client.executeGet("/index.html", InetAddress.getByName("localhost"), DEFAULT_SERVER_PORT);
	}
}