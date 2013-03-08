package ie.dcu.collir24;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseListingTask implements Runnable {
	private static final Logger LOGGER = Logger
			.getLogger(ParseListingTask.class.getName());
	private final HttpClient httpClient;
	private final String baseUrl;
	private final String path;
	private final Queue<String> pathsQueue;
	private final Downloader downloader;

	public ParseListingTask(final HttpClient httpClient, final String baseUrl,
			final Queue<String> pathsQueue, final Downloader downloader, final String path) {
		this.httpClient = httpClient;
		this.baseUrl = baseUrl;
		this.path = path;
		this.pathsQueue = pathsQueue;
		this.downloader = downloader;
	}

	public void run() {
		String completeUrl = baseUrl + path;
		try {
			parseDirectoryListing(completeUrl);
		} catch (ClientProtocolException e) {
			LOGGER.log(Level.WARNING, "Protocol exception: " + completeUrl, e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "IOException: " + completeUrl, e);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.WARNING, "Invalid URI: " + completeUrl, e);
		}
	}

	private void parseDirectoryListing(String completeUrl) throws ClientProtocolException,
			IOException, URISyntaxException {		
		HttpGet httpget = new HttpGet(new URI(completeUrl));
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = httpClient.execute(httpget, responseHandler);
		Document doc = Jsoup.parse(responseBody, completeUrl);
		Elements links = doc.select("a[href]");
		ListIterator<Element> linksIterator = links.listIterator();
		List<String> newPaths = new LinkedList<String>();
		while (linksIterator.hasNext()) {
			Element link = linksIterator.next();
			String href = link.attr("href");
			if (href.equals("") || href.equals("../")) {
				continue;
			}
			if (href.endsWith(".pom.asc") || href.endsWith(".pom")) {
				String fileToDownloadUrl = completeUrl + href;
				downloader.downloadFile(fileToDownloadUrl);
			}
			if (href.endsWith("/")) {
				newPaths.add(path + href);
			}
		}
		pathsQueue.addAll(newPaths);
	}

}
