package de.conterra.babelfish.overpass.config;

import org.openstreetmap.osmosis.xml.common.CompressionMethod;

/**
 * defines an JAXB adapter of {@link CompressionMethod}s
 *
 * @author ChrissW-R1
 * @version 0.2.0
 * @since 0.2.0
 */
public class CompressionMethodAdapter {
	/**
	 * converts a {@link CompressionMethod} to a {@link String}
	 *
	 * @param impl the {@link CompressionMethod} to convert
	 * @return the converted {@link CompressionMethod} as {@link String}
	 *
	 * @since 0.2.0
	 */
	public static String printEnumToString(CompressionMethod impl) {
		return impl.toString();
	}
}
