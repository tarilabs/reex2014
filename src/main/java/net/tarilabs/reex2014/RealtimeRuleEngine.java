package net.tarilabs.reex2014;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rememebr by default it would be accessed NOT concurrently, which is better.
 * @author mmortari
 *
 */
@Singleton
@Startup
public class RealtimeRuleEngine {
	static Logger LOG = LoggerFactory.getLogger(RealtimeRuleEngine.class);

	private KieBase kieBase;
	private KieSession kieSession;

	private Thread st;

	@PostConstruct
	public void init() {
		KieServices kieServices = KieServices.Factory.get();
		KieContainer kContainer = kieServices.getKieClasspathContainer();
        KieBaseConfiguration kieBaseConf = kieServices.newKieBaseConfiguration();
		kieBaseConf.setOption( EventProcessingOption.STREAM );
        kieBase = kContainer.newKieBase(kieBaseConf);
        
        for ( KiePackage kp : kieBase.getKiePackages() ) {
        	for (Rule rule : kp.getRules()) {
        		LOG.info("kp " + kp + " rule " + rule.getName());
        	}
        }
        
        LOG.info("Creating kieSession");
        KieSessionConfiguration config = kieServices.newKieSessionConfiguration();
        kieSession = kieBase.newKieSession(config, null);
        
        // TODO this on JavaEE 7 can be done much differently.
        st = new Thread() { 
            @Override
            public void run() {
            	kieSession.fireUntilHalt();
            }
        };
        st.start();
        
        LOG.info("init() end.");
	}
	
	public void insert(Object arg1) {
		LOG.info("insert() {}", arg1.getClass()); 
		LOG.debug("insert() {}", arg1);
		kieSession.insert(arg1);
		LOG.info("insert() {} end.", arg1.getClass());
	}
	
	@PreDestroy
	public void shutdown() {
		LOG.info("shutdown() initiated.");
		kieSession.halt();
		
		LOG.info("shutdown() end.");
	}

	public List<Map<String, Object>> query(String queryName) {
		QueryResults results = kieSession.getQueryResults(queryName);
		String[] identifiers = results.getIdentifiers();
		List<Map<String, Object>> ret =  new ArrayList<Map<String, Object>>();
		for ( QueryResultsRow o : results ) {
			HashMap<String, Object> res = new HashMap<String, Object>();
			for (String id : identifiers) {
				res.put(id, o.get(id));
			}
			ret.add(res);
		}
		return ret;
	}

	public List<String> queries() {
		List<String> queryNames = new ArrayList<String>();
		for (KiePackage kp : kieSession.getKieBase().getKiePackages()) {
			for (Query q : kp.getQueries()) {
				queryNames.add(q.getName());
			}
		}
		return queryNames;
	}
	
	public List<Alert<?>> listAlerts() {
		List<Alert<?>> ret = new ArrayList<Alert<?>>();
		@SuppressWarnings("unchecked")
		Collection<Alert<?>> sessionObjects = (Collection<Alert<?>>) kieSession.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object arg0) {
				return (arg0 instanceof Alert) ? true : false;
			}
		});
		ret.addAll(sessionObjects);
		return ret;
	}
	
	public Map<String, AtomicInteger> groupByClassAndCount() {
		Map<String, AtomicInteger> ret = new HashMap<String, AtomicInteger>();
		
		for (Object objects : kieSession.getObjects()) {
			String className = objects.getClass().getName();
			AtomicInteger cnt = ret.get(className);
			if (cnt == null) {
				AtomicInteger newCnt = new AtomicInteger(0);
				ret.put(className, newCnt);
				cnt = newCnt;
			}
			cnt.incrementAndGet();
		}
		
		return ret;
	}
}
