package de.conterra.babelfish.overpass.store;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.jts.JTS;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

import de.conterra.babelfish.overpass.io.OverpassHandler;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Envelope;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Polygon;

/**
 * defines a class to store {@link Feature}s
 * 
 * @version 0.0.1
 * @author chwe
 * @since 0.0.1
 * 
 * @param <G> the {@link GeometryObject} type
 */
public class FeatureStore<G extends GeometryObject>
{
	/**
	 * the {@link Logger} of this class
	 * 
	 * @since 0.0.1
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(FeatureStore.class);
	/**
	 * {@link Map} of all {@link FeatureStore}s
	 * 
	 * @since 0.0.1
	 */
	private static final Set<FeatureStore<?>> stores = Collections.newSetFromMap(new ConcurrentHashMap<FeatureStore<?>, Boolean>());
	/**
	 * the delay after a enveloping {@link Polygon} should be removed (in
	 * milliseconds)
	 * 
	 * @since 0.0.1
	 */
	private static final long expire_delay = 900000;
	/**
	 * the {@link Timer}, which removes expired {@link Polygon}s
	 * 
	 * @since 0.0.1
	 */
	private static final Timer timer = new Timer();
	
	/**
	 * the {@link EntityType}
	 * 
	 * @since 0.0.1
	 */
	private final EntityType entityType;
	/**
	 * the Overpass API script to use for requests on the server
	 * 
	 * @since 0.1.0
	 */
	private final String script;
	/**
	 * a {@link Map} of all stored {@link Feature}s
	 * 
	 * @since 0.0.1
	 */
	private final Map<Long, Feature<? extends GeometryFeatureObject<G>>> features = new ConcurrentHashMap<>();
	/**
	 * {@link Map} of all enveloping {@link Polygon}
	 * 
	 * @since 0.0.1
	 */
	private final ConcurrentHashMap<Envelope, DateTime> envelopes = new ConcurrentHashMap<>();
	
	static
	{
		FeatureStore.start();
	}
	
	/**
	 * standard constructor
	 * 
	 * @since 0.1.0
	 * 
	 * @param type the {@link GeometryObject} type
	 * @param script the Overpass API script to use for requests
	 */
	public FeatureStore(Class<G> type, String script)
	{
		this.entityType = OverpassHandler.typeFromClass(OverpassHandler.entityClassFromGeometry(type));
		this.script = script;
		
		FeatureStore.stores.add(this);
	}
	
	/**
	 * standard constructor
	 * 
	 * @since 0.0.1
	 * 
	 * @param type the {@link GeometryObject} type
	 * @param metaFilter the filter of meta data
	 */
	public FeatureStore(Class<G> type, Set<? extends String> metaFilter)
	{
		this.entityType = OverpassHandler.typeFromClass(OverpassHandler.entityClassFromGeometry(type));
		
		String script = "";
		
		switch (this.entityType)
		{
			case Way:
				script += "way";
				break;
			case Relation:
				script += "relation";
				break;
			default:
				script += "node";
		}
		
		for (String metaCondition : metaFilter)
			script += "[" + metaCondition + "]";
		
		script += "(" + OverpassHandler.BBOX_PLACEHOLDER + ");";
		script += "(._;>;);out meta;";
		
		this.script = script;
		
		FeatureStore.stores.add(this);
	}
	
	/**
	 * removes expired {@link Polygon}s from the store
	 * 
	 * @since 0.0.1
	 */
	private static void clean()
	{
		DateTime now = new DateTime();
		
		for (FeatureStore<?> store : FeatureStore.getStores())
		{
			for (Envelope envelope : store.envelopes.keySet())
			{
				if (store.envelopes.get(envelope).isAfter(now))
					store.envelopes.remove(envelope);
			}
		}
		
		FeatureStore.LOGGER.debug("All stores cleaned.");
	}
	
	/**
	 * gives all {@link FeatureStore}s
	 * 
	 * @since 0.0.1
	 * 
	 * @return a {@link Set} of all {@link FeatureStore}s
	 */
	public static Set<? extends FeatureStore<?>> getStores()
	{
		return new HashSet<>(FeatureStore.stores);
	}
	
	/**
	 * starts the {@link Timer} to clear the cache automatically
	 * 
	 * @since 0.0.1
	 */
	public static void start()
	{
		long delay = (long) (FeatureStore.expire_delay * 0.975);
		
		FeatureStore.LOGGER.debug("FeatureStore initializied with cleaning interval of " + delay + ".");
		
		FeatureStore.timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				FeatureStore.clean();
			}
		}, delay, delay);
	}
	
	/**
	 * clears all {@link FeatureStore}s and stops open tasks
	 * 
	 * @since 0.0.1
	 */
	public static void stop()
	{
		FeatureStore.LOGGER.debug("Stopping the FeatureStore...");
		
		FeatureStore.timer.cancel();
		
		FeatureStore.LOGGER.debug("Timer was canceled.");
		
		for (FeatureStore<?> store : FeatureStore.getStores())
			store.clear();
		
		FeatureStore.LOGGER.debug("All stores were cleared.");
	}
	
	/**
	 * request {@link Entity}s from the Overpass API and save it in the
	 * <code>features</code> {@link Map}
	 * 
	 * @since 0.0.1
	 * 
	 * @param spatialFilter the {@link GeometryObject} to limit the request
	 * @throws IOException if an error occurred on request the data from the
	 *         Overpass API
	 */
	private void request(GeometryObject spatialFilter)
	throws IOException
	{
		Geometry strippedGeometry = spatialFilter.toGeometry();
		
		org.opengis.geometry.Envelope jtsEnvelope = spatialFilter.getEnvelope();
		Envelope requestedEnvelope;
		if (jtsEnvelope instanceof Envelope)
			requestedEnvelope = (Envelope)jtsEnvelope;
		else
			requestedEnvelope = new Envelope(jtsEnvelope);
		
		for (Envelope storedEnvelope : this.envelopes.keySet())
		{
			if (requestedEnvelope.isIn(storedEnvelope))
				return;
			
			strippedGeometry = strippedGeometry.difference(storedEnvelope.toGeometry());
		}
		
		if (strippedGeometry.isEmpty())
			return;
		
		int numGeometries = strippedGeometry.getNumGeometries();
		
		FeatureStore.LOGGER.debug("The requested but not stored area consists of " + numGeometries + " separate geometries.");
		
		if (numGeometries > 1)
		{
			for (int i = 0; i < numGeometries; i++)
				this.request(new Envelope(JTS.toEnvelope(strippedGeometry.getGeometryN(i))));
			
			return;
		}
		
		try
		{
			CoordinateReferenceSystem crs = spatialFilter.getCoordinateReferenceSystem();
			
			org.opengis.geometry.Envelope strippedUnreferencedEnvelope = JTS.toEnvelope(strippedGeometry);
			GeneralDirectPosition strippedLowerCorner = new GeneralDirectPosition(crs);
			GeneralDirectPosition strippedUpperCorner = new GeneralDirectPosition(crs);
			
			for (int i = 0; i < strippedUnreferencedEnvelope.getDimension(); i++)
			{
				strippedLowerCorner.setOrdinate(i, strippedUnreferencedEnvelope.getMinimum(i));
				strippedUpperCorner.setOrdinate(i, strippedUnreferencedEnvelope.getMaximum(i));
			}
			
			Envelope strippedEnvelope = new Envelope(new EnvelopeImpl(strippedLowerCorner, strippedUpperCorner));
			
			@SuppressWarnings("unchecked")
			Class<G> geometryType = (Class<G>)OverpassHandler.geometryClassFromEntity(this.entityType);
			
			this.features.putAll(FeatureConverter.convert(geometryType, OverpassHandler.getFeatures(this.getScript(), strippedEnvelope)));
			
			this.envelopes.put(strippedEnvelope, (new DateTime()).plus(FeatureStore.expire_delay));
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * gives the Overpass API script to use for requests
	 * 
	 * @since 0.1.0
	 * 
	 * @return the Overpass API script
	 */
	public String getScript()
	{
		return this.script;
	}
	
	/**
	 * gives all stored {@link Feature}s
	 * 
	 * @since 0.0.1
	 * 
	 * @return a {@link Map} of all stored {@link Feature}s
	 */
	public Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<G>>> getFeatures()
	{
		return new HashMap<>(this.features);
	}
	
	/**
	 * gives all {@link Feature}s, which overlaps the <code>spatialFilter</code><br>
	 * Missing features will automatically requested
	 * 
	 * @since 0.0.1
	 * 
	 * @param spatialFilter the spatial filter
	 * @return a {@link Map} of all {@link Feature}s, which overlaps the
	 *         <code>spatialFilter</code>
	 */
	public Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<G>>> getFeatures(GeometryObject spatialFilter)
	{
		try
		{
			this.request(spatialFilter);
		}
		catch (IOException e)
		{
		}
		
		Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<G>>> allFeatures = this.getFeatures();
		Map<Long, Feature<? extends GeometryFeatureObject<G>>> result = new HashMap<>();
		
		for (long id : allFeatures.keySet())
		{
			Feature<? extends GeometryFeatureObject<G>> feature = allFeatures.get(id);
			
			if (feature.getFeature().getGeometry().overlaps(spatialFilter))
				result.put(id, feature);
		}
		
		return result;
	}
	
	/**
	 * removes all {@link Feature}s and border {@link Polygon}s from this store
	 * 
	 * @since 0.0.1
	 */
	public void clear()
	{
		this.features.clear();
		this.envelopes.clear();
	}
}