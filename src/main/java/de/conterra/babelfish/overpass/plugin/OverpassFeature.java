package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.overpass.store.PopupStore;
import de.conterra.babelfish.plugin.v10_02.feature.Attachment;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.feature.Popup;
import de.conterra.babelfish.plugin.v10_02.object.feature.FeatureObject;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * defines a {@link Feature}
 *
 * @param <T> the {@link FeatureObject} type
 * @author ChrissW-R1
 * @version 0.1.0
 * @since 0.1.0
 */
public class OverpassFeature<T extends FeatureObject>
		implements Feature<T> {
	/**
	 * the {@link FeatureObject}
	 *
	 * @since 0.1.0
	 */
	private final T feature;
	
	/**
	 * a {@link Set} of all {@link Attachment}s
	 *
	 * @since 0.1.0
	 */
	private final Set<Attachment> attachments = new LinkedHashSet<>();
	/**
	 * the {@link EntityType} of the {@link FeatureObject}
	 *
	 * @since 0.1.0
	 */
	private final EntityType entityType;
	/**
	 * the global unique identifier
	 *
	 * @since 0.1.0
	 */
	private final long id;
	
	/**
	 * standard constructor
	 *
	 * @param entityType the {@link EntityType}
	 * @param id         the global unique identifier (of the feature in the OpenStreetMap database)
	 * @param feature    the {@link FeatureObject}
	 * @since 0.1.0
	 */
	public OverpassFeature(EntityType entityType, long id, T feature) {
		this.entityType = entityType;
		this.id = id;
		this.feature = feature;
	}
	
	/**
	 * gives the {@link FeatureObject}
	 *
	 * @return the {@link FeatureObject}
	 *
	 * @since 0.1.0
	 */
	@Override
	public T getFeature() {
		return this.feature;
	}
	
	@Override
	public Set<? extends Attachment> getAttachments() {
		return new LinkedHashSet<>(this.attachments);
	}
	
	@Override
	public Popup getPopup() {
		return PopupStore.getPopup(this.getId(), this.getEntityType());
	}
	
	/**
	 * adds an {@link Attachment}
	 *
	 * @param attachment the {@link Attachment} to add
	 * @return {@code true}, if the {@link Attachment} was added successfully
	 *
	 * @see Set#add(Object)
	 * @since 0.1.0
	 */
	public boolean addAttachment(Attachment attachment) {
		return this.attachments.add(attachment);
	}
	
	/**
	 * removes an {@link Attachment}
	 *
	 * @param attachment the {@link Attachment} to remove
	 * @return {@code true}, if the {@link Set} contained the {@link Attachment}
	 *
	 * @see Set#remove(Object)
	 * @since 0.1.0
	 */
	public boolean removeAttachment(Attachment attachment) {
		return this.attachments.remove(attachment);
	}
	
	/**
	 * gives the {@link EntityType} of the {@link FeatureObject}
	 *
	 * @return the {@link EntityType} of the {@link FeatureObject}
	 *
	 * @since 0.1.0
	 */
	public EntityType getEntityType() {
		return this.entityType;
	}
	
	/**
	 * gives the unique identifier
	 *
	 * @return the global unique identifier
	 *
	 * @since 0.1.0
	 */
	public long getId() {
		return this.id;
	}
}