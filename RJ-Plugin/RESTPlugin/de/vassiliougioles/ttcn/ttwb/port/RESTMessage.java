package de.vassiliougioles.ttcn.ttwb.port;

import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriMessage;

import com.testingtech.ttcn.tri.TciValueContainer;

import de.vassiliougioles.ttcn.ttwb.codec.HeaderField;
import de.vassiliougioles.ttcn.ttwb.codec.ParamField;
import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

public abstract class RESTMessage extends HttpClient implements TTCNRESTMapping {

	public static boolean isSupportedEncoding(String encoding) {

		switch (encoding) {
		case _GET_ENCODING_NAME_:
		case _POST_ENCODING_NAME_:

		case _DELETE_ENCODING_NAME_:

		case _HEAD_ENCODING_NAME_:

		case _OPTIONS_ENCODING_NAME_:

		case _PUT_ENCODING_NAME_:

		case _PATCH_ENCODING_NAME_:
			return true;
		default:
			return false;
		}
	}

	public static RESTMessage makeMessage(TriMessage triMessage, TriAddress triAddress, Value portConfiguration)
			throws Exception {
		RecordValue restMsg;

		// Safe unwrap triMessage
		if (!(triMessage instanceof TciValueContainer)) {
			throw new Exception("Can't send message as we cannot retrieve information from sendMessage");
		}
		restMsg = (RecordValue) ((TciValueContainer) triMessage).getValue();

		return makeMessage(restMsg, triAddress, portConfiguration);
	}

	public static RESTMessage makeMessage(Value sendMessage, TriAddress triAddress, Value portConfiguration)
			throws Exception {
		RecordValue restMsg = (RecordValue) sendMessage;

		switch (restMsg.getType().getTypeEncoding()) {
		case _GET_ENCODING_NAME_:
			return new GETMessage(sendMessage, triAddress, portConfiguration);
		case _POST_ENCODING_NAME_:
			return new POSTMessage(sendMessage, triAddress, portConfiguration);
		case _DELETE_ENCODING_NAME_:
			return new DELETEMessage(sendMessage, triAddress, portConfiguration);
		case _HEAD_ENCODING_NAME_:
			return new HEADMessage(sendMessage, triAddress, portConfiguration);
		case _OPTIONS_ENCODING_NAME_:
			return new OPTIONSMessage(sendMessage, triAddress, portConfiguration);
		case _PUT_ENCODING_NAME_:
			return new PUTMessage(sendMessage, triAddress, portConfiguration);
		case _PATCH_ENCODING_NAME_:
			return new PATCHMessage(sendMessage, triAddress, portConfiguration);
		default:
			throw new Exception("Unsupported encoding: " + restMsg.getType().getTypeEncoding());
		}

	}

	protected String authorization = _DEFAULT_AUTHORIZATION_;
	protected String baseURL = _DEFAULT_BASE_URL_;
	private StringBuilder encBody = new StringBuilder();
	private StringBuilder encHeader = new StringBuilder();
	private StringBuilder encStatusLine = new StringBuilder();
	protected String endpoint;

	protected List<HeaderField> headers;

	protected Request request;
	protected RecordValue restMsg = null;
	Value sutAddress = null;

	protected RESTMessage(Value sendMessage, TriAddress triAddress, Value portConfiguration) throws Exception {
		super(new SslContextFactory());
		setFollowRedirects(false);

		sutAddress = portConfiguration;

		// Determine where to send
		processSendTo(triAddress);

		restMsg = (RecordValue) sendMessage;

		// Pass 1: Build Endpoint by replacing {} with field-values
		String endPoint = RESTJSONCodec.constructEndpoint(restMsg);

		// Pass 2: Collect all query-encoded fields, deep
		List<ParamField> params = ParamField.collectParams(restMsg, null);

		// Pass 3: Build (additional) query params
		String queryParams = ParamField.buildQueryParams(params);

		// concat (1) and (3)
		if (!baseURL.startsWith(_DEFAULT_BASE_URL_)) {
			endpoint = RESTJSONCodec.saveURLConcat(baseURL, endPoint, queryParams);
		} else {
			endpoint = RESTJSONCodec.saveURLConcat(_MAGIC_BASE_URL_, endPoint, queryParams);
		}
		headers = HeaderField.collectHeaders(restMsg, null);
	}

	public void defaultRequest(Request req) throws Exception {
		setEncStatusLine(req.getMethod() + " " + req.getURI() + " " + req.getVersion() + "\n");
		req.header("Accept", "application/json");

		if (!authorization.equals(_DEFAULT_AUTHORIZATION_)) {
			req.header("Authorization", authorization);
		}
		req.agent(_USER_AGENT_NAME_);

		for (Iterator<HeaderField> iterator = headers.iterator(); iterator.hasNext();) {
			HeaderField headerField = iterator.next();
			if (!headerField.notPresent()) {
				req.header(headerField.getHeaderName(),
						((UniversalCharstringValue) headerField.getValue()).getString());
			}
		}

		// Dumping the header fields
		HttpFields fields = req.getHeaders();

		boolean hasTransferEncoding = false;

		for (HttpField httpField : fields) {
			hasTransferEncoding = false;
			if (!httpField.getName().equals("Transfer-Encoding")) {
				setEncHeader(httpField.getName());
				setEncHeader(": ");
			}

			String[] values = httpField.getValues();
			for (int i = 0; i < values.length; i++) {
				if (httpField.getName().equals("Transfer-Encoding") && values[i].equals("chunked")) {
					hasTransferEncoding = true;
					break;
				} else if (httpField.getName().equals("Transfer-Encoding") && values[i].equals("chunked")) {
					hasTransferEncoding = true;
					throw new Exception("Unexpected Transfer-Encoding header. Value: " + values[i]);
				} else {
					setEncHeader(values[i]);
					if (i < values.length - 1) {
						setEncHeader(", ");
					}
				}
			}
			if (!hasTransferEncoding) {
				setEncHeader("\n");
			}
		}

		HeaderField.fillRequest(req, headers);
		getEncHeader().append(HeaderField.getHeaderBuilder(headers));
	}

	private void extractSendToInformation() {
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

	public StringBuilder getEncBody() {
		return encBody;
	}

	public String getEncBodyString() {
		return encBody.toString();
	}

	public StringBuilder getEncHeader() {
		return encHeader;
	}

	public String getEncHeaderString() {
		return encHeader.toString();
	}

	public String getEncMessageString() {
		StringBuilder theMessage =new StringBuilder();
		
		// In case we do not have a BASE URL we have to strip the MAGIC Sequences
		CharSequence sub = encStatusLine.subSequence(this.request.getMethod().length()+1, this.request.getMethod().length()+1+_MAGIC_BASE_URL_.length());
		if (sub.equals(_MAGIC_BASE_URL_)) {
			encStatusLine.replace(this.request.getMethod().length()+1, this.request.getMethod().length()+1+_MAGIC_BASE_URL_.length(), "");
		}
		
		theMessage.append(encStatusLine);
		theMessage.append(encHeader);
		theMessage.append(encBody);

		return theMessage.toString();
	}

	public StringBuilder getEncStatusLine() {
		return encStatusLine;
	}

	public String getEncStatusLineString() {
		return encStatusLine.toString();
	}

	/**
	 * getEndpoint returns the endpoint (scheme + baseURL + endPoint + queryParams)
	 * 
	 * @return
	 */
	public String getEndpoint() {
		return endpoint;
	}

	public String getMethodCall() {
		return request.getMethod() + " " + getEndpoint();
	}

	/**
	 * Processes the information passed via a send to argument to ttcn-3 send.
	 * 
	 * @param sutAddress
	 */
	public void processSendTo(TriAddress triAddress) throws Exception {
		if (triAddress != null && !(triAddress instanceof TciValueContainer)) {
			throw new Exception("Can't send message as we cannot retrieve information from sendMessage");
		}
		if (triAddress != null) {
			sutAddress = ((TciValueContainer) triAddress).getValue();
		}

		extractSendToInformation();
	}

	/**
	 * sendMessage encodes the message and send's it to the endpoint.
	 * 
	 * @return the string encoded response
	 * @throws Exception in case there are some problems
	 */
	public String sendMessage() throws Exception {
		start();
		StringBuilder encResponse = new StringBuilder();
		Response response = sendRequest(request);
		RESTJSONCodec.encodeResponseMessage(response, encResponse);
		stop();
		destroy();
		return encResponse.toString();
	}

	protected Response sendRequest(Request request) throws InterruptedException, TimeoutException, ExecutionException {
		Response response;
		try {
			response = request.send();
		} catch (ExecutionException hrex) {
			// e.g. if a
			if (hrex.getCause() instanceof HttpResponseException) {
				response = ((HttpResponseException) hrex.getCause()).getResponse(); // hrex.getResponse();
			} else if (hrex.getCause() instanceof ConnectException) {
				throw hrex;
			} else {
				response = null;
			}
		}
		return response;
	};

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;

	}

	public void setEncBody(String encBody) {
		this.encBody.append(encBody);
	}

	public void setEncHeader(String encHeader) {
		this.encHeader.append(encHeader);
	}

	public void setEncStatusLine(String encStatusLine) {
		this.encStatusLine.append(encStatusLine);
	}
}
