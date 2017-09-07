package de.conterra.babelfish.overpass.store;

import com.vividsolutions.jts.geom.Geometry;
import de.conterra.babelfish.overpass.io.OverpassHandler;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Envelope;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.jts.JTS;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.Set;

/**
 * defines a {@link FeatureStore}, which uses an Overpass API as source
 *
 * @param <G> the {@link GeometryObject} type
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.2.0
 */
@Slf4j
public class OverpassFeatureStore<G extends GeometryObject>
		extends FeatureStore<G> {
	/**
	 * the Overpass API script to use for requests on the server
	 *
	 * @since 0.2.0
	 */
	@Getter
	private final String script;
	
	/**
	 * constructor, with Overpass API script
	 *
	 * @param type   the {@link GeometryObject} type
	 * @param script the Overpass API script to use for requests
	 * @since 0.2.0
	 */
	public OverpassFeatureStore(Class<G> type, String script) {
		super(type);
		
		this.script = script;
	}
	
	/**
	 * constructor, with meta filter of meta data
	 *
	 * @param type       the {@link GeometryObject} type
	 * @param metaFilter the filter of meta data
	 * @since 0.2.0
	 */
	public OverpassFeatureStore(Class<G> type, Set<? extends String> metaFilter) {
		super(type);
		
		String script = StringUtils.EMPTY;
		
		switch (this.getEntityType()) {
			case Way:
				script += "way";
				break;
			case Relation:
				script += "rel";
				break;
			default:
				script += "node";
		}
		
		for (String metaCondition : metaFilter) {
			script += "[" + metaCondition + "]";
		}
		
		script += "(" + OverpassHandler.BBOX_PLACEHOLDER + ");";
		script += "(._;>>;);out meta;";
		
		this.script = script;
	}
	
	@Override
	protected void request(GeometryObject spatialFilter)
	throws IOException {
		Geometry strippedGeometry = spatialFilter.toGeometry();
		
		org.opengis.geometry.Envelope jtsEnvelope = spatialFilter.getEnvelope();
		Envelope                      requestedEnvelope;
		if (jtsEnvelope instanceof Envelope) {
			requestedEnvelope = (Envelope) jtsEnvelope;
		} else {
			requestedEnvelope = new Envelope(jtsEnvelope);
		}
		
		for (Envelope storedEnvelope : this.envelopes.keySet()) {
			if (requestedEnvelope.isIn(storedEnvelope)) {
				return;
			}
			
			strippedGeometry = strippedGeometry.difference(storedEnvelope.toGeometry());
		}
		
		if (strippedGeometry.isEmpty()) {
			return;
		}
		
		int numGeometries = strippedGeometry.getNumGeometries();
		
		log.debug("The requested but not stored area consists of " + numGeometries + " separate geometries.");
		
		if (numGeometries > 1) {
			for (int i = 0; i < numGeometries; i++) {
				this.request(new Envelope(JTS.toEnvelope(strippedGeometry.getGeometryN(i))));
			}
			
			return;
		}
		
		try {
			CoordinateReferenceSystem crs = spatialFilter.getCoordinateReferenceSystem();
			
			org.opengis.geometry.Envelope strippedUnreferencedEnvelope = JTS.toEnvelope(strippedGeometry);
			GeneralDirectPosition         strippedLowerCorner          = new GeneralDirectPosition(crs);
			GeneralDirectPosition         strippedUpperCorner          = new GeneralDirectPosition(crs);
			
			for (int i = 0; i < strippedUnreferencedEnvelope.getDimension(); i++) {
				strippedLowerCorner.setOrdinate(i, strippedUnreferencedEnvelope.getMinimum(i));
				strippedUpperCorner.setOrdinate(i, strippedUnreferencedEnvelope.getMaximum(i));
			}
			
			Envelope strippedEnvelope = new Envelope(new EnvelopeImpl(strippedLowerCorner, strippedUpperCorner));
			
			@SuppressWarnings("unchecked")
			Class<G> geometryType = (Class<G>) OverpassHandler.geometryClassFromEntity(this.getEntityType());
			
			this.features.putAll(FeatureConverter.convert(geometryType, OverpassHandler.getFeatures(this.getScript(), strippedEnvelope)));
			
			this.envelopes.put(strippedEnvelope, (new DateTime()).plus(FeatureStore.EXPIRE_DELAY));
		} catch (IllegalArgumentException e) {
			log.error("An error occurred while requesting the features!", e);
		}
	}
}
