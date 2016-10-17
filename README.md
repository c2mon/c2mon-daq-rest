# Overview

The REST DAQ allows the user to publish data through RESTful HTTP requests in two ways:

- Performing periodic GET requests to a pre-defined web service URL;
- Exposing a REST endpoint which accepts POST requests.

# Periodic GET

The following table describes the key/value pairs that the `DataTagAddress` must contain for the REST DAQ to perform a periodic GET.

| Key | Type | Mandatory? | Explanation |
| --- | ---- | ---------- | ----------- |
| mode | 'GET' | Yes | Selects periodic GET mode |
| url  | String | Yes | URL of the endpoint to be requested |
| getFrequency | Integer | No | Frequency (in seconds) in which the endpoint will be polled. If not set, defaults to 30 sec. |
| jsonPathExpression | String | No | [JSON Path](https://github.com/jayway/JsonPath) expression pointing to a property to be extracted from the HTTP response |

## Example configuration

```java
HashMap<String, String> address = new HashMap<>();
address.put("mode", "GET");
address.put("url", "http://jsonplaceholder.typicode.com/posts/2");
address.put("getFrequency", "20");
address.put("jsonPathExpression", "$.id");

DataTag tag = DataTag.create("someUsefulTag", String.class, new DataTagAddress(address)).build();
```


# POST endpoint

The following table describes the key/value pairs that the `DataTagAddress` must contain for the REST DAQ to expose a POST endpoint.

| Key | Type | Mandatory? | Explanation |
| --- | ---- | ---------- | ----------- |
| mode |'POST' | Yes | Selects POST endpoint mode |
| postFrequency | Integer | No | Expected update interval (in seconds). In case an update is not received within this interval, the tag will be invalidated.
By default, no interval is specified. |

## Example configuration

```java
HashMap<String, String> address = new HashMap<>();
address.put("mode", "POST");
address.put("postFrequency", "60");

DataTag tag = DataTag.create("myTagEndpoint", String.class, new DataTagAddress(address)).build();
```

## Example usage

The following example sends the value `1337` to the endpoint which has a tag with the id `1003` registered:

```bash
curl -XPOST http://localhost:8080/tags/1003 -d '1337' -H 'Content-Type: text/plain'
```

The following example does the same, but references the tag by name instead:

```bash
curl -XPOST http://137.138.46.95:8080/tags/myTagEndpoint -d '1337' -H 'Content-Type: text/plain'
```

### Note

The `Content-Type` header must be set correctly to ensure the POST body is decoded correctly. The two recommended types to use are `text/plain` and `text/json`.


# Commands

The REST DAQ does not support commands.


# Useful Links

- https://github.com/jayway/JsonPath
- [C2MON configuration API] (https://c2mon.web.cern.ch/c2mon/docs/latest/user-guide/client-api/configuration/#configuration-api)
- https://curl.haxx.se/docs/manpage.html
