package fr.polytech.http.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import fr.polytech.http.HttpMethods;

/**
 * This class represents an HTTP client.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class HttpClient
{
	/**
	 * The client HTTP version.
	 */
	public static final String CLIENT_HTTP_VERSION = "HTTP/1.1";

	/**
	 * The directory to save files.
	 */
	private final File directory;

	/**
	 * Create an HTTP client.
	 * 
	 * @param directory
	 *            The directory to save files.
	 */
	public HttpClient(File directory)
	{
		this.directory = directory;
	}

	/**
	 * Execute GET method.
	 * 
	 * @param resource
	 *            The resource.
	 * @param address
	 *            The server address.
	 * @param port
	 *            The target port.
	 * @throws IOException
	 *             If an error occurred.
	 */
	public void executeGet(String resource, InetAddress address, int port) throws IOException
	{
		final Socket socket = new Socket(address, port);

		final boolean autoFlush = true;
		final PrintWriter outputStreamWriter = new PrintWriter(socket.getOutputStream(), autoFlush);
		final BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		outputStreamWriter.println(String.format("%s %s %s", HttpMethods.GET, resource, CLIENT_HTTP_VERSION));
		outputStreamWriter.println("Connection: Close");
		outputStreamWriter.println();

		int data;
		final StringBuilder dataBuilder = new StringBuilder();
		while (true)
		{
			if (inputStreamReader.ready())
			{
				while ((data = inputStreamReader.read()) != -1)
				{
					dataBuilder.append((char) data);
				}

				break;
			}
		}

		final String answer = dataBuilder.toString();
		final String[] headersAndContent = answer.split("\r\n\r\n");
		final String[] headers = headersAndContent[0].split("\r\n");
		final String content = headersAndContent[1];

		if (headers[0].equals(CLIENT_HTTP_VERSION + " 200 OK") || headers[0].equals(CLIENT_HTTP_VERSION + " 302 Found"))
		{
			final Path path = new File(this.directory, resource).toPath();
			Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			System.out.println(String.format("The file %s was writen into %s folder", resource, this.directory));
		}
		else
		{
			System.err.println(headers[0]);
		}

		socket.close();
	}
}