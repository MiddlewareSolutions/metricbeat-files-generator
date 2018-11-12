package fr.middlewaresolutions.metricbeat.filesgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
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

/**
 * Abstract client for metricbeat 
 * @author emman
 *
 */
public class AbstractClient {

	protected JMXConnector jmxc;
	
	/** Use to find MBeans */
	static protected MBeanServerConnection msc;
	
	/** Logger */
	protected Logger LOG = Logger.getLogger(this.getClass().getName());

	protected SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	protected SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMdd");
	
	public AbstractClient() {
		super();
	}

	/**
	 * Generate a file with content
	 * @param path
	 * @param content
	 * @throws IOException 
	 */
	protected void generateFile(String path, StringBuffer content)
	throws IOException {
			
		FileWriter fw = new FileWriter(path, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(content.toString());
	    bw.close();
	
		LOG.info(path+ " generated.");
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
	 * @throws IOException 
	 * 
	 */
	protected MBeanServerConnection connectToJVM(String url, String user, String pwd) throws IOException {
		
		if (jmxc == null) {

			// environments vars for JMX
			Map<String, String[]> env = new HashMap<String, String[]>();

			if (user != null) {
				// Set user and password
				String[] credentials = { user, pwd };
				env.put(JMXConnector.CREDENTIALS, credentials);
			}

			LOG.info("New connection to "+url);
			JMXServiceURL jmxurl = new JMXServiceURL(url);
			jmxc = JMXConnectorFactory.connect(jmxurl, env);

		} 
		
		msc = jmxc.getMBeanServerConnection();
		
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
	
	/**
	 * This file exits ?
	 * @param path
	 * @return
	 */
	protected boolean fileExist(String path) {
		File nFile = new File(path);
		return nFile.exists();
	}

	@Override
	protected void finalize() throws Throwable {
		jmxc.close();
		jmxc = null;
		
		super.finalize();
	}
	
	
}