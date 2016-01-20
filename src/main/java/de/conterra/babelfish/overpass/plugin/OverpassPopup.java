package de.conterra.babelfish.overpass.plugin;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import de.conterra.babelfish.plugin.v10_02.feature.Popup;
import de.conterra.babelfish.plugin.v10_02.feature.PopupType;

/**
 * defines a {@link Popup}, which request its content from the Overpass API
 * 
 * @version 0.0.1
 * @author chwe
 * @since 0.0.1
 */
public class OverpassPopup
implements Popup
{
	/**
	 * the {@link EntityType}
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
	 * the HTML content
	 * 
	 * @since 0.0.1
	 */
	private final String content;
	
	/**
	 * standard constructor
	 * 
	 * @since 0.0.1
	 * 
	 * @param entityType the {@link EntityType}
	 * @param id the global unique identifier (of the feature in the
	 *        OpenStreetMap database)
	 * @param content the HTML content
	 */
	public OverpassPopup(EntityType entityType, long id, String content)
	{
		this.entityType = entityType;
		this.id = id;
		this.content = content;
	}
	
	@Override
	public PopupType getType()
	{
		return PopupType.HtmlText;
	}
	
	@Override
	public String getContent()
	{
		return this.content;
	}
	
	/**
	 * gives the {@link EntityType}
	 * 
	 * @since 0.0.1
	 * 
	 * @return the {@link EntityType}
	 */
	public EntityType getEntityType()
	{
		return this.entityType;
	}
	
	/**
	 * gives the global unique identifier
	 * 
	 * @since 0.0.1
	 * 
	 * @return the unique identifier
	 */
	public long getId()
	{
		return this.id;
	}
}