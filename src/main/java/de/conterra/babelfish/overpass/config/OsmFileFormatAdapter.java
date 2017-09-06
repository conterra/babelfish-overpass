package de.conterra.babelfish.overpass.config;

import de.conterra.babelfish.overpass.io.OsmFileFormat;

/**
 * defines an JAXB adapter of {@link OsmFileFormat}s
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.2.0
 */
public class OsmFileFormatAdapter {
	/**
	 * converts an {@link OsmFileFormat} to a {@link String}
	 *
	 * @param impl the {@link OsmFileFormat} to convert
	 * @return the converted {@link OsmFileFormat} as {@link String}
	 *
	 * @since 0.2.0
	 */
	public static String printEnumToString(OsmFileFormat impl) {
		return impl.toString();
	}
}
