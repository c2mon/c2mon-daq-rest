# Overview

The REST DAQ allows the user to acquire data through REST requests. This module provide two ways to acquire the data.

- The first way is do define a GET URL which links to a restful web-service. The data which this service provides will than requests in a given frequency from the module.
- The second way to acquire data is through the POST modus. If the POST modus is set, the module itself works as web-service which accept post requests.

# Configuration

## Equipment Address

No equipment address needs to be defined for the REST message handler. 

## DataTag Address

Each DataTag possess an address which is specified through a Map. Every map entry is of the Type `<String, String>`.

Furthermore the map can hold information for two kinds of addresses. The first is for the GET functionality the second is for the POST functionality.

### GET Values

| Key | Type | Mandatory? | Explanation |
| --- | ---- | ---------- | ----------- |
| mode | 'GET' | Yes | Defines that this Address represents the GET modus |
| url  | String | Yes | Defines the URL which provides a GET service |
| getFrequency | Integer | No | Defines the frequency (in sec) in which the GET request gets triggered. If not set the default value of 30 sec will be applied. |
| jsonPathExpression | String | No | If the response of the GET request is an JSON object an expression can define which value of the JSON object shall be extracted |

- If the GET modus is set in the Address the module will send a GET request in the given frequency to the defined URL.

 

### POST Values

| Key | Type | Mandatory? | Explanation |
| --- | ---- | ---------- | ----------- |
| mode |'POST' | Yes | Defines that this HardwareAddress runs in POST modus |
| postFrequency | Integer | No | Defines the time intervall (In sec) the DAQ expects an incoming POST message for the given Tag. In case the DAQ is not receiving a refresh it will invalidate the Tag. By default, no regular update is required. |

- If the the POST modus is set in the Address the module will expect a POST request in the defined frequency. If no POST message arrives in the given frequency the DAQ will set the corresponding DataTag to invalid.

# Examples

## Example for defining a DataTag with the GET Modus

```java
HashMap<String, String> tagAddressGET = new HashMap<>();
tagAddressGET.put("mode", "GET");
tagAddressGET.put("url", "http://jsonplaceholder.typicode.com/posts/2");
tagAddressGET.put("getFrequency", "20");
tagAddressGET.put("jsonPathExpression", "$.id");
 
DataTag tagGET = DataTag.builder()
    .id(1_001L)
    .name("restGetTagPattern")
    .description("rest GET data tag with a address and a jsonPath expression")
    .dataType(DataType.LONG)
    .address(new DataTagAddress(tagAddressGET)).build();
```

## Example for defining a DataTag with the POST Modus:

```java
HashMap<String, String> tagAddressPOST = new HashMap<>();
tagAddressPOST.put("mode", "POST");
tagAddressPOST.put("postFrequency", "60");
 
DataTag tagPOST = DataTag.builder()
    .id(1_002L)
    .name("restPostTag")
    .description("rest POST data tag")
    .dataType(DataType.STRING)
    .address(new DataTagAddress(tagAddressPOST)).build();
```


# Commands

The REST DAQ does not support commands.


# Special Behaviour

## Making a POST request

If the client makes a post call he must define content type in the header of the URI.
The two recommended types to use are `text/plain` and `text/json`. If you define another type the service could decode the message in a wrong way.

### Example:
```bash
curl -XPOST http://137.138.46.95:8080/tags/1003 -d '1337' -H 'Content-Type: text/plain'
```


# Useful Links

- https://github.com/jayway/JsonPath
- http://c2mon.web.cern.ch/c2mon/docs/#_online_configuration_with_the_c2mon_configuration_api
- https://curl.haxx.se/docs/manpage.html