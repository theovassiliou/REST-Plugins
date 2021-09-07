package de.vassiliougioles.ttcn.ttwb.port;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriMessage;

import com.testingtech.ttcn.tri.TriAddressImpl;
import com.testingtech.ttcn.tri.TriMessageImpl;
import com.testingtech.ttcn.tri.TriStatusImpl;

import de.vassiliougioles.ttcn.ttwb.codec.HeaderField;
import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

public class GETMessage extends RESTMessage {

	public GETMessage(TriMessage triMessage, TriAddress triAddress) throws Exception {
		super(triMessage, triAddress);
	}

	@Override
	public String sendMessage() throws Exception {
		StringBuilder dumpMessage = new StringBuilder();
		start();
		Request request = null;
		request = newRequest(strEndpoint);

		HeaderField.fillRequest(request, headers);
		dumpMessage.append(HeaderField.getHeaderBuilder(headers));

		Response response;
		try {
			response = request.send();
			StringBuilder builder = new StringBuilder();
			RESTJSONCodec.encodeResponseMessage(response, builder);
			stop();
		} catch (ExecutionException hrex) {
			if (hrex.getCause() instanceof HttpResponseException) {
				response = ((HttpResponseException) hrex.getCause()).getResponse(); // hrex.getResponse();
			} else {
				response = null;
			}
		}
		destroy();
		return dumpMessage.toString();
	}

}
