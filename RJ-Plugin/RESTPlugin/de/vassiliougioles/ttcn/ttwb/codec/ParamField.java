package de.vassiliougioles.ttcn.ttwb.codec;

import java.util.ArrayList;
import java.util.List;

import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.IntegerValue;
import org.etsi.ttcn.tci.LengthRestriction;
import org.etsi.ttcn.tci.MatchingMechanism;
import org.etsi.ttcn.tci.RangeBoundary;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.RecordOfValue;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;
import de.vassiliougioles.ttcn.ttwb.port.TTCNRESTMapping;

public class ParamField implements Value, TTCNRESTMapping {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5783188367814098502L;
	Value v = null;
	String fn = "";

	public ParamField(Value value, String fieldName) {
		v = value;
		fn = fieldName;
		return;
	}
	public Value getValue() {
		return v;
	}
	
	/**
	 * Returns the mapped fieldName. The mapping is retrieved from the valueEncoding
	 * with encode (fieldName) "param|header [: newFieldName]" 
	 * 
	 * if there is no value encoding or no newFieldName is used a mapped fieldName then the
	 * value of the parameter fieldName is being returned
	 * 
	 * @param val       the TciValue to be mapped, typically a field of a record
	 *                  value
	 * @param fieldName the name of the field
	 * @return fieldName if valueEncoding contains "param | header" or is null.
	 */
	public String getFieldName() {
		if (v.getValueEncoding() == null)
			return fn;
		String[] paramSplit = v.getValueEncoding().split(":");
		if (paramSplit.length > 1) {
			String paramName = paramSplit[1].trim();
			if (paramName == null) {
				return fn;
			} else {
				return paramName;
			}
		} else
			return fn;
	}

	public String encodedParamField() {
		StringBuilder encodedParamField = new StringBuilder();
		
		if (isParameter()) {
			String paramName = getFieldName();
			encodedParamField.append(paramName);
			String uriEncodedFieldValue = null;
			switch (v.getType().getTypeClass()) {
			case TciTypeClass.UNIVERSAL_CHARSTRING:
				uriEncodedFieldValue = ((UniversalCharstringValue) v).getString().replace("+", "%2");
				break;
			case TciTypeClass.INTEGER:
				uriEncodedFieldValue = Integer.toString(((IntegerValue) v).getInt());
				break;
			case TciTypeClass.CHARSTRING:
				uriEncodedFieldValue = ((CharstringValue) v).getString().replace("+", "%2");
				break;

			default:
				return "";
			}
			encodedParamField.append("=").append(uriEncodedFieldValue);
		}
		return encodedParamField.toString();
	}
	
	static public List<ParamField> collectParams(Value theValue, String fieldName) {
		List<ParamField> listOfValue = new ArrayList<ParamField>();

		switch (theValue.getType().getTypeClass()) {
		case TciTypeClass.SET:
		case TciTypeClass.RECORD:
			String[] fieldNames = ((RecordValue) theValue).getFieldNames();
			for (String fn : fieldNames) {
				Value aField = ((RecordValue) theValue).getField(fn);
				listOfValue.addAll(collectParams(aField, fn));
			}
			break;
		case TciTypeClass.SET_OF:
		case TciTypeClass.RECORD_OF:
			RecordOfValue rov = (RecordOfValue) theValue;
			for (int i = 0; i < rov.getLength(); i++) {
				Value val = rov.getField(i);
				listOfValue.addAll(collectParams(val, fieldName));
			}
			break;			
		case TciTypeClass.CHARSTRING:
		case TciTypeClass.INTEGER:
		case TciTypeClass.UNIVERSAL_CHARSTRING:
		case TciTypeClass.FLOAT:
		case TciTypeClass.BOOLEAN:
			if (theValue.getValueEncoding().startsWith(_PATH_QUERY_FIELD_ENCODING_PREFIX_)) {
				listOfValue.add(new ParamField(theValue, fieldName));
			}
			break;
		default:
			break;
		}
		return listOfValue;
	}
	
	static public String buildQueryParams(List<ParamField> params) {
		StringBuilder sb = new StringBuilder();
		boolean hasParam = false; 
		for (ParamField value : params) {
			String epf = value.encodedParamField();
			if (!epf.equals("")) {
				sb.append(epf);
				sb.append("&");
				hasParam = true;
			}
		}
		if (hasParam) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	
	public boolean isParameter() {

		return !v.notPresent() && (v.getValueEncoding().startsWith(_PATH_QUERY_FIELD_ENCODING_PREFIX_));

	}

	public Type getType() {
		return v.getType();
	}

	public boolean notPresent() {
		return v.notPresent();
	}

	public String getValueEncoding() {
		return v.getValueEncoding();
	}

	public String getValueEncodingVariant() {
		return v.getValueEncodingVariant();
	}

	public LengthRestriction getLengthRestriction() {
		return v.getLengthRestriction();
	}

	public LengthRestriction newLengthRestriction() {
		return v.newLengthRestriction();
	}

	public void setLengthRestriction(LengthRestriction restriction) {
		v.setLengthRestriction(restriction);
	}

	public RangeBoundary getUpperTypeBoundary() {
		return v.getUpperTypeBoundary();
	}

	public LengthRestriction getTypeLengthRestriction() {
		return v.getTypeLengthRestriction();
	}

	public boolean isIfPresentEnabled() {
		return v.isIfPresentEnabled();
	}

	public RangeBoundary getLowerTypeBoundary() {
		return v.getLowerTypeBoundary();
	}

	public void setIfPresentEnabled(boolean enabled) {
		v.setIfPresentEnabled(enabled);
	}

	public MatchingMechanism getTypeMatchingMechanism() {
		return v.getTypeMatchingMechanism();
	}

	public String[] getEncodeAttributes() {
		return v.getEncodeAttributes();
	}

	public String[] getVariantAttributes(String encoding) {
		return v.getVariantAttributes(encoding);
	}

	public boolean equals(Value value) {
		return v.equals(value);
	}

}
