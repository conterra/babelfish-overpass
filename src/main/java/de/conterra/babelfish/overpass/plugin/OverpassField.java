package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.plugin.v10_02.feature.Field;
import de.conterra.babelfish.plugin.v10_02.feature.FieldType;
import de.conterra.babelfish.plugin.v10_02.object.domain.DomainObject;
import de.conterra.babelfish.plugin.v10_02.object.domain.RangeDomain;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.osmbinary.Osmformat.ChangeSet;

/**
 * defines an OpenStreetMap meta key
 *
 * @author ChrissW-R1
 * @version 0.1.0
 * @since 0.1.0
 */
public class OverpassField
		implements Field {
	/**
	 * {@link OverpassField}, which stores the editor
	 *
	 * @since 0.1.0
	 */
	public static final OverpassField USER_FIELD = new OverpassField("osm:user");
	/**
	 * {@link OverpassField}, which stores the version<br>
	 * (how many changes were made on the {@link Entity}
	 *
	 * @since 0.1.0
	 */
	public static final OverpassField VERSION_FIELD = new OverpassField("osm:version") {
		private final int maxIntLength = Integer.toString(Integer.MAX_VALUE).length();
		private final RangeDomain domain = new RangeDomain("version range", 1, Integer.MAX_VALUE);
		
		@Override
		public FieldType getType() {
			return FieldType.Integer;
		}
		
		@Override
		public int getLength() {
			return this.maxIntLength;
		}
		
		@Override
		public DomainObject getDomain() {
			return this.domain;
		}
	};
	/**
	 * {@link OverpassField} of the last change
	 *
	 * @since 0.1.0
	 */
	public static final OverpassField LASTCHANGE_FIELD = new OverpassField("osm:lastChange") {
		@Override
		public FieldType getType() {
			return FieldType.Date;
		}
		
		@Override
		public int getLength() {
			return 30;
		}
	};
	/**
	 * {@link OverpassField} of the last {@link ChangeSet}
	 *
	 * @since 0.1.0
	 */
	public static final OverpassField CHANGESET_FIELD = new OverpassField("osm:changeset") {
		private final int maxLongLength = Long.toString(Long.MAX_VALUE).length();
		
		@Override
		public FieldType getType() {
			return FieldType.Double;
		}
		
		@Override
		public int getLength() {
			return this.maxLongLength;
		}
	};
	
	/**
	 * the field name
	 *
	 * @since 0.1.0
	 */
	private final String name;
	
	/**
	 * standard constructor
	 *
	 * @param name the field name
	 * @since 0.1.0
	 */
	public OverpassField(String name) {
		this.name = name;
	}
	
	/**
	 * constructor, with given {@link Tag}
	 *
	 * @param tag the {@link Tag} to create the {@link OverpassField} from
	 * @since 0.1.0
	 */
	public OverpassField(Tag tag) {
		this(tag.getKey());
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.String;
	}
	
	@Override
	public String getAlias() {
		return "";
	}
	
	@Override
	public int getLength() {
		return 255;
	}
	
	@Override
	public boolean isEditable() {
		return false;
	}
	
	@Override
	public DomainObject getDomain() {
		return null;
	}
}