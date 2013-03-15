package ie.dcu.collir24;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.Callable;
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
public class MetadataVersionTask extends AbstractDownloadTask implements
		Callable<String> {

	public MetadataVersionTask(String uriString, HttpClient httpClient) {
		super(uriString, httpClient);
	}

	public String call() throws Exception {
		String metadata = getMetadata();
		return getVersionToDownload(metadata);
	}

	protected static String getVersionToDownload(final String metaData)
			throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
		Reader meatdataReader = new StringReader(metaData);
		try {
			Document doc = builder.build(new StringReader(metaData));
			Element metadata = doc.getRootElement();
			Namespace ns = metadata.getNamespace();
			return getVersioningLatest(metadata, ns);
		} finally {
			meatdataReader.close();
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
