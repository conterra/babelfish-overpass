package de.conterra.babelfish.overpass.plugin;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import de.conterra.babelfish.overpass.store.PopupStore;
import de.conterra.babelfish.plugin.v10_02.feature.Attachment;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.feature.Popup;
import de.conterra.babelfish.plugin.v10_02.object.feature.FeatureObject;

/**
 * defines a {@link Feature}
 * 
 * @version 0.0.1
 * @author chwe
 * @since 0.0.1
 * 
 * @param <T> the {@link FeatureObject} type
 */
public class OverpassFeature<T extends FeatureObject>
implements Feature<T>
{
	/**
	 * the {@link FeatureObject}
	 * 
	 * @since 0.0.1
	 */
	private final T feature;
	
	/**
	 * a {@link Set} of all {@link Attachment}s
	 * 
	 * @since 0.0.1
	 */
	private final Set<Attachment> attachments = new LinkedHashSet<>();
	/**
	 * the {@link EntityType} of the {@link FeatureObject}
	 * 
	 * @since 0.0.1
	 */
	private final EntityType entityType;
	/**
	 * the global unique identifier
	 * 
	 * @since 0.0.1
	 */
	private final long id;
	
	/**
	 * standard constructor
	 * 
	 * @since 0.0.1
	 * 
	 * @param entityType the {@link EntityType}
	 * @param id the global unique identifier (of the feature in the
	 *        OpenStreetMap database)
	 * @param feature the {@link FeatureObject}
	 */
	public OverpassFeature(EntityType entityType, long id, T feature)
	{
		this.entityType = entityType;
		this.id = id;
		this.feature = feature;
	}
	
	/**
	 * gives the {@link FeatureObject}
	 * 
	 * @since 0.0.1
	 * 
	 * @return the {@link FeatureObject}
	 */
	@Override
	public T getFeature()
	{
		return this.feature;
	}
	
	@Override
	public Set<? extends Attachment> getAttachments()
	{
		return new LinkedHashSet<>(this.attachments);
	}
	
	@Override
	public Popup getPopup()
	{
		return PopupStore.getPopup(this.getId(), this.getEntityType());
	}
	
	/**
	 * adds an {@link Attachment}
	 * 
	 * @since 0.0.1
	 * 
	 * @param attachment the {@link Attachment} to add
	 * @return <code>true</code>, if the {@link Attachment} was added
	 *         successfully
	 * 
	 * @see Set#add(Object)
	 */
	public boolean addAttachment(Attachment attachment)
	{
		return this.attachments.add(attachment);
	}
	
	/**
	 * removes an {@link Attachment}
	 * 
	 * @since 0.0.1
	 * 
	 * @param attachment the {@link Attachment} to remove
	 * @return <code>true</code>, if the {@link Set} contained the
	 *         {@link Attachment}
	 * 
	 * @see Set#remove(Object)
	 */
	public boolean removeAttachment(Attachment attachment)
	{
		return this.attachments.remove(attachment);
	}
	
	/**
	 * gives the {@link EntityType} of the {@link FeatureObject}
	 * 
	 * @since 0.0.1
	 * 
	 * @return the {@link EntityType} of the {@link FeatureObject}
	 */
	public EntityType getEntityType()
	{
		return this.entityType;
	}
	
	/**
	 * gives the unique identifier
	 * 
	 * @since 0.0.1
	 * 
	 * @return the global unique identifier
	 */
	public long getId()
	{
		return this.id;
	}
}