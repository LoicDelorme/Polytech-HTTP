package fr.polytech.http;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * This class represents
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

	public static void main(String[] args) throws IOException
	{
		final HttpClient client = new HttpClient(DEFAULT_DIRECTORY);
		client.executeGet("localhost:1026/index.html", InetAddress.getByName("localhost"), DEFAULT_SERVER_PORT);
	}
}