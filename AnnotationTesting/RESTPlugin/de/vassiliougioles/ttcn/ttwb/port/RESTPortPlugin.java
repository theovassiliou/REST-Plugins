package de.vassiliougioles.ttcn.ttwb.port;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import org.apache.commons.lang3.StringUtils;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciParameter;
import org.etsi.ttcn.tci.TciParameterList;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.TciValueList;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriAddressList;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriParameterList;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriPortIdList;
import org.etsi.ttcn.tri.TriSignatureId;
import org.etsi.ttcn.tri.TriStatus;
import org.etsi.ttcn.tri.TriTestCaseId;

import com.testingtech.ttcn.tri.AbstractMsgBasedSA;
import com.testingtech.ttcn.tri.IXSAPlugin;
import com.testingtech.ttcn.tri.TriAddressImpl;
import com.testingtech.ttcn.tri.TriMessageImpl;
import com.testingtech.ttcn.tri.TriStatusImpl;
import com.testingtech.ttcn.tri.extension.XPortPluginProvider;

public class RESTPortPlugin extends AbstractMsgBasedSA implements XPortPluginProvider, IXSAPlugin {
	private static final long serialVersionUID = -6523964658234648218L;
	private String baseURL = "NO PROTOCOL SET:";
	private String authorization = "NO AUTHORIZATION SET";

	@Override
	public TriStatus triSendMC(TriComponentId componentId, TriPortId tsiPortId, TriAddressList addresses,
			TriMessage sendMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus triSAReset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus triExecuteTestcase(TriTestCaseId testCaseId, TriPortIdList tsiPorts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus triMap(TriPortId compPortId, TriPortId tsiPortId) {
		// TODO Auto-generated method stub

		return null;
	}

	public TriStatus triMapParam(TriPortId compPortId, TriPortId tsiPortId, TriParameterList paramList) {
		System.out.println("Are we here 4");
		return null;
	}

	@Override
	public TriStatus triUnmap(TriPortId compPortId, TriPortId tsiPortId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus triEndTestCase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus triSendBC(TriComponentId componentId, TriPortId tsiPortId, TriMessage sendMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IXSAPlugin getXPortPlugin() {
		// TODO Auto-generated method stub
		return new RESTPortPlugin();
	}

	@Override
	public TriStatus xtriMapParam(TriPortId compPortId, TriPortId tsiPortId, TciParameterList paramList) {
		// TODO Auto-generated method stub

		Enumeration parameterList = paramList.getParameters();
		while (parameterList.hasMoreElements()) {
			TciParameter param = (TciParameter) parameterList.nextElement();
			if (param.getParameterName().equals("baseUrl")) {
				baseURL = ((CharstringValue) param.getParameter()).getString();
			}
			if (param.getParameterName().equals("authorization")) {
				authorization = ((CharstringValue) param.getParameter()).getString();
			}

		}
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus xtriUnmapParam(TriPortId compPortId, TriPortId tsiPortId, TciParameterList paramList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriSend(TriComponentId componentId, TriPortId tsiPortId, Value sutAddress, Value sendMessage) {
		assert tsiPortId.getPortTypeName().split("\\.")[1]
				.equals("RESTfull") : "We only handle operations on RESTfull ports";
		// TODO: handle sutAddress to be able to redirect to another service
		RecordValue restGET = (RecordValue) sendMessage;

		// check whether encode is REST/get
		if (!restGET.getType().getTypeEncoding().equals("REST/get")) {
			return new TriStatusImpl();
		}

		// path has form: path: /location/{locationName} or /arrivalBoard/{id}?date={date}
		// with
		// path: stating this is a path
		// "/location/" stating the path
		// {locationName} that the value of field "locationName" should be taken
		String path = restGET.getType().getTypeEncodingVariant();
		if (!path.startsWith("path:")) {
			return new TriStatusImpl("We only support path variants for REST/get");
		}

		// so we only have paths so far
		System.out.println("encodingVariant: " + path);

		path = path.split(":")[1].trim();
		System.out.println("pathInstruction: " + path);

		String[] pathParams = StringUtils.substringsBetween(path, "{", "}");
		String instantiatedPath = new String(path.toString());
		for (String param : pathParams) {
			try {
				assert ((StringUtils.countMatches(restGET.getField(param).toString(), "\"")
						% 2) == 0) : "Uneven occurence of \". FIX handling.";
				assert ((restGET.getField(param).getType().getTypeClass() == TciTypeClass.CHARSTRING) || restGET
						.getField(param).getType()
						.getTypeClass() == TciTypeClass.UNIVERSAL_CHARSTRING) : "Only supporting Charstring Fields so far.";

				String uriEncodedFieldValue = ((UniversalCharstringValue) restGET.getField(param)).getString()
						.replace("+", "%2");
				instantiatedPath = StringUtils.replace(instantiatedPath, "{" + param + "}",
						URLEncoder.encode(uriEncodedFieldValue, "UTF-8").replace("+", "%20"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				new TriStatusImpl(e.getMessage());
			}
		}

		final String strURL = baseURL + instantiatedPath;
		// now we can call the url
		URL url;
		try {
			url = new URL(strURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			return new TriStatusImpl(e.getMessage());
		}
		HttpURLConnection httpConn;
		try {
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setUseCaches(false);
			// --header "Accept: application/json" --header "Authorization: Bearer
			// b78d0f3bedce61f05f42e5103e8b68b3"
			httpConn.setRequestProperty("Accept", "application/json");
			httpConn.setRequestProperty("Authorization", authorization);

			// create the receiver thread
			Thread receiverThread = new Thread() {

				@Override
				public void run() {
					try {
						if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
							logDebug("triSend: ResponseCode is not HTTP_OK, ResponseMessage is "
									+ httpConn.getResponseMessage());
						} else {
							try (BufferedInputStream is = new BufferedInputStream(httpConn.getInputStream())) {
								ByteArrayOutputStream os = new ByteArrayOutputStream();

								byte[] buf = new byte[512];
								do {
									int read = is.read(buf);
									if (read < 0) {
										break;
									}
									os.write(buf, 0, read);
								} while (true);

								// create message
								TriMessage rcvMessage = TriMessageImpl.valueOf(os.toByteArray());
								// enqueue the URL as response address to be able to identify it in TTCN-3
								TriAddressImpl rcvSutAddress = new TriAddressImpl(strURL.getBytes());
								triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);
							}
						}
					} catch (IOException ex) {
						logDebug("triSend: communication error " + ex.toString());

						// when the send should report an error use statement below
						// throw new TestCaseError("triSend: error " + ex.toString(), ex);
					} finally {
						// close the connection
						httpConn.disconnect();
					}
				}
			};

			receiverThread.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(instantiatedPath);
		return TriStatusImpl.OK;

	}

	@Override
	public TriStatus xtriSendBC(TriComponentId componentId, TriPortId tsiPortId, Value sendMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriSendMC(TriComponentId componentId, TriPortId tsiPortId, TciValueList sutAddresses,
			Value sendMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriCall(TriComponentId componentId, TriPortId tsiPortId, Value sutAddress,
			TriSignatureId signatureId, TciParameterList parameterList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriCallBC(TriComponentId componentId, TriPortId tsiPortId, TriSignatureId signatureId,
			TciParameterList parameterList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriCallMC(TriComponentId componentId, TriPortId tsiPortId, TciValueList sutAddresses,
			TriSignatureId signatureId, TciParameterList parameterList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriReply(TriComponentId componentId, TriPortId tsiPortId, Value sutAddress,
			TriSignatureId signatureId, TciParameterList parameterList, Value returnValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriReplyBC(TriComponentId componentId, TriPortId tsiPortId, TriSignatureId signatureId,
			TciParameterList parameterList, Value returnValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriReplyMC(TriComponentId componentId, TriPortId tsiPortId, TciValueList sutAddresses,
			TriSignatureId signatureId, TciParameterList parameterList, Value returnValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriRaise(TriComponentId componentId, TriPortId tsiPortId, Value sutAddress,
			TriSignatureId signatureId, Value exc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriRaiseBC(TriComponentId componentId, TriPortId tsiPortId, TriSignatureId signatureId,
			Value exc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus xtriRaiseMC(TriComponentId componentId, TriPortId tsiPortId, TciValueList sutAddresses,
			TriSignatureId signatureId, Value exc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value xtriConvert(TriPortId tsiPortId, Object value, Type typeHypothesis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId, TriAddress address,
			TriMessage sendMessage) {
		// TODO Auto-generated method stub
		return null;
	}

}
