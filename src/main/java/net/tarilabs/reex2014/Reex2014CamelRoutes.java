package net.tarilabs.reex2014;

import org.apache.camel.builder.RouteBuilder;

public class Reex2014CamelRoutes extends RouteBuilder {

	@Override
	public void configure() throws Exception {
//		from("rss:http://localhost:8080/reex2014-testRssSources/testRssSources/download.xml?splitEntries=true&throttleEntries=false&filter=true&consumer.delay=3600000")
//			.log("insert a RSS")
//			.to("ejb:java:global/reex2014/RealtimeRuleEngine?method=insert");
	}


}
