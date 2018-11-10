package fr.middlewaresolutions.metricbeat.filesgenerator.talendesb;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

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
		String group = getVariable("group");

		/** Throw error if all parameters ar not specified */
		if (url == null)
			throw new Exception("jmx.url should be specified. ex: service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun");
		
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
    		for(ObjectInstance oi: listMBeans(camelRoutePattern)) {
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
	    			mbeans.append(mBean2JolokiaFile(oi));
	    		}
	    		
	    		mbeans.append("\n");
	    		
	    		// For each route, generate a file
	    		for(ObjectInstance oi: groupe) {
	    			
	    			ObjectName route = oi.getObjectName();
	    			mbeans.append(mBean2HttpFile(oi));
	    			mbeans.append("\n");
	    		}
	    		
	    		
	    	    String fileName = "talendesb-routes"+groupNumber;
	    	            			
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
	private StringBuffer mBean2JolokiaFile(ObjectInstance oi) 
	throws Exception {
		StringBuffer mbContent = new StringBuffer();
		
		ObjectName route = oi.getObjectName();
		String name = route.getCanonicalName(); 
                
		// Utiliser le template jolokia
		String jolokia =  MessageFormat.format(
							rbTemplates.getString("jolokia.mbean"),
							name
							);
		
		mbContent.append(jolokia);
		
		return mbContent;
	}
	
	/**
	 * Generate content for one Object Instance
	 * @param oi
	 * @return
	 * @throws Exception 
	 */
	private StringBuffer mBean2HttpFile(ObjectInstance oi) 
	throws Exception {
		StringBuffer mbContent = new StringBuffer();
		
		ObjectName route = oi.getObjectName();
		String name = route.getCanonicalName(); 
        
		// get jolokia host
		String jolokiaHost = getVariable("jolokia_host");
		if (jolokiaHost == null)
			throw new Exception("Env jolokia.host is mandatory.");
		
        String date = sdf.format(Calendar.getInstance().getTime());
		
		// Utiliser le template http
		String http =  MessageFormat.format(
				rbTemplates.getString("http"),
				name,
				jolokiaHost
				);
		
		mbContent.append(http);
		
		return mbContent;
	}
}
