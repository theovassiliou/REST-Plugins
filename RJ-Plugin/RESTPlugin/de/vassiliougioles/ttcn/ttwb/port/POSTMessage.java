package de.vassiliougioles.ttcn.ttwb.port;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriMessage;

import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

/**
 * @author Theofanis Vassiliou-Gioles
 *
 */
public class POSTMessage extends RESTMessage {

	public POSTMessage(TriMessage triMessage, TriAddress triAddress, Value portConfiguration) throws Exception {
		super(triMessage, triAddress, portConfiguration);
		request = POST(endpoint);
		defaultRequest(request);
		request.content(new StringContentProvider(RESTJSONCodec.createBody(restMsg), "UTF-8"), _CONTENT_JSON_ENCODING_);

	}

	public String sendMessage() throws Exception {
		start();

		StringBuilder encResponse = new StringBuilder();
		Response response = sendRequest(request);
		RESTJSONCodec.encodeResponseMessage(response, encResponse);
		stop();
		destroy();
		return encResponse.toString();

	}

}
