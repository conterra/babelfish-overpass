package de.conterra.babelfish.overpass.plugin;

import java.awt.Color;
import java.util.Set;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import de.conterra.babelfish.plugin.v10_02.feature.FeatureLayer;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Polyline;
import de.conterra.babelfish.plugin.v10_02.object.renderer.RendererObject;
import de.conterra.babelfish.plugin.v10_02.object.renderer.SimpleRenderer;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleLineSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.style.SLSStyle;

/**
 * defines an {@link FeatureLayer}, which shows {@link Way}s as {@link Polyline}
 * s
 * 
 * @version 0.1.0
 * @author chwe
 * @since 0.1.0
 */
public class OverpassLineLayer
extends OverpassFeatureLayer<Polyline>
{
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
	 * @since 0.1.0
	 * 
	 * @param id the unique identifier
	 * @param name the user-friendly name
	 * @param desc the description
	 * @param script the Overpass API script to use for requests
	 * @param symbol the {@link SimpleLineSymbol} to render the {@link Way}s
	 */
	public OverpassLineLayer(int id, String name, String desc, String script, SimpleLineSymbol symbol)
	{
		super(Polyline.class, id, name, desc, script);
		
		this.renderer = OverpassLineLayer.createRenderer(name, symbol);
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
	 * @param symbol the {@link SimpleLineSymbol} to render the {@link Way}s
	 */
	public OverpassLineLayer(int id, String name, String desc, Set<? extends String> metaFilter, SimpleLineSymbol symbol)
	{
		super(Polyline.class, id, name, desc, metaFilter);
		
		this.renderer = OverpassLineLayer.createRenderer(name, symbol);
	}
	
	/**
	 * creates a {@link RendererObject} to render {@link Polyline}s with a
	 * {@link SimpleLineSymbol}
	 * 
	 * @since 0.1.0
	 * 
	 * @param name the label text
	 * @param symbol the {@link SimpleLineSymbol} to rendering {@link Polyline}s
	 * @return the generated {@link RendererObject}
	 */
	private static RendererObject createRenderer(String name, SimpleLineSymbol symbol)
	{
		SimpleLineSymbol sls;
		
		if (symbol != null)
			sls = symbol;
		else
			sls = OverpassLineLayer.DEFAULT_SYMBOL;
		
		return new SimpleRenderer(sls, name);
	}
	
	@Override
	public Class<Polyline> getGeometryType()
	{
		return Polyline.class;
	}
	
	@Override
	public RendererObject getRenderer()
	{
		return this.renderer;
	}
}