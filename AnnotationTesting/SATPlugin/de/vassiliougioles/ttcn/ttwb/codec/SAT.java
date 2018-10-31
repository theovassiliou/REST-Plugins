package de.vassiliougioles.ttcn.ttwb.codec;

import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;

import java.lang.reflect.Array;
import java.util.Arrays;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.FloatValue;
import org.etsi.ttcn.tci.RecordOfValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tri.TriMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.testingtech.ttcn.tci.codec.base.AbstractBaseCodec;

public class SAT extends AbstractBaseCodec implements TciCDProvided {

	static private JSONParser parser = new JSONParser();

	@Override
	public synchronized Value decode(TriMessage rcvdMessage, Type decodingHypothesis) {
		if (decodingHypothesis.getTypeEncoding().equals("REST/getResponse")) {

			RecordValue value = (RecordValue) decodingHypothesis.newInstance();
			Object obj;
			try {
				obj = parser.parse(new String(rcvdMessage.getEncodedMessage()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				return null;
			}

			RecordOfValue rov = (RecordOfValue) value.getField(value.getFieldNames()[0]);

			value.setField(value.getFieldNames()[0], mapJSON(rov, obj));

			return value;
		}

		return super.decode(rcvdMessage, decodingHypothesis);
	}

	private String[] getMappedFieldNames(RecordValue rv) {

		String[] result = (String[]) Array.newInstance(String.class, rv.getFieldNames().length);

		String[] allFields = rv.getFieldNames();
		for (int i = 0; i < allFields.length; i++) {

			// check whether we have a name mapping via encode(fieldName) attribute
			System.out.println(allFields[i] + " valueEncoding:" + rv.getField(allFields[i]).getValueEncoding());
			System.out.println(allFields[i] + " valueVarian:" + rv.getField(allFields[i]).getValueEncodingVariant());
			String mappedFieldName = rv.getField(allFields[i]).getValueEncodingVariant() == null ? allFields[i]
					: rv.getField(allFields[i]).getValueEncodingVariant();

			Array.set(result, i, mappedFieldName);
		}

		return result;

	}

	private Value mapJSON(Value field, Object obj) {
		JSONObject jo;
		JSONArray ja; 
		
		switch(field.getType().getTypeClass()) {
		case TciTypeClass.RECORD_OF: 
			// map an array
			ja = (JSONArray) obj;
			RecordOfValue rov = ((RecordOfValue) field);
			for (int i = 0; i < ja.size(); i++) {
				Value val = rov.getElementType().newInstance();
				// FIXME: Add some error handling
				mapJSON(val,((JSONArray) obj).get(i));
				rov.appendField(val);
			}
			break;
		case TciTypeClass.SET_OF:
			break;
		case TciTypeClass.RECORD:
			jo = (JSONObject) obj;
			RecordValue rv = (RecordValue) field;
			String[] allFieldsMapped = getMappedFieldNames(rv);
			String[] allFields = rv.getFieldNames();
			for (int i = 0; i < allFields.length; i++) {
				if(jo.get(allFieldsMapped[i]) != null) {
				rv.setField(allFields[i],  mapJSON(rv.getField(allFields[i]).getType().newInstance(), jo.get(allFieldsMapped[i])));
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
			System.out.println(template.getType().getName());
			System.out.println(template.toString());
			System.out.println("Type Encoding: " + template.getType().getTypeEncoding());
			System.out.println("Type Variant: " + template.getType().getTypeEncodingVariant());
			System.out.println("Value EncodeAttributes: " + Arrays.toString(template.getEncodeAttributes()));
			System.out.println("Value VariantAttributes: "
					+ Arrays.toString(template.getVariantAttributes(template.getValueEncoding())));

			if (template.getType().getName().equals("RecordOne")) {
				assertion("RecordOne", template.getType().getName());
				System.out.println(
						"locationName:" + ((RecordValue) template).getField("locationName").getValueEncoding());
				System.out.println(
						"locationName:" + ((RecordValue) template).getField("locationName").getValueEncodingVariant());
				System.out.println("path:" + ((RecordValue) template).getField("path").getValueEncoding());
				System.out.println("path:" + ((RecordValue) template).getField("path").getValueEncodingVariant());
			} else if (template.getType().getName().equals("RecordTwo")) {
				assertion("RecordTwo", template.getType().getName());
				System.out.println(
						"locationName:" + ((RecordValue) template).getField("locationName").getValueEncoding());
				System.out.println(
						"locationName:" + ((RecordValue) template).getField("locationName").getValueEncodingVariant());
				System.out.println("path:" + ((RecordValue) template).getField("path").getValueEncoding());
				System.out.println("path:" + ((RecordValue) template).getField("path").getValueEncodingVariant());

			} else {
				assertion("GetLocation", template.getType().getName());
				System.out.println("locationName Encoding:"
						+ ((RecordValue) template).getField("locationName").getValueEncoding());
				System.out.println("locationName Variant:"
						+ ((RecordValue) template).getField("locationName").getValueEncodingVariant());

				return super.encode(template);
			}

		}
		return super.encode(template);
	}

}
