package de.conterra.babelfish.overpass.plugin;

import java.awt.Color;
import java.util.Set;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import de.conterra.babelfish.plugin.v10_02.feature.FeatureLayer;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Polygon;
import de.conterra.babelfish.plugin.v10_02.object.renderer.RendererObject;
import de.conterra.babelfish.plugin.v10_02.object.renderer.SimpleRenderer;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleFillSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleLineSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.style.SFSStyle;

/**
 * defines an {@link FeatureLayer}, which shows {@link Way}s as {@link Polygon}s
 * 
 * @version 0.1.0
 * @author chwe
 * @since 0.1.0
 */
public class OverpassPolygonLayer
extends OverpassFeatureLayer<Polygon>
{
	/**
	 * the default {@link SimpleFillSymbol}, if non is defined
	 * 
	 * @since 0.1.0
	 */
	public static final SimpleFillSymbol DEFAULT_SYMBOL = new SimpleFillSymbol(SFSStyle.Solid, Color.YELLOW, OverpassLineLayer.DEFAULT_SYMBOL);
	
	/**
	 * the {@link RendererObject}
	 * 
	 * @since 0.1.0
	 */
	private final RendererObject renderer;
	
	/**
	 * constructor, with given Overpass API script
	 * 
	 * @since 0.1.0
	 * 
	 * @param id the unique identifier
	 * @param name the user-friendly name
	 * @param desc the description
	 * @param script the Overpass API script to use for requests
	 * @param symbol the {@link SimpleFillSymbol} to render the {@link Way}s
	 */
	public OverpassPolygonLayer(int id, String name, String desc, String script, SimpleFillSymbol symbol)
	{
		super(Polygon.class, id, name, desc, script);
		
		this.renderer = OverpassPolygonLayer.createRenderer(name, symbol);
	}
	
	/**
	 * constructor, with given meta filter
	 * 
	 * @since 0.1.0
	 * 
	 * @param id the unique identifier
	 * @param name the user-friendly name
	 * @param desc the description
	 * @param metaFilter the filter of meta data
	 * @param symbol the {@link SimpleFillSymbol} to render the {@link Way}s
	 */
	public OverpassPolygonLayer(int id, String name, String desc, Set<? extends String> metaFilter, SimpleFillSymbol symbol)
	{
		super(Polygon.class, id, name, desc, metaFilter);
		
		this.renderer = OverpassPolygonLayer.createRenderer(name, symbol);
	}
	
	/**
	 * creates a {@link RendererObject} to render {@link Polygon}s with a
	 * {@link SimpleLineSymbol}
	 * 
	 * @since 0.1.0
	 * 
	 * @param name the label text
	 * @param symbol the {@link SimpleLineSymbol} to rendering {@link Polygon}s
	 * @return the generated {@link RendererObject}
	 */
	private static RendererObject createRenderer(String name, SimpleFillSymbol symbol)
	{
		SimpleFillSymbol sfs;
		
		if (symbol != null)
			sfs = symbol;
		else
			sfs = OverpassPolygonLayer.DEFAULT_SYMBOL;
		
		return new SimpleRenderer(sfs, name);
	}
	
	@Override
	public Class<Polygon> getGeometryType()
	{
		return Polygon.class;
	}
	
	@Override
	public RendererObject getRenderer()
	{
		return this.renderer;
	}
}