package stamino
package circe

import io.circe._
import io.circe.optics.JsonPath.root

class CircePersisterSpec extends StaminoCirceSpec {
  import CirceTestDomain._

  val v1CartCreatedPersister: CircePersister[CartCreatedV1, V1] =
    persister[CartCreatedV1]("cart-created")

  val v2CartCreatedPersister: CircePersister[CartCreatedV2, V2] =
    persister[CartCreatedV2, V2](
      "cart-created",
      from[V1].to[V2](root.cart.items.each.obj.modify(_.add("price", Json.fromInt(1000)))),
    )

  val v3CartCreatedPersister: CircePersister[CartCreatedV3, V3] =
    persister[CartCreatedV3, V3](
      "cart-created",
      from[V1]
        .to[V2](root.cart.items.each.obj.modify(_.add("price", Json.fromInt(1000))))
        .to[V3](
          root.obj.modify(_.add("timestamp", Json.fromLong(System.currentTimeMillis - 3600000L))),
        ),
    )

  "V1 persisters produced by SprayCircePersister" should {
    "correctly persist and unpersist domain events " in {
      import v1CartCreatedPersister._
      unpersist(persist(v1CartCreated)) should equal(v1CartCreated)
    }
  }

  "V2 persisters with migrators produced by SprayCircePersister" should {
    "correctly persist and unpersist domain events " in {
      import v2CartCreatedPersister._
      unpersist(persist(v2CartCreated)) should equal(v2CartCreated)
    }

    "correctly migrate and unpersist V1 domain events" in {
      val v1Persisted = v1CartCreatedPersister.persist(v1CartCreated)
      val v2Unpersisted = v2CartCreatedPersister.unpersist(v1Persisted)

      v2Unpersisted.cart.items.map(_.price).toSet should equal(Set(1000))
    }
  }

  "V3 persisters with migrators produced by SprayCircePersister" should {
    "correctly persist and unpersist domain events " in {
      import v3CartCreatedPersister._
      unpersist(persist(v3CartCreated)) should equal(v3CartCreated)
    }

    "correctly migrate and unpersist V1 domain events" in {
      val v1Persisted = v1CartCreatedPersister.persist(v1CartCreated)
      val v2Persisted = v2CartCreatedPersister.persist(v2CartCreated)

      val v1Unpersisted = v3CartCreatedPersister.unpersist(v1Persisted)
      val v2Unpersisted = v3CartCreatedPersister.unpersist(v2Persisted)

      v1Unpersisted.cart.items.map(_.price).toSet should equal(Set(1000))
      v2Unpersisted.timestamp should ((be > 0L).and(be < System.currentTimeMillis))
    }
  }
}
