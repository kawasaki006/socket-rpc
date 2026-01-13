# Lightweight RPC Framework (Netty / Socket + ZooKeeper)

This project is a lightweight RPC framework implemented in Java, featuring:
- **Netty-based** transport (with an alternative **Socket** implementation)
- **ZooKeeper**-backed service registry & discovery with client-side cache + watchers
- Custom **binary RPC protocol** (magic code, versioning, length-field framing)
- Pluggable **serialization / compression / load balancing** via a custom SPI loader
- Client-side **dynamic proxy** for transparent interface-based RPC
- Basic resiliency primitives: **retry / rate limiting / circuit breaker** via annotations

---

## Project Structure

- `rpc-core`: framework core (protocol, transport, registry, proxy, SPI, resiliency)
- `test-api`: shared API interfaces / DTOs for testing
- `test-server`: test service provider
- `test-client`: test client

---

## Key Features

### 1) Custom RPC Protocol (Binary + Framing)

The protocol is encoded into a single message container `RpcMsg` and supports:
- **Magic code** to quickly reject invalid traffic
- **Version** field for forward compatibility
- **Message type** (RPC request/response + heartbeat request/response)
- **Serializer type** and **compress type** (pluggable)
- **Request ID** for correlating async responses
- **Length-field framing** to handle TCP fragmentation/coalescing

The Netty decoder extends `LengthFieldBasedFrameDecoder` and then parses fields in order (magic → version → length → msgType → serializer → compress → reqId → data).  
See `NettyRpcDecoder` for the exact layout and checks. 

The Netty encoder writes magic/version, reserves 4 bytes for length, and backfills message length after writing the payload. :contentReference[oaicite:2]{index=2}

---

### 2) Transport Layer: Netty Client/Server + Channel Reuse + Heartbeat

**Netty transport** is implemented under `transmission/netty`:
- Client uses a **ChannelPool** keyed by remote address to reuse connections
- Heartbeats are triggered via Netty `IdleStateHandler`:
  - client: send heartbeat if write-idle
  - server: close channel if read-idle for a period

On the client side, each outgoing request is associated with a `CompletableFuture`, stored in a map; the inbound handler completes the future when a response arrives (future correlation).  
See `UnprocessedRpcReq` for the correlation map. :contentReference[oaicite:3]{index=3}

---

### 3) ZooKeeper Service Registry & Discovery (Curator + Cache + Watchers)

Service discovery/registry is implemented via ZooKeeper (Curator):
- Provider registers instances under a root path per service (service name includes interface + version + group)
- Consumer discovers all instances, then applies **load balancing** to select one address
- Client maintains a **local address cache**, and uses **watchers** to update the cache on changes

---

### 4) Load Balancing Strategies

Load balancer is abstracted behind `LoadBalance` and includes multiple strategies:
- Random
- Round-robin
- Consistent hashing (stable routing per service key)

---

### 5) Extensibility via Custom SPI Loader

The framework includes a minimal SPI mechanism `CustomLoader`:
- Loads implementations from `META-INF/kawasaki-rpc/<interface-full-name>`
- Lazily instantiates implementations with caching (double-checked locking via `Holder`)
- Used for pluggable components such as serializers

See `CustomLoader` for the resource path convention and loading logic. :contentReference[oaicite:4]{index=4}

---

### 6) Serialization & Compression

Pluggable serializers (examples in repo):
- Kryo (thread-local instance)
- Hessian
- Protostuff

Compression:
- GZIP compression/decompression

Decoder decompresses and then deserializes based on the serializer type embedded in the message. 

---

### 7) Client-Side Proxy + Resiliency Annotations

The consumer calls RPC services via a dynamic proxy:
- Build `RpcReq` from interface + method + parameter types
- Send request via `RpcClient`
- Block on the returned `Future` (or integrate async downstream if needed)

Resiliency is annotation-driven:
- `@Retry`: retry on configured exception type, with max attempts & delay
- `@Limit`: rate limiting (token-bucket style via RateLimiter)
- `@Breaker`: circuit breaker with states CLOSED / OPEN / HALF_OPEN, failure threshold, half-open success rate, and window time

These are applied around method invocation so you can configure behavior per RPC method.

---

## Quick Demo

> Note: You need a running ZooKeeper instance and correct ZK address configured in constants.

1) Start ZooKeeper (example: local/VM/container)
2) Start provider:

```bash
mvn -q -pl socket-rpc/test-server -am test
# or run the Main class under test-server module
```

3) Start client:

```bash
mvn -pl socket-rpc/test-client -am test
# or run the Main class under test-client module
```