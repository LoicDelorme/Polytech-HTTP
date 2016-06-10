package fr.polytech.http.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class represents an HTTP server.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class HttpServer implements Runnable
{
	/**
	 * The HTTP version.
	 */
	public static final String HTTP_VERSION = "HTTP/1.1";

	/**
	 * The port.
	 */
	private final int port;

	/**
	 * The number of connection.
	 */
	private final int nbConnection;

	/**
	 * The resources directory.
	 */
	private File directory;

	/**
	 * Create an HTTP server.
	 * 
	 * @param port
	 *            The port.
	 * @param nbConnection
	 *            The number of connection.
	 * @param directory
	 *            The resources directory.
	 */
	public HttpServer(int port, int nbConnection, File directory)
	{
		this.port = port;
		this.nbConnection = nbConnection;
		this.directory = directory;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		ServerSocket socket = null;
		try
		{
			socket = new ServerSocket(this.port, this.nbConnection);
			while (true)
			{
				final Socket clientSocket = socket.accept();
				final HttpServerWorker httpServerWorker = new HttpServerWorker(clientSocket, this.directory);
				httpServerWorker.run();
			}
		}
		catch (Exception e)
		{
			try
			{
				socket.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}

			e.printStackTrace();
		}
	}
}