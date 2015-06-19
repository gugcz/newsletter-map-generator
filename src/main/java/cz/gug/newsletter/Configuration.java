package cz.gug.newsletter;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

public class Configuration {

	public static final String[] MONTH_NAMES = {"lednu", "únoru", "březnu", "dubnu", "květnu", "červnu", "červenci", "srpnu",
			"září", "říjnu", "listopadu", "prosinci"};

	private static final Logger LOG = Logger.getLogger(Configuration.class.getName());

	private static final String PROPERTIES_FILE = "/newsletter.properties";
	private static Configuration INSTANCE;

	private Properties properties;

	static {
		try {
			INSTANCE = new Configuration();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to read config file.", e);
		}
	}

	private Configuration() throws IOException {
		properties = new Properties();
		properties.load(Configuration.class.getResourceAsStream(PROPERTIES_FILE));
	}

	public static synchronized Configuration getInstance() {
		return INSTANCE;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public Locale getLocale() {
		return Locale.forLanguageTag(getProperty("locale"));
	}

	public DateTimeZone getTimeZone() {
		return DateTimeZone.forID(getProperty("timezone"));
	}


}
