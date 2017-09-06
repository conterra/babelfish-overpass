package de.conterra.babelfish.overpass.io;

import java.io.File;

/**
 * defines a format of an OSM-{@link File}
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.2.0
 */
public enum OsmFileFormat {
	/**
	 * plain OSM XML format
	 *
	 * @since 0.2.0
	 */
	XML,
	/**
	 * the Protocolbuffer Binary Format
	 *
	 * @since 0.2.0
	 */
	PBF;
}
