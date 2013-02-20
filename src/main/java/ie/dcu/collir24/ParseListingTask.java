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
	private final String url;
	private final Queue<String> pathsQueue;
	private final Downloader downloader;

	public ParseListingTask(final HttpClient httpClient, final String url,
			final Queue<String> pathsQueue, final Downloader downloader) {
		this.httpClient = httpClient;
		this.url = url;
		this.pathsQueue = pathsQueue;
		this.downloader = downloader;
	}

	public void run() {
		try {
			parseDirectoryListing();
		} catch (ClientProtocolException e) {
			LOGGER.log(Level.WARNING, "Protocol exception: " + url, e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "IOException: " + url, e);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.WARNING, "Invalid URI: " + url, e);
		}
	}

	private void parseDirectoryListing() throws ClientProtocolException,
			IOException, URISyntaxException {
		HttpGet httpget = new HttpGet(new URI(url));
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = httpClient.execute(httpget, responseHandler);
		Document doc = Jsoup.parse(responseBody, url);
		Elements links = doc.select("a[href]");
		ListIterator<Element> linksIterator = links.listIterator();
		while (linksIterator.hasNext()) {
			Element link = linksIterator.next();
			String href = link.attr("href");
			if (href.equals("") || href.equals("../")) {
				continue;
			}
			if (href.endsWith(".pom.asc") || href.endsWith(".pom")) {
				String fileToDownloadUrl = url + href;
				downloader.downloadFile(fileToDownloadUrl);
			}
			if (href.endsWith("/")) {
				pathsQueue.add(url + href);
			}
		}
	}

}
