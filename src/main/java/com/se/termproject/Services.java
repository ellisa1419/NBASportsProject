package com.se.termproject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Services {

	public static Object getResponse(String url, Class className) {
		
        String encoding = Base64.getEncoder().encodeToString((Constants.TOKEN+":"+Constants.PASSWORD).getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic "+encoding);
		HttpEntity<String> request = new HttpEntity<String>(headers);
		//Make the call
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange(url, HttpMethod.GET, request, className);
	}
	
	private static Date yesterday() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	
	public static List<HashMap<String, String>> getTodayScore(){
		List<HashMap<String, String>> gameDetails = new ArrayList();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    String[] time = dateFormat.format(yesterday()).split(" ");
	    String finalTime = time[0].replaceAll("/", "");	
	    String finalurl = Constants.SCORE_BOARD+finalTime;
		ResponseEntity<String> response = (ResponseEntity<String>) Services.getResponse(finalurl, String.class);
		String str = response.getBody(); 
		ObjectMapper mapper = new ObjectMapper();
			
			try {
				JsonNode root = mapper.readTree(str);
				JsonNode gameScores = root.get("scoreboard").get("gameScore");
				if(gameScores.isArray()) {
					gameScores.forEach(gameScore -> {
		        		JsonNode game = gameScore.get("game");
		        		HashMap<String,String> gameDetail = new HashMap();
		        		gameDetail.put("time", game.get("time").asText());
		        		gameDetail.put("awayTeam", game.get("awayTeam").get("Abbreviation").asText());
		        		gameDetail.put("homeTeam", game.get("homeTeam").get("Abbreviation").asText());
		        		gameDetail.put("awayScore", gameScore.get("awayScore").asText());
		        		gameDetail.put("homeScore", gameScore.get("homeScore").asText());
		        		gameDetail.put("location", game.get("location").asText());
		        		gameDetails.add(gameDetail);
		        		
		        	});
		        }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return gameDetails;
	}
	
	public static List<HashMap<String, String>> getFavTeams(Set<String> favTeams) {
		
		List<HashMap<String, String>> favDetails = new ArrayList();
		if(favTeams != null) {
			for(String teamId: favTeams) {
				ResponseEntity<String> res = (ResponseEntity<String>) Services.getResponse(Constants.ALL_TEAMS_URL+"?team="+teamId, String.class);
				String strResponse = res.getBody(); 
				ObjectMapper map = new ObjectMapper();
				// get previous win /loss
				try {
					JsonNode root = map.readTree(strResponse);
			        JsonNode teamlogs = root.get("overallteamstandings").get("teamstandingsentry");
			        
			        if(teamlogs.isArray()) {
			        	teamlogs.forEach(teamlog -> {
			        		JsonNode team = teamlog.get("team");
			        		HashMap<String,String> teamDetail = new HashMap();
			        		teamDetail.put("Name", team.get("Name").asText());
			        		teamDetail.put("ID", team.get("ID").asText());
			        		teamDetail.put("Abbreviation", team.get("Abbreviation").asText());
			        		teamDetail.put("City", team.get("City").asText());
			        		favDetails.add(teamDetail);
			        	});
			        }
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
		return favDetails;
	
}
	public static List<String> getNotificaion(String teamID, String time,List<String> notificationDetails ) {
		
		ResponseEntity<String> response = (ResponseEntity<String>) Services.getResponse(Constants.TEAM_INFO+teamID+"&date="+time, String.class);
		String str = response.getBody(); 
		ObjectMapper mapper = new ObjectMapper();
		// get previous win/loss
		try {
			JsonNode root = mapper.readTree(str);
	        JsonNode gamelogs = root.get("teamgamelogs").get("gamelogs");
	        
	        if(gamelogs!=null && gamelogs.isArray()) {
	        	gamelogs.forEach(gamelog -> {
	        		JsonNode game = gamelog.get("game");
	        		StringBuilder noti= new StringBuilder();
	        		String won = gamelog.get("stats").get("Wins").get("#text").asText();
	        		
	        		if(game.get("homeTeam").get("ID").asText().equals("teamID") ) {
	        			noti.append(game.get("homeTeam").get("Abbreviation").asText());
	        			noti.append(" has ");
	        			if(won.equals("1")) {
	        				noti.append("won");
	        			}
	        			else {
	        				noti.append("lost");
	        			}
	        			noti.append(" match against ");
	        			noti.append(game.get("awayTeam").get("Abbreviation").asText());
	        			noti.append( " on "+game.get("date").asText());
	        		}
	        		else {
	        			noti.append(game.get("awayTeam").get("Abbreviation").asText());
	        			noti.append(" has ");
	        			if(won.equals("1")) {
	        				noti.append("won");
	        			}
	        			else {
	        				noti.append("lost");
	        			}
	        			noti.append(" match against ");
	        			noti.append(game.get("homeTeam").get("Abbreviation").asText());
	        			noti.append( " on "+game.get("date").asText());
	        		}
	        		System.out.println(noti.toString());
	        		notificationDetails.add(noti.toString());

	        	});
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	return notificationDetails;
	}
	
	
	}