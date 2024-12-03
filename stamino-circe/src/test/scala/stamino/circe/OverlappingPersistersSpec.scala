package stamino
package circe

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

class OverlappingCircePersisterSpec extends StaminoCirceSpec {
  import OverlappingCircePersisterSpecDomain._

  implicit val payload1Codec: Codec[Payload1] = deriveCodec
  implicit val payload2Codec: Codec[Payload2] = deriveCodec

  implicit val eventPayload1Codec: Codec[Event[Payload1]] = deriveCodec
  implicit val eventPayload2Codec: Codec[Event[Payload2]] = deriveCodec

  "The persisters construction DSL" should {

    /** #43 In the future we might want to support this situation instead of failing at
      * initialization time
      */
    "correctly handle overlapping persisters" in {
      val e = intercept[IllegalArgumentException] {
        Persisters(persister[Event[Payload1]]("payload1"), persister[Event[Payload2]]("payload2"))
      }
      e.getMessage() should be(
        "requirement failed: Overlapping persisters: Persisters with keys 'payload1', 'payload2' all persist class stamino.circe.OverlappingCircePersisterSpecDomain$Event.",
      )

      /** When we actually want to support this situation, then this should work:
        *
        * val event1 = Event(Payload1("abcd")) persisters.unpersist(persisters.persist(event1))
        * should equal(event1) val event2 = Event(Payload2(42))
        * persisters.unpersist(persisters.persist(event2)) should equal(event2)
        */
    }
  }
}

object OverlappingCircePersisterSpecDomain {
  case class Event[P](payload: P)
  case class Payload1(msg: String)
  case class Payload2(value: Int)
}
