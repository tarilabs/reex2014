package net.tarilabs.reex2014;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

@ManagedBean(name="engineAPIBean")
@RequestScoped
@Stateless
@Path("ruleengine")
public class RealtimeRuleEngineRestAPI {
	@EJB
	private RealtimeRuleEngine engine;
	
	private String dummyTweetText;

	@GET
	@Path("query")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> query() {
		return engine.queries();
	}
	
	@GET
	@Path("query/{queryName}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, Object>> query(@PathParam("queryName") String queryName) {
		return engine.query(queryName);
	}
	
	public Date convertTime(long time){
	    return new Date(time);
	}
	
	public List<Alert<?>> listAlerts() {
		return engine.listAlerts();
	}
	
	public Map<String, AtomicInteger> groupByClassAndCount() {
		return engine.groupByClassAndCount();
	}
	
	public String getDummyTweetText() {
		return dummyTweetText;
	}

	public void setDummyTweetText(String dummyTweetText) {
		this.dummyTweetText = dummyTweetText;
	}

	public String insertDummyTweet() {
		try {
			if (dummyTweetText != null && !dummyTweetText.equals("")) {
				Status dummyTweet = tweet(dummyTweetText);
				engine.insert(dummyTweet);
				dummyTweetText = null;
			}
		} catch (Exception e) {
			// TODO put Flash message with error
			e.printStackTrace();
		}
		return "index";
	}
	
	/**
	 * Make a dummy Status twitter, with the supplied text. Please note this is only maintained for the text field, other attributes may be not maintained (eg.: hashtag, users, etc.)
	 * 
	 * @param text the tweet's text
	 * @return dummy Status twitter with the supplied text
	 * @throws TwitterException 
	 */
	private Status tweet(String text) throws TwitterException {
		String tweetJson = "{\"contributors\":null,"
				+ "\"text\":\""+text+"\","
				+ "\"geo\":null,\"retweeted\":false,\"in_reply_to_screen_name\":null,\"truncated\":false,"
				+ "\"lang\":\"en\",\"entities\":{\"symbols\":[],\"urls\":[],"
				+ "\"hashtags\":[],\"user_mentions\":[]},\"in_reply_to_status_id_str\":null,"
				+ "\"id\":476021012371632120,\"source\":\"<a href=\\\"http://www.hootsuite.com\\\" rel=\\\"nofollow\\\">Hootsuite<\\/a>\",\"in_reply_to_user_id_str\":null,\"favorited\":false,\"in_reply_to_status_id\":null,\"retweet_count\":0,\"created_at\":\"Mon Jun 09 15:20:28 +0000 2014\",\"in_reply_to_user_id\":null,\"favorite_count\":0,\"id_str\":\"476021012371632120\","
				+ "\"place\":null,\"user\":{\"location\":\"Milano\",\"default_profile\":false,\"profile_background_tile\":false,\"statuses_count\":18715,\"lang\":\"it\",\"profile_link_color\":\"CC0033\",\"profile_banner_url\":\"https://pbs.twimg.com/profile_banners/988355810/1399291563\",\"id\":988355810,\"following\":false,\"protected\":false,\"favourites_count\":45,\"profile_text_color\":\"333333\","
				+ "\"description\":\" Asd\",\"verified\":false,\"contributors_enabled\":false,\"profile_sidebar_border_color\":\"FFFFFF\","
				+ "\"name\":\"Asd\",\"profile_background_color\":\"E5E5E5\",\"created_at\":\"Tue Dec 04 08:48:07 +0000 2012\",\"is_translation_enabled\":false,\"default_profile_image\":false,\"followers_count\":31469,\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/3148489174/d864431a925aba1d9f701120fe294442_normal.png\",\"geo_enabled\":false,\"profile_background_image_url\":\"http://pbs.twimg.com/profile_background_images/378800000182749090/321-pxMY.png\",\"profile_background_image_url_https\":\"https://pbs.twimg.com/profile_background_images/378800000182749090/321-pxMY.png\",\"follow_request_sent\":false,\"entities\":{\"description\":{\"urls\":[]},\"url\":{\"urls\":[{\"expanded_url\":\"http://\",\"indices\":[0,22],\"display_url\":\"\",\"url\":\"\"}]}},\"url\":\"\",\"utc_offset\":7200,\"time_zone\":\"Rome\",\"notifications\":false,\"profile_use_background_image\":true,\"friends_count\":79,\"profile_sidebar_fill_color\":\"DDEEF6\","
				+ "\"screen_name\":\"ASd\",\"id_str\":\"988355810\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/3148489174/d864431a925aba1d9f701120fe294442_normal.png\",\"listed_count\":281,\"is_translator\":false},\"coordinates\":null}";
		Status status = (Status) DataObjectFactory.createObject(tweetJson);
		return status;
	}
}
