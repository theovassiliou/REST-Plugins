package de.vassiliougioles.ttcn.ttwb.port;

import org.eclipse.jetty.client.util.StringContentProvider;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriMessage;

import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

/**
 * @author Theofanis Vassiliou-Gioles
 *
 */
public class HEADMessage extends RESTMessage {

	public HEADMessage(Value sendMessage, TriAddress triAddress, Value portConfiguration) throws Exception {
		super(sendMessage, triAddress, portConfiguration);
		request = newRequest(endpoint);
		request.method("HEAD");
		defaultRequest(request);
		
		// It could be discussed whether HEAD can has a body. But in case it has one,
		// appended.
		// See relevant section in RFC 7231
		// (https://datatracker.ietf.org/doc/html/rfc7231#section-4)
		String body = RESTJSONCodec.createBody(restMsg);
		if (body != null) {
			request.content(new StringContentProvider(body, "UTF-8"), _CONTENT_JSON_ENCODING_);
		}
	}
}