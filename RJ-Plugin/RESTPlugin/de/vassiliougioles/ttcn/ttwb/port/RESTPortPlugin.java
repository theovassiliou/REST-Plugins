package de.vassiliougioles.ttcn.ttwb.port;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

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

import de.vassiliougioles.ttcn.ttwb.codec.RESTJSONCodec;

public class RESTPortPlugin extends AbstractRESTPortPlugin implements TTCNRESTMapping, PortPluginProvider {

	private static final long serialVersionUID = -6523964658234648218L;

	Value sutAddress = null;
	private RESTJSONCodec restCodec = null;

	public ISAPlugin getPortPlugin() {
		return new RESTPortPlugin();
	}

	public void setSutAddress(Value sutAddress) {

		if ((!(sutAddress == null)) && sutAddress.getType().getName().equals(_PORT_CONFIG_TYPE_NAME_)) {
			RecordValue rv = (RecordValue) sutAddress;
			String[] fieldNames = rv.getFieldNames();
			for (int i = 0; i < fieldNames.length; i++) {
				String string = fieldNames[i];
				if (string.equals(_CONFIG_BASEURL_FIELD_NAME_)) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					restCodec.setBaseUrl(cv.getString());
				} else if (string.equals(_CONFIG_AUTH_FIELD_NAME_) && !rv.getField(string).notPresent()) {
					UniversalCharstringValue cv = (UniversalCharstringValue) rv.getField(string);
					restCodec.setAuthorization(cv.getString());
				}
			}
		}
		this.sutAddress = sutAddress;
	}

	@Override
	public TriStatus triMapParam(TriPortId compPortId, TriPortId tsiPortId, TriParameterList paramList) {
		restCodec = (RESTJSONCodec) getRB().codecServer.getCodec(_ENCODING_NAME_);

		@SuppressWarnings("unchecked")
		Enumeration<TriParameter> parameterList = paramList.getParameters();
		while (parameterList.hasMoreElements()) {
			TriParameter triParam = (TriParameter) parameterList.nextElement();
			if (triParam instanceof TciValueContainer) {
				Value param = (Value) ((TciValueContainer) triParam).getValue();

				if (param.getType().getName().equals(_PORT_CONFIG_TYPE_NAME_)) {
					setSutAddress(param);
				}
			}
		}

		return TriStatusImpl.OK;

	}

	@Override
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId, TriAddress triAddress,
			TriMessage triSendMessage) {

		RESTMessage theMessage;
		try {
			theMessage = RESTMessage.makeMessage(triSendMessage, triAddress, sutAddress);

			String encResponse = theMessage.sendMessage();
			TriMessage rcvMessage = TriMessageImpl.valueOf(encResponse.toString().getBytes(StandardCharsets.UTF_8));
			// enqueue the URL as response address to be able to identify it in TTCN-3
			TriAddressImpl rcvSutAddress = new TriAddressImpl(theMessage.getMethodCall().getBytes());
			triEnqueueMsg(tsiPortId, rcvSutAddress, componentId, rcvMessage);

		} catch (Exception e) {
			logError(e.getMessage());
			return new TriStatusImpl(e.getMessage());
		}
		return TriStatusImpl.OK;

	}

	public TriStatus triUnmapParam(TriPortId compPortId, TriPortId tsiPortId, TriParameterList paramList) {
		sutAddress = null;
		return TriStatusImpl.OK;
	}

}
