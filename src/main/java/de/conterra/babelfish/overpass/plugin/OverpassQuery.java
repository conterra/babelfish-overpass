package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.overpass.store.FeatureStore;
import de.conterra.babelfish.plugin.v10_02.feature.DefaultQuery;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.feature.Query;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import org.josql.QueryParseException;

/**
 * defines {@link Query} to query to Overpass API
 *
 * @param <G> the {@link GeometryObject} type
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.1.0
 */
public class OverpassQuery<G extends GeometryObject>
		extends DefaultQuery<GeometryFeatureObject<G>> {
	/**
	 * the {@link FeatureStore} to query
	 *
	 * @since 0.1.0
	 */
	private final FeatureStore<G> store;
	
	/**
	 * standard constructor
	 *
	 * @param store the {@link FeatureStore} to query
	 * @since 0.1.0
	 */
	public OverpassQuery(FeatureStore<G> store) {
		this.store = store;
	}
	
	@Override
	public Iterable<? extends Feature<? extends GeometryFeatureObject<G>>> execute(Iterable<? extends Feature<? extends GeometryFeatureObject<G>>> features, GeometryObject geometry, String whereClause)
			throws QueryParseException {
		return super.execute(this.store.getFeatures(geometry).values(), geometry, whereClause);
	}
}