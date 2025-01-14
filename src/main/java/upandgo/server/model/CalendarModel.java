package upandgo.server.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.allen_sauer.gwt.log.client.Log;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;

import upandgo.server.CoursesServiceImpl;
import upandgo.shared.entities.Day;
import upandgo.shared.entities.Lesson;
import upandgo.shared.entities.LessonGroup;
import upandgo.shared.entities.Semester;
import upandgo.shared.model.scedule.Color;

public class CalendarModel {
	private static final String calendarName = "Technion's Lessons Schedule";

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static AppEngineDataStoreFactory DATA_STORE_FACTORY = AppEngineDataStoreFactory.getDefaultInstance();

	private static String clientSecretJson = "/client_secret.json";

	private static GoogleClientSecrets clientSecrets;

	private Calendar calendarService;

	private String calendarId;

	public CalendarModel() {
	}

	public void createCalendar(List<LessonGroup> lessons, Map<String, Color> colorMap, Semester semester) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user == null) {
			Log.warn("User was not signed in. schedule could not be exported!");
			return;
		}

		Credential credential = newFlow().loadCredential(user.getUserId());
		calendarService = getCalendarService(credential);
		
		// Create a new calendar
		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
		calendar.setSummary(calendarName);
		calendar.setTimeZone("Universal");

		// Insert the new calendar
		com.google.api.services.calendar.model.Calendar createdCalendar = calendarService.calendars().insert(calendar)
				.execute();
		calendarId = createdCalendar.getId();

		for (LessonGroup l : lessons) {
			if (l == null)
				continue;
			List<Event> events = createEvents(l, colorMap.get(l.getCourseID()), semester);
			for (Event ev : events) {
				Event res = calendarService.events().insert(calendarId, ev).execute();
				CoursesServiceImpl.someString += "\nEvent created: " + res.getHtmlLink();
			}
		}
	}

	private static GoogleClientSecrets getClientCredential() throws IOException {
		if (clientSecrets == null) {
			clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
					new InputStreamReader(CalendarModel.class.getResourceAsStream(clientSecretJson)));
		}
		return clientSecrets;
	}

	public static String getRedirectUri(HttpServletRequest req) {
		GenericUrl url = new GenericUrl(req.getRequestURL().toString());
		url.setRawPath("/oauth2callback");
		return url.build();
	}

	public static GoogleAuthorizationCodeFlow newFlow() throws IOException {
		return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, getClientCredential(),
				Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(DATA_STORE_FACTORY)
						.setAccessType("offline").build();
	}

	private static Calendar getCalendarService(Credential cred) {
		return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).build();
	}

	static List<Event> createEvents(LessonGroup lg, Color color, Semester semester) {
		List<Event> events = new ArrayList<>();

		for (Lesson l : lg.getLessons()) {
			if (l == null)
				continue;

			String startTimeStr = lessonStartToRfc(semester.getStartDate(), l.getStartTime().getDay(),
					l.getStartTime().getTime().getHour(), l.getStartTime().getTime().getMinute());
			EventDateTime startTime = new EventDateTime().setDateTime(new DateTime(startTimeStr))
					.setTimeZone("Universal");
			String endTimeStr = lessonStartToRfc(semester.getStartDate(), l.getEndTime().getDay(), l.getEndTime().getTime().getHour(),
					l.getEndTime().getTime().getMinute());
			EventDateTime endTime = new EventDateTime().setDateTime(new DateTime(endTimeStr)).setTimeZone("Universal");
			
			// create event:
			Event event = new Event().setSummary(l.getCourseId() + "\n" + l.getCourseName())
					.setLocation((l.getPlace() == null) ? "" : l.getPlace())
					.setDescription(String.valueOf(l.getGroup()) + "\n" + l.getType().name() + "\n"
							+ ((l.getRepresenter() == null) ? "" : l.getRepresenter().getFullName()))
					.setStart(startTime).setEnd(endTime)
					.setRecurrence(Arrays.asList(getRecurrenceRule(semester.getEndDate())))
					.setColorId(colorToColorId(color));
			events.add(event);
		}

		return events;
	}

	static String lessonStartToRfc(String semsterStart, Day weekDay, int hour, int minute) {
		String date = "";
		try {
			Date $ = new SimpleDateFormat("dd/MM/yyyy").parse(semsterStart);
			String euSemStartweekDay = new SimpleDateFormat("u").format($);
			@SuppressWarnings("boxing")
			int semStartWeekDay = (Integer.valueOf(euSemStartweekDay)+1)%7;	// +1 because we need Hebrew calendar
	
			int weekDayOffset = weekDay.ordinal() - semStartWeekDay;
			if(weekDayOffset < 0)
			weekDayOffset += 7;
			
			$ = DateUtils.addDays($, weekDayOffset);
			date = new SimpleDateFormat("yyyy-MM-dd").format($);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String hourStr = String.valueOf(hour);
		if (hour < 10)
			hourStr = "0" + hourStr;

		String minuteStr = String.valueOf(minute);
		if (minute < 10)
			minuteStr = "0" + minuteStr;

		return date + "T" + hourStr + ":" + minuteStr + ":00Z"; // e.g. "1985-04-12T23:20:50.52Z"
	}
	
	static String getRecurrenceRule(String semsterEnd) {
		String date = "";
		try {
			Date $ = new SimpleDateFormat("dd/MM/yyyy").parse(semsterEnd);
			date = new SimpleDateFormat("yyyyMMdd").format($);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return "RRULE:FREQ=WEEKLY;UNTIL=" + date + "T235959Z";
	}

	/**
	 * Returns an {@link IOException} (but not a subclass) in order to work
	 * around restrictive GWT serialization policy.
	 */
	public static IOException wrappedIOException(IOException e) {
		if (e.getClass() == IOException.class) {
			return e;
		}
		return new IOException(e.getMessage());
	}
	
	public static String colorToColorId(Color color) {
		switch(color) {
		case GOLD:
			return "5";
		case PALETURQUOISE:
			return "7";
		case SLATEBLUE:
			return "9";
		case BLUEVIOLET:
			return "3";
		case BROWN:
			return "11";
		case CORAL:
			return "4";
		case CHARTREUSE:
			return "10";
		case CORNFLOWERBLUE:
			return "1";
		case DARKORANGE:
			return "6";
		case SEAGREEN:
			return "2";
		case DARKSLATEGRAY:
			return "8";//not at all
		case GOLDENROD:
			return "5";//again
		case TURQUOISE:
			return "7";//again
		case NAVY:
			return "9";//again
		case FUCHSIA:
			return "3";//again
		case CRIMSON:
			return "11";//again
		case LIMEGREEN:
			return "10";//again
		case ORANGERED:
			return "11";//again
		case OLIVEDRAB:
			return "10";//again
		default:
			return "5";
		}
	}
}
