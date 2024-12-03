package stamino

import scala.reflect.ClassTag
import io.circe._
import migrations._
import java.nio.charset.StandardCharsets.UTF_8

package object circe {
  type CirceMigration = Migration[Json]
  type CirceMigrator[V <: Version] = Migrator[Json, V]

  def from[V <: V1: VersionInfo]: CirceMigrator[V] = migrations.from[Json, V]
  def persister[T: Codec: ClassTag](key: String): CircePersister[T, V1] =
    new V1CircePersister[T](key)

  def persister[T: Codec: ClassTag, V <: Version: VersionInfo: MigratableVersion](
    key: String,
    migrator: CirceMigrator[V],
  ): CircePersister[T, V] = new VnCircePersister[T, V](key, migrator)

  private[circe] def toCirceBytes[T](t: T)(implicit encoder: Encoder[T]): ByteString =
    ByteString(encoder(t).noSpaces)
  private[circe] def fromCirceBytes[T](bytes: ByteString)(implicit decoder: Decoder[T]): T =
    io.circe.parser.decode[T](new String(bytes.toArray, UTF_8)).fold(throw _, identity)
  private[circe] def parseCirce(bytes: ByteString): Json =
    io.circe.parser.parse(new String(bytes.toArray)).fold(throw _, identity)
}

package circe {
  sealed abstract class CircePersister[T: Codec: ClassTag, V <: Version: VersionInfo](key: String)
      extends Persister[T, V](key) {
    private[circe] def cannotUnpersist(p: Persisted) =
      s"""CircePersister[${implicitly[
          ClassTag[T],
        ].runtimeClass.getSimpleName}, V$currentVersion](key = "$key") cannot unpersist data with key "${p.key}" and version ${p.version}."""
  }

  private[circe] class V1CircePersister[T: Codec: ClassTag](key: String)
      extends CircePersister[T, V1](key) {
    def persist(t: T): Persisted = Persisted(key, currentVersion, toCirceBytes(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) fromCirceBytes[T](p.bytes)
      else throw new IllegalArgumentException(cannotUnpersist(p))
    }
  }

  private[circe] class VnCircePersister[
    T: Codec: ClassTag,
    V <: Version: VersionInfo: MigratableVersion,
  ](key: String, migrator: CirceMigrator[V])
      extends CircePersister[T, V](key) {
    override def canUnpersist(p: Persisted): Boolean =
      p.key == key && migrator.canMigrate(p.version)

    def persist(t: T): Persisted = Persisted(key, currentVersion, toCirceBytes(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p))
        migrator.migrate(parseCirce(p.bytes), p.version).as[T].fold(throw _, identity)
      else throw new IllegalArgumentException(cannotUnpersist(p))
    }
  }
}
