package fr.middlewaresolutions.metricbeat.filesgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import fr.middlewaresolutions.metricbeat.filesgenerator.activemq.Client;

/**
 * Abstract client for metricbeat 
 * @author emman
 *
 */
public class AbstractClient {

	/** Use to find MBeans */
	protected MBeanServerConnection msc;
	
	/** Logger */
	protected Logger LOG = Logger.getLogger(this.getClass().getName());

	public AbstractClient() {
		super();
	}

	/**
	 * Generate a file with content
	 * @param path
	 * @param content
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	protected void generateFile(String path, StringBuffer content)
	throws FileNotFoundException, UnsupportedEncodingException {
			
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println(content.toString());
		writer.close();
	
		LOG.info(path+ " generated.");
		// Owner of file should be root.
	}

	/**
	 * Get variables used in call
	 * 
	 * @param name
	 * @return
	 */
	protected static String getVariable(String name) {
		return System.getenv( name );
	}
	
	/**
	 * Connect to JVM
	 * 
	 */
	protected MBeanServerConnection connectToJVM(String url, String user, String pwd) {
		
		try {
			// environments vars for JMX
			Map<String, String[]> env = new HashMap<String, String[]>();

			if (user != null) {
				// Set user and password
				String[] credentials = { user, pwd };
				env.put(JMXConnector.CREDENTIALS, credentials);
			}

			JMXServiceURL jmxurl = new JMXServiceURL(url);
			JMXConnector jmxc = JMXConnectorFactory.connect(jmxurl, env);

			msc = jmxc.getMBeanServerConnection();

		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return null;
		}
		
		return msc;
	}
	
	/**
	 * List all mbeans
	 * 
	 * @param pattern
	 * @return
	 * @throws IOException 
	 * @throws MalformedObjectNameException 
	 */
	protected Set<ObjectInstance> listMBeans(String pattern) 
	throws MalformedObjectNameException, IOException {
		
		Set<ObjectInstance> mbeans = msc.queryMBeans(new ObjectName(pattern), null);
		return mbeans;
	}
}