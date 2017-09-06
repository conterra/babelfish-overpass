package de.conterra.babelfish.overpass.store;

import de.conterra.babelfish.overpass.io.OsmFile;
import de.conterra.babelfish.overpass.io.OverpassHandler;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * defines a {@link FeatureStore}, which gets the features from a {@link OsmFile}
 *
 * @param <G> the {@link GeometryObject} type
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.2.0
 */
@Slf4j
public class FileFeatureStore<G extends GeometryObject>
		extends FeatureStore<G> {
	/**
	 * the {@link OsmFile} to get the features from
	 *
	 * @since 0.2.0
	 */
	@Getter
	private final OsmFile file;
	
	/**
	 * constructor, with given data {@link File}
	 *
	 * @param type the {@link GeometryObject} type
	 * @param file the {@link OsmFile} to get the features from
	 * @since 0.2.0
	 */
	public FileFeatureStore(Class<G> type, OsmFile file) {
		super(type);
		
		this.file = file;
	}
	
	@Override
	protected void request(GeometryObject spatialFilter)
	throws IOException {
		if (!(this.getFeatures().isEmpty())) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		Class<G> geometryType = (Class<G>) OverpassHandler.geometryClassFromEntity(this.getEntityType());
		
		this.features.putAll(FeatureConverter.convert(geometryType, OverpassHandler.getFeatures(this.getFile())));
	}
}
