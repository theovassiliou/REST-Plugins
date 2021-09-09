package de.vassiliougioles.ttcn.ttwb.port;

import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriMessage;

import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

/**
 * @author Theofanis Vassiliou-Gioles
 *
 */
public class GETMessage extends RESTMessage {

	public GETMessage(TriMessage triMessage, TriAddress triAddress, Value portConfiguration) throws Exception {
		super(triMessage, triAddress, portConfiguration);
		
		request = newRequest(endpoint);
		defaultRequest(request);
	}


	public String sendMessage() throws Exception {
		start();
		Response response;
		StringBuilder encResponse = new StringBuilder();
		try {
			response = request.send();
			RESTJSONCodec.encodeResponseMessage(response, encResponse);
			stop();
		} catch (ExecutionException hrex) {
			if (hrex.getCause() instanceof HttpResponseException) {
				response = ((HttpResponseException) hrex.getCause()).getResponse(); // hrex.getResponse();
			} else {
				response = null;
			}
		}
		destroy();
		return encResponse.toString();

	}

}
