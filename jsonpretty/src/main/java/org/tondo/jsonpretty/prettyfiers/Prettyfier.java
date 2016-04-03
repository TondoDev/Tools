package org.tondo.jsonpretty.prettyfiers;

import java.io.InputStream;
import java.io.OutputStream;

public interface Prettyfier {

	public void prettyfy(InputStream is, OutputStream os, FormattingOptions options);
}
