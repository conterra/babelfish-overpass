package de.conterra.babelfish.overpass;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.conterra.babelfish.overpass.config.OverpassConfigType;
import de.conterra.babelfish.overpass.plugin.OverpassPlugin;
import de.conterra.babelfish.plugin.PluginAdapter;

/**
 * defines a class to store configuration
 * 
 * @version 0.1.0
 * @author chwe
 * @since 0.1.0
 */
public class OverpassConfigStore
{
	/**
	 * the {@link Logger} of this class
	 * 
	 * @since 0.0.1
	 */
	public static final Logger	LOGGER	= LoggerFactory.getLogger(OverpassConfigStore.class);
										
	/**
	 * {@link URL} to the Overpass service
	 * 
	 * @since 0.1.0
	 */
	public static final String	SERVICE_URL;
	/**
	 * the time to wait before retry an Overpass request (in milliseconds)
	 * 
	 * @since 0.1.0
	 */
	public static final long	RETRY_DELAY;
	/**
	 * the timeout of a Overpass request (in seconds)
	 * 
	 * @since 0.1.0
	 */
	public static final int		REQUEST_TIMEOUT;
								
	static
	{
		String serviceUrl = "http://overpass-api.de/api/";
		long retryDelay = 1000;
		int requestTimeout = 30;
		
		try
		{
			File configFile = new File(new File(PluginAdapter.getPluginFolder(OverpassPlugin.INSTANCE).toURI()), "config.xml");
			
			JAXBContext conext = JAXBContext.newInstance(OverpassConfigType.class);
			Unmarshaller unmarshaller = conext.createUnmarshaller();
			
			OverpassConfigType config = (OverpassConfigType)unmarshaller.unmarshal(configFile);
			
			serviceUrl = config.getServiceUrl();
			retryDelay = config.getRetryDelay();
			requestTimeout = config.getRequestTimeout();
		}
		catch (NullPointerException | URISyntaxException | JAXBException | ClassCastException e)
		{
			OverpassConfigStore.LOGGER.warn("Not able to load configuration file! Using standard values instead.", e);
		}
		
		SERVICE_URL = serviceUrl;
		RETRY_DELAY = retryDelay;
		REQUEST_TIMEOUT = requestTimeout;
		
		OverpassConfigStore.LOGGER.debug("Using Overpass service on " + serviceUrl);
	}
	
	/**
	 * private standard constructor, to prevent initialization
	 * 
	 * @since 0.1.0
	 */
	private OverpassConfigStore()
	{
	}
}