package de.conterra.babelfish.overpass.store;

import de.conterra.babelfish.overpass.io.OverpassHandler;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Envelope;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Polygon;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * defines a class to store {@link Feature}s
 *
 * @param <G> the {@link GeometryObject} type
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.1.0
 */
@Slf4j
public abstract class FeatureStore<G extends GeometryObject> {
	/**
	 * the delay after an enveloping {@link Polygon} should be removed (in milliseconds)
	 *
	 * @since 0.1.0
	 */
	public static final  long                 EXPIRE_DELAY = 900000;
	/**
	 * {@link Map} of all {@link FeatureStore}s
	 *
	 * @since 0.1.0
	 */
	private static final Set<FeatureStore<?>> stores       = Collections.newSetFromMap(new ConcurrentHashMap<FeatureStore<?>, Boolean>());
	/**
	 * the {@link Timer}, which removes expired {@link Polygon}s
	 *
	 * @since 0.1.0
	 */
	private static final Timer                timer        = new Timer();
	
	/**
	 * the {@link EntityType}
	 *
	 * @since 0.1.0
	 */
	@Getter
	private final EntityType entityType;
	/**
	 * a {@link Map} of all stored {@link Feature}s
	 *
	 * @since 0.1.0
	 */
	protected final Map<Long, Feature<? extends GeometryFeatureObject<G>>> features  = new ConcurrentHashMap<>();
	/**
	 * {@link Map} of all enveloping {@link Polygon}
	 *
	 * @since 0.1.0
	 */
	protected final Map<Envelope, DateTime>                                envelopes = new ConcurrentHashMap<>();
	
	static {
		FeatureStore.start();
	}
	
	/**
	 * standard constructor
	 *
	 * @param type the {@link GeometryObject} type
	 * @since 0.2.0
	 */
	public FeatureStore(Class<G> type) {
		this.entityType = OverpassHandler.typeFromClass(OverpassHandler.entityClassFromGeometry(type));
		
		FeatureStore.stores.add(this);
	}
	
	/**
	 * removes expired {@link Polygon}s from the store
	 *
	 * @since 0.1.0
	 */
	private static void clean() {
		DateTime now = new DateTime();
		
		for (FeatureStore<?> store : FeatureStore.getStores()) {
			for (Envelope envelope : store.envelopes.keySet()) {
				if (store.envelopes.get(envelope).isAfter(now)) {
					store.envelopes.remove(envelope);
				}
			}
		}
		
		log.debug("All stores cleaned.");
	}
	
	/**
	 * gives all {@link FeatureStore}s
	 *
	 * @return a {@link Set} of all {@link FeatureStore}s
	 *
	 * @since 0.1.0
	 */
	public static Set<? extends FeatureStore<?>> getStores() {
		return new HashSet<>(FeatureStore.stores);
	}
	
	/**
	 * starts the {@link Timer} to clear the cache automatically
	 *
	 * @since 0.1.0
	 */
	public static void start() {
		long delay = (long) (FeatureStore.EXPIRE_DELAY * 0.975);
		
		log.debug("FeatureStore initialized with cleaning interval of " + delay + ".");
		
		FeatureStore.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				FeatureStore.clean();
			}
		}, delay, delay);
	}
	
	/**
	 * clears all {@link FeatureStore}s and stops open tasks
	 *
	 * @since 0.1.0
	 */
	public static void stop() {
		log.debug("Stopping the FeatureStore...");
		
		FeatureStore.timer.cancel();
		
		log.debug("Timer was canceled.");
		
		for (FeatureStore<?> store : FeatureStore.getStores()) {
			store.clear();
		}
		
		log.debug("All stores were cleared.");
	}
	
	/**
	 * request {@link Entity}s from the Overpass API and save it in the {@code features} {@link Map}
	 *
	 * @param spatialFilter the {@link GeometryObject} to limit the request
	 * @throws IOException if an error occurred on request the data from the Overpass API
	 * @since 0.1.0
	 */
	protected abstract void request(GeometryObject spatialFilter)
	throws IOException;
	
	/**
	 * gives all stored {@link Feature}s
	 *
	 * @return a {@link Map} of all stored {@link Feature}s
	 *
	 * @since 0.1.0
	 */
	public Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<G>>> getFeatures() {
		return new HashMap<>(this.features);
	}
	
	/**
	 * gives all {@link Feature}s, which overlaps the {@code spatialFilter}<br>
	 * Missing features will automatically requested
	 *
	 * @param spatialFilter the spatial filter
	 * @return a {@link Map} of all {@link Feature}s, which overlaps the {@code spatialFilter}
	 *
	 * @since 0.1.0
	 */
	public Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<G>>> getFeatures(GeometryObject spatialFilter) {
		try {
			this.request(spatialFilter);
		} catch (IOException e) {
			log.warn("Unable to request features from store!", e);
		}
		
		Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<G>>> allFeatures = this.getFeatures();
		
		if (spatialFilter == null) {
			return allFeatures;
		}
		
		Map<Long, Feature<? extends GeometryFeatureObject<G>>> result = new HashMap<>();
		for (long id : allFeatures.keySet()) {
			Feature<? extends GeometryFeatureObject<G>> feature = allFeatures.get(id);
			
			if (feature.getFeature().getGeometry().overlaps(spatialFilter)) {
				result.put(id, feature);
			}
		}
		
		return result;
	}
	
	/**
	 * removes all {@link Feature}s and border {@link Polygon}s from this store
	 *
	 * @since 0.1.0
	 */
	public void clear() {
		this.features.clear();
		this.envelopes.clear();
	}
}
