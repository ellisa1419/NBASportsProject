package com.se.termproject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class AllTeams {
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/addToFavorite")
	public ModelAndView addToFavorite(@RequestParam("id") String teamID, HttpSession session) {
		
		Authentication authentication	= SecurityContextHolder.getContext().getAuthentication();
		String user_id = (String )authentication.getPrincipal();
		Optional<UserMaster> user = userRepository.findById(user_id);
		if(user.isPresent()) {
			Set<String> favTeams = user.get().getFavouriteTeams();
			favTeams.add(teamID);
			session.setAttribute("favTeams", favTeams);
			user.get().setFavouriteTeams(favTeams);
			userRepository.save(user.get());
		}
		return getTeams(session);
	}
	
	
	@GetMapping("/teams")
	public ModelAndView getTeams(HttpSession session) {
		ModelAndView showTeams = new ModelAndView("teams");
		ResponseEntity<NBATeamStanding> response = (ResponseEntity<NBATeamStanding>) Services.getResponse(Constants.ALL_TEAMS_URL, NBATeamStanding.class);
		NBATeamStanding ts = response.getBody(); 
        showTeams.addObject("teamStandingEntries", ts.getOverallteamstandings().getTeamstandingsentries());  
        showTeams.addObject("favTeams",session.getAttribute("favTeams"));
        return showTeams;
		
		
	}
	
	
	@GetMapping("/team")
	public ModelAndView getTeamInfo(@RequestParam("id") String teamID, HttpSession session) {
		ModelAndView teamInfo = new ModelAndView("teamInfo");
		List<HashMap<String, String>> gameDetails = new ArrayList();
		ResponseEntity<String> response = (ResponseEntity<String>) Services.getResponse(Constants.TEAM_INFO+teamID, String.class);
		String str = response.getBody(); 
		ObjectMapper mapper = new ObjectMapper();
		// get previous win/loss
		try {
			JsonNode root = mapper.readTree(str);
	        JsonNode gamelogs = root.get("teamgamelogs").get("gamelogs");
	        
	        if(gamelogs.isArray()) {
	        	gamelogs.forEach(gamelog -> {
	        		JsonNode game = gamelog.get("game");
	        		HashMap<String,String> gameDetail = new HashMap();
	        
	        		gameDetail.put("date", game.get("date").asText());
	        		gameDetail.put("location", game.get("location").asText());
	        		gameDetail.put("awayTeam", game.get("awayTeam").get("Abbreviation").asText());
	        		gameDetail.put("homeTeam", game.get("homeTeam").get("Abbreviation").asText());
	        		String won = gamelog.get("stats").get("Wins").get("#text").asText();
	        		if(won.equals("1")) {
	        			gameDetail.put("result", "Won");
	        		}
	        		else {
	        			gameDetail.put("result", "Lost");
	        		}
	        		gameDetails.add(gameDetail);
	        		String name = gamelog.get("team").get("Name").asText();
	        		teamInfo.addObject("teamName", name);
	        	});
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
		  
		// get schedule of match
		List<HashMap<String, String>> scheduleDetails = new ArrayList();
		ResponseEntity<String> scheduleResponse = (ResponseEntity<String>) Services.getResponse(Constants.SCHEDULE, String.class);
		String scheduleResponseStr = scheduleResponse.getBody(); 
		ObjectMapper objMapper = new ObjectMapper();
		
		try {
			JsonNode root = objMapper.readTree(scheduleResponseStr);
			JsonNode gamelogs = root.get("fullgameschedule").get("gameentry");
			if(gamelogs.isArray()) {
				gamelogs.forEach(gamelog -> {
					String awayTeam	= gamelog.get("awayTeam").get("ID").asText();
					String homeTeam	= gamelog.get("homeTeam").get("ID").asText();
					if(awayTeam.contains(teamID) ||  homeTeam.equals(teamID)) {
						HashMap<String,String> gameDetail = new HashMap();
						gameDetail.put("date",gamelog.get("date").asText());
						gameDetail.put("time",gamelog.get("time").asText());
						gameDetail.put("awayTeam",gamelog.get("awayTeam").get("Name").asText());
						gameDetail.put("homeTeam",gamelog.get("homeTeam").get("Name").asText());
						gameDetail.put("location",gamelog.get("location").asText());						
						scheduleDetails.add(gameDetail);
					}
				});
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		teamInfo.addObject("favTeams",session.getAttribute("favTeams") );
		teamInfo.addObject("teamId", teamID);
		teamInfo.addObject("scheduleDetails", scheduleDetails);     
		teamInfo.addObject("gameDetails", gameDetails);     
		return teamInfo;
	}
	
	@RequestMapping("/blockUser")
	public ModelAndView blockUser(@RequestParam("id") String teamID,HttpSession session) {
		Optional<UserMaster> user =userRepository.findById(teamID);
		UserMaster um =user.get();
		um.setBlocked(true);
		userRepository.save(um);
		
		return getTodayScore(session);
	}
	
	@GetMapping("/")
	public  ModelAndView getTodayScore( HttpSession session) {
		ModelAndView teamInfo = new ModelAndView("index");
		Authentication authentication	= SecurityContextHolder.getContext().getAuthentication();
		if(!authentication.getPrincipal().equals("anonymousUser")) {
			Optional<UserMaster> user = userRepository.findById((String)authentication.getPrincipal());
			if(user.isPresent()) {

				// if its not admin
				if (user.get().getRole() == null ||  !user.get().getRole().equals("Admin")) {
					
					// lets check if user is blocked
					if(user.get().isBlocked()) {
						teamInfo.addObject("messageBlocked", "This account has been blocked by admin!");
						SecurityContextHolder.getContext().setAuthentication(null);
						return teamInfo;
					}

						teamInfo.addObject("gameDetails", Services.getTodayScore()); 
						
						// get favorite teams here
						session.setAttribute("favTeams", user.get().getFavouriteTeams());
						
						Set<String> favTeams =(Set<String>) session.getAttribute("favTeams");
						
						if(favTeams != null) {
							List<String> notificationDetails = new ArrayList();
							teamInfo.addObject("favDetails", Services.getFavTeams(favTeams)); 
							// get notifications
							Date lastlogin =  user.get().getLastLogin();
							if(lastlogin!=null) {
								DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
								String[] time = dateFormat.format(lastlogin).split(" ");
								String finalTime = "since-"+time[0].replaceAll("/", "");	
								int i = 0;
								
								for (String team : favTeams) {
									if(i>1) {
										break;
									}
									notificationDetails =	Services.getNotificaion(team, finalTime, notificationDetails);
									i++;
								}
								
								
								
							}
							teamInfo.addObject("notificatons", notificationDetails);
							
						}						
						
						
				}
				// if its admin
				else {
					List<String> blockedUser = new ArrayList();
					List<HashMap<String, String>> allUserList= new ArrayList();
					Iterable<UserMaster> userList =userRepository.findAll();
					Iterator<UserMaster> iterator = userList.iterator();
					while(iterator.hasNext()) {
						UserMaster u =iterator.next();
						if(u.getName().equals(authentication.getPrincipal())){ // we don't want admin to block himself!
							continue;
						}
						if(u.isBlocked()) {
							blockedUser.add(u.getName());
						}
						HashMap<String,String> map= new HashMap();
						map.put("FacebookId", u.getName());
						map.put("FullName", u.getFull_name());
						allUserList.add(map);
						
					}
					
					teamInfo.addObject("blockedUser", blockedUser);
					teamInfo.addObject("userList", allUserList);
					session.setAttribute("isAdmin", "isAdmin");
					teamInfo.addObject("isAdmin", "isAdmin");
				}
			}
			
			// if user is not present, we delegate creating user to /user method called next and return team info
			else {
					teamInfo.addObject("gameDetails", Services.getTodayScore());
			}
			
			
			
				
		}
		return teamInfo;
	}
	
}