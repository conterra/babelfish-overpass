package de.conterra.babelfish.overpass.plugin;

import de.conterra.babelfish.overpass.config.*;
import de.conterra.babelfish.overpass.io.OsmFile;
import de.conterra.babelfish.overpass.store.FeatureStore;
import de.conterra.babelfish.overpass.store.FileFeatureStore;
import de.conterra.babelfish.overpass.store.OverpassFeatureStore;
import de.conterra.babelfish.plugin.v10_02.feature.*;
import de.conterra.babelfish.plugin.v10_02.feature.wrapper.LayerWrapper;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.GeometryObject;
import de.conterra.babelfish.plugin.v10_02.object.labeling.LabelingInfo;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleFillSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.SimpleLineSymbol;
import de.conterra.babelfish.plugin.v10_02.object.symbol.style.SFSStyle;
import de.conterra.babelfish.plugin.v10_02.object.symbol.style.SLSStyle;
import de.conterra.babelfish.util.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * defines the basic {@link FeatureLayer} to show features from the Overpass API
 *
 * @param <G> the geometry type
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.1.0
 */
@Slf4j
public abstract class OverpassFeatureLayer<G extends GeometryObject>
		implements FeatureLayer<G, GeometryFeatureObject<G>> {
	/**
	 * the geometry type
	 *
	 * @since 0.1.0
	 */
	@SuppressWarnings("unused")
	private final Class<G>        geometryType;
	/**
	 * the unique identifier
	 *
	 * @since 0.1.0
	 */
	private final int             id;
	/**
	 * the name shown to the user
	 *
	 * @since 0.1.0
	 */
	private final String          name;
	/**
	 * the description shown to the user
	 *
	 * @since 0.1.0
	 */
	private final String          desc;
	/**
	 * the {@link FeatureStore}
	 *
	 * @since 0.1.0
	 */
	private final FeatureStore<G> store;
	
	/**
	 * private standard constructor
	 *
	 * @param geometryType the {@link GeometryObject} type
	 * @param id           the unique identifier
	 * @param name         the user friendly name
	 * @param desc         the description show to the user
	 * @param store        the filter of meta data
	 * @since 0.1.0
	 */
	private OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, FeatureStore<G> store) {
		this.geometryType = geometryType;
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.store = store;
	}
	
	/**
	 * constructor, with given Overpass API script
	 *
	 * @param geometryType the {@link GeometryObject} type
	 * @param id           the unique identifier
	 * @param name         the user friendly name
	 * @param desc         the description show to the user
	 * @param script       the Overpass API script to use for requests
	 * @since 0.1.0
	 */
	@SuppressWarnings(
			{
					"unchecked", "rawtypes"
			})
	public OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, String script) {
		this(geometryType, id, name, desc, new OverpassFeatureStore(geometryType, script));
	}
	
	/**
	 * constructor, with given meta filter
	 *
	 * @param geometryType the {@link GeometryObject} type
	 * @param id           the unique identifier
	 * @param name         the user friendly name
	 * @param desc         the description show to the user
	 * @param metaFilter   the filter of meta data
	 * @since 0.1.0
	 */
	@SuppressWarnings(
			{
					"unchecked", "rawtypes"
			})
	public OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, Set<? extends String> metaFilter) {
		this(geometryType, id, name, desc, new OverpassFeatureStore(geometryType, metaFilter));
	}
	
	/**
	 * constructor, with given {@link OsmFile} and filter parameters
	 *
	 * @param geometryType the {@link GeometryObject} type
	 * @param id           the unique identifier
	 * @param name         the user friendly name
	 * @param desc         the description show to the user
	 * @param file         the {@link OsmFile} to get the features from
	 * @param typeKey      the {@link Tag} key to filter to
	 * @param typeValues   the {@link Tag} values to filter to
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, OsmFile file, String typeKey, Set<String> typeValues)
	throws FileNotFoundException {
		this(geometryType, id, name, desc, new FileFeatureStore(geometryType, file, typeKey, typeValues));
	}
	
	/**
	 * constructor, with given {@link OsmFile} to get the features from
	 *
	 * @param geometryType the {@link GeometryObject} type
	 * @param id           the unique identifier
	 * @param name         the user friendly name
	 * @param desc         the description show to the user
	 * @param file         the {@link OsmFile} to get the features from
	 * @throws FileNotFoundException if {@code file} doesn't exist
	 * @since 0.2.0
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public OverpassFeatureLayer(Class<G> geometryType, int id, String name, String desc, OsmFile file)
	throws FileNotFoundException {
		this(geometryType, id, name, desc, new FileFeatureStore(geometryType, file));
	}
	
	/**
	 * parses a {@link LineSymbolType} into a {@link SimpleLineSymbol}
	 *
	 * @param symbol the {@link LineSymbolType} to parse
	 * @return the {@link SimpleLineSymbol} representation of {@code symbol}
	 *
	 * @since 0.1.0
	 */
	public static SimpleLineSymbol parseSymbol(LineSymbolType symbol) {
		if (symbol == null) {
			return null;
		}
		
		return new SimpleLineSymbol(SLSStyle.valueOf(symbol.getStyle().value()), Color.decode(symbol.getColor()), symbol.getWidth());
	}
	
	/**
	 * parses a {@link FillSymbolType} into a {@link SimpleFillSymbol}
	 *
	 * @param symbol the {@link FillSymbolType} to parse
	 * @return the {@link SimpleFillSymbol} representation of {@code symbol}
	 *
	 * @since 0.2.0
	 */
	public static SimpleFillSymbol parseSymbol(FillSymbolType symbol) {
		SimpleFillSymbol sfs = null;
		
		if (symbol != null) {
			sfs = new SimpleFillSymbol(SFSStyle.Solid, Color.decode(symbol.getColor()), OverpassFeatureLayer.parseSymbol(symbol.getOutline()));
		}
		
		return sfs;
	}
	
	/**
	 * parses a {@link PictureSymbolType} into an {@link Image}
	 *
	 * @param symbol the {@link PictureSymbolType} to parse
	 * @return the {@link Image} representation of {@code symbol}
	 *
	 * @since 0.2.0
	 */
	public static Image parseImage(PictureSymbolType symbol) {
		Image image = null;
		
		if (symbol != null) {
			try {
				File imageFile = new File(OverpassPlugin.SERVICES_FOLDER, symbol.getPath());
				
				if (imageFile.exists()) {
					image = ImageIO.read(imageFile);
				}
			} catch (NullPointerException | IOException e) {
				log.debug("Symbol couldn't load from file: " + symbol.getPath(), e);
			}
			
			String symbolData = symbol.getData();
			if (image == null && symbolData != null && !(symbolData.isEmpty())) {
				image = DataUtils.toImage(DataUtils.decodeBase64(symbolData));
			}
		}
		
		return image;
	}
	
	/**
	 * creates an {@link OverpassFeatureLayer} from a given {@link LayerType}
	 *
	 * @param layer the {@link LayerType}
	 * @return the created {@link OverpassFeatureLayer}
	 *
	 * @throws IOException              id the {@link OsmFile} couldn't load
	 * @throws IllegalArgumentException if {@code layer} has an unknown type
	 * @since 0.1.0
	 */
	public static OverpassFeatureLayer<?> createLayer(LayerType layer)
	throws IOException, IllegalArgumentException {
		String  script    = layer.getScript();
		boolean useScript = script != null && !(script.isEmpty());
		
		FileType fileInfo = layer.getFile();
		boolean  useFile  = !useScript && (fileInfo != null);
		OsmFile  dataFile = null;
		if (useFile) {
			dataFile = new OsmFile(fileInfo);
		}
		
		if (layer instanceof NodeLayerType) {
			NodeLayerType nodeLayer = (NodeLayerType) layer;
			
			log.debug("Create a layer of nodes from " + nodeLayer.getName());
			
			Image image = OverpassFeatureLayer.parseImage(nodeLayer.getSymbol());
			
			if (useFile) {
				return new OverpassNodeLayer(nodeLayer.getId(), nodeLayer.getName(), nodeLayer.getDesc(), dataFile, image);
			} else if (useScript) {
				return new OverpassNodeLayer(nodeLayer.getId(), nodeLayer.getName(), nodeLayer.getDesc(), script, image);
			} else {
				return new OverpassNodeLayer(nodeLayer.getId(), nodeLayer.getName(), nodeLayer.getDesc(), new HashSet<>(nodeLayer.getMetaFilter()), image);
			}
		} else if (layer instanceof LineLayerType) {
			LineLayerType lineLayer = (LineLayerType) layer;
			
			log.debug("Create a layer of lines from " + lineLayer.getName());
			
			SimpleLineSymbol symbol = OverpassFeatureLayer.parseSymbol(lineLayer.getSymbol());
			
			if (useFile) {
				return new OverpassLineLayer(lineLayer.getId(), lineLayer.getName(), lineLayer.getDesc(), dataFile, symbol);
			} else if (useScript) {
				return new OverpassLineLayer(lineLayer.getId(), lineLayer.getName(), lineLayer.getDesc(), script, symbol);
			} else {
				return new OverpassLineLayer(lineLayer.getId(), lineLayer.getName(), lineLayer.getDesc(), new HashSet<>(lineLayer.getMetaFilter()), symbol);
			}
		} else if (layer instanceof PolygonLayerType) {
			PolygonLayerType polygonLayer = (PolygonLayerType) layer;
			
			log.debug("Create a layer of polygons from " + polygonLayer.getName());
			
			SimpleFillSymbol sfs    = null;
			FillSymbolType   symbol = polygonLayer.getSymbol();
			
			if (symbol != null) {
				sfs = new SimpleFillSymbol(SFSStyle.Solid, Color.decode(symbol.getColor()), OverpassFeatureLayer.parseSymbol(symbol.getOutline()));
			}
			
			if (useFile) {
				return new OverpassPolygonLayer(polygonLayer.getId(), polygonLayer.getName(), polygonLayer.getDesc(), dataFile, sfs);
			} else if (useScript) {
				return new OverpassPolygonLayer(polygonLayer.getId(), polygonLayer.getName(), polygonLayer.getDesc(), script, sfs);
			} else {
				return new OverpassPolygonLayer(polygonLayer.getId(), polygonLayer.getName(), polygonLayer.getDesc(), new HashSet<>(polygonLayer.getMetaFilter()), sfs);
			}
		}
		
		String msg = "The given layer has an unknown type!";
		log.error(msg);
		throw new IllegalArgumentException(msg);
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getDescription() {
		return this.desc;
	}
	
	@Override
	public String getCopyrightText() {
		return "\u00A9 OpenStreetMap contributors";
	}
	
	@Override
	public PopupType getPopupType() {
		return PopupType.HtmlText;
	}
	
	@Override
	public Field getObjectIdField() {
		return LayerWrapper.DEFAULT_OBJECT_ID_FIELD;
	}
	
	@Override
	public Field getGlobalIdField() {
		return LayerWrapper.DEFAULT_GLOBAL_ID_FIELD;
	}
	
	@Override
	public Field getDisplayField() {
		return OverpassField.NAME_FIELD;
	}
	
	@Override
	public Field getTypeIdField() {
		// ToDo Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<? extends Type<GeometryFeatureObject<G>>> getSubTypes() {
		return new LinkedHashSet<>();
	}
	
	@Override
	public Set<? extends Template<GeometryFeatureObject<G>>> getTemplates() {
		return new LinkedHashSet<>();
	}
	
	@Override
	public Query<GeometryFeatureObject<G>> getQuery() {
		return new OverpassQuery<G>(this.store);
	}
	
	@Override
	public Map<? extends String, ? extends Image> getImages() {
		return new LinkedHashMap<>();
	}
	
	@Override
	public Set<? extends Feature<GeometryFeatureObject<G>>> getFeatures() {
		Set<Feature<GeometryFeatureObject<G>>> res = new HashSet<>();
		
		for (Feature<? extends GeometryFeatureObject<G>> feature : this.getStore().getFeatures().values()) {
			final Feature<? extends GeometryFeatureObject<G>> f = feature;
			
			res.add(new Feature<GeometryFeatureObject<G>>() {
				@Override
				public GeometryFeatureObject<G> getFeature() {
					return f.getFeature();
				}
				
				@Override
				public Set<? extends Attachment> getAttachments() {
					return f.getAttachments();
				}
				
				@Override
				public Popup getPopup() {
					return f.getPopup();
				}
			});
		}
		
		return res;
	}
	
	@Override
	public int getMinScale() {
		return 0;
	}
	
	@Override
	public int getMaxScale() {
		return 0;
	}
	
	@Override
	public int getTranparency() {
		return 0;
	}
	
	@Override
	public LabelingInfo getLabelingInfo() {
		return null;
	}
	
	/**
	 * gives the {@link FeatureStore}
	 *
	 * @return the {@link FeatureStore}
	 *
	 * @since 0.1.0
	 */
	public FeatureStore<G> getStore() {
		return this.store;
	}
}
