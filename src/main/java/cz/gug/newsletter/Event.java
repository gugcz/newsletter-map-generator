package cz.gug.newsletter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

public class Event {

	private boolean published;
	private String groupShortcut;
	private String eventName;
	private String fromDate;
	private String address;
	private String city;
	private String link;
	private int day;

	public Event(JSONObject jsonObject){
		published = jsonObject.getBoolean("is_published");
		eventName = jsonObject.getString("event_name");
		link = jsonObject.getString("event_absolute_link");

		String rawFromDate = jsonObject.getString("date_from");
		DateTime parsedDate = ISODateTimeFormat.dateTimeParser().parseDateTime(rawFromDate);
		fromDate = DateTimeFormat.shortDateTime()
				.withLocale(Configuration.getInstance().getLocale())
				.withZone(Configuration.getInstance().getTimeZone())
				.print(parsedDate);
		day = parsedDate.getDayOfMonth();

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

		city = groups.getJSONObject(0).getString("location");

		if (address == null){
			address = city;
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

	public String getFromDate() {
		return fromDate;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getLink() {
		return link;
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.put("groupShortcut", groupShortcut);
		result.put("eventName", eventName);
		result.put("city", city);
		result.put("day", day);
		return result;
	}
}
