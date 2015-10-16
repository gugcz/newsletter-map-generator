package cz.gug.newsletter;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
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

		Map<String, List<Event>> eventsByCity = new EventReader(requestFactory).readEvents(year, month, true);
		if (eventsByCity.isEmpty()) {
			response.getWriter().print("<h1>No events found for selected month and year.</h1>");
			return;
		}
		JSONObject requestData = createMailchimpRequestData(eventsByCity, year, month);
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

	private JSONObject createMailchimpRequestData(Map<String, List<Event>> eventsByCity, int year, int month) {
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

		JSONObject mailContent = createMailContent(eventsByCity, year, month);
		mailContent.put("text", createPlainText(eventsByCity, year, month));
		requestData.put("content", mailContent);
		return requestData;
	}

	private String createSubject(int year, int month) {
		return String.format("Naše akce v %s %d", getMonthName(month), year);
	}

	private String getMonthName(int month) {
		return Configuration.MONTH_NAMES[month - 1];
	}

	private JSONObject createMailContent(Map<String, List<Event>> eventsByCity, int year, int month) {
		JSONObject sections = new JSONObject();
		sections.put("header", createSubject(year, month));

		ArrayList<String> cities = new ArrayList<>(eventsByCity.keySet());
		sortUsingLocale(cities);
		for (int i = 0; i < cities.size(); i++) {
			String city = cities.get(i);
			sections.put("repeat_1:" + i + ":city", city);
			List<Event> eventsInCity = eventsByCity.get(city);
			for (int j = 0; j < eventsInCity.size(); j++) {
				Event event = eventsInCity.get(j);
				String prefix = "repeat_1:" + i + ":repeat_1:" + j + ":";
				addEvent(sections, prefix, event);
			}
		}

		JSONObject mailContent = new JSONObject();
		mailContent.put("sections", sections);
		return mailContent;
	}

	private String createPlainText(Map<String, List<Event>> eventsByCity, int year, int month) {
		StringBuilder result = new StringBuilder();
		result.append("Otevřít v prohlížeči (*|ARCHIVE|*)\n\n")
				.append("GUG.cz\n" + createSubject(year, month) + "\n")
				.append("------------------------------------------------------------\n\n");

		ArrayList<String> cities = new ArrayList<>(eventsByCity.keySet());
		sortUsingLocale(cities);
		for (int i = 0; i < cities.size(); i++) {
			String city = cities.get(i);
			result.append("** " + city + "\n")
					.append("------------------------------------------------------------\n");

			List<Event> eventsInCity = eventsByCity.get(city);
			for (int j = 0; j < eventsInCity.size(); j++) {
				Event event = eventsInCity.get(j);
				result.append(event.getGroupShortcut().toUpperCase() + " - ")
						.append(event.getEventName());
				if (event.getOccurrenceName() != null) {
					result.append(" " + event.getOccurrenceName());
				}
				result.append(" (" + event.getLink() + ")\n")
						.append(event.getFromDate())
						.append("\n\n");
			}
			result.append("\n\n");
		}
		return result.toString();
	}

	private void addEvent(JSONObject sections, String prefix, Event event) {
		String nameWithOccurrence = event.getEventName();
		if (event.getOccurrenceName() != null) {
			nameWithOccurrence += " " + event.getOccurrenceName();
		}

		sections.put(prefix + "chapter_mark",
				String.format("<span class=\"%s_color chapter_mark\">%s</span>",
						event.getGroupShortcut().toLowerCase(),
						event.getGroupShortcut().toUpperCase()));
		sections.put(prefix + "event_name_and_place",
				String.format("<a href=\"%s\">%s</a><br>\n" +
								"<span class=\"place\">%s</span>",
						event.getLink(),
						nameWithOccurrence,
						event.getFromDate()));
	}

	private void sortUsingLocale(List list) {
		Collator collator = Collator.getInstance(configuration.getLocale());
		collator.setStrength(Collator.PRIMARY);
		Collections.sort(list, collator);
	}
}
