package de.conterra.babelfish.overpass.store;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opengis.geometry.coordinate.Position;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.conterra.babelfish.overpass.io.OverpassHandler;
import de.conterra.babelfish.overpass.plugin.OverpassFeature;
import de.conterra.babelfish.overpass.plugin.OverpassField;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.feature.Field;
import de.conterra.babelfish.plugin.v10_02.feature.wrapper.LayerWrapper;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Point;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Polyline;

/**
 * defines a class to convert Overpass {@link Entity}s to Babelfish
 * {@link Feature}s
 * 
 * @version 0.0.1
 * @author chwe
 * @since 0.0.1
 */
public class FeatureConverter
{
	/**
	 * the {@link Logger} of this class
	 * 
	 * @since 0.0.1
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(FeatureConverter.class);
	
	/**
	 * private standard constructor, to prevent initialization
	 * 
	 * @since 0.0.1
	 */
	private FeatureConverter()
	{
	}
	
	/**
	 * converts Overpass {@link Entity}s to Babelfish {@link Feature}s
	 * 
	 * @since 0.0.1
	 * 
	 * @param <G> the geometric type
	 * @param clazz the geometric {@link Class} type
	 * @param features the OpenStreetMap {@link Entity}s
	 * @return the converted OpenStreetMap {@link Entity}s as Babelfish
	 *         {@link Feature}s
	 */
	public static <G extends GeometryObject> Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<G>>> convert(Class<G> clazz, Map<? extends Long, ? extends Entity> features)
	{
		Map<Long, Point> points = new LinkedHashMap<>();
		Map<Long, Polyline> lines = new LinkedHashMap<>();
		Map<Long, Map<Field, Object>> metas = new LinkedHashMap<>();
		Map<String, OverpassField> metaKeys = new LinkedHashMap<>();
		
		for (long id : features.keySet())
		{
			Entity entity = features.get(id);
			
			if (entity instanceof Node)
			{
				Node node = (Node)entity;
				
				GeneralDirectPosition pos = new GeneralDirectPosition(OverpassHandler.OSM_CRS);
				pos.setOrdinate(0, node.getLatitude());
				pos.setOrdinate(1, node.getLongitude());
				
				try
				{
					Map<String, Object> metaTags = node.getMetaTags();
					if (metaTags.containsKey("ele"))
					{
						FeatureConverter.LOGGER.debug("Found ele tag with third ordinate on node " + id + ".");
						
						double ordinate = Double.parseDouble((String)metaTags.get("ele"));
						pos.setOrdinate(2, ordinate);
						
						FeatureConverter.LOGGER.debug("Added thrid ordinate " + ordinate + " to node " + id + ".");
					}
				}
				catch (ClassCastException | NumberFormatException e)
				{
					FeatureConverter.LOGGER.warn("Couldn't parse the third ordinate from ele tag!", e);
				}
				
				points.put(id, new Point(new PointImpl(pos)));
			}
		}
		
		for (long id : features.keySet())
		{
			Entity entity = features.get(id);
			
			if (entity instanceof Way)
			{
				Way way = (Way)entity;
				
				LinkedList<Position> pos = new LinkedList<>();
				
				for (WayNode node : way.getWayNodes())
				{
					long nodeId = node.getNodeId();
					pos.add(points.get(nodeId));
					
					FeatureConverter.LOGGER.debug("Added node " + points.get(nodeId) + " (" + nodeId + ") to way " + id + ".");
				}
				
				lines.put(id, new Polyline(new LineStringImpl(pos)));
			}
		}
		
		for (long id : features.keySet())
		{
			Entity entity = features.get(id);
			
			FeatureConverter.LOGGER.debug("Entity " + id + " is a " + entity.getType() + ".");
			
			Set<Tag> metaTags = new HashSet<>(entity.getTags());
			Map<Field, Object> metaFields = new LinkedHashMap<>();
			
			metaFields.put(LayerWrapper.DEFAULT_OBJECT_ID_FIELD, id);
			metaFields.put(OverpassField.USER_FIELD, entity.getUser().getName());
			metaFields.put(OverpassField.VERSION_FIELD, entity.getVersion());
			metaFields.put(OverpassField.LASTCHANGE_FIELD, new DateTime(entity.getTimestamp().getTime(), DateTimeZone.UTC));
			metaFields.put(OverpassField.CHANGESET_FIELD, (double) (entity.getChangesetId()));
			
			for (Tag tag : metaTags)
			{
				String key = tag.getKey();
				
				if ( ! (metaKeys.containsKey(key)))
				{
					metaKeys.put(key, new OverpassField(tag));
					
					FeatureConverter.LOGGER.debug("Created new meta field: " + key);
				}
				
				String value = tag.getValue();
				metaFields.put(metaKeys.get(key), value);
				
				FeatureConverter.LOGGER.debug("Added tag " + key + "=" + value + " to entity " + id + ".");
			}
			
			metas.put(id, metaFields);
		}
		
		LinkedHashMap<Long, Feature<GeometryFeatureObject<G>>> res = new LinkedHashMap<>();
		
		if (Polyline.class.isAssignableFrom(clazz))
		{
			FeatureConverter.LOGGER.debug("Return all ways. (" + lines.size() + ")");
			
			for (long id : lines.keySet())
			{
				@SuppressWarnings("unchecked")
				G line = (G)lines.get(id);
				
				res.put(id, new OverpassFeature<>(EntityType.Way, id, new GeometryFeatureObject<>(line, metas.get(id))));
			}
		}
		else if (Point.class.isAssignableFrom(clazz))
		{
			FeatureConverter.LOGGER.debug("Return all nodes. (" + points.size() + ")");
			
			for (long id : points.keySet())
			{
				@SuppressWarnings("unchecked")
				G point = (G)points.get(id);
				
				res.put(id, new OverpassFeature<>(EntityType.Node, id, new GeometryFeatureObject<>(point, metas.get(id))));
			}
		}
		
		return res;
	}
}