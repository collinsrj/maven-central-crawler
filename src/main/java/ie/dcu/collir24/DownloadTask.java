package ie.dcu.collir24;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class DownloadTask implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(DownloadTask.class
			.getName());
	private static final int MAX_BYTES = 10000000;// 10MB
	private final URI uri;
	private final HttpClient httpClient;
	private final HttpContext httpContext;
	private final String filePath;

	/**
	 * Create a download task
	 * 
	 * @param uriString the URL of the file to download
	 * @param httpClient an HTTP client instance
	 * @param filePath the local file to write to
	 */
	public DownloadTask(final String uriString, final HttpClient httpClient,
			final String filePath) {
		try {
			this.uri = new URI(uriString);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Not a valid URL: " + uriString,
					e);
		}
		this.httpClient = httpClient;
		this.httpContext = new BasicHttpContext();
		this.filePath = filePath;
	}

	public void run() {
		getFile();
	}

	private File getFile() {
		HttpGet httpget = null;
		File newFile = null;
		try {
			httpget = new HttpGet(uri);
			HttpResponse response = httpClient.execute(httpget, httpContext);
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();

			switch (statusCode) {
			case 200: {
				InputStream is = null;
				try {
					is = response.getEntity().getContent();
					newFile = writeFileToDisk(is, filePath);
				} finally {
					if (is != null) {
						is.close();
					}
				}
				break;
			}
			case 404: {
				LOGGER.fine("Didn't find signature file on Maven Central at URI: "
						+ uri);
				break;
			}
			default:
				LOGGER.warning("Unexpected response code when downloading signature file from URI: "
						+ uri);
			}
			EntityUtils.consume(entity);

		} catch (ClientProtocolException cpe) {
			httpget.abort();
			LOGGER.log(Level.SEVERE,
					"Problem getting signature file from URI: " + uri, cpe);
		} catch (IOException e) {
			httpget.abort();
			LOGGER.log(Level.SEVERE,
					"Problem getting signature file from URI: " + uri, e);
		}
		return newFile;
	}

	/**
	 * Creates and writes the signature file from the input
	 * 
	 * @param is
	 * @param signatureFile
	 */
	private static File writeFileToDisk(InputStream is, String path) {
		File newFile = new File(path);
		File parentDirectory = newFile.getParentFile();
		if (!parentDirectory.exists()) {
			parentDirectory.mkdirs();
		}
		ReadableByteChannel inputChannel = Channels.newChannel(is);
		FileOutputStream os = null;
		try {
			newFile.createNewFile();
			os = new FileOutputStream(newFile);
			FileChannel outputChannel = os.getChannel();
			outputChannel.transferFrom(inputChannel, 0, MAX_BYTES);

		} catch (FileNotFoundException e) {
			throw new RuntimeException("Couldn't find file to write at path: "
					+ path, e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Problem writing file: " + path, e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					LOGGER.warning("Couldn't close output stream for file: "
							+ path);
				}
			}
		}
		return newFile;
	}
}
