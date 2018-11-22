package de.vassiliougioles.ttcn.ttwb.codec;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpParser;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.FloatValue;
import org.etsi.ttcn.tci.IntegerValue;
import org.etsi.ttcn.tci.RecordOfValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.testingtech.ttcn.tci.codec.base.AbstractBaseCodec;

import de.vassiliougioles.ttcn.ttwb.port.TTCNRESTMapping;

public class RESTJSONCodec extends AbstractBaseCodec implements TTCNRESTMapping, TciCDProvided {

	static private JSONParser parser = new JSONParser();

	@Override
	public synchronized Value decode(TriMessage rcvdMessage, Type decodingHypothesis) {
		ResponseMessage rm = new ResponseMessage();
		HttpParser hParser = new HttpParser(rm);
		ByteBuffer bb = ByteBuffer.wrap(rcvdMessage.getEncodedMessage());

		while (!hParser.isComplete()) {
			hParser.parseNext(bb);
		}

		HttpFields headers = rm.getHeaderFields();

		if (decodingHypothesis.getTypeEncoding().equals(_GET_RESPONSE_ENCODING_NAME_)
				|| decodingHypothesis.getTypeEncoding().equals(_POST_RESPONSE_ENCODING_NAME_)) {
			try {
				RecordValue value = (RecordValue) decodingHypothesis.newInstance();
				String[] responseFieldsNames = value.getFieldNames();

				for (int i = 0; i < responseFieldsNames.length; i++) {
					Value aField = value.getField(responseFieldsNames[i]);
					if (aField.getValueEncoding().equals(_BODY_FIELD_ENCODING_NAME_)) {
						Object obj;
						if (rm.getContent() != null) {
							obj = parser.parse(rm.getContent());
							if (obj == null)
								continue; // Can't create a JSON object
							Value rov = (Value) value.getField(responseFieldsNames[i]);
							Value mappedObject = mapJSON(rov, obj);
							if (mappedObject == null)
								continue;
							value.setField(responseFieldsNames[i], mappedObject);
						}
					} else if (aField.getValueEncoding().startsWith(_HEADER_FIELD_ENCODING_PREFIX_)) {
						String headerSpec = aField.getValueEncoding();
						headerSpec = headerSpec.split(":")[1].trim();
						HttpField hField = headers.getField(headerSpec);
						if (hField == null) {
							value.setFieldOmitted(responseFieldsNames[i]);
							continue; // Header not included
						}
						String headerValue = hField.getValue();
						if (headerValue != null) { 
							// TODO: Check that the variant indicates string encoding
							((UniversalCharstringValue) aField).setString(headerValue);
							value.setField(responseFieldsNames[i], aField);
						}
					} else {
						return value;
					}
				}
				return value;
			} catch (ParseException e) {
				// Well, if we can't parse it, we can't parse it.
				return null;
			}
		} else if (decodingHypothesis.getTypeEncoding().equals(_HTTP_RESPONSE_ENCODING_NAME_)) {
			RecordValue httpResponseValue = (RecordValue) decodingHypothesis.newInstance();
			httpResponseValue.setField("statusLine", decodeStatusLine(httpResponseValue.getField("statusLine"), rm));
			httpResponseValue.setField("headers", decodeHeader(httpResponseValue.getField("headers"), rm));
			httpResponseValue.setField("body", decodeBody(httpResponseValue.getField("body"), rm));

			return httpResponseValue;
		}

		return super.decode(rcvdMessage, decodingHypothesis);
	}

	private Value decodeHeader(Value _headers, ResponseMessage rm) {
		RecordOfValue headers = (RecordOfValue) _headers;

		HttpFields httpFields = rm.getHeaderFields();
		int i = 0;
		for (HttpField httpField : httpFields) {
			RecordValue header = (RecordValue) headers.getElementType().newInstance();
			CharstringValue name = (CharstringValue) header.getField("name");
			CharstringValue val = (CharstringValue) header.getField("val");
			name.setString(httpField.getName());
			val.setString(httpField.getValue());

			header.setField("name", name);
			header.setField("val", val);

			headers.setField(i++, header);
		}

		return _headers;
	}

	private Value decodeBody(Value _body, ResponseMessage rm) {
		RecordValue body = (RecordValue) _body;
		CharstringValue messageBodyTxt = (CharstringValue) body.getField("messageBodyTxt");
		if (rm.getContent() != null) {
			messageBodyTxt.setString(rm.getContent());
			body.setField("messageBodyTxt", messageBodyTxt);
			body.setField("messageBodyRaw", newOctetstringValue(rm.getContent().getBytes(StandardCharsets.UTF_8)));
		}
		return _body;
	}

	private Value decodeStatusLine(Value _statusLine, ResponseMessage rm) {
		RecordValue statusLine = (RecordValue) _statusLine;
		IntegerValue statusCode = (IntegerValue) statusLine.getField("statusCode");
		CharstringValue reasonPhrase = (CharstringValue) statusLine.getField("reasonPhrase");

		statusCode.setInt(rm.getStatusCode());
		statusLine.setField("statusCode", statusCode);

		// REASON Phrase is optional
		if (rm.getReasonPhrase() != null) {
			reasonPhrase.setString(rm.getReasonPhrase());
			statusLine.setField("reasonPhrase", reasonPhrase);
		}
		return _statusLine;
	}

	private String[] getMappedFieldNames(RecordValue rv) {

		String[] result = (String[]) Array.newInstance(String.class, rv.getFieldNames().length);

		String[] allFields = rv.getFieldNames();
		for (int i = 0; i < allFields.length; i++) {

			// check whether we have a name mapping via encode(fieldName) attribute
			String mappedFieldName = rv.getField(allFields[i]).getValueEncodingVariant() == null ? allFields[i]
					: rv.getField(allFields[i]).getValueEncodingVariant();

			Array.set(result, i, mappedFieldName);
		}

		return result;

	}

	private Value mapJSON(Value field, Object obj) {
		JSONObject jo;
		JSONArray ja;

		switch (field.getType().getTypeClass()) {
		case TciTypeClass.RECORD_OF:
		case TciTypeClass.SET_OF:
			// map an array
			if (!(obj instanceof JSONArray))
				return null;
			ja = (JSONArray) obj;
			RecordOfValue rov = ((RecordOfValue) field);
			for (int i = 0; i < ja.size(); i++) {
				Value val = rov.getElementType().newInstance();
				// FIXME: Add some error handling
				mapJSON(val, ((JSONArray) obj).get(i));
				rov.appendField(val);
			}
			break;

		case TciTypeClass.RECORD:
			jo = (JSONObject) obj;
			RecordValue rv = (RecordValue) field;
			String[] allFieldsMapped = getMappedFieldNames(rv);
			String[] allFields = rv.getFieldNames();
			for (int i = 0; i < allFields.length; i++) {
				if (jo.get(allFieldsMapped[i]) != null) {
					rv.setField(allFields[i],
							mapJSON(rv.getField(allFields[i]).getType().newInstance(), jo.get(allFieldsMapped[i])));
				} else {
					// We found a mapping mismatch. No JSON object for this field allFields[i]
					continue;
				}

			}
			break;
		case TciTypeClass.CHARSTRING:
			// map a string
			// map an Object
			((CharstringValue) field).setString(obj.toString());
			break;
		case TciTypeClass.UNIVERSAL_CHARSTRING:
			// map a string
			// map an Object
			((UniversalCharstringValue) field).setString(obj.toString());
			break;
		case TciTypeClass.FLOAT:
			((FloatValue) field).setFloat(Float.parseFloat(obj.toString()));
			break;
		case TciTypeClass.INTEGER:
			((IntegerValue) field).setInt(Integer.parseInt(obj.toString()));
			break;

		default:
			logError("No support of type " + field.getType().getName() + " in mapJSON(). Fix me!");
			break;

		}

		return field;

	}

	@Override
	public synchronized TriMessage encode(Value template) {
		// Let's try to iterate over a given record
		return super.encode(template);
	}

	public String createJSONBody(RecordValue restMessage) {
		Value fieldToEncode = null;
		String[] fieldNames = restMessage.getFieldNames();
		for (int i = 0; i < fieldNames.length; i++) {
			String string = fieldNames[i];
			Value field = restMessage.getField(string);
			if (field.getValueEncoding().equals(_BODY_FIELD_ENCODING_NAME_)) {
				fieldToEncode = field;
			}
		}

		String theJSON = null;
		if (fieldToEncode != null) {
			theJSON = TTCN2JSONencode(fieldToEncode);
		}

		return theJSON;
	}

	public void encodeResponseMessage(ContentResponse response, StringBuilder builder) {
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
				if (i < fieldNames.length - 1) {
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
		default:
			logError("No support of type " + fieldToEncode.getType().getName() + " in TTCN2JSONEncode(). Fix!");
		}

		return builder.toString();
	}

	private String String2JSON(String string) {
		return new String("\"" + string + "\"");
	}

	public Request createHeaderFields(Request request, RecordValue restMessage, StringBuilder dumpMessage) {
		String[] allFields = restMessage.getFieldNames();
		for (int i = 0; i < allFields.length; i++) {
			String aFieldName = allFields[i];
			Value rField = restMessage.getField(aFieldName);
			if (rField.getValueEncoding().startsWith(_HEADER_FIELD_ENCODING_PREFIX_) && !(rField.notPresent())) {

				String headerSpec = null;
				if (rField.getType().getTypeClass() == TciTypeClass.UNIVERSAL_CHARSTRING) {
					String headerValue = ((UniversalCharstringValue) rField).getString();
					headerSpec = getHeaderName(rField, aFieldName);
					request.header(headerSpec, headerValue);
					dumpMessage.append(headerSpec + ": " + headerValue + "\n");
				}
			}
		}
		return request;
	}


	public Request createRequest(HttpClient client, String method, String authorization, String path,
			StringBuilder dumpMessage) throws Exception {
		client.start();
		Request request = null;
		switch (method) {
		case "GET":
			request = client.newRequest(path);
			break;
		case "POST":
			request = client.POST(path);
			break;
		default:
			logError("Unsupported method. Using GET instead");
			method = "GET";
			request = client.newRequest(path);
		}

		request = request.header("Accept", "application/json");

		if (!authorization.equals(_DEFAULT_AUTHORIZATION_)) {
			request = request.header("Authorization", authorization);
		}
		request = request.agent(_USER_AGENT_NAME_);

		dumpMessage.append(request.getVersion() + " " + request.getMethod() + " " + request.getURI() + " " + "\n");

		HttpFields fields = request.getHeaders();

		boolean hasTransferEncoding = false;
		for (HttpField httpField : fields) {
			hasTransferEncoding = false;
			if (!httpField.getName().equals("Transfer-Encoding")) {
				dumpMessage.append(httpField.getName()).append(": ");
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
					dumpMessage.append(values[i]);
					if (i < values.length - 1) {
						dumpMessage.append(", ");
					}
				}
			}
			if (!hasTransferEncoding) {
				dumpMessage.append("\n");
			} 
		}
		return request;
	}

	/**
	 * Encodes path elements of a REST message
	 * 
	 * @param restMessage
	 *            the REST message
	 * @param baseUrl
	 *            a non-null default baseURL to be used
	 * @return the path to be used for the REST message, or null if some error
	 *         occurred
	 */
	public String encodePath(RecordValue restMessage, String baseURL) {

		// path has form: path: /location/{locationName} or
		// /arrivalBoard/{id}?date={date}
		// with
		// path: stating this is a path
		// "/location/" stating the path
		// {locationName} that the value of field "locationName" should be taken
		String path = restMessage.getType().getTypeEncodingVariant();
		if (!path.startsWith(_REQUEST_PATH_VARIANT_PREFIX_)) {
			logError("We only support path encoding variants for REST messages");
			return null;
		}

		// so we only have paths so far
		path = path.split(":")[1].trim();

		String instantiatedPath = replacePathParams(restMessage, path);
		if (instantiatedPath == null)
			return baseURL;

		// a second alternative to pass parameters is to pass them as fields with encode
		// variants
		// first we have to check, whether there are already parameters encoded
		// Check for '?'
		boolean hasParams = instantiatedPath.contains("?");

		// iterate over all fields and check whether encode starts with "param"
		String[] allFields = restMessage.getFieldNames();
		StringBuilder paramListBuild = new StringBuilder();
		boolean creatingParamList = !hasParams;

		for (String fieldName : allFields) {
			Value field = restMessage.getField(fieldName);
			creatingParamList = !hasParams ;
			if (!field.notPresent() && field.getValueEncoding().startsWith(_PATH_PARAM_FIELD_ENCODING_PREFIX_)) {
				String paramName = getParamName(field, fieldName);
				if (creatingParamList) {
					paramListBuild.append("?");
					hasParams = true;
				} else {
					paramListBuild.append("&");
					hasParams = true;
				}

				paramListBuild.append(paramName);
				String uriEncodedFieldValue = null;
				switch (field.getType().getTypeClass()) {
				case TciTypeClass.UNIVERSAL_CHARSTRING:
					uriEncodedFieldValue = ((UniversalCharstringValue) field).getString().replace("+", "%2");
					break;
				case TciTypeClass.INTEGER:
					uriEncodedFieldValue = Integer.toString(((IntegerValue) field).getInt());
					break;
				case TciTypeClass.CHARSTRING:
					uriEncodedFieldValue = ((CharstringValue) field).getString().replace("+", "%2");
					break;
				default:
					logError("Unsupported field type " + field.getType().getName());
					continue;
				}
				paramListBuild.append("=").append(uriEncodedFieldValue);
			}
		}
		return baseURL + instantiatedPath + paramListBuild.toString();

	}

	private String getParamName(Value val, String fieldName) {
		String paramName = val.getValueEncoding().split(":")[1].trim();
		if(paramName.equals(".")) { return fieldName; }
		else { return paramName; }
	}

	private String getHeaderName(Value val, String fieldName) {
		String paramName = val.getValueEncoding().split(":")[1].trim();
		if(paramName.equals(".")) { return fieldName; }
		else { return paramName; }
	}

	
	private String replacePathParams(RecordValue restMessage, String path) {
		String[] pathParams = StringUtils.substringsBetween(path, "{", "}");
		String instantiatedPath = new String(path.toString());
		if (pathParams != null) {
			for (String param : pathParams) {
				try {
					assert ((StringUtils.countMatches(restMessage.getField(param).toString(), "\"")
							% 2) == 0) : "Uneven occurence of \". FIX handling.";
					if (!((restMessage.getField(param).getType().getTypeClass() == TciTypeClass.CHARSTRING)
							|| restMessage.getField(param).getType()
									.getTypeClass() == TciTypeClass.UNIVERSAL_CHARSTRING)) {
						logError("Only supporting Universal Charstring or Charstring Fields so far.");
						return null;
					}

					String uriEncodedFieldValue = ((UniversalCharstringValue) restMessage.getField(param)).getString()
							.replace("+", "%2");
					instantiatedPath = StringUtils.replace(instantiatedPath, "{" + param + "}",
							URLEncoder.encode(uriEncodedFieldValue, "UTF-8").replace("+", "%20"));
				} catch (UnsupportedEncodingException e) {
					logError("Unsupported Encoding execption", e);
					return null;
				}
			}
		}
		return instantiatedPath;
	}

}
