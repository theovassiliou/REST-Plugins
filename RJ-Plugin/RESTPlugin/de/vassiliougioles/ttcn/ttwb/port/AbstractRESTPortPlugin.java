package de.vassiliougioles.ttcn.ttwb.port;

import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriAddressList;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriParameterList;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriPortIdList;
import org.etsi.ttcn.tri.TriStatus;
import org.etsi.ttcn.tri.TriTestCaseId;

import com.testingtech.ttcn.tri.AbstractMsgBasedSA;
import com.testingtech.ttcn.tri.extension.PortPluginProvider;

public abstract class AbstractRESTPortPlugin extends AbstractMsgBasedSA implements PortPluginProvider {
	private static final long serialVersionUID = -6523964658234648218L;

	@Override
	public TriStatus triSendMC(TriComponentId componentId, TriPortId tsiPortId, TriAddressList addresses,
			TriMessage sendMessage) {
		return null;
	}

	@Override
	public TriStatus triSAReset() {
		return null;
	}

	@Override
	public TriStatus triExecuteTestcase(TriTestCaseId testCaseId, TriPortIdList tsiPorts) {
		return null;
	}

	@Override
	public TriStatus triMap(TriPortId compPortId, TriPortId tsiPortId) {
		return null;
	}

	public TriStatus triMapParam(TriPortId compPortId, TriPortId tsiPortId, TriParameterList paramList) {
		return null;
	}

	@Override
	public TriStatus triUnmap(TriPortId compPortId, TriPortId tsiPortId) {
		return null;
	}

	@Override
	public TriStatus triEndTestCase() {
		return null;
	}

	@Override
	public TriStatus triSendBC(TriComponentId componentId, TriPortId tsiPortId, TriMessage sendMessage) {
		return null;
	}

	@Override
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId, TriAddress address,
			TriMessage sendMessage) {
		return null;
	}

}
