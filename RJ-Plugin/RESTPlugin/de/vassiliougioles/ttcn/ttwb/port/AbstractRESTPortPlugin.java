package de.vassiliougioles.ttcn.ttwb.port;

import org.etsi.ttcn.tci.TciParameterList;
import org.etsi.ttcn.tci.TciValueList;
import org.etsi.ttcn.tci.Type;
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
import com.testingtech.ttcn.tri.extension.XPortPluginProvider;

public abstract class AbstractRESTPortPlugin extends AbstractMsgBasedSA implements XPortPluginProvider, IXSAPlugin {
	private static final long serialVersionUID = -6523964658234648218L;

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
		// TODO Auto-generated method stub
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
	public TriStatus xtriUnmapParam(TriPortId compPortId, TriPortId tsiPortId, TciParameterList paramList) {
		// TODO Auto-generated method stub
		return null;
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
