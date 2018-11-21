package de.vassiliougioles.ttcn.ttwb.port;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciParameter;
import org.etsi.ttcn.tci.TciParameterList;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriStatus;

import com.testingtech.ttcn.tri.IXSAPlugin;
import com.testingtech.ttcn.tri.TriAddressImpl;
import com.testingtech.ttcn.tri.TriMessageImpl;
import com.testingtech.ttcn.tri.TriStatusImpl;
import com.testingtech.ttcn.tri.extension.XPortPluginProvider;

import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

public class RESTPortPlugin extends AbstractRESTPortPlugin implements TTCNRESTMapping, XPortPluginProvider, IXSAPlugin {
	private static final long serialVersionUID = -6523964658234648218L;
	private String baseURL = _DEFAULT_BASE_URL_;
	private String authorization =  _DEFAULT_AUTHORIZATION_;
	private RESTJSONCodec restCodec = null;


	@Override
	public IXSAPlugin getXPortPlugin() {
		return new RESTPortPlugin();
	}

	@Override
	public TriStatus xtriMapParam(TriPortId compPortId, TriPortId tsiPortId, TciParameterList paramList) {

		restCodec = (RESTJSONCodec) getRB().codecServer.getCodec(_ENCODING_NAME_);
		
		@SuppressWarnings("unchecked")
		Enumeration<TciParameter> parameterList = paramList.getParameters();
		while (parameterList.hasMoreElements()) {
			TciParameter param = (TciParameter) parameterList.nextElement();
			if (param.getParameter().getType().getName().equals(_PORT_CONFIG_TYPE_NAME_)) {
				RecordValue rv = (RecordValue) param.getParameter();
				extractSendToInformation(rv);
			}

			if (param.getParameterName().equals(_CONFIG_BASEURL_FIELD_NAME_)) {
				baseURL = ((CharstringValue) param.getParameter()).getString();
			}
			if (param.getParameterName().equals(_CONFIG_AUTH_FIELD_NAME_)) {
				authorization = ((CharstringValue) param.getParameter()).getString();
			}

		}
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus xtriUnmapParam(TriPortId compPortId, TriPortId tsiPortId, TciParameterList paramList) {
		 baseURL = _DEFAULT_BASE_URL_;
		 authorization =  _DEFAULT_AUTHORIZATION_;
		return null;
	}

	@Override
	public TriStatus xtriSend(TriComponentId componentId, TriPortId tsiPortId, Value sutAddress, Value sendMessage) {
		assert tsiPortId.getPortTypeName().split("\\.")[1]
				.equals(_PORT_TYPE_NAME_) : "We only handle operations on " + _PORT_TYPE_NAME_ + " ports";

		StringBuilder dumpMessage = new StringBuilder();
		extractSendToInformation(sutAddress);

		// check whether encode is REST/get
		if (sendMessage.getType().getTypeEncoding().equals(_GET_ENCODING_NAME_)) {
			RecordValue restGET = (RecordValue) sendMessage;

			final String strURL =  restCodec.encodePath(restGET, baseURL);
			// now we can call the url

			// Instantiate HttpClient
			HttpClient httpClient = new HttpClient(new SslContextFactory());

			// Configure HttpClient, for example:
			httpClient.setFollowRedirects(false);

			// Start HttpClient
			try {
				httpClient.start();
				Request request = restCodec.createRequest(httpClient,  "GET", authorization,strURL, dumpMessage);
				System.out.println("---------");
				System.out.println("Message send:");
				System.out.println(dumpMessage);
				System.out.println("---------");
				
				ContentResponse response = request.send();

				StringBuilder builder = new StringBuilder();
				restCodec.encodeResponseMessage(response, builder);
				TriMessage rcvMessage = TriMessageImpl.valueOf(builder.toString().getBytes(StandardCharsets.UTF_8));
				// enqueue the URL as response address to be able to identify it in TTCN-3
				TriAddressImpl rcvSutAddress = new TriAddressImpl(strURL.getBytes());
				triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);

			} catch (Exception e1) {
				e1.printStackTrace();
				new TriStatusImpl(e1.getMessage());
			}
			return TriStatusImpl.OK;
		} else if (sendMessage.getType().getTypeEncoding().equals(_POST_ENCODING_NAME_)) {

			RecordValue restPOST = (RecordValue) sendMessage;

			final String strURL =  restCodec.encodePath(restPOST, baseURL);
			HttpClient httpClient = new HttpClient(new SslContextFactory());
			httpClient.setFollowRedirects(false);
			try {
				httpClient.start();
				Request request = restCodec.createRequest(httpClient,  "POST", authorization,strURL, dumpMessage);

				request.content(new StringContentProvider(restCodec.createJSONBody(restPOST), "UTF-8"), _CONTENT_ENCODING_);
				dumpMessage.append(restCodec.createJSONBody(restPOST));
				System.out.println("---------");
				System.out.println("Message send:");
				System.out.println(dumpMessage);
				System.out.println("---------");
				
				ContentResponse response = request.send();

				StringBuilder builder = new StringBuilder();
				restCodec.encodeResponseMessage(response, builder);
				TriMessage rcvMessage = TriMessageImpl.valueOf(builder.toString().getBytes(StandardCharsets.UTF_8));
				// enqueue the URL as response address to be able to identify it in TTCN-3
				TriAddressImpl rcvSutAddress = new TriAddressImpl(strURL.getBytes());
				triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);

			} catch (Exception e1) {
				logError("Failed to send request or to receive response.", e1);
				new TriStatusImpl("An Error occured.");
			}
			
			try {
				httpClient.stop();
			} catch (Exception e) {
				e.printStackTrace();
				new TriStatusImpl(e.getMessage());
			}
			httpClient.destroy();
			return TriStatusImpl.OK;
		} else {
			return TriStatusImpl.OK;
		}

	}



	private void extractSendToInformation(Value sutAddress) {
		if ((!(sutAddress == null)) && sutAddress.getType().getName().equals(_PORT_CONFIG_TYPE_NAME_)) {
			RecordValue rv = (RecordValue) sutAddress;
			String[] fieldNames = rv.getFieldNames();
			for (int i = 0; i < fieldNames.length; i++) {
				String string = fieldNames[i];
				if (string.equals(_CONFIG_BASEURL_FIELD_NAME_)) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					baseURL = cv.getString();
				} else if (string.equals(_CONFIG_AUTH_FIELD_NAME_)) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					authorization = cv.getString();
				}
			}
		}
	}

}
