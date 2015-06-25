package cz.gug.newsletter;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.json.JSONObject;
import org.json.JSONTokener;

public class CreateNewsletterServlet extends HttpServlet {

	private Configuration configuration;
	private HttpRequestFactory requestFactory;

	@Override
	public void init() throws ServletException {
		configuration = Configuration.getInstance();
		requestFactory = new UrlFetchTransport().createRequestFactory();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int year = Integer.parseInt(request.getParameter("year"));
		int month = Integer.parseInt(request.getParameter("month"));

		List<Event> events = new EventReader(requestFactory).readEvents(year, month);
		if (events.isEmpty()) {
			response.getWriter().print("<h1>No events found for selected month and year.</h1>");
			return;
		}
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
		return String.format("Naše akce v %s %d", getMonthName(month), year);
	}

	private String getMonthName(int month) {
		return Configuration.MONTH_NAMES[month - 1];
	}

	private JSONObject createMailContent(List<Event> events, int year, int month) {
		JSONObject sections = new JSONObject();
		sections.put("header", createSubject(year, month));

		Map<String, List<Event>> eventsByCity = splitEventsByCities(events);
		int index = 0;
		ArrayList<String> cities = new ArrayList<>(eventsByCity.keySet());
		sortUsingLocale(cities);
		for (String city : cities) {
			sections.put("repeat_1:" + index + ":city", city);
			sections.put("repeat_1:" + index + ":events", createEventsHtml(eventsByCity.get(city)));
			index++;
		}

		JSONObject mailContent = new JSONObject();
		mailContent.put("sections", sections);
		return mailContent;
	}

	private String createEventsHtml(List<Event> events) {
		StringBuilder result = new StringBuilder();
		for (Event event : events) {
			result.append(String.format(" <tr>\n" +
							"    <td class=\"chapter\">\n" +
							"        <span class=\"%s_color chapter_mark\">%s</span>\n" +
							"    </td>\n" +
							"    <td class=\"event_name\">\n" +
							"        <a href=\"%s\" target=\"_blank\">\n" +
							"            %s\n" +
							"        </a><br/>\n" +
							"        <span class=\"place\">%s</span>\n" +
							"    </td>\n" +
							"</tr>",
					event.getGroupShortcut().toLowerCase(),
					event.getGroupShortcut().toUpperCase(),
					event.getLink(),
					event.getEventName(),
					event.getFromDate()));
		}
		return result.toString();
	}

	private Map<String, List<Event>> splitEventsByCities(List<Event> events) {
		Map<String, List<Event>> result = new HashMap<>();
		for (Event event : events) {
			if (!result.containsKey(event.getCity())) {
				result.put(event.getCity(), new ArrayList<Event>());
			}
			result.get(event.getCity()).add(event);
		}
		return result;
	}

	private void sortUsingLocale(List list) {
		Collator collator = Collator.getInstance(configuration.getLocale());
		collator.setStrength(Collator.PRIMARY);
		Collections.sort(list, collator);
	}
}
