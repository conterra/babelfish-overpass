package de.conterra.babelfish.overpass.store;

import de.conterra.babelfish.overpass.plugin.OverpassFeature;
import de.conterra.babelfish.overpass.plugin.OverpassPopup;
import de.conterra.babelfish.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.joda.time.DateTime;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * defines a store of {@link OverpassPopup}s
 *
 * @author ChrissW-R1
 * @version 0.1.0
 * @since 0.1.0
 */
public class PopupStore {
	/**
	 * the delay after an {@link OverpassPopup} should be expire (in milliseconds)
	 *
	 * @since 0.1.0
	 */
	private static final long                     expire_delay    = 900000;
	/**
	 * the time to try again an Overpass API request
	 *
	 * @since 0.1.0
	 */
	private static final long                     try_again_delay = 15000;
	/**
	 * the {@link Timer}, which removes expired {@link OverpassPopup}s
	 *
	 * @since 0.1.0
	 */
	private static final Timer                    timer           = new Timer();
	/**
	 * the stored {@link OverpassPopup}
	 *
	 * @since 0.1.0
	 */
	private static       Map<Long, OverpassPopup> popups          = new ConcurrentHashMap<>();
	/**
	 * the expire times of every stored {@link OverpassPopup}
	 *
	 * @since 0.1.0
	 */
	private static       Map<Long, DateTime>      expires         = new ConcurrentHashMap<>();
	
	static {
		PopupStore.start();
	}
	
	/**
	 * private standard constructor, to prevent initialization
	 *
	 * @since 0.1.0
	 */
	private PopupStore() {
	}
	
	/**
	 * removes expired {@link OverpassPopup}s from the store
	 *
	 * @since 0.1.0
	 */
	private static void clean() {
		DateTime now = new DateTime();
		
		for (long id : PopupStore.expires.keySet()) {
			if (PopupStore.expires.get(id).isAfter(now)) {
				PopupStore.popups.remove(id);
				PopupStore.expires.remove(id);
			}
		}
	}
	
	/**
	 * gives an {@link OverpassPopup} of an {@link OverpassFeature}<br>
	 * If it is not stored, it will be created automatically
	 *
	 * @param id         the OpenStreetMap identifier to get the popup of
	 * @param entityType the {@link EntityType} of the requested object
	 * @return the {@link OverpassPopup} of the {@link Entity} with the identifier {@code id}
	 *
	 * @since 0.1.0
	 */
	public static OverpassPopup getPopup(long id, EntityType entityType) {
		PopupStore.clean();
		
		if (!(PopupStore.expires.containsKey(id))) {
			String content;
			String entityTypeName = entityType.name().toLowerCase();
			long   delay;
			
			try {
				String query = "[out:popup];" + entityTypeName + "(" + id + ");out;";
				
				InputStream inputStream = (new URL("http://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, StringUtils.UTF8.toString()))).openStream();
				content = IOUtils.toString(inputStream, StringUtils.UTF8);
				inputStream.close();
				delay = PopupStore.try_again_delay;
			} catch (IOException e) {
				content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
				content += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n";
				content += "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n";
				content += "<head>\r\n";
				content += "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" lang=\"en\"/>\r\n";
				content += "<title>Error on loading popup content!</title>\r\n";
				content += "</head>\r\n";
				content += "<body>\r\n";
				content += "<p align=\"center\"><strong>Couldn't load popup content from Overpass API!</strong></p>\r\n";
				content += "<p>The following exception occurred:<br />\r\n";
				content += "<code><pre>" + StringEscapeUtils.escapeHtml4(ExceptionUtils.getStackTrace(e)) + "</pre></code>\r\n";
				content += "</p>\r\n";
				content += "<p style=\"font-size:smaller\">OpenStreetMap object: <a href=\"https://www.openstreetmap.org/" + entityTypeName + "/" + id + "\" target=\"_blank\">" + id + "</a></p>\r\n";
				content += "</body>\r\n";
				content += "</html>\r\n";
				
				delay = PopupStore.expire_delay;
			}
			
			PopupStore.popups.put(id, new OverpassPopup(entityType, id, content));
			PopupStore.expires.put(id, (new DateTime()).plus(delay));
		}
		
		return PopupStore.popups.get(id);
	}
	
	/**
	 * removes all {@link OverpassPopup}s from this store
	 *
	 * @since 0.1.0
	 */
	public static void clear() {
		PopupStore.popups.clear();
		PopupStore.expires.clear();
	}
	
	/**
	 * starts the {@link Timer} to clear the cache automatically
	 *
	 * @since 0.1.0
	 */
	public static void start() {
		long delay = (long) (PopupStore.expire_delay * 0.975);
		
		PopupStore.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				PopupStore.clean();
			}
		}, delay, delay);
	}
	
	/**
	 * stops open tasks
	 *
	 * @since 0.1.0
	 */
	public static void stop() {
		PopupStore.timer.cancel();
	}
}
