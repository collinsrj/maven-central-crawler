package ie.dcu.collir24;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TestMetaDataDownloadTask {
	private static final String TEST_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<metadata><groupId>org.apache.archiva</groupId><artifactId>archiva-webdav</artifactId><version>1.1</version><versioning><latest>1.3.6</latest><release>1.3.6</release><versions><version>1.1</version><version>1.1.1</version><version>1.1.2</version><version>1.1.3</version><version>1.2-M1</version><version>1.2</version><version>1.1.4</version><version>1.2.1</version><version>1.2.2</version><version>1.3</version><version>1.3.1</version><version>1.3.2</version><version>1.3.3</version><version>1.3.4</version><version>1.3.5</version><version>1.4-M1</version><version>1.4-M2</version><version>1.4-M3</version><version>1.3.6</version></versions><lastUpdated>20130107111009</lastUpdated></versioning></metadata>";
	private static final String TEST_DATA_VERSION = "1.3.6";

	private static final String TEST_DATA_NO_LATEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<metadata><groupId>com.canoo</groupId><artifactId>webtest</artifactId><version>586</version><versioning><versions><version>586</version><version>268</version><version>277</version><version>1193</version></versions></versioning></metadata>";
	private static final String TEST_DATA_NO_LATEST_VERSION = "586";

	private static final String TEST_DATA_VERSIONING_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<metadata><groupId>docbook</groupId><artifactId>docbook-xml</artifactId><versioning><versions><version>4.2</version></versions></versioning></metadata>";
	private static final String TEST_DATA_VERSIONING_VERSION_VERSION = "4.2";

	private static final String TEST_NO_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<metadata><plugins><plugin><name>Maven XBean Plugin</name><prefix>xbean</prefix><artifactId>maven-xbean-plugin</artifactId></plugin></plugins></metadata>";

	/**
	 * Gets the version from the /metadata/versioning/latest element
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParseMetaData() throws Exception {
		String versionToDownload = MetadataVersionTask
				.getVersionToDownload(TEST_DATA);
		assertEquals(TEST_DATA_VERSION, versionToDownload);
	}

	/**
	 * Gets the version where there is no versioning block
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParseMetaDataNoLatest() throws Exception {
		String versionToDownload = MetadataVersionTask
				.getVersionToDownload(TEST_DATA_NO_LATEST);
		assertEquals(TEST_DATA_NO_LATEST_VERSION, versionToDownload);
	}

	/**
	 * Gets the version where the latest element isn't specified in a versioning
	 * block
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParseMetaDataVersioningVersion() throws Exception {
		String versionToDownload = MetadataVersionTask
				.getVersionToDownload(TEST_DATA_VERSIONING_VERSION);
		assertEquals(TEST_DATA_VERSIONING_VERSION_VERSION, versionToDownload);
	}

	/**
	 * Gets the version where the latest element isn't specified in a versioning
	 * block
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParseMetaDataNoVersion() throws Exception {
		String versionToDownload = MetadataVersionTask
				.getVersionToDownload(TEST_NO_VERSION);
		assertNull(versionToDownload);
	}
}
