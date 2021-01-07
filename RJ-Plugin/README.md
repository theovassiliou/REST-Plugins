## RJ-Plugin
RJ-Plugin is a TTworkbench plugin that supports communication with webservices via http-REST. 
Currently the following functionality is supported: 

 - JSON encoded data (only), support of XML in progress
 - GET, POST, PUT, DELETE
 
 We will further develop and enhance this plugin. Please note that this plugin is in an early status. Nevertheless we encourage you 
 to give us feedback by emailing us directly at: vassiliou-gioles@tu-berlin.de
 
As examples we provide this plugin with some TTCN-3 test suites for a publicly available APIs. 

 * Deutsche Bahn API
 * ThinkSpeak API
 * Swagger PetStore example
 
 ### Swagger PetStore example
 
 The swagger petstore API implementation is available as docker container. To run the example simply: 
 
 ```
 docker pull openapitools/openapi-petstore
 docker run -d -e OPENAPI_BASE_PATH=/v2 -e DISABLE_API_KEY=1 -e DISABLE_OAUTH=1 -p 80:8080 openapitools/openapi-petstore
 ```
 
 If you would also like to use the API_KEY features use
 
 ```
 docker run -d -e OPENAPI_BASE_PATH=/v2 -e DISABLE_OAUTH=1 -p 80:8080 openapitools/openapi-petstore
 ```
 instead, as this enables API_KEY authentification. Please note that OAUTH is currently NOT supported but is work-in-progress