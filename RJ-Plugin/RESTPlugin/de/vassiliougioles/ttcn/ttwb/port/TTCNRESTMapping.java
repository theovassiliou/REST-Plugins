package de.vassiliougioles.ttcn.ttwb.port;

public interface TTCNRESTMapping {
	final String _DEFAULT_BASE_URL_ = "NO PROTOCOL SET:";
	final String _DEFAULT_AUTHORIZATION_ = "NO AUTHORIZATION SET";
	final String _USER_AGENT_NAME_ = "TTworkbench/29 RESTPortPlugin/0.1";
	final String _ENCODING_NAME_ = "RESTful";
	final String _PORT_TYPE_NAME_ = "RESTful";
	final String _PORT_CONFIG_TYPE_NAME_ = "RESTAPIconfig";
	final String _DEFAULT_HEADERS_FIELD_NAME_ = "defaultHeaders";
	final String _CONFIG_BASEURL_FIELD_NAME_ = "baseUrl";
	final String _CONFIG_AUTH_FIELD_NAME_ = "authorization";
	final String _CONTENT_JSON_ENCODING_ = "application/json";
	final String _CONTENT_FORMDATA_ENCODING_ = "application/x-www-form-urlencoded";

	final String _HTTP_ENCONDING_NAME_PREFIX = "HTTP/";
	final String _HTTP_RESPONSE_ENCODING_NAME_ = "HTTP/response";

	final String _ENCODING_NAME_PREFIX_ = "REST/";
	final String _REST_RESPONSE_ENCODING_NAME_ = "REST/response";
	
	final String _GET_ENCODING_NAME_ = "REST/get";
	final String _POST_ENCODING_NAME_ = "REST/post";
	final String _HEAD_ENCODING_NAME_ = "REST/head";
	final String _OPTIONS_ENCODING_NAME_ = "REST/options";
	final String _PUT_ENCODING_NAME_ = "REST/put";
	final String _DELETE_ENCODING_NAME_ = "REST/delete";
	final String _PATCH_ENCODING_NAME_ = "REST/patch";
	

	final String _REQUEST_PATH_VARIANT_PREFIX_ = "path:";
	
	final String _GET_RESPONSE_ENCODING_NAME_ = "REST/getResponse";
	final String _POST_RESPONSE_ENCODING_NAME_ = "REST/postResponse";
	final String _HEAD_RESPONSE_ENCODING_NAME_ = "REST/headResponse";
	final String _OPTIONS_RESPONSE_ENCODING_NAME_ = "REST/postResponse";
	final String _PUT_RESPONSE_ENCODING_NAME_ = "REST/putResponse";
	final String _DELETE_RESPONSE_ENCODING_NAME_ = "REST/deleteResponse";
	final String _PATCH_RESPONSE_ENCODING_NAME_ = "REST/patchResponse";
	
	

	final String _HEADER_FIELD_ENCODING_PREFIX_ = "header";
	final String _PATH_QUERY_FIELD_ENCODING_PREFIX_ = "query";
	
	final String _BODY_FORMDATA_FIELD_ENCODING_PREFIX_ = "body/formdata";
	final String _BODY_FIELD_JSON_ENCODING_NAME_ = "body/json";
	
}
