package de.vassiliougioles.ttcn.ttwb.codec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.client.api.Request;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.IntegerValue;
import org.etsi.ttcn.tci.LengthRestriction;
import org.etsi.ttcn.tci.MatchingMechanism;
import org.etsi.ttcn.tci.RangeBoundary;
import org.etsi.ttcn.tci.RecordOfValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;

import de.vassiliougioles.ttcn.ttwb.port.TTCNRESTMapping;

public class HeaderField implements Value, TTCNRESTMapping {

	private static final long serialVersionUID = 6491965355018143505L;
	/**
	 * 
	 */
	Value v = null;
	String fn = "";

	public HeaderField(Value value, String fieldName) {
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
	 * if there is no value encoding or no newFieldName is used a mapped fieldName
	 * then the value of the parameter fieldName is being returned
	 * 
	 * @param val       the TciValue to be mapped, typically a field of a record
	 *                  value
	 * @param fieldName the name of the field
	 * @return fieldName if valueEncoding contains "param | header" or is null.
	 */
	public String getHeaderName() {
		if (v.getValueEncoding() == null)
			return fn;
		String[] headerSplit = v.getValueEncoding().split(":");
		if (headerSplit.length > 1) {
			String headerName = headerSplit[1].trim();
			if (headerName == null) {
				return fn;
			} else {
				return headerName;
			}
		} else
			return fn;
	}

	public String getHeaderValue() {
		String headerValue = "";

		if (isHeader() || (v.getType().getName().equals("HeaderField"))) {
			switch (v.getType().getTypeClass()) {
			case TciTypeClass.UNIVERSAL_CHARSTRING:
				if (!v.notPresent())
					headerValue = ((UniversalCharstringValue) v).getString();
				break;
			case TciTypeClass.INTEGER:
				if (!v.notPresent())
					headerValue = Integer.toString(((IntegerValue) v).getInt());
				break;
			case TciTypeClass.CHARSTRING:
				if (!v.notPresent())
					headerValue = ((CharstringValue) v).getString();
				break;

			default:
				return "";
			}
		}
		return headerValue;
	}

	static public boolean isIn(HeaderField headerField, List<HeaderField> theList) {
		for (Iterator iterator = theList.iterator(); iterator.hasNext();) {
			HeaderField listField = (HeaderField) iterator.next();
			if (headerField.fn.equals(listField.fn)) {
				return true;
			}

		}
		return false;
	}

	static public List<HeaderField> collectHeaders(Value theValue, String fieldName) {
		List<HeaderField> listOfValue = new ArrayList<HeaderField>();

		switch (theValue.getType().getTypeClass()) {
		case TciTypeClass.SET:
		case TciTypeClass.RECORD:
			String[] fieldNames = ((RecordValue) theValue).getFieldNames();
			for (String fn : fieldNames) {
				Value aField = ((RecordValue) theValue).getField(fn);
				listOfValue.addAll(collectHeaders(aField, fn));
			}
			break;
		case TciTypeClass.RECORD_OF:
		case TciTypeClass.SET_OF:
			RecordOfValue rov = (RecordOfValue) theValue;
			if (rov.getType().getName() == "HeaderFields") {
				for (int i = 0; i < rov.getLength(); i++) {
					RecordValue v = (RecordValue) rov.getField(i);
					Value tv = v.getField("val");
					String fn = ((UniversalCharstringValue) v.getField("name")).getString();
					listOfValue.add(new HeaderField(tv, ((HeaderField) v).getHeaderName()));

				}
			}
			break;
		case TciTypeClass.CHARSTRING:
		case TciTypeClass.INTEGER:
		case TciTypeClass.UNIVERSAL_CHARSTRING:
		case TciTypeClass.FLOAT:
		case TciTypeClass.BOOLEAN:
			if (theValue.getValueEncoding().startsWith(_HEADER_FIELD_ENCODING_PREFIX_)) {
				listOfValue.add(new HeaderField(theValue, fieldName));
			}
			break;
		default:
			break;
		}
		return listOfValue;
	}

	static public StringBuilder fillRequest(Request request, List<HeaderField> headers) {
		StringBuilder sb = new StringBuilder();

		for (HeaderField headerField : headers) {
			if (headerField.isHeader()) {
				// FIXME: Why is this in?
				// request.header(headerField.getHeaderName(), headerField.getHeaderValue());
				sb.append(headerField.getHeaderName() + ": " + headerField.getHeaderValue() + "\n");
			}
		}

		return sb;

	}

	public boolean isHeader() {
		return (!v.notPresent() && (v.getValueEncoding().startsWith(_HEADER_FIELD_ENCODING_PREFIX_)) || !fn.equals(""));
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
