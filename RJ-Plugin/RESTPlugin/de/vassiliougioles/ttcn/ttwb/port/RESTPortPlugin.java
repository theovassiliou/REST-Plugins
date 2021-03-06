package de.vassiliougioles.ttcn.ttwb.port;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriParameter;
import org.etsi.ttcn.tri.TriParameterList;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriStatus;

import com.testingtech.ttcn.tri.ISAPlugin;
import com.testingtech.ttcn.tri.TciValueContainer;
import com.testingtech.ttcn.tri.TriAddressImpl;
import com.testingtech.ttcn.tri.TriMessageImpl;
import com.testingtech.ttcn.tri.TriStatusImpl;
import com.testingtech.ttcn.tri.extension.PortPluginProvider;

import de.vassiliougioles.ttcn.ttwb.codec.HeaderField;
import de.vassiliougioles.ttcn.ttwb.codec.ParamField;
import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

public class RESTPortPlugin extends AbstractRESTPortPlugin implements TTCNRESTMapping, PortPluginProvider {

	private static final long serialVersionUID = -6523964658234648218L;
	private String baseURL = _DEFAULT_BASE_URL_;

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
		restCodec.setBaseUrl(baseURL);
	}

	public String getAuthorization() {
		return authorization;
	}

	private String authorization = _DEFAULT_AUTHORIZATION_;
	private RESTJSONCodec restCodec = null;
	private List<HeaderField> defaultHeaders = null;

	public ISAPlugin getPortPlugin() {
		return new RESTPortPlugin();
	}

	private void setAuthorization(String authorization) {
		this.authorization = authorization;
		restCodec.setAuthorization(authorization);
	}

//	private void setDefaultHeadersFromValues(Value dh) {
//		if (dh.getType().getTypeClass() != TciTypeClass.RECORD_OF) {
//			return;
//		}
//
//		if (this.defaultHeaders == null) {
//			this.defaultHeaders = new ArrayList<HeaderField>();
//		}
//
//		this.defaultHeaders.addAll(HeaderField.collectHeaders(dh, null));
//	}

	@Override
	public TriStatus triMapParam(TriPortId compPortId, TriPortId tsiPortId, TriParameterList paramList) {
		restCodec = (RESTJSONCodec) getRB().codecServer.getCodec(_ENCODING_NAME_);

		setBaseURL(_DEFAULT_BASE_URL_);
		setAuthorization(_DEFAULT_AUTHORIZATION_);

		this.defaultHeaders = null;

		@SuppressWarnings("unchecked")
		Enumeration<TriParameter> parameterList = paramList.getParameters();
		while (parameterList.hasMoreElements()) {
			TriParameter triParam = (TriParameter) parameterList.nextElement();
			if (triParam instanceof TciValueContainer) {
				Value param = (Value) ((TciValueContainer) triParam).getValue();

				if (param.getType().getName().equals(_PORT_CONFIG_TYPE_NAME_)) {
					RecordValue rv = (RecordValue) param;
					extractSendToInformation(rv);
				}

				if (triParam.getParameterName().equals(_CONFIG_BASEURL_FIELD_NAME_)) {
					setBaseURL(((CharstringValue) param).getString());
				}
				if (triParam.getParameterName().equals(_CONFIG_AUTH_FIELD_NAME_)) {
					setAuthorization(((CharstringValue) param).getString());
				}
//				if (triParam.getParameterName().equals(_DEFAULT_HEADERS_FIELD_NAME_)) {
//					setDefaultHeadersFromValues(param);
//				}

			}
		}
		return TriStatusImpl.OK;

	}

	public TriStatus triUnmapParam(TriPortId compPortId, TriPortId tsiPortId, TriParameterList paramList) {
		setBaseURL(_DEFAULT_BASE_URL_);
		setAuthorization(_DEFAULT_AUTHORIZATION_);
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId, TriAddress triAddress,
			TriMessage triSendMessage) {
		Value sutAddress = null;
		Value sendMessage = null;
		List<HeaderField> theMessageHeaders = new ArrayList<HeaderField>();

		if (triAddress != null && !(triAddress instanceof TciValueContainer)) {
			return new TriStatusImpl("Can't send message as we cannot retrieve information from sendMessage");
		}
		if (!(triSendMessage instanceof TciValueContainer)) {
			return new TriStatusImpl("Can't send message as we cannot retrieve information from sendMessage");
		}
		if (triAddress != null) {
			sutAddress = ((TciValueContainer) triAddress).getValue();
		}
		if (triSendMessage != null) {
			sendMessage = ((TciValueContainer) triSendMessage).getValue();
		}

		assert tsiPortId.getPortTypeName().split("\\.")[1].equals(_PORT_TYPE_NAME_) : "We only handle operations on "
				+ _PORT_TYPE_NAME_ + " ports";

		StringBuilder dumpMessage = new StringBuilder();
		extractSendToInformation(sutAddress);

		RecordValue restMsg = (RecordValue) sendMessage;

		// Pass 1: Build Endpoint by replacing {} with field-values
		String endPoint = restCodec.constructEndpoint(restMsg);
		// Pass 2: Collect all param-encoded field, deep
		List<ParamField> params = ParamField.collectParams(restMsg, null);
		// Pass 3: Build (additional) query params
		String queryParams = ParamField.buildQueryParams(params);
		// Concat (1) and (3)
		String strEndpoint = restCodec.saveURLConcat(baseURL, endPoint, queryParams);
		// Instantiate HttpClient
		HttpClient httpClient = new HttpClient(new SslContextFactory());
		// Configure HttpClient, for example:
		httpClient.setFollowRedirects(false);

		List<HeaderField> headers = HeaderField.collectHeaders(restMsg, null);

		// check whether encode is REST/get
		if (sendMessage.getType().getTypeEncoding().equals(_GET_ENCODING_NAME_)) {

			// Start HttpClient
			try {
				httpClient.start();
				Request request = restCodec.createRequest(httpClient, "GET", getAuthorization(), strEndpoint,
						dumpMessage, headers);
				dumpMessage.append(HeaderField.fillRequest(request, headers));

				Response response = sendRequest(request);
				if (response != null) {
					StringBuilder builder = new StringBuilder();
					restCodec.encodeResponseMessage(response, builder);
					TriMessage rcvMessage = TriMessageImpl.valueOf(builder.toString().getBytes(StandardCharsets.UTF_8));
					// enqueue the URL as response address to be able to identify it in TTCN-3
					TriAddressImpl rcvSutAddress = new TriAddressImpl(strEndpoint.getBytes());
					triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				new TriStatusImpl(e1.getMessage());
			}
			try {
				httpClient.stop();
			} catch (Exception e) {
				e.printStackTrace();
				new TriStatusImpl(e.getMessage());
			}
			httpClient.destroy();
			return TriStatusImpl.OK;
		} else if (sendMessage.getType().getTypeEncoding().equals(_POST_ENCODING_NAME_)) {

			try {
				httpClient.start();
				Request request = restCodec.createRequest(httpClient, "POST", getAuthorization(), strEndpoint,
						dumpMessage, headers);
				dumpMessage.append(HeaderField.fillRequest(request, headers));
				request.content(new StringContentProvider(restCodec.createBody(restMsg), "UTF-8"),
						_CONTENT_JSON_ENCODING_);
				dumpMessage.append("\n" + restCodec.createBody(restMsg));

				Response response = sendRequest(request);
				if (response != null) {
					StringBuilder builder = new StringBuilder();
					restCodec.encodeResponseMessage(response, builder);
					TriMessage rcvMessage = TriMessageImpl.valueOf(builder.toString().getBytes(StandardCharsets.UTF_8));
					// enqueue the URL as response address to be able to identify it in TTCN-3
					TriAddressImpl rcvSutAddress = new TriAddressImpl(strEndpoint.getBytes());
					triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);
				}

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
		} else if (sendMessage.getType().getTypeEncoding().equals(_DELETE_ENCODING_NAME_)) {

			try {
				httpClient.start();
				Request request = restCodec.createRequest(httpClient, "DELETE", getAuthorization(), strEndpoint,
						dumpMessage, headers);
				if (!headers.isEmpty()) {
					dumpMessage.append(HeaderField.fillRequest(request, headers));
				}
				String body = restCodec.createBody(restMsg);
				if (body != null) {
					request.content(new StringContentProvider(body, "UTF-8"), _CONTENT_JSON_ENCODING_);
					dumpMessage.append("\n" + body);
				} else {
					dumpMessage.append("\n");
				}
				Response response = sendRequest(request);
				if (response != null) {
					StringBuilder builder = new StringBuilder();
					restCodec.encodeResponseMessage(response, builder);
					TriMessage rcvMessage = TriMessageImpl.valueOf(builder.toString().getBytes(StandardCharsets.UTF_8));
					// enqueue the URL as response address to be able to identify it in TTCN-3
					TriAddressImpl rcvSutAddress = new TriAddressImpl(strEndpoint.getBytes());
					triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);
				}

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

	private Response sendRequest(Request request) throws InterruptedException, TimeoutException, ExecutionException {
		Response response;
		try {
			response = request.send();
		} catch (ExecutionException hrex) {
			// e.g. if a
			logWarn(hrex.getCause().getMessage() + ". Continuing.");
			if (hrex.getCause() instanceof HttpResponseException) {
				response = ((HttpResponseException) hrex.getCause()).getResponse(); // hrex.getResponse();
			} else {
				response = null;
			}
		}
		return response;
	}

	private void extractSendToInformation(Value sutAddress) {
		if ((!(sutAddress == null)) && sutAddress.getType().getName().equals(_PORT_CONFIG_TYPE_NAME_)) {
			RecordValue rv = (RecordValue) sutAddress;
			String[] fieldNames = rv.getFieldNames();
			for (int i = 0; i < fieldNames.length; i++) {
				String string = fieldNames[i];
				if (string.equals(_CONFIG_BASEURL_FIELD_NAME_)) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					setBaseURL(cv.getString());
				} else if (string.equals(_CONFIG_AUTH_FIELD_NAME_) && !rv.getField(string).notPresent()) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					setAuthorization(cv.getString());
				}
			}
		}
	}

}
