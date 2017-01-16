package model.course;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StuffMember implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7103892149028991099L;
	
	public String fName;
	public String lName;
	public String title;
	public String email;
	public String office;
	public final List<LocalDateTime> officeHours;
	
	public StuffMember(String fName1, String lName1) {
		// THIS CONSTRCUTOR MADE FOR TESTS, DO NOT USE WITHOUT A GOOD REASON
		this.fName = fName1;
		this.lName = lName1;
		officeHours = null;
	}
	
	public StuffMember(String fName1, String lName1, String title) {
		// THIS CONSTRCUTOR MADE FOR TESTS, DO NOT USE WITHOUT A GOOD REASON
		this.fName = fName1;
		this.lName = lName1;
		this.title = title;
		this.office = this.email = "";
		officeHours = null;
	}
	
	
	
	public String getFirstName () {
		return this.fName;
	}
	
	public String getLastName () {
		return this.lName;
	}
	
	public String getTitle () {
		return this.title;
	}
	
	public StuffMember(String fName1, String lName1, String ttl, String eml, String office1,
																			List<LocalDateTime> ofHours) {
		this.fName = fName1;
		this.lName = lName1;
		this.title = ttl;
		this.email = eml;
		this.office = office1;
		this.officeHours = new ArrayList<>(ofHours);
	}
	
	@Override
	public boolean equals(Object ¢){
		return ¢ != null && (¢ == this || ¢ instanceof StuffMember 
				&& ((this.fName).equals(((StuffMember) ¢).getFirstName()))
				&& ((this.lName).equals(((StuffMember) ¢).getLastName()))
				&& ((this.title).equals(((StuffMember) ¢).getTitle())));
	}
	
	@Override
	public String toString(){
		return fName + " " + lName;
	}
	
	@Override
	public int hashCode() {
	    return Objects.hash(fName, lName, title);
	}
	
}
