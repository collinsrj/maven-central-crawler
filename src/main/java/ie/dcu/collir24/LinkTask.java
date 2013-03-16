package ie.dcu.collir24;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Given a URL, follow that downloading as appropriate and creating new
 * {@link LinkTask}s as required.
 * 
 * @author rcollins
 * 
 */
public class LinkTask extends AbstractDownloadTask implements Runnable {
	private final ExecutorService exec;

	public LinkTask(final String uriString, final HttpClient httpClient,
			ExecutorService exec) {
		super(uriString, httpClient);
		this.exec = exec;
	}

	private static final Pattern DOWNLOAD_PATTERN = Pattern
			.compile("maven-metadata.xml$|[^javadoc|sources|test\\-sources|tests].jar$|.pom$|.pom.asc$|[^javadoc|sources|test\\-sources|tests].jar.asc$");

	public void run() {
		String listing;
		try {
			listing = getListing();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Problem with URI: " + uri.toString(), e);
			return;
		}
		List<String>[] listingDetails = parseListing(listing, uri.toString());
		List<String> filesToDownload = listingDetails[0];
		List<String> linksToFollow = listingDetails[1];
		String latestVersion = getLatestVersion(filesToDownload);
		processListing(filesToDownload, linksToFollow, latestVersion);
	}

	private String getLatestVersion(List<String> filesToDownload) {
		String latestVersion = null;
		String metadata = containsMetadata(filesToDownload);
		if (metadata != null) {
			Future<String> latestVersionFuture = exec
					.submit(new MetadataVersionTask(metadata, httpClient));
			try {
				latestVersion = latestVersionFuture.get();
			} catch (InterruptedException e) {
				Thread.interrupted();
			} catch (ExecutionException e) {
				LOGGER.log(Level.WARNING, "Problem retrieving metadata.", e);
			}
		}
		return latestVersion;
	}

	/**
	 * Checks
	 * 
	 * @return
	 */
	private static String containsMetadata(List<String> filesToDownload) {
		for (String file : filesToDownload) {
			if (file.endsWith("maven-metadata.xml")) {
				return file;
			}
		}
		return null;
	}

	private void processListing(List<String> filesToDownload,
			List<String> linksToFollow, String latestVersion) {
		if (latestVersion == null) {
			for (String fileToDownload : filesToDownload) {
				exec.submit(new FileDownloadTask(fileToDownload, httpClient));
			}
			for (String linkToFollow : linksToFollow) {
				exec.submit(new LinkTask(linkToFollow, httpClient, exec));
			}
		} else {
			exec.submit(new LinkTask(uri + latestVersion + "/", httpClient,
					exec));
		}
	}

	private String getListing() throws ClientProtocolException, IOException {
		HttpGet httpget = new HttpGet(uri);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		return httpClient.execute(httpget, responseHandler);
	}

	@SuppressWarnings("unchecked")
	protected static List<String>[] parseListing(String listing,
			String completeUrl) {
		List<String> filesToDownload = new ArrayList<String>();
		List<String> linksToFollow = new ArrayList<String>();
		Document doc = Jsoup.parse(listing, completeUrl);
		Elements links = doc.select("a[href]");
		ListIterator<Element> linksIterator = links.listIterator();
		while (linksIterator.hasNext()) {
			Element link = linksIterator.next();
			String href = link.absUrl("href");

			if (isParent(completeUrl, href)) {
				continue;
			}
			if (href.endsWith("/")) {
				linksToFollow.add(href);
				continue;
			}
			Matcher matcher = DOWNLOAD_PATTERN.matcher(href);
			if (matcher.find()) {
				filesToDownload.add(href);
			}
		}
		return new List[] { filesToDownload, linksToFollow };
	}

	/**
	 * Checks to see if one String is the parent of another
	 * 
	 * @param link
	 *            a link e.g. http://example.com/1/
	 * @param parent
	 *            the test parent e.g. http://example.com/
	 * @return true if 'parent' is the parent of link
	 */
	protected static boolean isParent(final String link, final String parent) {
		File linkFile = new File(link);
		File parentFile = new File(parent);
		return linkFile.getParentFile().equals(parentFile);
	}

}
