package de.vassiliougioles.ttcn.ttwb.codec;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.http.HttpParser.ResponseHandler;
import org.eclipse.jetty.http.HttpVersion;
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

class ResponseMessage implements ResponseHandler {
	private HttpFields headerFields = null;
	private String content_ = null;
	private HttpVersion httpVersion = null;
	private int statusCode = 0;
	private String reasonPhrase = null;

	public ResponseMessage() {
	}

	@Override
	public void badMessage(int arg0, String arg1) {
		// TODO Auto-generated method stub
		System.out.println("Are we in bad message?");
	}

	@Override
	public boolean content(ByteBuffer arg0) {
		content_ = null;
		if (arg0.hasArray()) {

			// content_ = StandardCharsets.UTF_8.decode(arg0).toString();

			content_ = new String(arg0.array(), arg0.arrayOffset() + arg0.position(), arg0.remaining());

		} else {
			// content_ = StandardCharsets.UTF_8.decode(arg0).toString();

			final byte[] b = new byte[arg0.remaining()];
			arg0.duplicate().get(b);
			content_ = new String(b);

		}
		return true;
	}

	@Override
	public boolean contentComplete() {
		if (content_ != null)
			return true;
		return true;
	}

	@Override
	public void earlyEOF() {
		// TODO: No clue what to do
		System.out.println("are we in earlyEOF?");
	}

	@Override
	public int getHeaderCacheSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean headerComplete() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean messageComplete() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void parsedHeader(HttpField arg0) {
		// TODO Auto-generated method stub
		if (headerFields == null) {
			headerFields = new HttpFields();
		}
		headerFields.add(arg0);
	}

	@Override
	public boolean startResponse(HttpVersion arg0, int arg1, String arg2) {
		httpVersion = arg0;
		setStatusCode(arg1);
		setReasonPhrase(arg2);
		return true;
	}

	public HttpFields getHeaderFields() {
		return headerFields;
	}

	public String getContent() {
		return content_;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

}

public class RESTJSONCodec extends AbstractBaseCodec implements TciCDProvided {

	static private JSONParser parser = new JSONParser();

	@Override
	public synchronized Value decode(TriMessage rcvdMessage, Type decodingHypothesis) {
		ResponseMessage rm = new ResponseMessage();
		HttpParser hParser = new HttpParser(rm);
		ByteBuffer bb = ByteBuffer.wrap(rcvdMessage.getEncodedMessage());
		while (!hParser.isComplete()) {
			hParser.parseNext(bb);
		}

		if (decodingHypothesis.getTypeEncoding().equals("REST/getResponse")
				|| decodingHypothesis.getTypeEncoding().equals("REST/postResponse")) {
			try {
				RecordValue value = (RecordValue) decodingHypothesis.newInstance();
				Object obj;
				if (rm.getContent() != null) {
					obj = parser.parse(rm.getContent());
					if (obj == null)
						return null; // Can't create a JSON object
					Value rov = (Value) value.getField(value.getFieldNames()[0]);
					Value mappedObject = mapJSON(rov, obj);
					if (mappedObject == null)
						return null;
					value.setField(value.getFieldNames()[0], mappedObject);
					return value;
				} else {
					return value;
				}
			} catch (ParseException e) {
				// Well, if we can't parse it, we can't parse it.
				return null;
			}
		} else if (decodingHypothesis.getTypeEncoding().equals("HTTP/response")) {
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
					return null;
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
			Double jd = (Double) obj;
			((FloatValue) field).setFloat(Float.parseFloat(jd.toString()));
			break;
		default:
			break;

		}

		return field;

	}

	private void assertion(String template, String value) {
		if (!template.equals(value)) {
			// logError("AssertionError: " + value + " expected to be " + template);
			System.out.println("AssertionError: " + value + " expected to be " + template);
			tciErrorReq("AssertionError: " + value + " expected to be " + template);
		}
	}

	@Override
	public synchronized TriMessage encode(Value template) {
		// Let's try to iterate over a given record
		switch (template.getType().getTypeClass()) {
		case TciTypeClass.RECORD:
			break;
		}
		return super.encode(template);
	}

}
