# zio-uuid

[![CI](https://github.com/guizmaii-opensource/zio-uuid/actions/workflows/ci.yaml/badge.svg)](https://github.com/guizmaii-opensource/zio-uuid/actions/workflows/ci.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/com.guizmaii/zio-uuid_3.svg)](https://central.sonatype.com/artifact/com.guizmaii/zio-uuid_3)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Time-based, sortable UUIDs and [TypeIDs](https://github.com/jetify-com/typeid) for ZIO 2.

`zio-uuid` is a "ZIOfied" fork of [uuid4cats-effect](https://github.com/ant8e/uuid4cats-effect)
by [Antoine Comte](https://github.com/ant8e).

## What you get

|         | time-based                 | sortable | random |
|--------:|:---------------------------|:--------:|:------:|
| UUID v1 | ✅ gregorian calendar       |          |        |
| UUID v6 | ✅ gregorian calendar       |    ✅     |        |
| UUID v7 | ✅ unix epoch               |    ✅     |   ✅    |

Each version has its own type — `UUIDv1`, `UUIDv6`, `UUIDv7` — so you cannot accidentally pass one
where another is expected. They are [zio-prelude](https://github.com/zio/zio-prelude) subtypes of
`java.util.UUID`, so they are usable anywhere a `UUID` is, at no runtime cost.

Implementation follows [RFC 9562](https://datatracker.ietf.org/doc/html/rfc9562).

There is no UUIDv4 here on purpose: ZIO already provides one via `ZIO.randomWith(_.nextUUID)`.

## Installation

```scala
libraryDependencies += "com.guizmaii" %% "zio-uuid" % "1.1.1"
```

Scala 3 only. Requires Java 17+.

## Usage

Generators are services. Provide the layer once, then use the accessors:

```scala
import zio.*
import zio.uuid.*

val program: ZIO[UUIDGenerator & TypeIDGenerator, IllegalArgumentException, Unit] =
  for {
    v7     <- UUIDGenerator.uuidV7
    v6     <- UUIDGenerator.uuidV6
    v1     <- UUIDGenerator.uuidV1
    typeid <- TypeIDGenerator.generate("user")
    _      <- Console.printLine(typeid.value).orDie // e.g. user_01h455vb4pex5vsknk084sn02q
  } yield ()

program.provide(UUIDGenerator.live, TypeIDGenerator.live)
```

### TypeIDs

A [TypeID](https://github.com/jetify-com/typeid) is a UUIDv7 with a type prefix, encoded in
base32. `TypeID` is a plain case class of `prefix` and `uuid`; `value` renders the canonical string.

```scala
import java.util.UUID
import zio.uuid.TypeID

TypeID.decode("user_01h455vb4pex5vsknk084sn02q") // Validation[DecodeError, TypeID]
TypeID.build("user", UUID.fromString("..."))     // Validation[BuildError, TypeID]
```

Both return a zio-prelude `Validation`, so invalid prefixes and non-UUIDv7 inputs are rejected
rather than thrown. A prefix must be at most 63 lowercase ASCII characters.

A zio-json `JsonCodec[TypeID]` is provided. `zio-json` is an optional dependency — add it yourself
if you want the codec:

```scala
libraryDependencies += "dev.zio" %% "zio-json" % "1.0.0"
```

## ⚠️ The generators are stateful

Uniqueness and monotonicity are only guaranteed **per generator instance**. Each generator keeps its
state in a `Ref`, so providing the layer more than once gives you independent generators, and the
UUIDs they produce are no longer monotonically increasing relative to each other.

Do **not** do this — each `provideLayer` builds a fresh generator:

```scala
val id0 = UUIDGenerator.uuidV7.provideLayer(UUIDGenerator.live)
val id1 = UUIDGenerator.uuidV7.provideLayer(UUIDGenerator.live)
```

Do this instead — one generator, shared:

```scala
(
  for {
    id0 <- UUIDGenerator.uuidV7
    id1 <- UUIDGenerator.uuidV7
  } yield ()
).provideLayer(UUIDGenerator.live)
```

The safest approach is to provide `UUIDGenerator.live` once in your application's boot sequence, so
the same instance is reused everywhere.

## Contributing

Issues and pull requests are welcome at
[guizmaii-opensource/zio-uuid](https://github.com/guizmaii-opensource/zio-uuid).

## Credits

A fork of [uuid4cats-effect](https://github.com/ant8e/uuid4cats-effect)
by [Antoine Comte](https://github.com/ant8e).

## License

[Apache 2.0](LICENSE)

Copyright 2023-2026 Jules Ivanic and the zio-uuid contributors.
