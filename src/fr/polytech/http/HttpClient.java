package fr.polytech.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

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

		final OutputStream outputStream = socket.getOutputStream();
		final InputStream inputStream = socket.getInputStream();

		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

		final StringBuilder httpRequest = new StringBuilder();
		httpRequest.append(HttpMethods.GET);
		httpRequest.append(" ");
		httpRequest.append(resource);
		httpRequest.append(" ");
		httpRequest.append(HTTP_VERSION);
		httpRequest.append("\r\n");
		final byte[] httpRequestBytes = httpRequest.toString().getBytes();

		final byte[] requestBytes = Arrays.copyOf(httpRequestBytes, httpRequestBytes.length + 1);
		requestBytes[httpRequestBytes.length] = -1;

		bufferedOutputStream.write(requestBytes);
		bufferedOutputStream.flush();

		int readByte;
		final StringBuilder data = new StringBuilder();
		while (((readByte = bufferedInputStream.read()) != 255) || (bufferedInputStream.available() > 0))
		{
			if (readByte != 255)
			{
				data.append((char) readByte);
			}
		}

		final String[] parsedData = data.toString().split("\r\n\r\n");
		final String[] parsedAnswer = parsedData[0].split("\r\n");
		if (parsedAnswer[0].equals(HTTP_VERSION + " 200 OK"))
		{
			final String fileName = resource.substring(resource.lastIndexOf("/"), resource.length());
			final File resourceFile = new File(this.directory, fileName);
			Files.write(resourceFile.toPath(), parsedData[parsedData.length - 1].getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
		else
		{
			System.err.println(parsedAnswer[0]);
		}

		socket.close();
	}
}