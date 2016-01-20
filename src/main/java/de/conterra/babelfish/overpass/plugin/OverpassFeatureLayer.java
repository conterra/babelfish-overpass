package de.conterra.babelfish.overpass.plugin;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.conterra.babelfish.overpass.config.FillSymbolType;
import de.conterra.babelfish.overpass.config.LayerType;
import de.conterra.babelfish.overpass.config.LineLayerType;
import de.conterra.babelfish.overpass.config.LineSymbolType;
import de.conterra.babelfish.overpass.config.NodeLayerType;
import de.conterra.babelfish.overpass.config.PictureSymbolType;
import de.conterra.babelfish.overpass.config.PolygonLayerType;
import de.conterra.babelfish.overpass.store.FeatureStore;
import de.conterra.babelfish.plugin.PluginAdapter;
import de.conterra.babelfish.plugin.v10_02.feature.Attachment;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.feature.FeatureLayer;
import de.conterra.babelfish.plugin.v10_02.feature.Field;
import de.conterra.babelfish.plugin.v10_02.feature.Popup;
import de.conterra.babelfish.plugin.v10_02.feature.PopupType;
import de.conterra.babelfish.plugin.v10_02.feature.Query;
import de.conterra.babelfish.plugin.v10_02.feature.Template;
import de.conterra.babelfish.plugin.v10_02.feature.Type;
import de.conterra.babelfish.plugin.v10_02.feature.wrapper.LayerWrapper;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import de.conterra.babelfish.plugin.v10_02.object.labeling.LabelingInfo;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleFillSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleLineSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.style.SFSStyle;
import de.conterra.babelfish.plugin.v10_02.object.symbol.style.SLSStyle;
import de.conterra.babelfish.util.DataUtils;

/**
 * defines the basic {@link FeatureLayer} to show features from the Overpass API
 * 
 * @version 0.0.1
 * @author chwe
 * @since 0.0.1
 * 
 * @param <G> the geometry type
 */
public abstract class OverpassFeatureLayer<G extends GeometryObject>
implements FeatureLayer<G, GeometryFeatureObject<G>>
{
	/**
	 * the {@link Logger} of this class
	 * 
	 * @since 0.1
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(OverpassFeatureLayer.class);
	
	/**
	 * the geometry type
	 * 
	 * @since 0.0.1
	 */
	@SuppressWarnings("unused")
	private final Class<G> geometryType;
	/**
	 * the unique identifier
	 * 
	 * @since 0.0.1
	 */
	private final int id;
	/**
	 * the name shown to the user
	 * 
	 * @since 0.0.1
	 */
	private final String name;
	/**
	 * the description shown to the user
	 * 
	 * @since 0.0.1
	 */
	private final String desc;
	/**
	 * the {@link FeatureStore}
	 * 
	 * @since 0.0.1
	 */
	private final FeatureStore<G> store;
	
	/**
	 * private standard constructor
	 * 
	 * @since 0.1.0
	 * 
	 * @param geometryType the {@link GeometryObject} type
	 * @param id the unique identifier
	 * @param name the user friendly name
	 * @param desc the description show to the user
	 * @param store the filter of meta data
	 */
	private OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, FeatureStore<G> store)
	{
		this.geometryType = geometryType;
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.store = store;
	}
	
	/**
	 * constructor, with given Overpass API script
	 * 
	 * @since 0.1.0
	 * 
	 * @param geometryType the {@link GeometryObject} type
	 * @param id the unique identifier
	 * @param name the user friendly name
	 * @param desc the description show to the user
	 * @param script the Overpass API script to use for requests
	 */
	@SuppressWarnings(
	{
			"unchecked", "rawtypes"
	})
	public OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, String script)
	{
		this(geometryType, id, name, desc, new FeatureStore(geometryType, script));
	}
	
	/**
	 * constructor, with given meta filter
	 * 
	 * @since 0.0.1
	 * 
	 * @param geometryType the {@link GeometryObject} type
	 * @param id the unique identifier
	 * @param name the user friendly name
	 * @param desc the description show to the user
	 * @param metaFilter the filter of meta data
	 */
	@SuppressWarnings(
	{
			"unchecked", "rawtypes"
	})
	public OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, Set<? extends String> metaFilter)
	{
		this(geometryType, id, name, desc, new FeatureStore(geometryType, metaFilter));
	}
	
	/**
	 * parses a {@link LineSymbolType} to a {@link SimpleLineSymbol}
	 * 
	 * @since 0.1.0
	 * 
	 * @param symbol the {@link LineSymbolType} to parse
	 * @return the {@link SimpleLineSymbol} representation of
	 *         <code>symbol</code>
	 */
	private static SimpleLineSymbol parseSymbol(LineSymbolType symbol)
	{
		if (symbol == null)
			return null;
		
		return new SimpleLineSymbol(SLSStyle.valueOf(symbol.getStyle().value()), Color.decode(symbol.getColor()), symbol.getWidth());
	}
	
	/**
	 * creates an {@link OverpassFeatureLayer} from a given {@link LayerType}
	 * 
	 * @since 0.0.1
	 * 
	 * @param layer the {@link LayerType}
	 * @return the created {@link OverpassFeatureLayer}
	 * @throws IllegalArgumentException if <code>layer</code> has an unknown
	 *         type
	 */
	public static OverpassFeatureLayer<?> createLayer(LayerType layer)
	throws IllegalArgumentException
	{
		String script = layer.getScript();
		boolean useScript = script != null && !script.isEmpty();
		
		if (layer instanceof NodeLayerType)
		{
			NodeLayerType nodeLayer = (NodeLayerType)layer;
			
			OverpassFeatureLayer.LOGGER.debug("Create a layer of nodes from " + nodeLayer.getName());
			
			Image image = null;
			PictureSymbolType symbol = nodeLayer.getSymbol();
			
			if (symbol != null)
			{
				try
				{
					File imageFile = new File(new File(PluginAdapter.getPluginFolder(OverpassPlugin.INSTANCE).toURI()), symbol.getPath());
					
					if (imageFile.exists())
						image = ImageIO.read(imageFile);
				}
				catch (URISyntaxException | NullPointerException | IOException e)
				{
				}
				
				String symbolData = symbol.getData();
				if (image == null && symbolData != null && ! (symbolData.isEmpty()))
					image = DataUtils.toImage(DataUtils.decodeBase64(symbolData));
			}
			
			if (useScript)
				return new OverpassNodeLayer(nodeLayer.getId(), nodeLayer.getName(), nodeLayer.getDesc(), script, image);
			
			return new OverpassNodeLayer(nodeLayer.getId(), nodeLayer.getName(), nodeLayer.getDesc(), new HashSet<>(nodeLayer.getMetaFilter()), image);
		}
		else if (layer instanceof LineLayerType)
		{
			LineLayerType lineLayer = (LineLayerType)layer;
			
			OverpassFeatureLayer.LOGGER.debug("Create a layer of lines from " + lineLayer.getName());
			
			SimpleLineSymbol symbol = OverpassFeatureLayer.parseSymbol(lineLayer.getSymbol());
			
			if (useScript)
				return new OverpassLineLayer(lineLayer.getId(), lineLayer.getName(), lineLayer.getDesc(), script, symbol);
			
			return new OverpassLineLayer(lineLayer.getId(), lineLayer.getName(), lineLayer.getDesc(), new HashSet<>(lineLayer.getMetaFilter()), symbol);
		}
		else if (layer instanceof PolygonLayerType)
		{
			PolygonLayerType polygonLayer = (PolygonLayerType)layer;
			
			OverpassFeatureLayer.LOGGER.debug("Create a layer of polygons from " + polygonLayer.getName());
			
			SimpleFillSymbol sfs = null;
			FillSymbolType symbol = polygonLayer.getSymbol();
			
			if (symbol != null)
				sfs = new SimpleFillSymbol(SFSStyle.Solid, Color.decode(symbol.getColor()), OverpassFeatureLayer.parseSymbol(symbol.getOutline()));
			
			if (useScript)
				return new OverpassPolygonLayer(polygonLayer.getId(), polygonLayer.getName(), polygonLayer.getDesc(), script, sfs);
			
			return new OverpassPolygonLayer(polygonLayer.getId(), polygonLayer.getName(), polygonLayer.getDesc(), new HashSet<>(polygonLayer.getMetaFilter()), sfs);
		}
		
		String msg = "The given layer have an unknown type!";
		OverpassFeatureLayer.LOGGER.error(msg);
		throw new IllegalArgumentException(msg);
	}
	
	@Override
	public int getId()
	{
		return this.id;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public String getDescription()
	{
		return this.desc;
	}
	
	@Override
	public String getCopyrightText()
	{
		return "\u00A9 OpenStreetMap contributors";
	}
	
	@Override
	public PopupType getPopupType()
	{
		return PopupType.HtmlText;
	}
	
	@Override
	public Field getObjectIdField()
	{
		return LayerWrapper.DEFAULT_OBJECT_ID_FIELD;
	}
	
	@Override
	public Field getGlobalIdField()
	{
		return LayerWrapper.DEFAULT_GLOBAL_ID_FIELD;
	}
	
	@Override
	public Field getDisplayField()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Field getTypeIdField()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<? extends Type<GeometryFeatureObject<G>>> getSubTypes()
	{
		return new LinkedHashSet<>();
	}
	
	@Override
	public Set<? extends Template<GeometryFeatureObject<G>>> getTemplates()
	{
		return new LinkedHashSet<>();
	}
	
	@Override
	public Query<GeometryFeatureObject<G>> getQuery()
	{
		return new OverpassQuery<G>(this.store);
	}
	
	@Override
	public Map<? extends String, ? extends Image> getImages()
	{
		return new LinkedHashMap<>();
	}
	
	@Override
	public Set<? extends Feature<GeometryFeatureObject<G>>> getFeatures()
	{
		Set<Feature<GeometryFeatureObject<G>>> res = new HashSet<>();
		
		for (Feature<? extends GeometryFeatureObject<G>> feature : this.getStore().getFeatures().values())
		{
			final Feature<? extends GeometryFeatureObject<G>> f = feature;
			
			res.add(new Feature<GeometryFeatureObject<G>>()
			{
				@Override
				public GeometryFeatureObject<G> getFeature()
				{
					return f.getFeature();
				}
				
				@Override
				public Set<? extends Attachment> getAttachments()
				{
					return f.getAttachments();
				}
				
				@Override
				public Popup getPopup()
				{
					return f.getPopup();
				}
			});
		}
		
		return res;
	}
	
	@Override
	public int getMinScale()
	{
		return 0;
	}
	
	@Override
	public int getMaxScale()
	{
		return 0;
	}
	
	@Override
	public int getTranparency()
	{
		return 0;
	}
	
	@Override
	public LabelingInfo getLabelingInfo()
	{
		return null;
	}
	
	/**
	 * gives the {@link FeatureStore}
	 * 
	 * @since 0.0.1
	 * 
	 * @return the {@link FeatureStore}
	 */
	public FeatureStore<G> getStore()
	{
		return this.store;
	}
}