/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whichepisode;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import static java.lang.System.in;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WhichEpisode {
    final private String url = "http://www.omdbapi.com/";
    final private String charset = "UTF-8";
    final static String LATEST = "latest";
    final static String NEXT = "next";
    private String title = "";
    private String season = "1";
    private JsonNode previousEpisode;
    
    public WhichEpisode(String title) {
        this.title = title;
    }
    
    public static void main(String[] args) {
        try {
            if(args.length < 2)
            {
                System.out.println("Syntax: whichepisode series_title episode_option\nepisode_option: latest\tnext");
                return;
            }
            
            String option = "";
            if(args[1].toLowerCase().equals(WhichEpisode.LATEST))
            {
                option = WhichEpisode.LATEST;
            }
            else if(args[1].toLowerCase().equals(WhichEpisode.NEXT))
            {
                option = WhichEpisode.NEXT;
            }
            else
            {
                System.out.println("Syntax: whichepisode series_title episode_option\nepisode_option: latest\tnext");
                return;
            }
            
            WhichEpisode we = new WhichEpisode(args[0]);
            InputStream response = we.sendHTTPGETRequest();
            String json = we.getJSON(response);
            int totalSeasons = we.getNumOfSeasons(json);
            
            for(int i=1; i<=totalSeasons; i++) {
                we.setSeason(i);
                response = we.sendHTTPGETRequest();
                json = we.getJSON(response);
            
                if(we.findEpisode(json, option))
                    break;
            }
                        
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WhichEpisode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(WhichEpisode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WhichEpisode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setSeason(int season) {
        this.season = Integer.toString(season);
    }
    
    public InputStream sendHTTPGETRequest() throws IOException {
        String query = String.format("t=%s&season=%s",
                    URLEncoder.encode(title, charset),
                    URLEncoder.encode(season, charset));
            
            URLConnection connection = new URL(url + "?" + query).openConnection();
            connection.setRequestProperty("Accept-Charset", charset);
            return connection.getInputStream();
    }
    
    public String getJSON(InputStream response) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(response, "UTF-8")); 
        return br.readLine();
    }
    
    public int getNumOfSeasons(String json) throws IOException {
        JsonNode rootNode = new ObjectMapper().readTree(json);
        String totalSeasons = rootNode.get("totalSeasons").toString().replace("\"", "");
        return Integer.parseInt(totalSeasons);
    }
    
    public boolean findEpisode (String json, String option) throws IOException {
        JsonNode rootNode = new ObjectMapper().readTree(json);
        JsonNode episodes = rootNode.get("Episodes");
        for(int i=0; i<episodes.size(); i++) {
            JsonNode episode = episodes.get(i);
            String cleanDate = episode.get("Released").toString().replace("\"", "");
            String[] date = cleanDate.split("-");
            
            int year = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int day = Integer.parseInt(date[2]);
            
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            
            Calendar epDate = Calendar.getInstance();
            epDate.set(year,month-1, day, 0, 0, 0);//year, month-1, day);
            
           if(today.compareTo(epDate) < 0 || today.compareTo(epDate) == 0)
            {
                if(option.equals(WhichEpisode.LATEST)) 
                {
                    System.out.println(rootNode.get("Title").toString().replace("\"", "") + " S" + season + "E" + previousEpisode.get("Episode").toString().replace("\"", "")+ " " + previousEpisode.get("Title") + " at "  + previousEpisode.get("Released").toString().replace("\"", "") + "\n");
                    return true;
                }
                System.out.println(rootNode.get("Title").toString().replace("\"", "") + " S" + season + "E" + episode.get("Episode").toString().replace("\"", "")+ " " + episode.get("Title") + " at "  + episode.get("Released").toString().replace("\"", "") + "\n");
                return true;
            }
           
            previousEpisode = episode;
        }
        
        return false;
    }  
}
