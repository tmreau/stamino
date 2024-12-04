package stamino

class PersistersSpec extends StaminoSpec {
  import TestDomain._
  import TestOnlyPersister._

  val itemPersister = persister[Item]("item")
  val cartPersister = persister[Cart]("cart")
  val cartCreatedPersister = persister[CartCreated]("cart-created")

  val persisters = Persisters(itemPersister, cartPersister)

  "A non-empty instance of Persisters" should {
    import persisters._

    "correctly implement canPersist()" in {
      canPersist(item1) should be(true)
      canPersist(cart) should be(true)

      canPersist(cartCreated) should be(false)
      canPersist("Huh?") should be(false)
    }

    "correctly implement canUnpersist()" in {
      canUnpersist(itemPersister.persist(item1)) should be(true)
      canUnpersist(cartPersister.persist(cart)) should be(true)

      canUnpersist(cartCreatedPersister.persist(cartCreated)) should be(false)
      canUnpersist(Persisted("unknown", 1, ByteString("..."))) should be(false)
      canUnpersist(Persisted("item", 2, ByteString("..."))) should be(false)

      // works because canUnpersist only looks at the key and the version, not at the raw data
      canUnpersist(Persisted("item", 1, ByteString("Not an item at all!"))) should be(true)
    }

    "correctly implement persist() and unpersist()" in {
      unpersist(persist(item1)) should equal(item1)
      unpersist(persist(cart)) should equal(cart)
    }

    "throw an UnregisteredTypeException when persisting an unregistered type" in {
      a[UnregisteredTypeException] should be thrownBy persist("a raw String is not supported")
    }

    "throw an UnsupportedDataException when unpersisting data with an unknown key" in {
      an[UnsupportedDataException] should
        be thrownBy unpersist(Persisted("unknown", 1, ByteString("...")))
    }

    "throw an UnsupportedDataException when deserializing data with an unsupported version" in {
      an[UnsupportedDataException] should
        be thrownBy unpersist(Persisted("item", 2, ByteString("...")))
    }

    "throw an UnrecoverableDataException when an exception occurs while deserializing" in {
      an[UnrecoverableDataException] should
        be thrownBy unpersist(Persisted("item", 1, ByteString("not an item")))
    }
  }
}
