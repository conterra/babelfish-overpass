package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.overpass.config.ServiceType;
import de.conterra.babelfish.overpass.config.Services;
import de.conterra.babelfish.overpass.store.FeatureStore;
import de.conterra.babelfish.overpass.store.PopupStore;
import de.conterra.babelfish.plugin.Plugin;
import de.conterra.babelfish.plugin.PluginAdapter;
import de.conterra.babelfish.plugin.ServiceContainer;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URISyntaxException;

/**
 * defines a {@link Plugin} to send REST requests to the Overpass API
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.1.0
 */
@Slf4j
public class OverpassPlugin
		implements Plugin {
	/**
	 * the only instance of a {@link OverpassPlugin}<br>
	 * (singleton pattern)
	 *
	 * @since 0.1.0
	 */
	public static final OverpassPlugin INSTANCE = new OverpassPlugin();
	
	/**
	 * standard constructor
	 *
	 * @since 0.1.0
	 */
	public OverpassPlugin() {
	}
	
	@Override
	public String getName() {
		return "Overpass";
	}
	
	@Override
	public boolean init() {
		FeatureStore.start();
		PopupStore.start();
		
		boolean res = true;
		
		try {
			File servicesFolder = new File(new File(PluginAdapter.getPluginFolder(OverpassPlugin.INSTANCE).toURI()), "services");
			
			if (servicesFolder.exists() || servicesFolder.mkdirs()) {
				JAXBContext context = JAXBContext.newInstance(Services.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				
				for (File file : servicesFolder.listFiles()) {
					try {
						Services services = (Services) (unmarshaller.unmarshal(file));
						
						for (ServiceType xmlService : services.getService()) {
							if (!(ServiceContainer.registerService(new OverpassFeatureService(xmlService))))
								res = false;
						}
					} catch (JAXBException | ClassCastException e) {
						String msg = "Could not load a valid Overpass configuration from: " + file.getName();
						log.warn(msg, e);
					}
				}
			} else
				log.error("Couldn't create directory \'services\' in which the service configurations must stored!");
		} catch (NullPointerException | URISyntaxException | JAXBException e) {
			String msg = "Exception occurred: " + e.getMessage();
			log.error(msg, e);
			throw new NullPointerException(msg);
		}
		
		return res;
	}
	
	@Override
	public boolean shutdown() {
		FeatureStore.stop();
		PopupStore.stop();
		PopupStore.clear();
		
		boolean res = true;
		for (OverpassFeatureService service : OverpassFeatureService.SERVICES.values()) {
			OverpassFeatureService.SERVICES.remove(service);
			if (!ServiceContainer.unregisterService(service))
				res = false;
		}
		
		return res;
	}
}