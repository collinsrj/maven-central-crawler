package ie.dcu.collir24;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * Base class for HTTP downloads.
 * 
 * @author rcollins
 * 
 */
public abstract class AbstractDownloadTask {
	protected static final String MAVEN_BASE = "http://repo1.maven.org/maven2/";
	static final Logger LOGGER = Logger.getLogger(AbstractDownloadTask.class
			.getName());
	protected static final int MAX_BYTES = 10000000;// 10MB
	final URI uri;
	final HttpClient httpClient;
	final HttpContext httpContext;

	/**
	 * Create a download task
	 * 
	 * @param uriString
	 *            the URL of the file to download
	 * @param httpClient
	 *            an HTTP client instance
	 * @param filePath
	 *            the local file to write to
	 */
	public AbstractDownloadTask(final String uriString,
			final HttpClient httpClient) {
		try {
			this.uri = new URI(uriString);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Not a valid URL: " + uriString,
					e);
		}
		this.httpClient = httpClient;
		this.httpContext = new BasicHttpContext();
	}

}
