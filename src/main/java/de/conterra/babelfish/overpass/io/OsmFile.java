package de.conterra.babelfish.overpass.io;

import lombok.Getter;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;

import java.io.File;

/**
 * defines a {@link File}, which contains OpenStreetMap data
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.2.0
 */
public class OsmFile {
	/**
	 * the data {@link File}
	 *
	 * @since 0.2.0
	 */
	@Getter
	private final File              dataFile;
	/**
	 * the {@link OsmFileFormat} of {@code dataFile}
	 *
	 * @since 0.2.0
	 */
	@Getter
	private final OsmFileFormat     fileFormat;
	/**
	 * the {@link CompressionMethod} of {@code dataFile}
	 *
	 * @since 0.2.0
	 */
	@Getter
	private final CompressionMethod compression;
	
	/**
	 * standard constructor
	 *
	 * @param dataFile    the data {@link File}
	 * @param fileFormat  the {@link OsmFileFormat} of {@code dataFile}
	 * @param compression the {@link CompressionMethod} of {@code dataFile}
	 * @since 0.2.0
	 */
	public OsmFile(File dataFile, OsmFileFormat fileFormat, CompressionMethod compression) {
		this.dataFile = dataFile;
		this.fileFormat = fileFormat;
		this.compression = compression;
	}
}
