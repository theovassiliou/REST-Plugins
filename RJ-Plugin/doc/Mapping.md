# Mapping HTTP and JSON to TTCN-3

## Mapping HTTP to TTCN-3

### By example

Let's start with a simple GET request

	GET https://localhost:5056/v1/pets/findByStatus?status=available&status=sold

We can see several properties of this HTTP request: 

 - This request is a `GET` request (operation)
 - The request uses `https` as (scheme)
 - the services is provided by `localhost` at port `5056` (host)
 - `v1` is common to all to endpoints (basePath)
 - `pets` is the ressource
 - `findByStatus` is the operation
 - The request uses query parameters, in this case multiple occurences of `status`  


#### TTCN-3 Requests 

A HTTP request is translated to a `record` type with adding encoding information to the `encoding` and `variant` attributes.

	type record FindByStatus {
		record of StatusEnum status
		} with {
			encode "REST/get";
			variant "path: /pet/findByStatus";
			encode (status) "query";
		}

	type string StatusEnum { "available", "pending", "sold"};
	
Important mapping rules for this requests are the following

 - record type name free to choose
 - `encode "REST/get"` specifies that the request is a GET request, other supported encoding attributes are "REST/post", "REST/put", "REST/head", "REST/options", "REST/put", "REST/delete", "REST/patch"
 - `variant "path: /pet/findByStatus"` specifies the endpoint. It should be noted here, that the the basePath is _not_ included. See [TTCN-3 Ports](#TTCN-3 Ports) for basePath specification.
 - types for query parameters: openapiv3.string, int32, int64, double

You can process the StatusLine of the request via the `sender` in form of 
 
	var HTTPPort.address requestLine;
	service.receive(?) -> sender requestLine;
	log(requestLine);

In order to process the full Request the PortPlugin offer the feature of mirror ports. Mirror ports are TSI port that have a port type name prefix of "Mirror_"
 
 
#### TTCN-3 Responses

To process our result in this example we are using the generic `HTTPResponse` type. It's definition looks as follows: 

	type record HTTPResponse {
		StatusLine statusLine, set of Header headers, Body body optional
	}
	with {
		encode "HTTP/response"
	}
	
In other words a response contains a 
 
 - statusLine
 - a set of headers, each representing one header field (name, val)
 - a body. The message body is a record containing both the text representation (messageBodyTxt) as well as the raw representation (messageBodyRaw)

If a HTTP Response is recceived the Status Line of the respective request is available at the `sender` in form of 

	var HTTPPort.address requestLine;
	service.receive(?) -> sender requestLine;
	log(requestLine);




#### TTCN-3 Ports

First we have to configure our TTCN-3 test system, that is uses REST communication. This is been done as follows

	import from Lib_HTTP all;
	
	type port HTTPPort message {
		in HTTPResponse;
		out FindByStatus;
		map param (RESTAPIconfig config);
	}
	with {
		encode "RESTful"
	}

According to the mapping the `encode` attribute has to be set to `RESTful`. A `RESTful` port has to be a message based port, the port type name can be chosen freely.
`Lib_HTTP` defines a generic HTTP response record type `HTTPResponse`, as well a s a generic `HTTPRequest` record type. However, in this example we a chosen, to use a service specific type `FindByStatus` that represents our `GET` opration. 

#### Port Configuration


