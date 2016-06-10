package fr.polytech.http.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
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
	 * The HTTP version.
	 */
	public static final String HTTP_VERSION = "HTTP/1.1";

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

		final PrintWriter outputStreamWriter = new PrintWriter(socket.getOutputStream(), true);
		final BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		final String httpRequest = String.format("%s %s %s\r\n", HttpMethods.GET, resource, HTTP_VERSION);
		outputStreamWriter.print(httpRequest);
		outputStreamWriter.print(-1);

		int data;
		final StringBuilder dataBuilder = new StringBuilder();
		while ((data = inputStreamReader.read()) != -1)
		{
			dataBuilder.append(data);
		}

		final String answer = dataBuilder.toString();
		final String[] parsedAnswer = answer.split("\r\n\r\n");
		final String[] parsedRequest = parsedAnswer[0].split("\r\n");
		final String resourceData = parsedAnswer[parsedAnswer.length - 1];

		if (parsedRequest[0].equals(HTTP_VERSION + " 200 OK"))
		{
			Files.write(new File(this.directory, resource).toPath(), resourceData.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
		else
		{
			System.err.println(parsedRequest[0]);
		}

		socket.close();
	}
}