package  de.vassiliougioles.ttcn.ttwb.codec;

import org.etsi.ttcn.tci.TciCDProvided;

import com.testingtech.ttcn.extension.CodecProvider;
import com.testingtech.util.plugin.PluginInitException;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;


public class SATProvider implements CodecProvider {

	public TciCDProvided getCodec(RB rb, String encodingRule) throws PluginInitException {
		TciCDProvided codec = new SAT();
		return codec;
	}

}
