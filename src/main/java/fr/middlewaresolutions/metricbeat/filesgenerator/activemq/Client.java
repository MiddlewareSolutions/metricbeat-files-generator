package fr.middlewaresolutions.metricbeat.filesgenerator.activemq;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
public class Client extends AbstractClient {

	/** Properties for talend ESB */
	private static ResourceBundle rbTemplates = ResourceBundle.getBundle("activemq");
	
	/** Pattern to find routes */
	private String queuesPattern = rbTemplates.getString("pattern");
	
	/**
	 * Call form command line.
	 * 
	 * Example: -Djmx.url=service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi -Djmx.user=admin -Djmx.pwd=admin -Dtarget.path=/etc/metricbeat/modules/
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String url = getVariable("jmx_url");
		String user = getVariable("jmx_user");
		String passwd = getVariable("jmx_pwd");
		String path = getVariable("target_path");
		String group = getVariable("group");

		/** Throw error if all parameters ar not specified */
		if (url == null)
			throw new Exception("jmx_url should be specified. ex: service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi");
		
		// Initialize MBean Client
		Client client = new Client();
		// start processing
		client.start(url, user, passwd, path, group);
		
	}
	
	/**
	 * start processing
	 * 
	 */
	public void start(String url, String user, String pwd, String path, String group) {
		 try {

    		 // connect JVM
    		connectToJVM(url, user, pwd);
    		
    		
    		
    		// get jolokia host
    		String jolokiaHost = getVariable("jolokia_host");
    		if (jolokiaHost == null)
    			throw new Exception("Env jolokia.host is mandatory.");
    		
    		HashMap<Integer, ArrayList<ObjectInstance>> map = new HashMap<Integer, ArrayList<ObjectInstance>>();
    		
    		Integer qte = Integer.MAX_VALUE;
    		if (group != null)
    			qte = Integer.valueOf(group);
    		
    		Integer cpt = 0;
    		Integer index = 0;
    		ArrayList<ObjectInstance> aGroup = new ArrayList<ObjectInstance>();
    		for(ObjectInstance oi: listMBeans(queuesPattern)) {
    			if (cpt++ < qte)
    				aGroup.add(oi);
    			else {
    				cpt = 0;
    				index++;
    				map.put(index, aGroup);
    				aGroup = new ArrayList<ObjectInstance>(); 
    			}
    		}
    		
    		for(Integer groupNumber: map.keySet()) {
    			ArrayList<ObjectInstance> groupe = map.get(groupNumber);
    			
	            String date = sdf.format(Calendar.getInstance().getTime());
	            
	    		// Utiliser le template jolokia
	    		String header =  MessageFormat.format(
	    							rbTemplates.getString("jolokia.header"),
	    							jolokiaHost
	    							);
	    		StringBuffer mbeans = new StringBuffer();
	    		
	    		mbeans.append("# "+ date);
	    		mbeans.append("\n");
	    		mbeans.append(header);
	    		
	    		// For each route, generate a file
	    		for(ObjectInstance oi: groupe) {
	    			
	    			ObjectName route = oi.getObjectName();
	    			mbeans.append(mBean2File(oi));
	    		}
	    		
	    		mbeans.append("\n");
	    		
	    	    String fileName = "activemq-queues"+groupNumber;
	    	            			
	    	    // for this route, generate file
	    		generateFile(path+fileName+".yml", mbeans);
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
		
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        
		// Utiliser le template jolokia
		String jolokia =  MessageFormat.format(
							rbTemplates.getString("jolokia.mbean"),
							name
							);
		
		mbContent.append(jolokia);
		
		return mbContent;
	}
	
	
}
