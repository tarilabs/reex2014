package net.tarilabs.reex2014;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="camelRoutesAPI")
@RequestScoped
@Stateless
public class CamelRoutesAPI {
	final static Logger LOG = LoggerFactory.getLogger(CamelRoutesAPI.class);

	protected static final String RULE_ENGINE_JNDI = "java:global/reex2014/"+PseudoRealtimeRuleEngine.class.getSimpleName();
	
	@EJB
	private CamelBootstrap cb;
	
	// EJB POJO for rss route build 
	private String rssUrl;
	
	// EJB POJO for twitter route build
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;
	private String twitterUsername;
	
	public void addRssRoute(String rssUrl) throws Exception {
		LOG.info("addRssRoute() {}", rssUrl);
		final String param = rssUrl;
		cb.getCamelContext().addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("rss:"+param+"?splitEntries=true&throttleEntries=false&filter=true&consumer.delay=3600000")
					.log("insert a RSS")
					.to("ejb:" + RULE_ENGINE_JNDI + "?method=insert")
					;
			}
		});
	}
	
	public void addTwitterRoute(String twitterUsername) throws Exception {
		LOG.info("addTwitterRoute() {}", twitterUsername);
		final String twUser = twitterUsername;
		cb.getCamelContext().addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("twitter://timeline/user?user="
						+ twUser
						+ "&type=polling&delay=60"
						+ "&consumerKey=" + consumerKey
						+ "&consumerSecret=" + consumerSecret
						+ "&accessToken=" + accessToken
						+ "&accessTokenSecret=" + accessTokenSecret)
					.log("insert a Tweet ${body.getClass()} - ${body}")
					.to("ejb:" + RULE_ENGINE_JNDI + "?method=insert")
					;
			}
		});
	}
	
	public List<RouteDefinition> listRoutes() {
		List<RouteDefinition> ret = new ArrayList<RouteDefinition>();
		ModelCamelContext mctx = (ModelCamelContext) cb.getCamelContext();
		for (RouteDefinition r : mctx.getRouteDefinitions()) {
			ret.add(r);
		}
		return ret;
	}

	public String getRssUrl() {
		return rssUrl;
	}

	public void setRssUrl(String rssUrl) {
		this.rssUrl = rssUrl;
	}
	
	public Object addRssRouteCommand() {
		try {
			addRssRoute(rssUrl);
			rssUrl = null;
		} catch (Exception e) {
			// TODO put Flash message with error
			e.printStackTrace();
		}
		return "index";
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	public String getTwitterUsername() {
		return twitterUsername;
	}

	public void setTwitterUsername(String twitterUsername) {
		this.twitterUsername = twitterUsername;
	}

	public String addTwitterRouteCommand() {
		try {
			addTwitterRoute(twitterUsername);
			consumerKey = null;
			consumerSecret = null;
			accessToken = null;
			accessTokenSecret = null;
			twitterUsername = null;
		} catch (Exception e) {
			// TODO put Flash message with error
			e.printStackTrace();
		}
		return "index";
	}
}
