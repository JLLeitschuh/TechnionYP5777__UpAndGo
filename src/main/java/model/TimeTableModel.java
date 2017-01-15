package model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;

import logic.Scheduler;
import model.course.Course;
import model.course.LessonGroup;
import model.schedule.Timetable;
import property.TimeTableProperty;

public class TimeTableModel implements Model {

	protected List<Course> courses = new ArrayList<>();

	protected boolean isDaysoffCount = false;
	protected boolean isBlankSpaceCount = false;
	protected LocalTime minStartTime = null;
	protected LocalTime maxEndTime = null;
	
	protected List<List<LessonGroup>> lessonGroups = new ArrayList<>();
	protected int sched_index = 0;
	
	protected HashMultimap<String, PropertyChangeListener> listenersMap = HashMultimap.create();
	
	public TimeTableModel() {
		// nothing to do here
	}
	
	public void setCourses(List<Course> ¢) {
		courses = new ArrayList<>(¢);
	}
	
	public void setDaysoffFlag(boolean f) {
		isDaysoffCount = f;
	}
	
	public void setBlankSpaceFlag(boolean f) {
		isBlankSpaceCount = f;
	}
	
	public void setMinStartTime(LocalTime t) {
		minStartTime = t;
	}
	
	public void setMaxEndTime(LocalTime t) {
		maxEndTime = t;
	}
	
	public void loadSchedule() {
	//	System.out.println(isDaysoffCount + " " + isBlankSpaceCount + " " + minStartTime + " " + maxEndTime);
	//	List<Timetable> tables = Lists.newArrayList(Scheduler.sortedBy(
	//			Scheduler.getTimetablesList(courses), isDaysoffCount, isBlankSpaceCount, minStartTime, maxEndTime));
		System.out.println("%$%$%$%%$%$%$%  \n"+ courses+ " ");
				Scheduler.getTimetablesList(courses);
		List<Timetable> tables = new ArrayList<>();
		lessonGroups.clear();
		sched_index = 0;
		tables.forEach((x) -> lessonGroups.add(x.getLessonGroups()));

		notifySchedListeners();
	}
	
	public void loadNextSchedule() {
		++sched_index;
		notifySchedListeners();
	}

	public void loadPrevSchedule() {
		--sched_index;
		notifySchedListeners();
		
	}
	
	private void notifySchedListeners() {
		this.listenersMap.get(TimeTableProperty.SCHEDULE).forEach((x) -> x.propertyChange(
				(new PropertyChangeEvent(this, TimeTableProperty.SCHEDULE, null, lessonGroups.get(sched_index)))));
	}
	
	@Override
	public void addPropertyChangeListener(String property, PropertyChangeListener l) {
		if (property == null || l == null)
			throw new NullPointerException();
		this.listenersMap.put(property, l);

	}

	@Override
	public void removePropertyChangeListener(String property, PropertyChangeListener l) {
		if (property != null && l != null && this.listenersMap.containsKey(property))
			this.listenersMap.remove(property, l);

	}
}
