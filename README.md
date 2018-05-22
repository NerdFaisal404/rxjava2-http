# rxjava2-http
Transmit RxJava2 Flowable over http (with backpressure of course)

Status: *pre-alpha* (in development)

## Getting started

### Maven dependency
```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>rxjava2-http</artifactId>
  <version>VERSION_HERE</version>
</dependency>
```

### Create servlet
The servlet below exposes the `Flowable.range(1, 1000)` stream across HTTP:

```java
@WebServlet(urlPatterns={"/stream"})
public class HandlerServlet extends FlowableHttpServlet {
      
    public HandlerServlet() {
        super(
          Flowable
            .range(1,1000)
            .map(DefaultSerializer.instance()::serialize)      
        );
    }
}
```
The default behaviour is to schedule requests on `Schedulers.io()` but this is configurable via another `FlowableHttpServlet` constructor.

### Create client
Assuming the servlet above is listening on `http://localhost:8080/stream`, this is how you access it over HTTP:

```java
Flowable<Integer> numbers = 
    Client
       .get("http://localhost:8080/")
       .deserialized();
```
More client options are available. Here is an example:

```java
Flowable<Integer> numbers = 
	Client
	  .get("http://localhost:8080/") //
	  .bufferSize(100) // request in batches of 100, default is 16
	  .delayErrors(false) // as soon as an error is noticed emit and throw away any queued items
	  .serializer(serializer); // used for deserialization
```

## Design
WebSockets is a natural for this but can be blocked by corporate firewalls so this library starts with support for HTTP 1.0. 

We want API support for these actions:

* subscribe
* request
* cancel

Support is provided via these URL paths

Path | Method | Action | Returns
--- | --- | --- | --
`/`   | GET | subscribe with no request | Stream ID then binary stream
`/?r=REQUEST`|GET | subscribe with initial request | Stream ID then binary stream
`/?id=ID&r=REQUEST` |GET| request more from given stream | nothing
`/?id=ID&r=-1` |GET| cancel given stream | nothing

The format returned in the subscribe calls is (EBNF):

```
Stream = Id {Item} [ Error | Complete ];
Id = SignedLong; // 8 bytes
Item = Length {Byte};
Length = SignedInteger; // 4 bytes
Error = NegativeLength {Byte};
NegativeLength = SignedInteger; // Not Integer.MIN_VALUE
Complete = Integer.MIN_VALUE // 4 bytes, SignedInteger
```

The core of the library is support for publishing a `Flowable<ByteBuffer>` over HTTP. Serialization is a little optional extra that occurs at both ends.

### Nested Flowables
I have a design in mind for publishing nested Flowables over HTTP as well (representing the beginning of a nested Flowable with a special length value). I don't currently have a use case but if you do raise an issue and we'll implement it.
