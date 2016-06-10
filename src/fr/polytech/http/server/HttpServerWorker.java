package fr.polytech.http.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;

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
			final boolean autoFlush = true;
			final PrintWriter outputStreamWriter = new PrintWriter(this.socket.getOutputStream(), autoFlush);
			final BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

			boolean isRunning = true;
			while (isRunning)
			{
				String data;
				final StringBuilder dataBuilder = new StringBuilder();
				while (((data = inputStreamReader.readLine()) != null) && (!data.isEmpty()))
				{
					dataBuilder.append(data);
				}

				final String incomingRequest = dataBuilder.toString();
				if (incomingRequest.isEmpty())
				{
					break;
				}

				final String[] parsedIncomingRequest = incomingRequest.split(" ");
				if (parsedIncomingRequest.length != 3)
				{
					outputStreamWriter.println(String.format("%s 400 Bad Request", HttpServer.SERVER_HTTP_VERSION));
					outputStreamWriter.println();
					break;
				}

				if (!HttpServer.SERVER_HTTP_VERSION.equals(parsedIncomingRequest[2]))
				{
					outputStreamWriter.println(String.format("%s 505 HTTP Version not supported", HttpServer.SERVER_HTTP_VERSION));
					outputStreamWriter.println();
					break;
				}

				final File resource = new File(this.directory, parsedIncomingRequest[1]);
				if (!resource.exists())
				{
					outputStreamWriter.println(String.format("%s 404 Not Found", HttpServer.SERVER_HTTP_VERSION));
					outputStreamWriter.println();
					break;
				}

				switch (parsedIncomingRequest[0])
				{
					case "GET":
						final byte[] resourceData = Files.readAllBytes(resource.toPath());

						outputStreamWriter.println(String.format("%s 200 OK", HttpServer.SERVER_HTTP_VERSION));
						outputStreamWriter.println(String.format("Content-Length: %d", resourceData.length));
						outputStreamWriter.println(String.format("Content-Type: %s", getContentType(resource)));
						outputStreamWriter.println();
						outputStreamWriter.println(new String(resourceData, Charset.forName("UTF-8")));
						outputStreamWriter.println();
						isRunning = false;
						break;
					default:
						outputStreamWriter.println(String.format("%s 405 Method Not Allowed", HttpServer.SERVER_HTTP_VERSION));
						outputStreamWriter.println();
						isRunning = false;
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

	/**
	 * Get the content type according to the resource's extension.
	 * 
	 * @param resource
	 *            The resource.
	 * @return The corresponding content type.
	 */
	private String getContentType(File resource)
	{
		final String resourceName = resource.getName();
		final String extension = resourceName.substring(resourceName.lastIndexOf("."), resourceName.length());
		switch (extension)
		{
			case ".txt":
				return "text/plain";
			case ".html":
				return "text/html";
			case ".png":
				return "image/png";
			case ".jpg":
				return "image/jpg";
			default:
				return "";
		}
	}
}