package fr.middlewaresolutions.logstash.filesgenerator.talendesb;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import fr.middlewaresolutions.metricbeat.filesgenerator.AbstractClient;

import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;

/**
 * Client déclanche la génération à distance.
 * 
 * @author emman
 *
 */
public class Client extends AbstractClient  {
	
	/**
	 * Call form command line.
	 * 
	 * Example: -Djmx.url=service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun -Djmx.user=tadmin -Djmx.pwd=tadmin -Dtarget.path=/etc/metricbeat/modules/
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		// the 'default' scheduler is defined in "quartz.properties" found
		// in the current working directory, in the classpath, or
		// resorts to a fall-back default that is in the quartz.jar

		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler = sf.getScheduler();

		// Scheduler will not execute jobs until it has been started (though they can be scheduled before start())
		scheduler.start();
		
		String url = getVariable("jmx_url");
		String user = getVariable("jmx_user");
		String passwd = getVariable("jmx_pwd");
		String path = getVariable("target_path");

		/** Throw error if all parameters ar not specified */
		if (url == null)
			throw new Exception("jmx.url should be specified. ex: service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun");
		
		// define the job and tie it to our HelloJob class
		  JobDetail job = newJob(TalendESBJob.class)
				  .withIdentity("talendesb", "jolokia") // name "myJob", group "group1"
				  .usingJobData("url", url)
				  .usingJobData("user", user)
				  .usingJobData("passwd", passwd)
				  .usingJobData("path", path)
				  .build();

		  // Trigger the job to run now, and then every 40 seconds
		  Trigger trigger =newTrigger()
		      .withIdentity("triggerJolokia", "jolokia")
		      .startNow()
		      .withSchedule(CronScheduleBuilder.cronSchedule(cronPerMinute))       
		      .build();

		  // Tell quartz to schedule the job using our trigger
		  scheduler.scheduleJob(job, trigger);
		
	}
	
}
