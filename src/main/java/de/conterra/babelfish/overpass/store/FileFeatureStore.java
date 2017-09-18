package de.conterra.babelfish.overpass.store;

import de.conterra.babelfish.overpass.io.OsmFile;
import de.conterra.babelfish.overpass.io.OverpassHandler;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	private final OsmFile     file;
	/**
	 * the {@link Tag} key to filter to
	 *
	 * @since 0.2.0
	 */
	@Getter
	private final String      typeKey;
	/**
	 * the {@link Tag} values to filter to
	 *
	 * @since 0.2.0
	 */
	@Getter
	private final Set<String> typeValues;
	/**
	 * delimiter RegEx to split the tag value
	 */
	@Getter
	private final String      delimiter;
	
	/**
	 * constructor, with given data {@link File}
	 *
	 * @param type       the {@link GeometryObject} type
	 * @param file       the {@link OsmFile} to get the features from
	 * @param typeKey    the {@link Tag} key to filter to
	 * @param typeValues the {@link Tag} values to filter to
	 * @param delimiter  delimiter RegEx to split the {@link Tag} value
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	public FileFeatureStore(Class<G> type, OsmFile file, String typeKey, Set<String> typeValues, String delimiter)
	throws FileNotFoundException {
		super(type);
		
		this.file = file;
		this.typeKey = typeKey;
		this.typeValues = typeValues;
		this.delimiter = delimiter;
		
		this.loadFromFile();
	}
	
	/**
	 * constructor, with given data {@link File}
	 *
	 * @param type the {@link GeometryObject} type
	 * @param file the {@link OsmFile} to get the features from
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	public FileFeatureStore(Class<G> type, OsmFile file)
	throws FileNotFoundException {
		this(type, file, null, null, null);
	}
	
	@Override
	protected void request(GeometryObject spatialFilter)
	throws IOException {
		if (!(this.getFeatures().isEmpty())) {
			return;
		}
		
		this.loadFromFile();
	}
	
	/**
	 * loads all features from {@code file}
	 *
	 * @throws FileNotFoundException if the {@link File} doesn't exist
	 * @since 0.2.0
	 */
	private void loadFromFile()
	throws FileNotFoundException {
		@SuppressWarnings("unchecked")
		Class<G> geometryType = (Class<G>) OverpassHandler.geometryClassFromEntity(this.getEntityType());
		
		Map<? extends Long, ? extends Entity> entities = OverpassHandler.getFeatures(this.getFile());
		if (this.typeKey != null && this.typeValues != null) {
			Map<Long, Entity> filteredEntities = new HashMap<>();
			
			for (Entity entity : entities.values()) {
				for (Tag tag : entity.getTags()) {
					if (!(tag.getKey().equals(this.typeKey))) {
						continue;
					}
					
					String tagValue = tag.getValue();
					long   id       = entity.getId();
					
					if (this.typeValues.contains(tagValue)) {
						filteredEntities.put(id, entity);
						break;
					}
					
					if (this.delimiter != null) {
						for (String value : tagValue.split(this.delimiter)) {
							if (this.typeValues.contains(value)) {
								filteredEntities.put(id, entity);
								break;
							}
						}
					}
				}
			}
			
			entities = filteredEntities;
		}
		
		this.features.putAll(FeatureConverter.convert(geometryType, entities));
	}
}
