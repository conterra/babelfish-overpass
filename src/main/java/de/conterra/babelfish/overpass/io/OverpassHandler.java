package de.conterra.babelfish.overpass.io;

import crosby.binary.osmosis.OsmosisReader;
import de.conterra.babelfish.overpass.config.OverpassConfigStore;
import de.conterra.babelfish.plugin.v10_02.object.geometry.*;
import de.conterra.babelfish.util.GeoUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * defines a class to handle the Overpass API connection
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.1.0
 */
@Slf4j
public class OverpassHandler {
	/**
	 * the default {@link CoordinateReferenceSystem} of the OpenStreetMap database
	 *
	 * @since 0.1.0
	 */
	public static final CoordinateReferenceSystem OSM_CRS;
	/**
	 * the placeholder of a bounding box
	 *
	 * @since 0.1.0
	 */
	public static final String BBOX_PLACEHOLDER = "{{bbox}}";
	
	static {
		CoordinateReferenceSystem crs;
		
		try {
			crs = GeoUtils.decodeCrs("4326");
		} catch (FactoryException e) {
			crs = DefaultGeographicCRS.WGS84;
		}
		
		OSM_CRS = crs;
	}
	
	/**
	 * private standard constructor, to prevent initialization
	 *
	 * @since 0.1.0
	 */
	private OverpassHandler() {
	}
	
	/**
	 * gives the {@link InputStream} to the requested OpenStreetMap data
	 *
	 * @param script the Overpass API script the request on the server
	 * @param bbox   the bounding box
	 * @return an {@link InputStream} to the requested data
	 *
	 * @throws IllegalArgumentException if the script contains no placeholder of the bounding box
	 * @throws IOException              if an error occurred on request the data from the Overpass API
	 * @since 0.1.0
	 */
	private static InputStream request(String script, Envelope bbox)
	throws IllegalArgumentException, IOException {
		if (!script.contains(OverpassHandler.BBOX_PLACEHOLDER)) {
			throw new IllegalArgumentException("The script must contain one or more \"{{bbox}}\"!");
		}
		
		String timeoutStartTag = "[timeout:";
		
		int idxTimeoutStart = script.indexOf(timeoutStartTag);
		int idxTimeoutEnd   = idxTimeoutStart < 0 ? 0 : script.indexOf("]", idxTimeoutStart);
		
		if (idxTimeoutStart < 0) {
			idxTimeoutStart = 0;
		}
		
		String timeoutEndTag = idxTimeoutEnd <= 0 ? "];" : StringUtils.EMPTY;
		
		script = script.substring(0, idxTimeoutStart) + timeoutStartTag + OverpassConfigStore.REQUEST_TIMEOUT + timeoutEndTag + script.substring(idxTimeoutEnd);
		
		if (!script.contains("out ")) {
			script += "out meta;";
		}
		
		if (bbox != null) {
			DirectPosition lowerCorner;
			DirectPosition upperCorner;
			
			try {
				lowerCorner = GeoUtils.transform(bbox.getLowerCorner(), OverpassHandler.OSM_CRS);
				upperCorner = GeoUtils.transform(bbox.getUpperCorner(), OverpassHandler.OSM_CRS);
			} catch (TransformException e) {
				String msg = "Couldn't transform requested CRS to " + OverpassHandler.OSM_CRS.getName().getCode() + "!";
				log.error(msg, e);
				throw new IOException(msg, e);
			}
			
			String bboxString = lowerCorner.getOrdinate(0) + "," + lowerCorner.getOrdinate(1) + "," + upperCorner.getOrdinate(0) + "," + upperCorner.getOrdinate(1);
			
			script = script.replace(OverpassHandler.BBOX_PLACEHOLDER, bboxString);
		} else {
			script = script.replace("(" + OverpassHandler.BBOX_PLACEHOLDER + ")", StringUtils.EMPTY);
			script = script.replace(OverpassHandler.BBOX_PLACEHOLDER, StringUtils.EMPTY);
		}
		
		try {
			int attempts = 0;
			
			while (attempts < 3) {
				attempts++;
				
				log.debug("Request features from the Overpass API with the following script: " + script);
				
				URLConnection connection = (new URL(OverpassConfigStore.SERVICE_URL + "interpreter?data=" + URLEncoder.encode(script, "UTF-8"))).openConnection();
				connection.connect();
				
				if (connection instanceof HttpURLConnection) {
					HttpURLConnection httpConnection = (HttpURLConnection) connection;
					
					int statusCode = httpConnection.getResponseCode();
					
					log.debug("Overpass API returned HTTP status code " + statusCode + ".");
					
					if (statusCode == 400) {
						String msg = "An error occured during the execution of the overpass query! This is what overpass API returned:\r\n";
						msg += IOUtils.toString(httpConnection.getErrorStream());
						
						log.error(msg);
						throw new IOException(msg);
					} else if (statusCode == 429 || statusCode == 504) {
						IOUtils.close(httpConnection);
						
						log.debug("Close connection and retry after " + OverpassConfigStore.RETRY_DELAY + " milliseconds.");
						
						long stopTime = System.currentTimeMillis() + OverpassConfigStore.RETRY_DELAY;
						while (System.currentTimeMillis() < stopTime) {
							;
						}
						
						continue;
					}
					
					return httpConnection.getInputStream();
				} else {
					IOUtils.close(connection);
					throw new IOException("The URL didn't point to a connection using HTTP!");
				}
			}
		} catch (IOException e) {
			String msg = "An error occurred while request the Overpass API!";
			log.error(msg, e);
			throw new IOException(msg, e);
		}
		
		throw new IOException("An unkown error occurred while waiting for Overpass API request!");
	}
	
	/**
	 * gives the {@link EntityType}, which the given {@link Class} corresponds to
	 *
	 * @param clazz the {@link Class} to get the {@link EntityType} of
	 * @return the {@link EntityType} of the given {@link Class}
	 *
	 * @since 0.1.0
	 */
	public static EntityType typeFromClass(Class<? extends Entity> clazz) {
		if (Relation.class.isAssignableFrom(clazz)) {
			log.debug(clazz + " is a relation.");
			
			return EntityType.Relation;
		} else if (Way.class.isAssignableFrom(clazz)) {
			log.debug(clazz + " is a way.");
			
			return EntityType.Way;
		}
		
		log.debug("Couldn't identify " + clazz + " as a relation or way. Return default value node.");
		
		return EntityType.Node;
	}
	
	/**
	 * gives the {@link Entity} {@link Class} representation of {@link GeometryObject} {@link Class}
	 *
	 * @param clazz the {@link GeometryObject} {@link Class} to get the {@link Entity} {@link Class} of
	 * @return the {@link Entity} {@link Class} of the given {@link GeometryObject} {@link Class}
	 *
	 * @since 0.1.0
	 */
	public static Class<? extends Entity> entityClassFromGeometry(Class<? extends GeometryObject> clazz) {
		if (MultiLine.class.isAssignableFrom(clazz) || Multipoint.class.isAssignableFrom(clazz)) {
			log.debug(clazz + " is a relation.");
			
			return Relation.class;
		} else if (Polygon.class.isAssignableFrom(clazz) || de.conterra.babelfish.plugin.v10_02.object.geometry.Envelope.class.isAssignableFrom(clazz) || Polyline.class.isAssignableFrom(clazz)) {
			log.debug(clazz + " is a way.");
			
			return Way.class;
		}
		
		log.debug("Couldn't identify " + clazz + " as a relation or way. Return default value node.");
		
		return Node.class;
	}
	
	/**
	 * gives the {@link GeometryObject} representation of a given {@link EntityType}
	 *
	 * @param entityType the {@link EntityType} to get the representation of
	 * @return the {@link GeometryObject} representation of {@code entityType}
	 *
	 * @since 0.1.0
	 */
	public static Class<? extends GeometryObject> geometryClassFromEntity(EntityType entityType) {
		switch (entityType) {
			case Way:
				return Polyline.class;
			case Relation:
				return MultiLine.class;
			default:
				return Point.class;
		}
	}
	
	/**
	 * reads all {@link Entity}s from a given {@link RunnableSource}
	 *
	 * @param reader the {@link RunnableSource} to read
	 * @return a {@link Map} of all read features
	 *
	 * @since 0.2.0
	 */
	private static Map<? extends Long, ? extends Entity> readFeatures(RunnableSource reader) {
		final Map<Long, Entity> res = new HashMap<>();
		
		reader.setSink(new Sink() {
			@Override
			public void process(EntityContainer entityContainer) {
				Entity entity = entityContainer.getEntity();
				
				res.put(entity.getId(), entity);
			}
			
			@Override
			public void initialize(Map<String, Object> map) {
			}
			
			@Override
			public void complete() {
			}
			
			@Override
			public void release() {
			}
		});
		
		Thread readerThread = new Thread(reader);
		readerThread.start();
		
		while (readerThread.isAlive()) {
			try {
				readerThread.join();
			} catch (InterruptedException e) {
			}
		}
		
		return res;
	}
	
	/**
	 * gives a {@link Set} of all requested {@link Entity}s
	 *
	 * @param script the Overpass API script to request on the server
	 * @param bbox   the bounding box
	 * @return a {@link Set} of all delivered {@link Entity}s
	 *
	 * @throws IllegalArgumentException if no script or bounding box was given
	 * @throws IOException              if an error occurred on request the data from the Overpass API
	 * @since 0.1.0
	 */
	public static Map<? extends Long, ? extends Entity> getFeatures(String script, Envelope bbox)
	throws IllegalArgumentException, IOException {
		return OverpassHandler.readFeatures(new OsmosisReader(OverpassHandler.request(script, bbox)));
	}
	
	/**
	 * gives a {@link Set} of all {@link Entity}s from a {@link File}
	 *
	 * @param file the {@link OsmFile} to read from
	 * @return a {@link Set} of all {@link Entity}s read from the data {@link File}
	 *
	 * @throws IllegalArgumentException if a inconsistent set of {@code dataFile}, {@code fileFormat} and {@code compression}
	 * @throws FileNotFoundException    if the given {@link File} doesn't exist or is {@code null}
	 * @since 0.2.0
	 */
	public static Map<? extends Long, ? extends Entity> getFeatures(OsmFile file)
	throws IllegalArgumentException, FileNotFoundException {
		File dataFile = file.getDataFile();
		
		if (dataFile == null || !(dataFile.exists())) {
			throw new FileNotFoundException();
		}
		
		RunnableSource reader;
		
		switch (file.getFileFormat()) {
			case XML:
				reader = new XmlReader(file.getDataFile(), true, file.getCompression());
				break;
			case PBF:
				reader = new OsmosisReader(new FileInputStream(file.getDataFile()));
				break;
			default:
				throw new IllegalArgumentException("Unknown file format!");
		}
		
		return OverpassHandler.readFeatures(reader);
	}
}
