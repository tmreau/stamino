package stamino

import scala.reflect._
import scala.util._

/** A Persister[T, V] provides a type-safe API for persisting instances of T at version V and
  * unpersisting persisted instances of T for all versions up to and including version V.
  */
abstract class Persister[T: ClassTag, V <: Version: VersionInfo](val key: String) {
  lazy val currentVersion = Version.numberFor[V]

  def persist(t: T): Persisted
  def unpersist(persisted: Persisted): T

  def canPersist(a: AnyRef): Boolean = convertToT(a).isDefined
  def canUnpersist(p: Persisted): Boolean = p.key == key && p.version <= currentVersion

  private[stamino] def convertToT(any: AnyRef): Option[T] = any match {
    case t: T => Some(t)
    case _    => None
  }

  private[stamino] def persistAny(any: AnyRef): Persisted = {
    convertToT(any)
      .map(persist(_))
      .getOrElse(
        throw new IllegalArgumentException(
          s"persistAny() was called on Persister[${implicitly[ClassTag[T]].runtimeClass}] with an instance of ${any.getClass}.",
        ),
      )
  }

  private[stamino] def unpersistAny(persisted: Persisted): AnyRef = {
    Try(unpersist(persisted).asInstanceOf[AnyRef]) match {
      case Success(anyref) => anyref
      case Failure(error)  => throw UnrecoverableDataException(persisted, error)
    }
  }

  private[stamino] val tag = classTag[T]
}
