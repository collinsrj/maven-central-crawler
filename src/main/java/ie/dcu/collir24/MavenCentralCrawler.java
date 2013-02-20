package ie.dcu.collir24;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MavenCentralCrawler implements Downloader {
	private static final String MAVEN_REPO_BASE = "http://repo1.maven.org/maven2/";
	private static final Logger LOGGER = Logger
			.getLogger(MavenCentralCrawler.class.getName());
	private HttpClient httpClient;
	private final Queue<String> mavenRootPaths = new ConcurrentLinkedQueue<String>();
	private final String pathToDownloadTo;
	private final ExecutorService exec = Executors.newFixedThreadPool(6);

	public MavenCentralCrawler() {
		File downloadDir = new File("maven2");
		if (!downloadDir.exists()) {
			downloadDir.mkdir();
		}
		pathToDownloadTo = downloadDir.getAbsolutePath() + "/";

		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		cm.setDefaultMaxPerRoute(4);// increase from the default of 2
		httpClient = gzipClient(new DefaultHttpClient(cm));
		// mavenRootPaths
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(
					MavenCentralCrawler.class
							.getResourceAsStream("TestMavenRoots.txt")));
			while (r.ready()) {
				mavenRootPaths.add(r.readLine());
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"Couldn't initialize list of Maven Root Paths.", e);
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					throw new RuntimeException(
							"Couldn't initialize list of Maven Root Paths.", e);
				}
			}
		}

	}

	private static DefaultHttpClient gzipClient(
			final DefaultHttpClient httpClient) {
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {

			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				if (!request.containsHeader("Accept-Encoding")) {
					request.addHeader("Accept-Encoding", "gzip");
				}
			}

		});

		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {

			public void process(final HttpResponse response,
					final HttpContext context) throws HttpException,
					IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					Header ceheader = entity.getContentEncoding();
					if (ceheader != null) {
						HeaderElement[] codecs = ceheader.getElements();
						for (int i = 0; i < codecs.length; i++) {
							if (codecs[i].getName().equalsIgnoreCase("gzip")) {
								response.setEntity(new GzipDecompressingEntity(
										response.getEntity()));
								return;
							}
						}
					}
				}
			}

		});
		return httpClient;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MavenCentralCrawler crawler = new MavenCentralCrawler();
		try {
			crawler.crawlCentral();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		crawler.shutdownExecAndCleanup();
	}

	private void shutdownExecAndCleanup() {
		try {
			exec.shutdown();
			exec.awaitTermination(4, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			Thread.interrupted();
		} finally {
			exec.shutdownNow();
			httpClient.getConnectionManager().shutdown();
		}
	}

	private void crawlCentral() throws ClientProtocolException, IOException {
		LOGGER.info("starting...");
		StringBuilder sb = new StringBuilder();
		int count = 0;
		while (!mavenRootPaths.isEmpty()) {
			String rootPath = mavenRootPaths.poll();
			sb.setLength(0);
			sb.append(MAVEN_REPO_BASE);
			sb.append(rootPath);
			String url = sb.toString();
			HttpGet httpget;
			try {
				httpget = new HttpGet(new URI(url));
			} catch (URISyntaxException e) {
				LOGGER.log(Level.WARNING, "Invalid URI: " + url, e);
				continue;
			}
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
					String filePath = pathToDownloadTo + rootPath + href;
					exec.execute(new DownloadTask(fileToDownloadUrl,
							httpClient, filePath));
				}
				if (href.endsWith("/")) {
					mavenRootPaths.add(rootPath + href);
				}
			}
			count++;
			if (count % 100 == 0) {
				System.out.println("Count is: " + count + " queue size is "
						+ mavenRootPaths.size());
			}

		}
		LOGGER.info("finishing...");
	}

	public void downloadFile(String url) {

	}
}
