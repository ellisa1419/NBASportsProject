package com.se.termproject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserMaster {
	@Id
	private String name;
	private String favouriteTeams;
	private String role;
	private String full_name;
	private boolean isBlocked;
	
	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public Set<String> getFavouriteTeams() {
		if(favouriteTeams ==  null || favouriteTeams == "") {
			return new HashSet<String>();
		}
		String[] teams = favouriteTeams.split(",");
		return new HashSet<String>(Arrays.asList(teams));
	}

	public void setFavouriteTeams(Set<String> favouriteTeams) {
		this.favouriteTeams = String.join(",", favouriteTeams);
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setFavouriteTeams(String favouriteTeams) {
		this.favouriteTeams = favouriteTeams;
	}

	private Date lastLogin;
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}