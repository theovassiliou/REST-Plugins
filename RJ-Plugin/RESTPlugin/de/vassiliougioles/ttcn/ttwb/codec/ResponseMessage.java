package de.vassiliougioles.ttcn.ttwb.codec;

import java.nio.ByteBuffer;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.HttpParser.ResponseHandler;

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