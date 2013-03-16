package ie.dcu.collir24;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class TestLinkTask {
	private static final String METADATA = "<html>"
			+ "<head><title>Index of /maven2/jhunlang/jmorph/</title></head>"
			+ "<body bgcolor=\"white\">"
			+ "<h1>Index of /maven2/jhunlang/jmorph/</h1><hr><pre><a href=\"../\">../</a>"
			+ "<a href=\"0.2/\">0.2/</a>                                               04-Jan-2007 19:17                   -"
			+ "<a href=\"maven-metadata.xml\">maven-metadata.xml</a>                                 20-Sep-2005 05:53                 202"
			+ "<a href=\"maven-metadata.xml.md5\">maven-metadata.xml.md5</a>                             04-Jan-2007 19:17                  71"
			+ "<a href=\"maven-metadata.xml.sha1\">maven-metadata.xml.sha1</a>                            09-Jul-2006 13:39                 126"
			+ "</pre><hr></body>" + "</html>";

	private static final String JAR_POM = "<html>"
			+ "<head><title>Index of /maven2/commons-beanutils/commons-beanutils-core/1.7.0/</title></head>"
			+ "<body bgcolor=\"white\">"
			+ "<h1>Index of /maven2/commons-beanutils/commons-beanutils-core/1.7.0/</h1><hr><pre><a href=\"../\">../</a>"
			+ "<a href=\"commons-beanutils-core-1.7.0.jar\">commons-beanutils-core-1.7.0.jar</a>                   22-Nov-2005 18:08              168760"
			+ "<a href=\"commons-beanutils-core-1.7.0.jar.md5\">commons-beanutils-core-1.7.0.jar.md5</a>               22-Nov-2005 18:08                  32"
			+ "<a href=\"commons-beanutils-core-1.7.0.jar.sha1\">commons-beanutils-core-1.7.0.jar.sha1</a>              22-Nov-2005 18:08                  40"
			+ "<a href=\"commons-beanutils-core-1.7.0.pom\">commons-beanutils-core-1.7.0.pom</a>                   28-Dec-2005 13:05                 719"
			+ "<a href=\"commons-beanutils-core-1.7.0.pom.md5\">commons-beanutils-core-1.7.0.pom.md5</a>               04-Jan-2006 13:33                 172"
			+ "<a href=\"commons-beanutils-core-1.7.0.pom.sha1\">commons-beanutils-core-1.7.0.pom.sha1</a>              04-Jan-2006 13:33                 180"
			+ "<a href=\"maven-metadata.xml\">maven-metadata.xml</a>                                 20-Sep-2005 05:46                 138"
			+ "<a href=\"maven-metadata.xml.md5\">maven-metadata.xml.md5</a>                             04-Jan-2007 19:16                 102"
			+ "<a href=\"maven-metadata.xml.sha1\">maven-metadata.xml.sha1</a>                            09-Jul-2006 13:38                 157"
			+ "</pre><hr></body>" + "</html>";

	private static final String JAR_POM_ASC = "<html>"
			+ "<head><title>Index of /maven2/commons-io/commons-io/2.4/</title></head>"
			+ "<body bgcolor=\"white\">"
			+ "<h1>Index of /maven2/commons-io/commons-io/2.4/</h1><hr><pre><a href=\"../\">../</a>"
			+ "<a href=\"commons-io-2.4-javadoc.jar\">commons-io-2.4-javadoc.jar</a>                         12-Jun-2012 22:22              724124"
			+ "<a href=\"commons-io-2.4-javadoc.jar.asc\">commons-io-2.4-javadoc.jar.asc</a>                     12-Jun-2012 22:23                 499"
			+ "<a href=\"commons-io-2.4-javadoc.jar.asc.md5\">commons-io-2.4-javadoc.jar.asc.md5</a>                 08-Mar-2013 22:20                  32"
			+ "<a href=\"commons-io-2.4-javadoc.jar.asc.sha1\">commons-io-2.4-javadoc.jar.asc.sha1</a>                08-Mar-2013 22:20                  40"
			+ "<a href=\"commons-io-2.4-javadoc.jar.md5\">commons-io-2.4-javadoc.jar.md5</a>                     05-Jul-2012 16:03                  63"
			+ "<a href=\"commons-io-2.4-javadoc.jar.sha1\">commons-io-2.4-javadoc.jar.sha1</a>                    05-Jul-2012 16:03                  71"
			+ "<a href=\"commons-io-2.4-sources.jar\">commons-io-2.4-sources.jar</a>                         12-Jun-2012 22:22              246635"
			+ "<a href=\"commons-io-2.4-sources.jar.asc\">commons-io-2.4-sources.jar.asc</a>                     19-Jun-2012 14:48                 488"
			+ "<a href=\"commons-io-2.4-sources.jar.asc.md5\">commons-io-2.4-sources.jar.asc.md5</a>                 08-Mar-2013 22:20                  32"
			+ "<a href=\"commons-io-2.4-sources.jar.asc.sha1\">commons-io-2.4-sources.jar.asc.sha1</a>                08-Mar-2013 22:20                  40"
			+ "<a href=\"commons-io-2.4-sources.jar.md5\">commons-io-2.4-sources.jar.md5</a>                     05-Jul-2012 16:03                  63"
			+ "<a href=\"commons-io-2.4-sources.jar.sha1\">commons-io-2.4-sources.jar.sha1</a>                    05-Jul-2012 16:03                  71"
			+ "<a href=\"commons-io-2.4-test-sources.jar\">commons-io-2.4-test-sources.jar</a>                    12-Jun-2012 22:22              199048"
			+ "<a href=\"commons-io-2.4-test-sources.jar.asc\">commons-io-2.4-test-sources.jar.asc</a>                12-Jun-2012 22:23                 499"
			+ "<a href=\"commons-io-2.4-test-sources.jar.asc.md5\">commons-io-2.4-test-sources.jar.asc.md5</a>            08-Mar-2013 22:20                  32"
			+ "<a href=\"commons-io-2.4-test-sources.jar.asc.sha1\">commons-io-2.4-test-sources.jar.asc.sha1</a>           08-Mar-2013 22:20                  40"
			+ "<a href=\"commons-io-2.4-test-sources.jar.md5\">commons-io-2.4-test-sources.jar.md5</a>                05-Jul-2012 16:03                  68"
			+ "<a href=\"commons-io-2.4-test-sources.jar.sha1\">commons-io-2.4-test-sources.jar.sha1</a>               05-Jul-2012 16:03                  76"
			+ "<a href=\"commons-io-2.4-tests.jar\">commons-io-2.4-tests.jar</a>                           12-Jun-2012 22:22              303024"
			+ "<a href=\"commons-io-2.4-tests.jar.asc\">commons-io-2.4-tests.jar.asc</a>                       12-Jun-2012 22:23                 499"
			+ "<a href=\"commons-io-2.4-tests.jar.asc.md5\">commons-io-2.4-tests.jar.asc.md5</a>                   08-Mar-2013 22:20                  32"
			+ "<a href=\"commons-io-2.4-tests.jar.asc.sha1\">commons-io-2.4-tests.jar.asc.sha1</a>                  08-Mar-2013 22:20                  40"
			+ "<a href=\"commons-io-2.4-tests.jar.md5\">commons-io-2.4-tests.jar.md5</a>                       05-Jul-2012 16:03                  61"
			+ "<a href=\"commons-io-2.4-tests.jar.sha1\">commons-io-2.4-tests.jar.sha1</a>                      05-Jul-2012 16:03                  69"
			+ "<a href=\"commons-io-2.4.jar\">commons-io-2.4.jar</a>                                 12-Jun-2012 22:22              185140"
			+ "<a href=\"commons-io-2.4.jar.asc\">commons-io-2.4.jar.asc</a>                             12-Jun-2012 22:23                 499"
			+ "<a href=\"commons-io-2.4.jar.asc.md5\">commons-io-2.4.jar.asc.md5</a>                         08-Mar-2013 22:20                  32"
			+ "<a href=\"commons-io-2.4.jar.asc.sha1\">commons-io-2.4.jar.asc.sha1</a>                        08-Mar-2013 22:20                  40"
			+ "<a href=\"commons-io-2.4.jar.md5\">commons-io-2.4.jar.md5</a>                             05-Jul-2012 16:03                  55"
			+ "<a href=\"commons-io-2.4.jar.sha1\">commons-io-2.4.jar.sha1</a>                            05-Jul-2012 16:03                  63"
			+ "<a href=\"commons-io-2.4.pom\">commons-io-2.4.pom</a>                                 12-Jun-2012 22:22               10166"
			+ "<a href=\"commons-io-2.4.pom.asc\">commons-io-2.4.pom.asc</a>                             12-Jun-2012 22:23                 499"
			+ "<a href=\"commons-io-2.4.pom.asc.md5\">commons-io-2.4.pom.asc.md5</a>                         08-Mar-2013 22:20                  32"
			+ "<a href=\"commons-io-2.4.pom.asc.sha1\">commons-io-2.4.pom.asc.sha1</a>                        08-Mar-2013 22:20                  40"
			+ "<a href=\"commons-io-2.4.pom.md5\">commons-io-2.4.pom.md5</a>                             05-Jul-2012 16:03                  55"
			+ "<a href=\"commons-io-2.4.pom.sha1\">commons-io-2.4.pom.sha1</a>                            05-Jul-2012 16:03                  63"
			+ "</pre><hr></body>" + "</html>";

	private static final String JAR_POM_ASC_2 = "<html>"
			+ "<head><title>Index of /maven2/com/impetus/kundera-tests/2.4/</title></head>"
			+ "<body bgcolor=\"white\">"
			+ "<h1>Index of /maven2/com/impetus/kundera-tests/2.4/</h1><hr><pre><a href=\"../\">../</a>"
			+ "<a href=\"kundera-tests-2.4-javadoc.jar\">kundera-tests-2.4-javadoc.jar</a>                      08-Mar-2013 13:20               66921"
			+ "<a href=\"kundera-tests-2.4-javadoc.jar.asc\">kundera-tests-2.4-javadoc.jar.asc</a>                  08-Mar-2013 13:20                 316"
			+ "<a href=\"kundera-tests-2.4-javadoc.jar.asc.md5\">kundera-tests-2.4-javadoc.jar.asc.md5</a>              08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4-javadoc.jar.asc.sha1\">kundera-tests-2.4-javadoc.jar.asc.sha1</a>             08-Mar-2013 13:20                  40"
			+ "<a href=\"kundera-tests-2.4-javadoc.jar.md5\">kundera-tests-2.4-javadoc.jar.md5</a>                  08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4-javadoc.jar.sha1\">kundera-tests-2.4-javadoc.jar.sha1</a>                 08-Mar-2013 13:20                  40"
			+ "<a href=\"kundera-tests-2.4-sources.jar\">kundera-tests-2.4-sources.jar</a>                      08-Mar-2013 13:20               12174"
			+ "<a href=\"kundera-tests-2.4-sources.jar.asc\">kundera-tests-2.4-sources.jar.asc</a>                  08-Mar-2013 13:20                 316"
			+ "<a href=\"kundera-tests-2.4-sources.jar.asc.md5\">kundera-tests-2.4-sources.jar.asc.md5</a>              08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4-sources.jar.asc.sha1\">kundera-tests-2.4-sources.jar.asc.sha1</a>             08-Mar-2013 13:20                  40"
			+ "<a href=\"kundera-tests-2.4-sources.jar.md5\">kundera-tests-2.4-sources.jar.md5</a>                  08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4-sources.jar.sha1\">kundera-tests-2.4-sources.jar.sha1</a>                 08-Mar-2013 13:20                  40"
			+ "<a href=\"kundera-tests-2.4.jar\">kundera-tests-2.4.jar</a>                              08-Mar-2013 13:20               19429"
			+ "<a href=\"kundera-tests-2.4.jar.asc\">kundera-tests-2.4.jar.asc</a>                          08-Mar-2013 13:20                 316"
			+ "<a href=\"kundera-tests-2.4.jar.asc.md5\">kundera-tests-2.4.jar.asc.md5</a>                      08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4.jar.asc.sha1\">kundera-tests-2.4.jar.asc.sha1</a>                     08-Mar-2013 13:20                  40"
			+ "<a href=\"kundera-tests-2.4.jar.md5\">kundera-tests-2.4.jar.md5</a>                          08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4.jar.sha1\">kundera-tests-2.4.jar.sha1</a>                         08-Mar-2013 13:20                  40"
			+ "<a href=\"kundera-tests-2.4.pom\">kundera-tests-2.4.pom</a>                              08-Mar-2013 13:20               14728"
			+ "<a href=\"kundera-tests-2.4.pom.asc\">kundera-tests-2.4.pom.asc</a>                          08-Mar-2013 13:20                 316"
			+ "<a href=\"kundera-tests-2.4.pom.asc.md5\">kundera-tests-2.4.pom.asc.md5</a>                      08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4.pom.asc.sha1\">kundera-tests-2.4.pom.asc.sha1</a>                     08-Mar-2013 13:20                  40"
			+ "<a href=\"kundera-tests-2.4.pom.md5\">kundera-tests-2.4.pom.md5</a>                          08-Mar-2013 13:20                  32"
			+ "<a href=\"kundera-tests-2.4.pom.sha1\">kundera-tests-2.4.pom.sha1</a>                         08-Mar-2013 13:20                  40"
			+ "</pre><hr></body>" + "</html>";

	private static final String TEST_URL = "http://repo1.maven.org/maven2/com/";

	@Test
	public void testParseMetaData() throws Exception {
		List<String>[] listing = LinkTask.parseListing(METADATA, TEST_URL);
		List<String> expectedDownloads = Arrays.asList(new String[] { TEST_URL
				+ "maven-metadata.xml" });
		List<String> expectedLinks = Arrays.asList(new String[] { TEST_URL
				+ "0.2/" });
		List[] expected = { expectedDownloads, expectedLinks };
		assertArrayEquals(expected, listing);
	}

	@Test
	public void testParseJarPom() throws Exception {
		List<String>[] listing = LinkTask.parseListing(JAR_POM, TEST_URL);
		List<String> expectedDownloads = Arrays.asList(new String[] {
				TEST_URL + "commons-beanutils-core-1.7.0.jar",
				TEST_URL + "commons-beanutils-core-1.7.0.pom",
				TEST_URL + "maven-metadata.xml" });
		List<String> expectedLinks = Collections.EMPTY_LIST;
		List[] expected = { expectedDownloads, expectedLinks };
		assertArrayEquals(expected, listing);
	}

	@Test
	public void testParseJarPomAsc() throws Exception {
		List<String>[] listing = LinkTask.parseListing(JAR_POM_ASC, TEST_URL);
		List<String> expectedDownloads = Arrays.asList(new String[] {
				TEST_URL + "commons-io-2.4.jar",
				TEST_URL + "commons-io-2.4.jar.asc",
				TEST_URL + "commons-io-2.4.pom",
				TEST_URL + "commons-io-2.4.pom.asc" });
		List<String> expectedLinks = Collections.EMPTY_LIST;
		List[] expected = { expectedDownloads, expectedLinks };
		assertArrayEquals(expected, listing);
	}

	@Test
	public void testParseJarPomAsc2() throws Exception {
		String testUrl = "http://repo1.maven.org/maven2/com/impetus/kundera-tests/2.4/";
		List<String>[] listing = LinkTask.parseListing(JAR_POM_ASC_2, testUrl);
		List<String> expectedDownloads = Arrays.asList(new String[] {
				testUrl + "kundera-tests-2.4.jar",
				testUrl + "kundera-tests-2.4.jar.asc",
				testUrl + "kundera-tests-2.4.pom",
				testUrl + "kundera-tests-2.4.pom.asc" });
		List<String> expectedLinks = Collections.EMPTY_LIST;
		List[] expected = { expectedDownloads, expectedLinks };
		assertArrayEquals(expected, listing);
	}

	@Test
	public void testParent() throws Exception {
		assertTrue(LinkTask.isParent(
				"http://repo1.maven.org/maven2/com/impetus/kundera-tests/2.4/",
				"http://repo1.maven.org/maven2/com/impetus/kundera-tests/"));
		assertFalse(LinkTask.isParent(
				"http://repo1.maven.org/maven2/com/impetus/kundera-tests/2.4/",
				"http://repo1.maven.org/maven2/com/impetus/kundera-tests/2.4/"));
	}
}
