package cz.gug.newsletter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class EventReader {

	private Configuration configuration = Configuration.getInstance();
	private HttpRequestFactory requestFactory;

	public EventReader(HttpRequestFactory requestFactory) {
    	this.requestFactory = requestFactory;
	}

	public List<Event> readEvents(int year, int month) throws IOException {
		DateTime dateFrom = new DateTime(year, month, 1, 1, 0, 0);
		DateTime dateTo = new DateTime(year, month + 1, 1, 0, 0);

		GenericUrl url = new GenericUrl(configuration.getProperty("gug.web.endpoint"));
		url.put("Token", configuration.getProperty("gug.web.api.key"));
		url.put("date_from_after", ISODateTimeFormat.dateTime().print(dateFrom));
		url.put("date_from_before", ISODateTimeFormat.dateTime().print(dateTo));
		url.put("date_from_status", "known");
		HttpRequest eventsRequest = requestFactory.buildGetRequest(url);
		InputStream eventsInputStream = eventsRequest.execute().getContent();

		JSONObject jsonObject = new JSONObject(new JSONTokener(eventsInputStream));

		JSONArray eventsArray = jsonObject.getJSONObject("_embedded").getJSONArray("event_occurrences");
		List<Event> events = new ArrayList<>(eventsArray.length());
		for (int i = 0; i < eventsArray.length(); i++) {
			events.add(new Event(eventsArray.getJSONObject(i)));
		}
		return events;
	}

}
