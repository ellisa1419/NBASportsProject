package com.se.termproject;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
class Team{
	@JsonProperty("ID")
	String id;
	public String getID() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAbbreviation() {
		return abbreviation;
	}
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}
	@JsonProperty("City")
	String city;
	@JsonProperty("Name")
	String name;
	@JsonProperty("Abbreviation")
	String abbreviation;
}

@Data
class TeamStandingsEntry{
	Team team;
	public Team getTeam() {
		return team;
	}
	public void setTeam(Team team) {
		this.team = team;
	}
	public Long getRank() {
		return rank;
	}
	public void setRank(Long rank) {
		this.rank = rank;
	}
	Long rank;	
}

@Data
class OverAllTeamStandings{
	String lastUpdatedOn;
	@JsonProperty("teamstandingsentry")
	ArrayList<TeamStandingsEntry> teamstandingsentries;
	public ArrayList<TeamStandingsEntry> getTeamstandingsentries() {
		return teamstandingsentries;
	}
	public void setTeamstandingsentries(ArrayList<TeamStandingsEntry> teamstandingsentries) {
		this.teamstandingsentries = teamstandingsentries;
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NBATeamStanding {
	OverAllTeamStandings overallteamstandings;

	public OverAllTeamStandings getOverallteamstandings() {
		return overallteamstandings;
	}

	public void setOverallteamstandings(OverAllTeamStandings overallteamstandings) {
		this.overallteamstandings = overallteamstandings;
	}
	
}