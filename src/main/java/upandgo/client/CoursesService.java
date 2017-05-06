package upandgo.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import upandgo.shared.entities.constraint.TimeConstraint;
import upandgo.shared.entities.course.Course;
import upandgo.shared.entities.course.CourseId;
import upandgo.shared.model.scedule.Schedule;

/**
 * 
 * @author Nikita Dizhur
 * @since 05-05-17
 * 
 * Remote Procedure Call Service for retrieving information about courses in DB and selecting needed courses.
 * 
 */

@RemoteServiceRelativePath("coursesManipulations")
public interface CoursesService extends RemoteService {
	
	public ArrayList<CourseId> getSelectedCourses();

	public ArrayList<CourseId> getNotSelectedCourses(String query, String faculty);
	
	public ArrayList<String> getFaculties();

	public Course getCourseDetails();

	public void selectCourse(CourseId id);

	public void unselectCourse(CourseId id);
	
	public Schedule getSchedule(List<CourseId> selectedCourses, List<TimeConstraint> constraintsList);

	public Schedule getNextSchedule(Schedule schedule);

	public Schedule getPreviousSchedule(Schedule schedule);
}
