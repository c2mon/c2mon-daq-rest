# Overview
[![build status](https://gitlab.cern.ch/c2mon/c2mon-daq-rest/badges/master/build.svg)](https://gitlab.cern.ch/c2mon/c2mon-daq-rest/commits/master)

The REST DAQ allows to publish data through RESTful HTTP requests in two ways:

- Sending value updates via HTTP POST message
- Performing periodic GET requests to a pre-defined web service URL

At startup, the DAQ process contacts first the C2MON server to check, if there is already a configuration for the given process name.
If not, it will create on-the-fly a new one for you.

Note, that this default behaviour can be turned off by adding `c2mon.daq.rest.autoConfiguration=false` to your c2mon-daq.properties file. This might be interesting in case you want to gain more control about your existing DAQ processes (see also [C2MON Configuration API]).

# Sending value updates

Let's start with a simple example.
Using the [CURL command] we can send the following JSON message to the REST DAQ and create at the same time a new `DataTag` of type Double. We assume that the the DAQ is running on the same host:

```bash
curl -i \
-H "Accept: application/json" \
-H "Content-Type:application/json" \
-X POST --data '{"name": "rack/XYZ/temperature", "value": 23, "metadata": {"building": 123, "responsible": "Jon Doe"}}' "http://localhost:8080/update"
```

## Supported fields in JSON message

The JSON message is internally represented by [RestTagUpdate] class, which allows sending the following parameters:

| Field | Description | Type | Mandatory? |  Default value |
|-------|-------------|------|------------|----------------|
| name | The tag name | String | Yes |  |
| value | The tag value, which can be any raw data value or JSON object | Object | No | `null` |
| valueDescription | An optional, non-sticky description for this explicit value update. | String | No |  |
| description | A static description of the tag, which will be sent with every update | String | No |  |
| postFrequency | Expected update interval (in seconds). In case an update is not received within this interval, the tag will be invalidated. By default, no interval is specified. | Integer | No | -1 |
| type | The value type, which can be any raw data type or complex Java Class name | String | No | The default type is calculated automatically from the given value. If the first update contains no value the type will be `String`. In case of a JSON message the type is `java.util.HashMap` |
| metadata | The very first update allows setting metadata for the tag, which will then be propagated with every subsequent message. | JSON | No |  |

Note, that currently all optional fields except of `name`, `value` and `valueDescription` are only in the very beginning when a new DataTag is created.
However, we are planning to make this part more dynamic in the future in order to change for instance metadata dynamically.

[RestTagUpdate]: https://gitlab.cern.ch/c2mon/c2mon-daq-rest/blob/master/src/main/java/cern/c2mon/daq/rest/RestTagUpdate.java

## Simplified message API

If you find the JSON HTTP POST call too complex for your use-case, we also provide a second way of sending value updates. However, it only allows sending the value itself and no assumes that the tag already exists. Otherwise, it will not accept the update. So, you have at least once make use of the JSON message to create the DataTag or alternatively use the Configuration API (see section below).

### Example

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



# Downloading latest stable distribution tarball

The c2mon-daq-rest tarball release can be downloaded from [CERN Nexus Repository](https://nexus.web.cern.ch/nexus/#nexus-search;gav~cern.c2mon.daq~c2mon-daq-rest~~tar.gz~)

Please check [here](https://gitlab.cern.ch/c2mon/c2mon-daq-rest/tags) for the latest stable releaes version.

## Installing

- Download the latest stable distribution tarball
- Note, that the tarball does not include a root folder, so you have to create this yourself before extracting it:
  
  ```bash
  mkdir c2mon-daq-rest; tar -xzf c2mon-daq-rest-1.0.x-dist.tar.gz -C c2mon-daq-rest
  
  ```
  
Please note that you have first to generate a configuration for a REST DAQ process, before you can actually start the DAQ. This is explained further down.


# Periodic GET

This feature is interesting, if you want to extract metrics from an existing REST service. To do this, you must configure new DataTags with the [C2MON Configuration API] in order to specify from where to fetch the data.

The following table describes the key/value pairs that the `DataTagAddress` must contain for the REST DAQ to perform a periodic GET.

| Key | Type | Mandatory? | Explanation |
| --- | ---- | ---------- | ----------- |
| mode | 'GET' | Yes | Selects periodic GET mode |
| url  | String | Yes | URL of the endpoint to be requested |
| getFrequency | Integer | No | Frequency (in seconds) in which the endpoint will be polled. If not set, defaults to 30 sec. |
| jsonPathExpression | String | No | [JSON Path] expression pointing to a property to be extracted from the HTTP response |

## Example configuration

```java
HashMap<String, String> address = new HashMap<>();
address.put("mode", "GET");
address.put("url", "http://jsonplaceholder.typicode.com/posts/2");
address.put("getFrequency", "20");
address.put("jsonPathExpression", "$.id");

DataTag tag = DataTag.create("someUsefulTag", String.class, new DataTagAddress(address)).build();
```



# Changing REST DAQ configuration with C2MON Configuration API

It is also possible to configure the REST DAQ from 'outside' with the [C2MON Configuration API].
This can be useful, if you want to change things manually or just to have more control about the tag creation.

The following table describes the key/value pairs that the `DataTagAddress` must contain for the REST DAQ to expose a POST endpoint.

| Key | Type | Mandatory? | Explanation |
| --- | ---- | ---------- | ----------- |
| mode |'POST' | Yes | Selects POST endpoint mode |
| postFrequency | Integer | No | Expected update interval (in seconds). In case an update is not received within this interval, the tag will be invalidated. By default, no interval is specified. |

## Example configuration

```java
HashMap<String, String> address = new HashMap<>();
address.put("mode", "POST");
address.put("postFrequency", "60");

DataTag tag = DataTag.create("myTagEndpoint", String.class, new DataTagAddress(address)).build();
```

A full configuration example ready to download is available from here:

https://gitlab.cern.ch/c2mon/c2mon-configuration-examples

## General Process/Equipment configuration
In order to configure RESTful datatags you have first to declare a REST DAQ (if it does not yet exist) [Process] and [Equipment] to which you want then to attach the tags.

Please read therefore also the documentation about the [C2MON Configuration API].

The `EquipmentMessageHandler` class to be specified during the Equipment creation is: `cern.c2mon.daq.rest.RestMessageHandler`

[Process]: http://c2mon.web.cern.ch/c2mon/docs/user-guide/client-api/configuration/#configuring-processes
[Equipment]: http://c2mon.web.cern.ch/c2mon/docs/user-guide/client-api/configuration/#configuring-equipment

# Commands

For now, the REST DAQ does not support CommandTags.


# Building from Source
C2MON uses a [Maven][]-based build system.

## Prerequisites

[Git], [JDK 8 update 20 or later][JDK8 build] and [Maven].

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.8.0` folder
extracted from the JDK download.

## Check out sources
`git clone https://github.com/c2mon/c2mon-daq-rest.git`

## Compile and test; build jar and distribution tarball
`mvn package -DskipDockerBuild -DskipDockerTag --settings settings.xml`

### C2MON Maven settings

As C2MON is not (yet) storing the Artifacts in Central Maven Repository, please use the [Maven settings](settings.xml) file of this project to compile the code.


# Useful Links

- [C2MON configuration API]
- [JSON Path]
- [CURL command]


[JSON Path]: https://github.com/jayway/JsonPath
[C2MON Configuration API]: http://c2mon.web.cern.ch/c2mon/docs/user-guide/client-api/configuration/
[CURL command]: https://curl.haxx.se/docs/manpage.html
[Maven]: http://maven.apache.org
[Git]: http://help.github.com/set-up-git-redirect
[JDK8 build]: http://www.oracle.com/technetwork/java/javase/downloads
[Pull requests]: http://help.github.com/send-pull-requests
