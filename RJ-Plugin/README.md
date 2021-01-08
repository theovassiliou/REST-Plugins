# RJ-Plugin
RJ-Plugin is a TTworkbench plugin that supports simple communication with webservices via http(s)-REST. 
Currently the following functionality is supported: 

 - JSON encoded data (only), support of XML planned
 - GET, POST, PUT, DELETE
 - http and https
 
 We will further develop and enhance this plugin. Please note that this plugin is in an early status. Nevertheless we encourage you 
 to give us feedback by emailing us directly at: vassiliou-gioles@tu-berlin.de

## Installation

See [Installation](Installation.md) on how to install RJ-Plugin in you TTworkbench environment and make use of it in your test suites.

## Examples

As examples we provide this plugin with some TTCN-3 example test cases for a publicly available API. 

 
### Swagger PetStore example
 
The swagger petstore API implementation is available online at [petstore.swagger.io](petstore.swagger.io) or as docker container. 
The example comes preconfigured to be executed against the online web-service. Take a look at the [TTCN-3 source file](ttcn3/PetStoreExample.ttcn3)
 
However, if you would like to have more control on the communication you can download the Swagger PetStore as docker container as follows.
 
```
docker pull openapitools/openapi-petstore
docker run -d -e OPENAPI_BASE_PATH=/v2 -e DISABLE_API_KEY=1 -e DISABLE_OAUTH=1 -p 80:8080 openapitools/openapi-petstore
```
 
If you would also like to use the API_KEY features use
 
```
docker run -d -e OPENAPI_BASE_PATH=/v2 -e DISABLE_OAUTH=1 -p 80:8080 openapitools/openapi-petstore
```

 instead, as this enables API_KEY authentification. Please note that OAUTH is currently NOT supported but might be supported in a future version.
 

 
## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/theovassiliou/RJ-Plugin/tags).

## Authors

* **Theo Vassiliou** - *Author* - [Theo Vassiliou](https://github.com/theovassiliou)

## License

This project is licensed under Eclipse Public License - v 2.0 - see the [LICENSE](LICENSE) file for details
