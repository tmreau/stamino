package stamino

import scala.reflect._
import org.apache.pekko.actor._
import org.apache.pekko.serialization._

object TestOnlyPersister {
  private val system = ActorSystem("TestOnlyPersister")
  private val javaSerializer = new JavaSerializer(system.asInstanceOf[ExtendedActorSystem])
  import javaSerializer._

  def persister[T <: AnyRef: ClassTag](key: String): Persister[T, V1] = new JavaPersister[T](key)

  private class JavaPersister[T <: AnyRef: ClassTag](key: String) extends Persister[T, V1](key) {
    def persist(t: T): Persisted = Persisted(key, currentVersion, toBinary(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) fromBinary(p.bytes.toArray).asInstanceOf[T]
      else throw new IllegalArgumentException("")
    }
  }
}
