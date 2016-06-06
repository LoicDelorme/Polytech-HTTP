package fr.polytech.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
			final InputStream inputStream = this.socket.getInputStream();
			final OutputStream outputStream = this.socket.getOutputStream();

			final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

			while (this.socket.isConnected())
			{
				int readByte;
				final int newLineCharacterCode = 10;
				final StringBuilder data = new StringBuilder();
				while (true)
				{
					readByte = bufferedInputStream.read();
					data.append((char) readByte);

					if (readByte == newLineCharacterCode)
					{
						break;
					}
				}

				final String initialRequest = data.toString();
				final String[] parsedRequest = initialRequest.substring(0, initialRequest.length() - 2).split(" ");

				if (parsedRequest.length != 3)
				{
					bufferedOutputStream.write(String.format("%s 400 Bad Request\r\n", HttpServer.HTTP_VERSION).getBytes());
					bufferedOutputStream.flush();
					continue;
				}

				if (!sameHttpVersion(parsedRequest[2]))
				{
					bufferedOutputStream.write(String.format("%s 505 HTTP Version not supported\r\n", HttpServer.HTTP_VERSION).getBytes());
					bufferedOutputStream.flush();
					continue;
				}

				switch (parsedRequest[0])
				{
					case "GET":
						if (!resourceExists(parsedRequest[1]))
						{
							bufferedOutputStream.write(String.format("%s 404 Not Found\r\n", HttpServer.HTTP_VERSION).getBytes());
							bufferedOutputStream.flush();
						}
						else
						{
							final byte[] readData = readResource(parsedRequest[1]);

							final StringBuilder answer = new StringBuilder();
							answer.append(HttpServer.HTTP_VERSION);
							answer.append(" ");
							answer.append("200 OK");
							answer.append("\r\n");

							answer.append("Content-Length: ");
							answer.append(readData.length);
							answer.append("\r\n");

							answer.append("Content-Type: ");
							answer.append("text/html");
							answer.append("\r\n");

							answer.append("\r\n");

							final byte[] answerBegin = answer.toString().getBytes();

							final byte[] finalData = new byte[answerBegin.length + readData.length];
							for (int index = 0; index < answerBegin.length; index++)
							{
								finalData[index] = answerBegin[index];
							}

							for (int index = 0; index < readData.length; index++)
							{
								finalData[answerBegin.length + index] = readData[index];
							}

							bufferedOutputStream.write(finalData);
							bufferedOutputStream.flush();
						}
						break;
					default:
						bufferedOutputStream.write(String.format("%s 405 Method Not Allowed\r\n", HttpServer.HTTP_VERSION).getBytes());
						bufferedOutputStream.flush();
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
	 * Read resource.
	 * 
	 * @param resource
	 *            The resource path.
	 * @return The read data.
	 */
	private byte[] readResource(String resource)
	{
		try
		{
			final String parsedResource = resource.substring(resource.lastIndexOf("/"), resource.length());
			final File resourceFile = new File(this.directory, parsedResource);
			return Files.readAllBytes(resourceFile.toPath());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Check if the resource exists.
	 * 
	 * @param resource
	 *            The resource path.
	 * @return True or False.
	 */
	private boolean resourceExists(String resource)
	{
		final String parsedResource = resource.substring(resource.lastIndexOf("/"), resource.length());
		final File resourceFile = new File(this.directory, parsedResource);

		return resourceFile.exists();
	}

	/**
	 * Check if the client's HTTP version is the same that the server.
	 * 
	 * @param httpVersion
	 *            The HTTP version.
	 * @return True or False.
	 */
	private boolean sameHttpVersion(String httpVersion)
	{
		return (httpVersion.compareTo(HttpServer.HTTP_VERSION) == 0);
	}
}