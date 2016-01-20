package de.conterra.babelfish.overpass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.opengis.geometry.Envelope;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import crosby.binary.osmosis.OsmosisReader;
import de.conterra.babelfish.overpass.io.OverpassHandler;
import de.conterra.babelfish.overpass.store.FeatureStore;
import de.conterra.babelfish.plugin.v10_02.feature.Feature;
import de.conterra.babelfish.plugin.v10_02.object.feature.GeometryFeatureObject;
import de.conterra.babelfish.plugin.v10_02.object.geometry.Point;
import de.conterra.babelfish.util.DataUtils;

/**
 * defines a class
 * 
 * @version 0.0.1
 * @author chwe
 * @since 0.0.1
 */
public class TestMain
{
	public int nodes = 0;
	public int ways = 0;
	public int relations = 0;
	
	/**
	 * standard constructor
	 * 
	 * @since 0.0.1
	 */
	public TestMain()
	{
	}
	
	/**
	 * what does this method do?
	 * 
	 * @since 0.0.1
	 * 
	 * @param args arguments from the console
	 */
	public static void main(String[] args)
	{
		try
		{
			System.out.println("Sparkasse: " + DataUtils.encodeBase64(DataUtils.toByteArray(ImageIO.read(new File("W:\\Babelfish\\sparkasse.png")))));
			System.out.println("Volksbank: " + DataUtils.encodeBase64(DataUtils.toByteArray(ImageIO.read(new File("W:\\Babelfish\\volksbank.png")))));
			System.out.println("Deutsche Bank: " + DataUtils.encodeBase64(DataUtils.toByteArray(ImageIO.read(new File("W:\\Babelfish\\deutsche_bank.png")))));
			System.out.println("Postbank: " + DataUtils.encodeBase64(DataUtils.toByteArray(ImageIO.read(new File("W:\\Babelfish\\postbank.png")))));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String test = "Ich will alle möglichen \u00e5\u00e6\u00e9\u00f8 Zeichen erssetzen, die nicht alphanumerischer Natur sind. Also auch der Doppelpunkt in osm:user! Und natürlich auch Zahlen, wie 7-8-5~";
		System.out.println(test.replaceAll("[^\\p{L}0-9]", "_"));
		
		Set<String> metaFilter = new HashSet<>();
		metaFilter.add("~\".\"~\".{120}\"");
		
		FeatureStore<Point> store = new FeatureStore<>(Point.class, metaFilter);
		
		de.conterra.babelfish.plugin.v10_02.object.geometry.Envelope env = new de.conterra.babelfish.plugin.v10_02.object.geometry.Envelope(
		new EnvelopeImpl(new DirectPosition2D(OverpassHandler.OSM_CRS, 51.95973, 7.61015), new DirectPosition2D(OverpassHandler.OSM_CRS, 51.96423, 7.61775)));
		
		Map<? extends Long, ? extends Feature<? extends GeometryFeatureObject<?>>> features = store.getFeatures(env);
		System.out.println("Count: " + features.size());
		
		for (long id : features.keySet())
		{
			Feature<? extends GeometryFeatureObject<?>> feature = features.get(id);
			
			System.out.println("---------------------------------------------------------");
			System.out.println(feature.getPopup().getContent());
			System.out.println("---------------------------------------------------------");
		}
	}
	
	public void getPopups()
	throws IllegalArgumentException, IOException
	{
		EntityType type = EntityType.Node;
		Set<String> where = new HashSet<>();
		Envelope bbox = new EnvelopeImpl(new DirectPosition2D(OverpassHandler.OSM_CRS, 51.93391678338885, 7.651022672653198), new DirectPosition2D(OverpassHandler.OSM_CRS, 51.93556721492968, 7.653753161430359));
		
		where.add("highway=bus_stop");
		
		// for (Entity entity : OverpassHandler.getFeatures(type, where,
		// bbox).values())
		// {
		// long id = entity.getId();
		//
		// System.out.println("Entity: " + id);
		// System.out.println("Class " +
		// OverpassHandler.typeFromClass(entity.getClass()));
		// System.out.println("---------------------------------------------------------");
		// System.out.println(PopupStore.getPopup(id, type).getContent());
		// System.out.println("---------------------------------------------------------");
		// System.out.println("\r\n\r\n\r\n");
		//
		// if (entity instanceof Way)
		// {
		// Way w = (Way)entity;
		//
		// for (WayNode node : w.getWayNodes())
		// node.getNodeId();
		// }
		// }
	}
	
	public void doIt()
	throws IllegalArgumentException, IOException
	{
		// the input file
		File file = new File("C:/Users/chwe/Documents/workspace/babelfish-overpass/src/main/resources/drink.osm");
		
		Sink sinkImplementation = new Sink()
		{
			@Override
			public void process(EntityContainer entityContainer)
			{
				Entity entity = entityContainer.getEntity();
				if (entity instanceof Node)
				{
					TestMain.this.nodes++;
				}
				else if (entity instanceof Way)
				{
					TestMain.this.ways++;
				}
				else if (entity instanceof Relation)
				{
					TestMain.this.relations++;
				}
			}
			
			@Override
			public void release()
			{
			}
			
			@Override
			public void complete()
			{
			}
			
			@Override
			public void initialize(Map<String, Object> metaData)
			{
				// TODO Auto-generated method stub
			}
		};
		
		boolean pbf = false;
		CompressionMethod compression = CompressionMethod.None;
		
		String filename = file.getName().toLowerCase(Locale.ROOT);
		if (filename.endsWith(".pbf"))
			pbf = true;
		else if (filename.endsWith(".gz"))
			compression = CompressionMethod.GZip;
		else if (filename.endsWith(".bz2"))
			compression = CompressionMethod.BZip2;
		
		RunnableSource reader;
		
		if (pbf)
		{
			reader = new OsmosisReader(new FileInputStream(file));
		}
		else
			reader = new XmlReader(file, false, compression);
		
		reader.setSink(sinkImplementation);
		
		Thread readerThread = new Thread(reader);
		readerThread.start();
		
		while (readerThread.isAlive())
		{
			try
			{
				readerThread.join();
			}
			catch (InterruptedException e)
			{
				/* do nothing */
			}
		}
		
		System.out.println("Nodes: " + this.nodes + " Ways: " + this.ways + " Relations: " + this.relations);
	}
}