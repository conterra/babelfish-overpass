package de.conterra.babelfish.overpass.io;

import de.conterra.babelfish.overpass.config.FileType;
import de.conterra.babelfish.overpass.plugin.OverpassPlugin;
import lombok.Getter;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;

import java.io.File;
import java.io.FileNotFoundException;

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
	
	/**
	 * constructor, created from {@link FileType}
	 *
	 * @param xmlFileType the {@link FileType} to get the information from
	 * @throws FileNotFoundException if the file path is not set or the {@link File} couldn't found under the given path
	 */
	public OsmFile(FileType xmlFileType)
	throws FileNotFoundException {
		String filePath = xmlFileType.getPath();
		File   file     = new File(OverpassPlugin.SERVICES_FOLDER, xmlFileType.getPath());
		
		if (!(file.exists())) {
			throw new FileNotFoundException("Couldn't load file from given path! (" + filePath + ")");
		}
		
		this.dataFile = file;
		this.fileFormat = xmlFileType.getType();
		this.compression = xmlFileType.getCompression();
	}
}
