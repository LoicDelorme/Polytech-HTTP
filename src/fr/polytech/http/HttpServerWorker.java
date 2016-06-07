package fr.polytech.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
			final InputStream inputStream = this.socket.getInputStream();
			final OutputStream outputStream = this.socket.getOutputStream();

			final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

			while (true)
			{
				final StringBuilder data = new StringBuilder();
				int readByte;
				while (((readByte = bufferedInputStream.read()) != 255) && (readByte != -1))
				{
					data.append((char) readByte);
				}

				if (readByte == -1)
				{
					break;
				}

				final String initialRequest = data.toString();
				final String[] parsedRequest = initialRequest.substring(0, initialRequest.length() - 2).split(" ");

				if (parsedRequest.length != 3)
				{
					bufferedOutputStream.write(String.format("%s 400 Bad Request\r\n", HttpServer.HTTP_VERSION).getBytes());
					bufferedOutputStream.flush();
					continue;
				}

				if (!HttpServer.HTTP_VERSION.equals(parsedRequest[2]))
				{
					bufferedOutputStream.write(String.format("%s 505 HTTP Version not supported\r\n", HttpServer.HTTP_VERSION).getBytes());
					bufferedOutputStream.flush();
					continue;
				}

				final String parsedResource = parsedRequest[1].substring(parsedRequest[1].lastIndexOf("/"), parsedRequest[1].length());
				final File resourceFile = new File(this.directory, parsedResource);
				if (!resourceFile.exists())
				{
					bufferedOutputStream.write(String.format("%s 404 Not Found\r\n", HttpServer.HTTP_VERSION).getBytes());
					bufferedOutputStream.flush();
					continue;
				}

				switch (parsedRequest[0])
				{
					case "GET":
						final byte[] resource = Files.readAllBytes(resourceFile.toPath());

						final StringBuilder httpAnswer = new StringBuilder();
						httpAnswer.append(HttpServer.HTTP_VERSION);
						httpAnswer.append(" ");
						httpAnswer.append("200 OK");
						httpAnswer.append("\r\n");
						httpAnswer.append("Content-Length: ");
						httpAnswer.append(resource.length);
						httpAnswer.append("\r\n");
						httpAnswer.append("Content-Type: ");
						httpAnswer.append("text/html");
						httpAnswer.append("\r\n");
						httpAnswer.append("\r\n");
						final byte[] httpAnswerBytes = httpAnswer.toString().getBytes();

						final byte[] answer = Arrays.copyOf(httpAnswerBytes, httpAnswerBytes.length + resource.length + 1);
						for (int index = 0; index < resource.length; index++)
						{
							answer[httpAnswerBytes.length + index] = resource[index];
						}
						answer[httpAnswerBytes.length + resource.length] = -1;

						bufferedOutputStream.write(answer);
						bufferedOutputStream.flush();
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
}