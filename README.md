![License](https://img.shields.io/badge/license-MIT-blue.svg "License: MIT")

Stamino is based on [Stamina](https://github.com/scalapenos/stamina)

# Stamino

Stamino is a Pekko serialization toolkit written specifically for use with Pekko Persistence.
It has a strong focus on long-term viability of your persisted data so it provides support for **versioning** that data,
**auto-migrating** that data at read time to be compatible with your current event and domain classes, and a **testkit**
to make sure all older versions of your persisted data are still readable.

# Stamino in Detail
Stamino is a Pekko serialization toolkit written specifically for use with Pekko Persistence. Its main defining characteristics are:

- support for explicitly versioning serialized data. Stamino always stores a version number with the serialized data.
- support for explicitly migrating serialized data written for older versions to the latest version
- support for defining (de)serialization behavior in code. This includes (auto-)generating so-called *persisters* from Scala case classes and an API for specifying migration behaviors.
- decoupling from fully qualified class names (or randomly generated ids) as serialization keys. Instead, Stamino uses simple String keys to identify serialized types.
- support for multiple serialization libraries as implementation plugins, as long as they can be ported/adjusted/wrapped in order to support the above features.

The first implementation (powered by stamina) was based on spray-json.
Stamino adds support for circe using circe-optics.
It supports migration from older versions using a very simple little DSL to pre-process the JSON AST based on the specific version being read before deserialization takes place.
Here's an example using Circe:

```scala
// This example uses explicitly versioned case classes (i.e. the same
// domain class in three different versions with three different names)
// to more easily show how to deal with versions and migrations.
//
// Normally, of course, you would only need one case class,
// which would always represent the current version (V3 in this case).
import io.circe._
import io.circe.optics.JsonPath.root
import stamino.circe._

// For this example, we auto-generated json codecs from circe-generic
// This is not a requirement and you can use explicit codecs too.
import io.circe.generic.auto._

// circe persister for V1.
// Essentially equivalent to any existing Akka serializer except
// for the simple API used to specify/generate them.
val v1CartCreatedPersister = persister[CartCreatedV1]("cart-created")

// circe persister for V2 but with support for migration
// of data writen in the V1 format.
val v2CartCreatedPersister = persister[CartCreatedV2, V2](
  "cart-created",
  from[V1]
    .to[V2](root.cart.items.each.obj.modify(_.add("price", Json.fromInt(1000))))
)

// circe persister for V3 but with support for migration
// of data writen in the V1 and V2 formats.
val v3CartCreatedPersister = persister[CartCreatedV3, V3](
  "cart-created",
  from[V1]
    .to[V2](root.cart.items.each.obj.modify(_.add("price", Json.fromInt(1000))))
    .to[V3](root.obj.modify(_.add("timestamp", Json.fromLong(System.currentTimeMillis - 3600000L))))
)
```

For these persisters to be actually used by the Pekko serialization system, you will need to bundle them into a Pekko
serializer and then register that serializer for your classes. To make that registration process a little simpler,
Stamino comes with a marker trait called `Persistable`. Of course you can use your own marker traits instead.

```scala
class CartCreatedV3(...) extends Persistable
```

In the example below we create a subclass of `StaminoPekkoSerializer` and pass all our Persister instances into it. We
then register this serializer with Pekko in our application.conf and bind it to all instances/subclasses of the
`Persistable` marker trait.

```scala
class MakeEventSerializer extends StaminoPekkoSerializer(v3CartCreatedPersister, ...)
```

```
pekko.actor {
  serializers {
    make-serializer  = "org.make.MakeEventSerializer"
  }
  serialization-bindings {
    "stamino.Persistable" = make-serializer
  }
}
```