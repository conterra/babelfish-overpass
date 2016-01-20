package de.conterra.babelfish.overpass.plugin;

import org.josql.QueryParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.conterra.babelfish.overpass.store.FeatureStore;
import de.conterra.babelfish.plugin.v10_02.feature.DefaultQuery;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.feature.Query;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;

/**
 * defines {@link Query} to query to Overpass API
 * 
 * @version 0.0.1
 * @author chwe
 * @since 0.0.1
 * 
 * @param <G> the {@link GeometryObject} type
 */
public class OverpassQuery<G extends GeometryObject>
extends DefaultQuery<GeometryFeatureObject<G>>
{
	/**
	 * the {@link Logger} of this class
	 * 
	 * @since 0.0.1
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(OverpassQuery.class);
	/**
	 * the {@link FeatureStore} to query
	 * 
	 * @since 0.0.1
	 */
	private final FeatureStore<G> store;
	
	/**
	 * standard constructor
	 * 
	 * @since 0.0.1
	 * 
	 * @param store the {@link FeatureStore} to query
	 */
	public OverpassQuery(FeatureStore<G> store)
	{
		this.store = store;
	}
	
	@Override
	public Iterable<? extends Feature<? extends GeometryFeatureObject<G>>> execute(Iterable<? extends Feature<? extends GeometryFeatureObject<G>>> features, GeometryObject geometry, String whereClause)
	throws QueryParseException
	{
		return super.execute(this.store.getFeatures(geometry).values(), geometry, whereClause);
	}
}