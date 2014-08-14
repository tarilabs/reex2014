package net.tarilabs.reex2014;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * This is for bootstrapping Camel as embedded and expose via Singleton EJB
 * @author mmortari
 *
 */
@Singleton
@Startup
public class CamelBootstrap {
	protected CamelContext camelContext;
	protected ProducerTemplate producerTemplate;

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public ProducerTemplate getProducerTemplate() {
		return producerTemplate;
	}
	
	@PostConstruct
	protected void init() throws Exception {
		camelContext = new DefaultCamelContext();
		camelContext.addRoutes(new Reex2014CamelRoutes());
		camelContext.start();
		producerTemplate = camelContext.createProducerTemplate();
	}
}
