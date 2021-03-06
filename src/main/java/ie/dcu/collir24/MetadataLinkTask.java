package ie.dcu.collir24;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

/**
 * Gets the latest version if it is available from the metadata file.
 * 
 * @author rcollins
 * 
 */
public class MetadataLinkTask extends LinkTask implements Runnable {
	private final List<String> filesToDownload;
	private final List<String> linksToFollow;
	private final String uriString;
	private static final int METADATA_FILENAME_LENGTH = "maven-metadata.xml"
			.length();

	public MetadataLinkTask(final String uriString,
			final List<String> filesToDownload,
			final List<String> linksToFollow, HttpClient httpClient,
			ExecutorService exec) {
		super(uriString, httpClient, exec);
		this.uriString = uriString;
		this.filesToDownload = filesToDownload;
		this.linksToFollow = linksToFollow;
	}

	public void run() {
		System.out
				.println("m:" + uri.toString().substring(MAVEN_BASE.length()));
		String versionToDownload = getVersionToDownload(getMetadata());
		if (versionToDownload == null || versionToDownload.isEmpty()) {
			processListing(filesToDownload, linksToFollow);
		} else {
			// the URI is pointing to the metadata file
			String basePath = uriString.substring(0, uriString.length()
					- METADATA_FILENAME_LENGTH);

			String linkToDownload = new StringBuilder(basePath)
					.append(versionToDownload).append("/").toString();
			exec.submit(new LinkTask(linkToDownload, httpClient, exec));
		}
	}

	protected static String getVersionToDownload(final String metaData) {
		SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
		Reader meatdataReader = new StringReader(metaData);
		try {
			Document doc = builder.build(new StringReader(metaData));
			Element metadata = doc.getRootElement();
			Namespace ns = metadata.getNamespace();
			return getVersioningLatest(metadata, ns);
		} catch (JDOMException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			try {
				meatdataReader.close();
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Unable to close reader.", e);
			}
		}
	}

	/**
	 * Get the version from the /metadata/versioning/latest tag if it exists
	 * 
	 * @param metadata
	 * @param ns
	 * @return the version or null if it isn't found
	 */
	private static String getVersioningLatest(final Element metadata,
			final Namespace ns) {
		String version;
		Element versioning = metadata.getChild("versioning", ns);
		if (versioning == null) {
			version = metadata.getChildText("version", ns);
		} else if (versioning.getChild("latest", ns) == null) {
			Element versions = versioning.getChild("versions", ns);
			version = versions.getChildText("version", ns);
		} else {
			version = versioning.getChildText("latest", ns);
		}
		return version;
	}

	/**
	 * Get the metadata file from Maven Central
	 * 
	 * @return
	 */
	private String getMetadata() {
		HttpGet httpget = null;
		try {
			httpget = new HttpGet(uri);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			return httpClient.execute(httpget, responseHandler);
		} catch (ClientProtocolException cpe) {
			httpget.abort();
			LOGGER.log(Level.SEVERE, "Problem getting file from URI: " + uri,
					cpe);
		} catch (IOException e) {
			httpget.abort();
			LOGGER.log(Level.SEVERE, "Problem getting file from URI: " + uri, e);
		}
		return "";
	}

}
