package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.plugin.v10_02.feature.Popup;
import de.conterra.babelfish.plugin.v10_02.feature.PopupType;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

/**
 * defines a {@link Popup}, which request its content from the Overpass API
 *
 * @author ChrissW-R1
 * @version 0.1.0
 * @since 0.1.0
 */
public class OverpassPopup
		implements Popup {
	/**
	 * the {@link EntityType}
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
	 * the HTML content
	 *
	 * @since 0.1.0
	 */
	private final String content;
	
	/**
	 * standard constructor
	 *
	 * @param entityType the {@link EntityType}
	 * @param id         the global unique identifier (of the feature in the OpenStreetMap database)
	 * @param content    the HTML content
	 * @since 0.1.0
	 */
	public OverpassPopup(EntityType entityType, long id, String content) {
		this.entityType = entityType;
		this.id = id;
		this.content = content;
	}
	
	@Override
	public PopupType getType() {
		return PopupType.HtmlText;
	}
	
	@Override
	public String getContent() {
		return this.content;
	}
	
	/**
	 * gives the {@link EntityType}
	 *
	 * @return the {@link EntityType}
	 *
	 * @since 0.1.0
	 */
	public EntityType getEntityType() {
		return this.entityType;
	}
	
	/**
	 * gives the global unique identifier
	 *
	 * @return the unique identifier
	 *
	 * @since 0.1.0
	 */
	public long getId() {
		return this.id;
	}
}