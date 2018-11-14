package de.vassiliougioles.ttcn.ttwb.port;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.FloatValue;
import org.etsi.ttcn.tci.IntegerValue;
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

		@SuppressWarnings("unchecked")
		Enumeration<TciParameter> parameterList = paramList.getParameters();
		while (parameterList.hasMoreElements()) {
			TciParameter param = (TciParameter) parameterList.nextElement();
			if (param.getParameterName().equals("config")) {
				RecordValue rv = (RecordValue) param.getParameter();
				String[] fieldNames = rv.getFieldNames();
				for (int i = 0; i < fieldNames.length; i++) {
					String string = fieldNames[i];
					if (string.equals("baseUrl")) {
						UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
						baseURL = cv.getString();
					} else if (string.equals("authorization")) {
						UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
						authorization = cv.getString();
					}

				}
			}

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

		if ((!(sutAddress == null)) && sutAddress.getType().getName().equals("RESTAPIconfig")) {
			RecordValue rv = (RecordValue) sutAddress;
			String[] fieldNames = rv.getFieldNames();
			for (int i = 0; i < fieldNames.length; i++) {
				String string = fieldNames[i];
				if (string.equals("baseUrl")) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					baseURL = cv.getString();
				} else if (string.equals("authorization")) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					authorization = cv.getString();
				}
			}
		}

		// check whether encode is REST/get
		if (sendMessage.getType().getTypeEncoding().equals("REST/get")) {
			RecordValue restGET = (RecordValue) sendMessage;

			// path has form: path: /location/{locationName} or
			// /arrivalBoard/{id}?date={date}
			// with
			// path: stating this is a path
			// "/location/" stating the path
			// {locationName} that the value of field "locationName" should be taken
			String path = restGET.getType().getTypeEncodingVariant();
			if (!path.startsWith("path:")) {
				return new TriStatusImpl("We only support path variants for REST/get");
			}

			// so we only have paths so far
			path = path.split(":")[1].trim();

			String[] pathParams = StringUtils.substringsBetween(path, "{", "}");
			String instantiatedPath = new String(path.toString());
			if (pathParams != null) {
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
			}
			final String strURL = baseURL + instantiatedPath;
			// now we can call the url
			logDebug("GETing from: " + strURL);
			// Instantiate HttpClient
			HttpClient httpClient = new HttpClient(new SslContextFactory());

			// Configure HttpClient, for example:
			httpClient.setFollowRedirects(false);

			// Start HttpClient
			try {
				httpClient.start();
				Request request = httpClient.newRequest(strURL);
				request = request.header("Accept", "application/json");

				if (!authorization.equals("NO AUTHORIZATION SET")) {
					request = request.header("Authorization", authorization);
				} else {
					// FIXME: hanlding issue if no auth-header given but requested. jett.send()
					// throws expection
				}
				request = request.agent("TTworkbench/26 RESTPortPlugin/0.1");
				ContentResponse response = request.send();

				StringBuilder builder = new StringBuilder();
				builder.append(response.getVersion() + " ").append(response.getStatus()).append(" ")
						.append(response.getReason()).append("\n");

				HttpFields fields = response.getHeaders();

				boolean hasTransferEncoding = false;
				for (HttpField httpField : fields) {
					hasTransferEncoding = false;
					if (!httpField.getName().equals("Transfer-Encoding")) {
						builder.append(httpField.getName()).append(": ");
					}

					String[] values = httpField.getValues();
					for (int i = 0; i < values.length; i++) {
						if (httpField.getName().equals("Transfer-Encoding") && values[i].equals("chunked")) {
							hasTransferEncoding = true;
							break;
						} else if (httpField.getName().equals("Transfer-Encoding") && values[i].equals("chunked")) {
							hasTransferEncoding = true;
							logError("Unexpected Transfer-Encoding header. Value: " + values[i]);
						} else {
							builder.append(values[i]);
							if (i < values.length - 1) {
								builder.append(", ");
							}
						}
					}
					if (!hasTransferEncoding) {
						builder.append("\n");
					} else {
						builder.append("Content-Length: ").append(response.getContent().length).append("\n");
					}
				}

				builder.append("\n").append(response.getContentAsString()).append("\n");
				builder.append("\n");
				System.out.println(builder.toString());
				TriMessage rcvMessage = TriMessageImpl.valueOf(builder.toString().getBytes(StandardCharsets.UTF_8));
				// enqueue the URL as response address to be able to identify it in TTCN-3
				TriAddressImpl rcvSutAddress = new TriAddressImpl(strURL.getBytes());
				triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				new TriStatusImpl(e1.getMessage());
			}

			System.out.println(instantiatedPath);
			return TriStatusImpl.OK;
		} else if (sendMessage.getType().getTypeEncoding().equals("REST/post")) {

			RecordValue restPOST = (RecordValue) sendMessage;

			// the sendMessage is POSTed to
			// encodingVariant and all parameters are replaced by field values
			// the content of the POST contains an encoded record field with
			// encode "body/JSON"
			// this field is encoded as JSON and used in the body of the

			// path has form: path: /graphql/ or
			// /graphql/{id}?date={date}
			// with
			// path: stating this is a path
			// "/graphql/" stating the path
			// {id} that the value of field "locationName" should be taken
			String path = restPOST.getType().getTypeEncodingVariant();
			if (!path.startsWith("path:")) {
				return new TriStatusImpl("We only support path variants for REST/post");
			}

			// so we only have paths so far
			path = path.split(":")[1].trim();

			String[] pathParams = StringUtils.substringsBetween(path, "{", "}");
			String instantiatedPath = new String(path.toString());
			if (pathParams != null) {
				for (String param : pathParams) {
					try {
						assert ((StringUtils.countMatches(restPOST.getField(param).toString(), "\"")
								% 2) == 0) : "Uneven occurence of \". FIX handling.";
						assert ((restPOST.getField(param).getType().getTypeClass() == TciTypeClass.CHARSTRING)
								|| restPOST.getField(param).getType()
										.getTypeClass() == TciTypeClass.UNIVERSAL_CHARSTRING) : "Only supporting Charstring Fields so far.";

						String uriEncodedFieldValue = ((UniversalCharstringValue) restPOST.getField(param)).getString()
								.replace("+", "%2");
						instantiatedPath = StringUtils.replace(instantiatedPath, "{" + param + "}",
								URLEncoder.encode(uriEncodedFieldValue, "UTF-8").replace("+", "%20"));

					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						new TriStatusImpl(e.getMessage());
					}
				}
			}
			final String strURL = baseURL + instantiatedPath;
			// now we can call the url
			logDebug("POSTing to: " + strURL);

			// Instantiate HttpClient
			HttpClient httpClient = new HttpClient(new SslContextFactory());

			// Configure HttpClient, for example:
			httpClient.setFollowRedirects(false);

			// Start HttpClient
			try {
				httpClient.start();
				Request request = httpClient.POST(strURL);
				request = request.header("Accept", "application/json");

				request = request.header("Authorization", authorization);
				// request = request.header ("Content-Type", "application/json");
				request = request.agent("TTworkbench/26 RESTPortPlugin/0.1");

				// let's build the message body

				// it is in the field of the record with the encode attribute "body/JSON"
				Value fieldToEncode = null;
				String[] fieldNames = restPOST.getFieldNames();
				for (int i = 0; i < fieldNames.length; i++) {
					String string = fieldNames[i];
					Value field = restPOST.getField(string);
					if (field.getValueEncoding().equals("body/JSON")) {
						fieldToEncode = field;
					}
				}

				String theJSON = null;
				if (fieldToEncode != null) {
					theJSON = TTCN2JSONencode(fieldToEncode);
				}

				request.content(new StringContentProvider(theJSON, "UTF-8"), "application/json");
				logDebug("The Body: " + theJSON);
				ContentResponse response = request.send();

				StringBuilder builder = new StringBuilder();
				builder.append(response.getVersion() + " ").append(response.getStatus()).append(" ")
						.append(response.getReason()).append("\n");

				HttpFields fields = response.getHeaders();

				boolean hasTransferEncoding = false;
				for (HttpField httpField : fields) {
					hasTransferEncoding = false;
					if (!httpField.getName().equals("Transfer-Encoding")) {
						builder.append(httpField.getName()).append(": ");
					}

					String[] values = httpField.getValues();
					for (int i = 0; i < values.length; i++) {
						if (httpField.getName().equals("Transfer-Encoding") && values[i].equals("chunked")) {
							hasTransferEncoding = true;
							break;
						} else if (httpField.getName().equals("Transfer-Encoding") && values[i].equals("chunked")) {
							hasTransferEncoding = true;
							logError("Unexpected Transfer-Encoding header. Value: " + values[i]);
						} else {
							builder.append(values[i]);
							if (i < values.length - 1) {
								builder.append(", ");
							}
						}
					}
					if (!hasTransferEncoding) {
						builder.append("\n");
					} else {
						builder.append("Content-Length: ").append(response.getContent().length).append("\n");
					}
				}

				builder.append("\n").append(response.getContentAsString()).append("\n");
				builder.append("\n");
				System.out.println(builder.toString());
				TriMessage rcvMessage = TriMessageImpl.valueOf(builder.toString().getBytes(StandardCharsets.UTF_8));
				// enqueue the URL as response address to be able to identify it in TTCN-3
				TriAddressImpl rcvSutAddress = new TriAddressImpl(strURL.getBytes());
				triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				new TriStatusImpl(e1.getMessage());
			}

			return TriStatusImpl.OK;
		} else {
			return TriStatusImpl.OK;
		}

	}

	private String TTCN2JSONencode(Value fieldToEncode) {
		StringBuilder builder = new StringBuilder();

		switch (fieldToEncode.getType().getTypeClass()) {
		case TciTypeClass.RECORD:
			builder.append("{ ");
			RecordValue rv = (RecordValue) fieldToEncode;
			String[] fieldNames = rv.getFieldNames();
			for (int i = 0; i < fieldNames.length; i++) {
				builder.append(String2JSON(fieldNames[i])).append(": ");
				builder.append(TTCN2JSONencode(rv.getField(fieldNames[i])));
				if (i < fieldNames.length - 2) {
					builder.append(", ");
				}
			}
			builder.append(" }");
			break;
		case TciTypeClass.CHARSTRING:
			builder.append(String2JSON(((CharstringValue) fieldToEncode).getString()));
			break;
		case TciTypeClass.UNIVERSAL_CHARSTRING:
			builder.append(String2JSON(((UniversalCharstringValue) fieldToEncode).getString()));
			break;
		case TciTypeClass.INTEGER:
			builder.append(((IntegerValue) fieldToEncode).getInt());
			break;
		case TciTypeClass.FLOAT:
			builder.append(((FloatValue) fieldToEncode).getFloat());
			break;
		}

		return builder.toString();
	}

	private String String2JSON(String string) {
		// TODO Auto-generated method stub
		return new String("\"" + string + "\"");
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
