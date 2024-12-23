package stamino
package testkit

import java.io.File

import org.scalatest._
import org.scalatest.events._
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.reflect.ClassTag

class ScalatestTestGenerationSpec extends StaminoTestKitSpec {

  import TestDomain._

  case class ItemPersister(override val key: String) extends Persister[Item, V2](key) {
    def persist(t: Item): Persisted = Persisted(key, currentVersion, ByteString())
    def unpersist(p: Persisted): Item = item1
  }

  "A spec generated by StaminoTestKit" when {
    "there is only version 1" should {
      val spec = new StaminoTestKit with AnyWordSpecLike {
        val persisters = Persisters(ItemPersister("item1"))
        "TestDomainSerialization" should {
          persisters.generateTestsFor(
            sample(item1),
            sample("failing-item-2", item2),
            sample("3-level-nested-structure", Level1.Level2.Level3),
          )
        }
      }

      "generate test for serialization roundtrips" in {
        spec.testNames should contain("TestDomainSerialization should persist and unpersist Item")
      }
      "generate test for deserialization of stored serialized items" in {
        spec.testNames should contain(
          "TestDomainSerialization should deserialize the stored serialized form of Item version 1",
        )
      }
      "executes test for successful serialization roundtrips" in {
        val myRep = execSpec(spec)
        val Some(res) =
          myRep.findResultEvent("TestDomainSerialization should persist and unpersist Item")
        res should be(anInstanceOf[TestSucceeded])

      }
      "executes test for unsuccessful serialization roundtrips" in {
        val myRep = execSpec(spec)
        val Some(res) = myRep.findResultEvent(
          "TestDomainSerialization should persist and unpersist Item failing-item-2",
        )
        res should be(anInstanceOf[TestFailed])
      }
      "executes test for successful for deserialization of stored serialized items" in {
        val myRep = execSpec(spec)
        val Some(res) = myRep.findResultEvent(
          "TestDomainSerialization should deserialize the stored serialized form of Item version 1",
        )
        res should be(anInstanceOf[TestSucceeded])
      }
      "executes test for unsuccessful for deserialization of stored serialized items" in {
        val myRep = execSpec(spec)
        val Some(res) = myRep.findResultEvent(
          "TestDomainSerialization should deserialize the stored serialized form of Item failing-item-2 version 1",
        )
        res should be(anInstanceOf[TestFailed])
      }

      "creates missing sample file in /tmp directory for the newest version" in {
        val myRep = execSpec(spec)
        myRep.testFailedEventsReceived
          .find(
            _.message.startsWith(
              "You appear to have added a new serialization sample to the stamino persisters' test",
            ),
          )
          .value
        new File(System.getProperty("java.io.tmpdir"), "/item1-v2-default") should exist
      }
    }

    "the sample is only suitable for version 2 and up" should {
      val spec = new StaminoTestKit with AnyWordSpecLike {
        val persisters = Persisters(ItemPersister("item2"))
        "TestDomainSerialization" should {
          persisters.generateTestsFor(sample(item2).from[V2])
        }
      }

      "execute test for missing version 2 sample data" in {
        val myRep = execSpec(spec)
        val Some(res) = myRep.findResultEvent(
          "TestDomainSerialization should deserialize the stored serialized form of Item version 2",
        )
        res should be(anInstanceOf[TestFailed])
      }

      "not execute test for version 1 sample data" in {
        spec.testNames shouldNot contain(
          "TestDomainSerialization should deserialize the stored serialized form of Item version 1",
        )
      }
    }

    "a sample is available for both version 1 and (the migrated event) version 2" should {
      val spec = new StaminoTestKit with AnyWordSpecLike {
        val persisters = Persisters(ItemPersister("item1"))
        "TestDomainSerialization" should {
          persisters.generateTestsFor(sample(item1))
        }
      }

      "generate tests for missing sample data for all versions" in {
        spec.testNames should contain(
          "TestDomainSerialization should deserialize the stored serialized form of Item version 1",
        )
        spec.testNames should contain(
          "TestDomainSerialization should deserialize the stored serialized form of Item version 2",
        )
      }
    }
  }

  def anInstanceOf[T: ClassTag] = {
    val clazz = implicitly[ClassTag[T]].runtimeClass
    new BePropertyMatcher[AnyRef] {
      def apply(left: AnyRef) = BePropertyMatchResult(
        left.getClass.isAssignableFrom(clazz),
        s"an instance of ${clazz.getName}",
      )
    }
  }

  private def execSpec(spec: AnyWordSpecLike): EventRecordingReporter = {
    val myRep = new EventRecordingReporter
    spec.run(
      None,
      Args(myRep, Stopper.default, Filter(), ConfigMap.empty, None, new Tracker, Set.empty),
    )
    myRep
  }

}
