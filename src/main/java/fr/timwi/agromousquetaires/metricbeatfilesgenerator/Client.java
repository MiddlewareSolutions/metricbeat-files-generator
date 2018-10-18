package fr.timwi.agromousquetaires.metricbeatfilesgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Client déclanche la génération à distance.
 * 
 * @author emman
 *
 */
public class Client {

	/** Pattern to find routes */
	private String camelRoutePattern = "org.apache.camel:context=*,type=routes,name=*";
	
	/** Use to find MBeans */
	private MBeanServerConnection msc;
	
	/** Metricbeat base path*/
	private String basePath = "/etc/metricbeat/modules/";
	
	private static ResourceBundle rbTemplates = ResourceBundle.getBundle("templates");
	
	private Logger LOG = Logger.getLogger(this.getClass().getName());
	
	
	/**
	 * Appel depuis la ligne de commande.
	 * 
	 * Exempl: service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun tadmin tadmin /etc/metricbeat/modules/
	 * @param args
	 */
	public static void main(String[] args) {
		String url = args[0];
		String user = args[1];
		String passwd = args[2];
		String path = args[3];

		 try {
			 Map<String, String[]> env = new HashMap<String, String[]>();
			 String[] credentials = {user, passwd};
			 env.put(JMXConnector.CREDENTIALS, credentials);
			 
            JMXServiceURL jmxurl = new JMXServiceURL(url);
            JMXConnector jmxc = JMXConnectorFactory.connect(jmxurl, env);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

    		// Initialize MBean Client
    		Client client = new Client();
    		client.msc = mbsc;
    		
    		for(ObjectInstance oi: client.listRoutes()) {
    			
    			ObjectName route = oi.getObjectName();
    	        String routeId = client.msc.getAttribute(route, "RouteId").toString();
    	            			
    			client.generateFile(path+routeId+".yml", client.mBean2File(oi));
    		}


        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	/**
	 * Generate files for metricbeat monitoring
	 * 
	 * @throws MalformedObjectNameException
	 * @throws IOException
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public static void getFilesForMetricbeat() 
	throws MalformedObjectNameException, IOException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
		// Get the platform MBeanServer
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		// Initialize MBean Client
		Client client = new Client();
		client.msc = (MBeanServerConnection) mbs;
		
		for(ObjectInstance oi: client.listRoutes()) {
			client.generateFile(client.basePath, client.mBean2File(oi));
		}
	}
	
	/**
	 * List all routes
	 * @param msc
	 * @return
	 * @throws IOException 
	 * @throws MalformedObjectNameException 
	 */
	private Set<ObjectInstance> listRoutes() throws MalformedObjectNameException, IOException {
		Set<ObjectInstance> routes = msc.queryMBeans(new ObjectName(camelRoutePattern), null);
		
		return routes;
	}
	
	/**
	 * Generate content for one Object Instance
	 * @param oi
	 * @return
	 * @throws IOException 
	 * @throws ReflectionException 
	 * @throws MBeanException 
	 * @throws InstanceNotFoundException 
	 * @throws AttributeNotFoundException 
	 */
	private StringBuffer mBean2File(ObjectInstance oi) 
	throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		StringBuffer mbContent = new StringBuffer();
		
		/* CamelConfigBean existe ??
		SystemConfigMBean mbeanProxy =
	            (SystemConfigMBean) MBeanServerInvocationHandler.newProxyInstance(
	                mbeanServerConnection, mbeanName, SystemConfigMBean.class, true);
		*/
		
		ObjectName route = oi.getObjectName();
		String name = route.getCanonicalName(); 
        String routeId = msc.getAttribute(route, "RouteId").toString();
        String camelId = msc.getAttribute(route, "CamelId").toString();
        String camelMngtName = msc.getAttribute(route, "CamelManagementName").toString();
        
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        
		// Utiliser le template jolokia
		String jolokia =  MessageFormat.format(
							rbTemplates.getString("jolokia"),
							name
							);
		
		// Utiliser le template http
		String http =  MessageFormat.format(
				rbTemplates.getString("http"),
				name
				);
		
		mbContent.append("# "+ date);
		mbContent.append("\n");
		mbContent.append(jolokia);
		mbContent.append("\n");
		mbContent.append(http);
		
		return mbContent;
	}
	
	/**
	 * Generate a file with content
	 * @param path
	 * @param content
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	private void generateFile(String path, StringBuffer content) 
	throws FileNotFoundException, UnsupportedEncodingException {

		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println(content.toString());
		writer.close();

		LOG.info(path+ " generated.");
		// Owner of file should be root.
	}
	
	
	
}
