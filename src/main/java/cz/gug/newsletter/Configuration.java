package cz.gug.newsletter;

import java.io.IOException;
import java.util.Properties;

public class Configuration {

	private static final String PROPERTIES_FILE = "/newsletter.properties";

	private Properties properties;

	public Configuration() throws IOException {
		properties = new Properties();
		properties.load(Configuration.class.getResourceAsStream(PROPERTIES_FILE));
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

}
