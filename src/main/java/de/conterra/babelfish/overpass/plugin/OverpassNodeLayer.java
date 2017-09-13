package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.overpass.io.OsmFile;
import de.conterra.babelfish.plugin.v10_02.feature.FeatureLayer;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Point;
import de.conterra.babelfish.plugin.v10_02.object.renderer.RendererObject;
import de.conterra.babelfish.plugin.v10_02.object.renderer.SimpleRenderer;
import de.conterra.babelfish.plugin.v10_02.object.symbol.PictureMarkerSymbol;
import de.conterra.babelfish.util.DataUtils;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Set;

/**
 * defines an {@link FeatureLayer}, which shows {@link Node}s
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.1.0
 */
public class OverpassNodeLayer
		extends OverpassFeatureLayer<Point> {
	/**
	 * the default {@link Image}, if non is defined
	 *
	 * @since 0.1.0
	 */
	private static final Image DEFAULT_IMAGE = DataUtils.toImage(DataUtils.decodeBase64("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAACWUlEQVR42u1WTUgVURSesmhTEWXQwmghQUEWrkKCMBe2CEQwV22yN2/eI3MVRUSLASHcFLoI3CgUCRrRDzNzy01vm2ELCSJzaUghoosWKeXXd55XmXffjL33UIOaC4c3c+b8fO+ce8/9LKvC5QRwKdCirK1efxXA1QCHmHQwBOCt6DY1qe3jeFqhh8kmQolNmRCbjIdTG1tuhduhJIuUdwaQL5QPlO9rOoXrGwbAdbE948PJBMg6Hqrj9oCbww4mvka7DvH5dzdhAiABkACQlfXQqEG4nH6XrGQl679YpZCOiolJVuFM2kdL3Pf8VRzgJ8WOTT6Kw46PKdr0y9VcFoB0gDYZKkz0IKzvUthFXW9o6PyinC5KnsNug6Q8Tb3G/oLh5eMC9Z/5214E4MpL7NHMZtlWqBGdBOD7aAT1siMqWE/9V5OirYLQPHKesiC54qrQnXf0MWC/wskI/jdLcE2x49nDMdpMmj6UOokp75Ijtg2dz3CARt8075s1An0i2Tz6p1ayZXtJy95HgJCY85Jj3QCyB8ySE/WLy8+xrxwOSb+hiNb1rY88wKMIp/uVHlsCvxcR70YRYWXJamk8FsP1Z3g8z5d9Xfs4J75RMcmwH1vAtpWSe2igctowmhM923GHz0vaKVVKYmkV7Yd1nKV8DIVWPv8oaKtCTqouPfcKkssG0txfV6eZNuOs0MMSicqt1SMoviF9nfwxY2+NWR0KB/nyUQ+hEWccO82g7U9QxW9nSwFAu7sZhYviY35LBTgSOtrLawMp6+OElGor7hM93N6wOjfl/TeNnA0tLXxblAAAAABJRU5ErkJggg=="));
	
	/**
	 * the {@link RendererObject}
	 *
	 * @since 0.1.0
	 */
	private final RendererObject renderer;
	
	/**
	 * constructor, with given Overpass API script
	 *
	 * @param id     the unique identifier
	 * @param name   the user-friendly name
	 * @param desc   the user-friendly name
	 * @param script the Overpass API script to use for requests
	 * @param image  the {@link Image} with which the points will be rendered
	 * @since 0.1.0
	 */
	public OverpassNodeLayer(int id, String name, String desc, String script, Image image) {
		super(Point.class, id, name, desc, script);
		
		this.renderer = OverpassNodeLayer.createRenderer(name, image);
	}
	
	/**
	 * constructor, with given meta filter
	 *
	 * @param id         the unique identifier
	 * @param name       the user-friendly name
	 * @param desc       the description
	 * @param metaFilter the filter of meta data
	 * @param image      the {@link Image} with which the points will be rendered
	 * @since 0.1.0
	 */
	public OverpassNodeLayer(int id, String name, String desc, Set<? extends String> metaFilter, Image image) {
		super(Point.class, id, name, desc, metaFilter);
		
		this.renderer = OverpassNodeLayer.createRenderer(name, image);
	}
	
	/**
	 * constructor, with given {@link OsmFile} and filter parameters
	 *
	 * @param id    the unique identifier
	 * @param name  the user-friendly name
	 * @param desc  the description
	 * @param file  the {@link OsmFile} to get the features from
	 * @param image the {@link Image} with which the points will be rendered
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	public OverpassNodeLayer(int id, String name, String desc, OsmFile file, String typeKey, Set<String> typeValues, Image image)
	throws FileNotFoundException {
		super(Point.class, id, name, desc, file, typeKey, typeValues);
		
		this.renderer = OverpassNodeLayer.createRenderer(name, image);
	}
	
	/**
	 * constructor, with given {@link OsmFile} to get the features from
	 *
	 * @param id    the unique identifier
	 * @param name  the user-friendly name
	 * @param desc  the description
	 * @param file  the {@link OsmFile} to get the features from
	 * @param image the {@link Image} with which the points will be rendered
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	public OverpassNodeLayer(int id, String name, String desc, OsmFile file, Image image)
	throws FileNotFoundException {
		super(Point.class, id, name, desc, file);
		
		this.renderer = OverpassNodeLayer.createRenderer(name, image);
	}
	
	/**
	 * creates a {@link RendererObject} to render {@link Point}s with an {@link Image}
	 *
	 * @param name  the label text
	 * @param image the {@link Image} to use for rendering {@link Point}s
	 * @return the generated {@link RendererObject}
	 *
	 * @since 0.1.0
	 */
	private static RendererObject createRenderer(String name, Image image) {
		Image img;
		
		if (image != null) {
			img = image;
		} else {
			img = OverpassNodeLayer.DEFAULT_IMAGE;
		}
		
		return new SimpleRenderer(new PictureMarkerSymbol(img), name);
	}
	
	@Override
	public Class<Point> getGeometryType() {
		return Point.class;
	}
	
	@Override
	public RendererObject getRenderer() {
		return this.renderer;
	}
}
