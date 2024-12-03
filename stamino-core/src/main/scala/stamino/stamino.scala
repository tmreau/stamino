package object stamino {

  /** Type alias for Pekko's ByteSttring so we don't have to import it everywhere. */
  type ByteString = org.apache.pekko.util.ByteString
  val ByteString = org.apache.pekko.util.ByteString
}

/** */
package stamino {

  /** Marker trait for classes that should be persisted using the StaminaPekkoSerializer.
    *
    * This marker trait can be used to mark all your top-level persistable classes (i.e. events,
    * snapshots, etc.) so that you will only need a few lines of configuration in your
    * application.conf, namely:
    * {{{
    * pekko.actor.serializers.stamina = <FQCN of your subclass of StaminaPekkoSerializer>
    * pekko.actor.serialization-bindings {
    *   "stamina.Persistable" = stamina
    * }
    * }}}
    */
  trait Persistable extends java.io.Serializable

  import scala.util.control._

  case class UnregisteredTypeException(obj: AnyRef)
      extends RuntimeException(s"No persister registered for class: ${obj.getClass}")
      with NoStackTrace

  case class UnsupportedDataException(persisted: Persisted)
      extends RuntimeException(
        s"No unpersister registered for key: '${persisted.key}' and version: ${persisted.version}",
      )
      with NoStackTrace

  case class UnrecoverableDataException(persisted: Persisted, error: Throwable)
      extends RuntimeException(
        s"Error while trying to unpersist data with key '${persisted.key}' and version ${persisted.version}. Cause: ${error}",
      )
      with NoStackTrace
}
