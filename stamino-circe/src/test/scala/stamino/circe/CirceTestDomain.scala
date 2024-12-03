package stamino.circe

import io.circe._
import io.circe.generic.semiauto._

object CirceTestDomain {
  case class ItemId(value: Int) extends AnyVal
  case class CartId(value: Int) extends AnyVal
  implicit val itemIdCodec: Codec[ItemId] = deriveCodec
  implicit val cartIdCodec: Codec[CartId] = deriveCodec

  private implicit def toItemId(int: Int): ItemId = ItemId(int)
  private implicit def toCartId(int: Int): CartId = CartId(int)

  // ==========================================================================
  // V1
  // ==========================================================================

  case class ItemV1(id: ItemId, name: String)

  object ItemV1 {
    implicit val codec: Codec[ItemV1] = deriveCodec
  }

  case class CartV1(id: CartId, items: List[ItemV1])

  object CartV1 {
    implicit val codec: Codec[CartV1] = deriveCodec
  }

  case class CartCreatedV1(cart: CartV1)

  object CartCreatedV1 {
    implicit val codec: Codec[CartCreatedV1] = deriveCodec
  }

  val v1Item1 = ItemV1(1, "Wonka Bar")
  val v1Item2 = ItemV1(2, "Everlasting Gobstopper")
  val v1Cart = CartV1(1, List(v1Item1, v1Item2))
  val v1CartCreated = CartCreatedV1(v1Cart)

  // ==========================================================================
  // V2
  // ==========================================================================

  case class ItemV2(id: ItemId, name: String, price: Int)

  object ItemV2 {
    implicit val codec: Codec[ItemV2] = deriveCodec
  }

  case class CartV2(id: CartId, items: List[ItemV2])

  object CartV2 {
    implicit val codec: Codec[CartV2] = deriveCodec
  }

  case class CartCreatedV2(cart: CartV2)

  object CartCreatedV2 {
    implicit val codec: Codec[CartCreatedV2] = deriveCodec
  }

  val v2Item1 = ItemV2(1, "Wonka Bar", 500)
  val v2Item2 = ItemV2(2, "Everlasting Gobstopper", 489)
  val v2Cart = CartV2(1, List(v2Item1, v2Item2))
  val v2CartCreated = CartCreatedV2(v2Cart)

  // ==========================================================================
  // V3
  // ==========================================================================

  case class ItemV3(id: ItemId, name: String, price: Int)

  object ItemV3 {
    implicit val codec: Codec[ItemV3] = deriveCodec
  }

  case class CartV3(id: CartId, items: List[ItemV3])

  object CartV3 {
    implicit val codec: Codec[CartV3] = deriveCodec
  }

  case class CartCreatedV3(cart: CartV3, timestamp: Long)

  object CartCreatedV3 {
    implicit val codec: Codec[CartCreatedV3] = deriveCodec
  }

  val v3Item1 = ItemV3(1, "Wonka Bar", 500)
  val v3Item2 = ItemV3(2, "Everlasting Gobstopper", 489)
  val v3Cart = CartV3(1, List(v3Item1, v3Item2))
  val v3CartCreated = CartCreatedV3(v3Cart, System.currentTimeMillis)
}
