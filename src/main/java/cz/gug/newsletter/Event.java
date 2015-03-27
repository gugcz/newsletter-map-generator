package cz.gug.newsletter;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

public class Event {

	private boolean published;
	private String groupShortcut;
	private String eventName;
	private String newsletterText;
	private String fromDate;
	private String newsletterButtonLabel;
	private String address;

	public Event(JSONObject jsonObject){
		published = jsonObject.getBoolean("is_published");
		eventName = jsonObject.getString("event_name");

		if (JsonUtils.isNullOrEmpty(jsonObject, "newsletter_text")){
			newsletterText = jsonObject.getString("event_tagline");
		} else {
			newsletterText = jsonObject.getString("newsletter_text");
		}

		if (JsonUtils.isNullOrEmpty(jsonObject, "newsletter_button_label")){
			newsletterButtonLabel = "VÍCE O AKCI";
		} else {
			newsletterButtonLabel = jsonObject.getString("newsletter_button_label").toUpperCase();
		}

		String rawFromDate = jsonObject.getString("date_from");
		DateTime parsedDate = ISODateTimeFormat.dateTimeParser().parseDateTime(rawFromDate);
		fromDate = DateTimeFormat.mediumDate().withLocale(Locale.forLanguageTag("cs")).print(parsedDate);

		JSONObject embedded = jsonObject.getJSONObject("_embedded");
		JSONArray groups = embedded.getJSONArray("groups");
		if (groups.length() > 0){
			groupShortcut = groups.getJSONObject(0).getJSONObject("embedded").getJSONObject("section").getString("shortcut");
		} else {
			throw new IllegalArgumentException("No asocciated group.");
		}

		if (!embedded.isNull("venue")){
			JSONObject venue = embedded.getJSONObject("venue");
			if (JsonUtils.isNullOrEmpty(venue, "address")){
				address = venue.getString("address");
			} else if (JsonUtils.isNullOrEmpty(venue, "name")){
				address = venue.getString("name");
			}
		}
		if (address == null){
			address = groups.getJSONObject(0).getString("location");
		}
	}

	public boolean isPublished() {
		return published;
	}

	public String getGroupShortcut() {
		return groupShortcut;
	}

	public String getEventName() {
		return eventName;
	}

	public String getNewsletterText() {
		return newsletterText;
	}

	public String getFromDate() {
		return fromDate;
	}

	public String getNewsletterButtonLabel() {
		return newsletterButtonLabel;
	}

	public String getAddress() {
		return address;
	}

}
