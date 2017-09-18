package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.overpass.io.OsmFile;
import de.conterra.babelfish.plugin.v10_02.feature.FeatureLayer;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Polyline;
import de.conterra.babelfish.plugin.v10_02.object.renderer.RendererObject;
import de.conterra.babelfish.plugin.v10_02.object.renderer.SimpleRenderer;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleLineSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.style.SLSStyle;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Set;

/**
 * defines an {@link FeatureLayer}, which shows {@link Way}s as {@link Polyline}s
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.1.0
 */
public class OverpassLineLayer
		extends OverpassFeatureLayer<Polyline> {
	/**
	 * the default {@link SimpleLineSymbol}, if non is defined
	 *
	 * @since 0.1.0
	 */
	public static final SimpleLineSymbol DEFAULT_SYMBOL = new SimpleLineSymbol(SLSStyle.Solid, Color.CYAN, 1);
	
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
	 * @param desc   the description
	 * @param script the Overpass API script to use for requests
	 * @param symbol the {@link SimpleLineSymbol} to render the {@link Way}s
	 * @since 0.1.0
	 */
	public OverpassLineLayer(int id, String name, String desc, String script, SimpleLineSymbol symbol) {
		super(Polyline.class, id, name, desc, script);
		
		this.renderer = OverpassLineLayer.createRenderer(name, symbol);
	}
	
	/**
	 * constructor, with given meta filter
	 *
	 * @param id         the unique identifier
	 * @param name       the user-friendly name
	 * @param desc       the description
	 * @param metaFilter the filter of meta data
	 * @param symbol     the {@link SimpleLineSymbol} to render the {@link Way}s
	 * @since 0.1.0
	 */
	public OverpassLineLayer(int id, String name, String desc, Set<? extends String> metaFilter, SimpleLineSymbol symbol) {
		super(Polyline.class, id, name, desc, metaFilter);
		
		this.renderer = OverpassLineLayer.createRenderer(name, symbol);
	}
	
	/**
	 * constructor, with given {@link OsmFile} and filter parameters
	 *
	 * @param id         the unique identifier
	 * @param name       the user-friendly name
	 * @param desc       the description
	 * @param file       the {@link OsmFile} to get the features from
	 * @param typeKey    the {@link Tag} key to filter to
	 * @param typeValues the {@link Tag} values to filter to
	 * @param delimiter  delimiter RegEx to split the {@link Tag} value
	 * @param symbol     the {@link SimpleLineSymbol} to render the {@link Way}s
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	public OverpassLineLayer(int id, String name, String desc, OsmFile file, String typeKey, Set<String> typeValues, String delimiter, SimpleLineSymbol symbol)
	throws FileNotFoundException {
		super(Polyline.class, id, name, desc, file, typeKey, typeValues, delimiter);
		
		this.renderer = OverpassLineLayer.createRenderer(name, symbol);
	}
	
	/**
	 * constructor, with given {@link OsmFile} to get the features from
	 *
	 * @param id     the unique identifier
	 * @param name   the user-friendly name
	 * @param desc   the description
	 * @param file   the {@link OsmFile} to get the features from
	 * @param symbol the {@link SimpleLineSymbol} to render the {@link Way}s
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	public OverpassLineLayer(int id, String name, String desc, OsmFile file, SimpleLineSymbol symbol)
	throws FileNotFoundException {
		super(Polyline.class, id, name, desc, file);
		
		this.renderer = OverpassLineLayer.createRenderer(name, symbol);
	}
	
	/**
	 * creates a {@link RendererObject} to render {@link Polyline}s with a {@link SimpleLineSymbol}
	 *
	 * @param name   the label text
	 * @param symbol the {@link SimpleLineSymbol} to rendering {@link Polyline}s
	 * @return the generated {@link RendererObject}
	 *
	 * @since 0.1.0
	 */
	private static RendererObject createRenderer(String name, SimpleLineSymbol symbol) {
		SimpleLineSymbol sls;
		
		if (symbol != null) {
			sls = symbol;
		} else {
			sls = OverpassLineLayer.DEFAULT_SYMBOL;
		}
		
		return new SimpleRenderer(sls, name);
	}
	
	@Override
	public Class<Polyline> getGeometryType() {
		return Polyline.class;
	}
	
	@Override
	public RendererObject getRenderer() {
		return this.renderer;
	}
}
