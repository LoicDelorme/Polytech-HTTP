package fr.polytech.http.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * This class represents an HTTP server worker.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class HttpServerWorker implements Runnable
{
	/**
	 * The client socket.
	 */
	private final Socket socket;

	/**
	 * The resources directory.
	 */
	private File directory;

	/**
	 * Create an HTTP server worker.
	 * 
	 * @param socket
	 *            The client socket.
	 * @param directory
	 *            The resources directory.
	 */
	public HttpServerWorker(Socket socket, File directory)
	{
		this.socket = socket;
		this.directory = directory;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			final PrintWriter outputStreamWriter = new PrintWriter(this.socket.getOutputStream(), true);
			final BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

			while (true)
			{
				String data;
				final StringBuilder dataBuilder = new StringBuilder();
				while (((data = inputStreamReader.readLine()) != null) && (!"-1".equals(data)))
				{
					dataBuilder.append(data);
				}

				if (dataBuilder.length() == 0)
				{
					break;
				}

				final String request = dataBuilder.toString();
				final String[] parsedRequest = request.split(" ");

				if (parsedRequest.length != 3)
				{
					outputStreamWriter.println(String.format("%s 400 Bad Request\r\n", HttpServer.HTTP_VERSION));
					outputStreamWriter.println("-1");
					continue;
				}

				if (!HttpServer.HTTP_VERSION.equals(parsedRequest[2]))
				{
					outputStreamWriter.println(String.format("%s 505 HTTP Version not supported\r\n", HttpServer.HTTP_VERSION));
					outputStreamWriter.println("-1");
					continue;
				}

				final File resourceFile = new File(this.directory, parsedRequest[1]);
				if (!resourceFile.exists())
				{
					outputStreamWriter.println(String.format("%s 404 Not Found\r\n", HttpServer.HTTP_VERSION));
					outputStreamWriter.println("-1");
					continue;
				}

				switch (parsedRequest[0])
				{
					case "GET":
						final byte[] requestedResourceData = Files.readAllBytes(resourceFile.toPath());
						final byte[] formatedAnswer = String.format("%s 200 OK\r\nContent-Length: %d\r\nContent-Type: %s\r\n\r\n", HttpServer.HTTP_VERSION, requestedResourceData.length, "text/html").getBytes();

						final byte[] finalAnswer = Arrays.copyOf(formatedAnswer, formatedAnswer.length + requestedResourceData.length);
						for (int offset = 0; offset < requestedResourceData.length; offset++)
						{
							finalAnswer[formatedAnswer.length + offset] = requestedResourceData[offset];
						}

						outputStreamWriter.println(new String(finalAnswer));
						outputStreamWriter.println("-1");
						break;
					default:
						outputStreamWriter.println(String.format("%s 405 Method Not Allowed\r\n", HttpServer.HTTP_VERSION));
						outputStreamWriter.println("-1");
						break;
				}
			}

			this.socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}