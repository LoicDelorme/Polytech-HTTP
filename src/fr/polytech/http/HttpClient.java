package fr.polytech.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class represents an HTTP client.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class HttpClient
{
	public static final int DEFAULT_PORT = 1026;

	public byte[] executeGet(String targetURL, int port)
	{
		try
		{
			final Socket socket = new Socket(targetURL, port);

			final OutputStream outputStream = socket.getOutputStream();
			final InputStream inputStream = socket.getInputStream();

			// generate get.
			outputStream.write(b);
			inputStream.read(b);

			return;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}