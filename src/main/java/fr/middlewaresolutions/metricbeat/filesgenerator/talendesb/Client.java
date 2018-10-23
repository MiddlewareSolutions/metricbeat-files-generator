package fr.middlewaresolutions.metricbeat.filesgenerator.talendesb;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import fr.middlewaresolutions.metricbeat.filesgenerator.AbstractClient;

/**
 * Client déclanche la génération à distance.
 * 
 * @author emman
 *
 */
public class Client extends AbstractClient  {

	/** Properties for talend ESB */
	private static ResourceBundle rbTemplates = ResourceBundle.getBundle("talendesb");
	
	/** Pattern to find routes */
	private String camelRoutePattern = rbTemplates.getString("pattern");
	
	/**
	 * Call form command line.
	 * 
	 * Example: -Djmx.url=service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun -Djmx.user=tadmin -Djmx.pwd=tadmin -Dtarget.path=/etc/metricbeat/modules/
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String url = getVariable("jmx_url");
		String user = getVariable("jmx_user");
		String passwd = getVariable("jmx_pwd");
		String path = getVariable("target_path");

		/** Throw error if all parameters ar not specified */
		if (url == null)
			throw new Exception("jmx.url should be specified. ex: service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun");
		
		// Initialize MBean Client
		Client client = new Client();
		// start processing
		client.start(url, user, passwd, path);
		
	}
	
	/**
	 * start processing
	 * 
	 */
	public void start(String url, String user, String pwd, String path) {
		 try {
			 
			 // connect JVM
    		connectToJVM(url, user, pwd);
    		
    		// use prefix for files
    		String prefix = rbTemplates.getString("prefix");
    		
    		// For each route, generate a file
    		for(ObjectInstance oi: listMBeans(camelRoutePattern)) {
    			
    			ObjectName route = oi.getObjectName();
    	        String fileName = msc.getAttribute(route, "RouteId").toString();
    	            			
    	        // for this route, generate file
    			generateFile(path+prefix+fileName+".yml", mBean2File(oi));
    		}


        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	
	/**
	 * Generate content for one Object Instance
	 * @param oi
	 * @return
	 * @throws Exception 
	 */
	private StringBuffer mBean2File(ObjectInstance oi) 
	throws Exception {
		StringBuffer mbContent = new StringBuffer();
		
		ObjectName route = oi.getObjectName();
		String name = route.getCanonicalName(); 
        
		// get jolokia host
		String jolokiaHost = getVariable("jolokia_host");
		if (jolokiaHost == null)
			throw new Exception("Env jolokia.host is mandatory.");
		
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        
		// Utiliser le template jolokia
		String jolokia =  MessageFormat.format(
							rbTemplates.getString("jolokia"),
							name,
							jolokiaHost
							);
		
		// Utiliser le template http
		String http =  MessageFormat.format(
				rbTemplates.getString("http"),
				name,
				jolokiaHost
				);
		
		mbContent.append("# "+ date);
		mbContent.append("\n");
		mbContent.append(jolokia);
		mbContent.append("\n");
		mbContent.append(http);
		
		return mbContent;
	}
	
	
}
