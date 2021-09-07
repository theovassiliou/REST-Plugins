package de.vassiliougioles.ttcn.ttwb.port;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
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

public abstract class RESTMessage extends HttpClient  implements TTCNRESTMapping {

	Value sutAddress = null;
	protected String baseURL = _DEFAULT_BASE_URL_;
	protected String authorization = _DEFAULT_AUTHORIZATION_;
	protected List<HeaderField> headers;
	protected RecordValue restMsg = null;
	protected String strEndpoint ;
	
	protected RESTMessage(TriMessage triMessage, TriAddress triAddress) throws Exception {
		super(new SslContextFactory());
		setFollowRedirects(false);
		
		// Determine where to send
		processSendTo(triAddress);
		
		// Pass 1: Build Endpoint by replacing {} with field-values
		String endPoint = RESTJSONCodec.constructEndpoint(restMsg);
		
		// Pass 2: Collect all query-encoded fields, deep
		List<ParamField> params = ParamField.collectParams(restMsg, null);
		
		// Pass 3: Build (additional) query params
		String queryParams = ParamField.buildQueryParams(params);
		
		// concat (1) and (3)
		strEndpoint = RESTJSONCodec.saveURLConcat(baseURL, endPoint, queryParams);
		
		headers = HeaderField.collectHeaders(restMsg, null);

	}

	/**
	 * Processes the information passed via a send to argument to ttcn-3 send.
	 * 
	 * @param triAddress
	 */
	private void processSendTo(TriAddress triAddress) throws Exception {
		if (triAddress != null && !(triAddress instanceof TciValueContainer)) {
			throw new Exception("Can't send message as we cannot retrieve information from sendMessage");
		}

		if (triAddress != null) {
			sutAddress = ((TciValueContainer) triAddress).getValue();
		}

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

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;

	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public RESTMessage makeMessage(TriMessage triMessage, TriAddress triAddress) throws Exception {

		// Safe unwrap triMessage
		if (!(triMessage instanceof TciValueContainer)) {
			throw new Exception("Can't send message as we cannot retrieve information from sendMessage");
		}
		if (triMessage != null) {
			restMsg = (RecordValue) ((TciValueContainer) triMessage).getValue();
		}
		
		switch (restMsg.getType().getTypeEncoding()) {
		case _GET_ENCODING_NAME_:
			return new GETMessage(triMessage, triAddress);

		default:
			throw new Exception("Unsupported encoding: " + restMsg.getType().getTypeEncoding());
		}
		
	}
	
	public abstract String sendMessage() throws Exception;
	
}
