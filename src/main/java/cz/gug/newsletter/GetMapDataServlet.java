package cz.gug.newsletter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpRequestFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetMapDataServlet extends HttpServlet {

	private HttpRequestFactory requestFactory;

	@Override
	public void init() throws ServletException {
		requestFactory = new UrlFetchTransport().createRequestFactory();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int year = Integer.parseInt(request.getParameter("year"));
		int month = Integer.parseInt(request.getParameter("month"));

		Map<String, List<Event>> events = new EventReader(requestFactory).readEvents(year, month);
		JSONObject result = new JSONObject();
		for (Map.Entry<String, List<Event>> eventsInCity : events.entrySet()) {
			JSONArray eventsArray = new JSONArray();
			for (Event event : eventsInCity.getValue()) {
				eventsArray.put(event.toJSON());
			}
			result.put(eventsInCity.getKey(), eventsArray);
		}
		response.setContentType("application/json; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.getWriter().write(result.toString());
	}

}
