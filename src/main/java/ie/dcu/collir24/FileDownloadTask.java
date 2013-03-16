package ie.dcu.collir24;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

/**
 * Downloads a file
 * 
 * @author rcollins
 * 
 */
public class FileDownloadTask extends AbstractDownloadTask implements Runnable {

	private static final String DOWNLOAD_DIR = System
			.getProperty("download.dir");
	private static final String MAVEN_BASE = "http://repo1.maven.org/maven2/";
	/**
	 * The path to write the file to
	 */
	private final String filePath;

	/**
	 * 
	 * @param uriString
	 *            the full URL to the file
	 * @param httpClient
	 *            the {@link HttpClient} to use
	 */
	public FileDownloadTask(String uriString, HttpClient httpClient) {
		super(uriString, httpClient);
		filePath = uriString.replace(MAVEN_BASE, DOWNLOAD_DIR);
	}

	public void run() {
		LOGGER.info(uri.toString());
		getFile();
	}

	private void getFile() {
		HttpGet httpget = null;
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
					writeFileToDisk(is, filePath);
				} finally {
					if (is != null) {
						is.close();
					}
				}
				break;
			}
			case 404: {
				LOGGER.fine("Unable to download file at URI: " + uri);
				break;
			}
			default:
				LOGGER.warning("Unexpected response code when downloading file from URI: "
						+ uri);
			}
			EntityUtils.consume(entity);
		} catch (IOException e) {
			httpget.abort();
			LOGGER.log(Level.SEVERE, "Problem getting file from URI: " + uri, e);
		}
	}

	/**
	 * Creates and writes the signature file from the input
	 * 
	 * @param is
	 * @param signatureFile
	 */
	private static void writeFileToDisk(InputStream is, String path) {
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
	}

}
