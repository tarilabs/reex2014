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
	
	@EJB
	private CamelBootstrap cb;
	
	private String rssUrl;
	
	public void addRssRoute(String rssUrl) throws Exception {
		LOG.info("addRssRoute() {}", rssUrl);
		final String param = rssUrl;
		cb.getCamelContext().addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("rss:"+param+"?splitEntries=true&throttleEntries=false&filter=true&consumer.delay=3600000")
					.log("insert a RSS")
					.to("ejb:java:global/reex2014/RealtimeRuleEngine?method=insert")
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
	
	public Object addRss() {
		try {
			addRssRoute(rssUrl);
			rssUrl = null;
		} catch (Exception e) {
			// TODO put Flash message with error
			e.printStackTrace();
		}
		return "index";
	}
}
