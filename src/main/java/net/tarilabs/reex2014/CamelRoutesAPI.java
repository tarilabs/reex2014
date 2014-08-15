package net.tarilabs.reex2014;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="camelRoutesAPI")
@RequestScoped
@Stateless
public class CamelRoutesAPI {
	final static Logger LOG = LoggerFactory.getLogger(CamelRoutesAPI.class);

	// I've tried java:app and java:module JNDI but doesn't cope well with Camel ejb: component. So this solution works:
	
	@Resource(lookup="java:app/AppName")
	private String appName;
	
	private String getRuleEngineJNDI() {
		return "java:global/"
				+ appName
				+ "/"
				+ PseudoRealtimeRuleEngine.class.getSimpleName();
	}
	
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
		final String feedURL = rssUrl;
		cb.getCamelContext().addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("rss:" + feedURL 
						+ ((feedURL.contains("?")) ? "&" : "?")
						+ "splitEntries=true&throttleEntries=false&filter=true&consumer.delay=3600000")
					.log("insert a RSS")
					.to("ejb:" + getRuleEngineJNDI() + "?method=insert")
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
					.to("ejb:" + getRuleEngineJNDI() + "?method=insert")
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
	
	public static String formatInputFromDefinition(FromDefinition from) {
		URI uri = URI.create(from.getEndpointUri());
		if (uri.getScheme().startsWith("twitter")) {
			return formatInputFromURI(uri);
		}
		return uri.toString();
	}

	public static String formatInputFromURI(URI uri) {
		Map<String, List<String>> splitQuery = new HashMap<String, List<String>>();
		try {
			splitQuery = splitQuery(uri);
		} catch (Exception e) {
			// do nothing.
		}
		String[] hide = new String[]{"consumerKey" , "consumerSecret","accessToken","accessTokenSecret"};
		for (String key : hide) {
			if (splitQuery.containsKey(key)) {
				List<String> hidden = new ArrayList<String>();
				hidden.add("[hidden]");
				splitQuery.put(key, hidden);
			}
		}
		return uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "?" + splitQuery;
	}
	
	// credits http://stackoverflow.com/a/13592567/893991 (without using ext libs) 
	public static Map<String, List<String>> splitQuery(URI url)	throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		final String[] pairs = url.getQuery().split("&");
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!query_pairs.containsKey(key)) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
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
