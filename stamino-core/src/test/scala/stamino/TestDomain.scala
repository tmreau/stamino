package stamino

object TestDomain {
  type ItemId = Int
  type CartId = Int

  case class Item(id: ItemId, name: String, fieldThatWasIntroducedInVersionTwo: String)
  case class Cart(id: CartId, items: List[Item])
  case class CartCreated(cart: Cart)

  val defaultValueForMigratedEvents = "Delicious!"
  val item1 = Item(1, "Wonka Bar", defaultValueForMigratedEvents)
  val item2 = Item(2, "Everlasting Gobstopper", "For children with very little pocket money")
  val cart = Cart(1, List(item1, item2))
  val cartCreated = CartCreated(cart)
}
