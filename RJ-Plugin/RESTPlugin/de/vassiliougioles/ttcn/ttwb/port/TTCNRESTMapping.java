package de.vassiliougioles.ttcn.ttwb.port;

public interface TTCNRESTMapping {
	final String _DEFAULT_BASE_URL_ = "NO PROTOCOL SET:";
	final String _DEFAULT_AUTHORIZATION_ = "NO AUTHORIZATION SET";
	final String _USER_AGENT_NAME_ = "TTworkbench/26 RESTPortPlugin/0.1";
	final String _ENCODING_NAME_ = "RESTfull/json";
	final String _PORT_TYPE_NAME_ = "RESTfull";
	final String _PORT_CONFIG_TYPE_NAME_ = "RESTAPIconfig";
	final String _CONFIG_BASEURL_FIELD_NAME_ = "baseUrl";
	final String _CONFIG_AUTH_FIELD_NAME_ = "authorization";
	final String _CONTENT_ENCODING_ = "application/json";

	final String _GET_ENCODING_NAME_ = "REST/get";
	final String _POST_ENCODING_NAME_ = "REST/post";
	final String _REQUEST_PATH_VARIANT_PREFIX_ = "path:";
	
	final String _GET_RESPONSE_ENCODING_NAME_ = "REST/getResponse";
	final String _POST_RESPONSE_ENCODING_NAME_ = "REST/postResponse";
	final String _HTTP_RESPONSE_ENCODING_NAME_ = "HTTP/response";

	final String _HEADER_FIELD_ENCODING_PREFIX_ = "header";
	final String _BODY_FIELD_ENCODING_NAME_ = "body/JSON";
	
}
