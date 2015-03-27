package cz.gug.newsletter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CreateNewsletterServlet extends HttpServlet {

	private Configuration configuration;
	private HttpRequestFactory requestFactory;

	@Override
	public void init() throws ServletException {
		try {
			configuration = new Configuration();
		} catch (IOException e) {
			throw new ServletException(e);
		}
		requestFactory = new UrlFetchTransport().createRequestFactory();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int year = Integer.parseInt(request.getParameter("year"));
		int month = Integer.parseInt(request.getParameter("month"));

		List<Event> events = readEvents(year, month);
		JSONObject requestData = createMailchimpRequestData(events, year, month);
		String campaignUrl = createMailchimpCampaign(requestData);
		response.getWriter().print("<a href = \"" + campaignUrl + "\">" + campaignUrl + "</a>");
	}

	private String createMailchimpCampaign(JSONObject requestData) throws IOException {
		GenericUrl url = new GenericUrl(configuration.getProperty("mailchimp.create.campaign.endpoint"));
		HttpContent content = ByteArrayContent.fromString("application/json", requestData.toString());
		HttpRequest httpRequest = requestFactory.buildPostRequest(url, content);
		InputStream inputStream = httpRequest.execute().getContent();
		JSONObject jsonObject = new JSONObject(new JSONTokener(inputStream));
		return jsonObject.getString("archive_url");
	}

	private JSONObject createMailchimpRequestData(List<Event> events, int year, int month) {
		JSONObject requestData = new JSONObject();
		requestData.put("apikey", configuration.getProperty("mailchimp.api.key"));
		requestData.put("type", "regular");

		JSONObject options = new JSONObject();
		options.put("list_id", configuration.getProperty("mail.list.id"));
		options.put("template_id", configuration.getProperty("template.id"));
		options.put("from_email", configuration.getProperty("from.email"));
		options.put("from_name", configuration.getProperty("from.name"));
		options.put("subject", createSubject(year, month));
		requestData.put("options", options);

		requestData.put("content", createMailContent(events, year, month));
		return requestData;
	}

	private String createSubject(int year, int month) {
		DateTime dateTime = new DateTime(year, month, 1, 0, 0);
		return DateTimeFormat.forPattern("MMMM YYYY").print(dateTime).toUpperCase();
	}

	private JSONObject createMailContent(List<Event> events, int year, int month) {
		JSONObject sections = new JSONObject();
		sections.put("header", createSubject(year, month));
		int gdgIndex = 0;
		int gbgIndex = 0;
		int gegIndex = 0;
		int gxgIndex = 0;
		for (Event event : events) {
			if (event.isPublished()) {
				String fieldNameStart;
				String group = event.getGroupShortcut();
				switch (group) {
					case "GDG":
						fieldNameStart = "repeat_2:" + gdgIndex + ":gdg_";
						gdgIndex++;
						break;
					case "GBG":
						fieldNameStart = "repeat_3:" + gbgIndex + ":gbg_";
						gbgIndex++;
						break;
					case "GEG":
						fieldNameStart = "repeat_4:" + gegIndex + ":geg_";
						gegIndex++;
						break;
					case "GXG":
						fieldNameStart = "repeat_5:" + gxgIndex + ":gxg_";
						gxgIndex++;
						break;
					default:
						throw new IllegalArgumentException("Unknown group " + group);
				}

				sections.put(fieldNameStart + "event_title", event.getEventName());
				sections.put(fieldNameStart + "event_description", event.getNewsletterText());
				sections.put(fieldNameStart + "event_date", event.getFromDate());
				sections.put(fieldNameStart + "event_button", event.getNewsletterButtonLabel());
				sections.put(fieldNameStart + "event_location", event.getAddress());
			}
		}

		JSONObject mailContent = new JSONObject();
		mailContent.put("sections", sections);
		return mailContent;
	}

	private List<Event> readEvents(int year, int month) throws IOException {
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
