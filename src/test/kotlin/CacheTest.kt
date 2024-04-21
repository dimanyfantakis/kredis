import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import server.cache.LRUCache
import server.cache.RespType

class CacheTest : BehaviorSpec({
    Given("An empty cache") {
        val cache = LRUCache
        Then("The head and the tail should not be pointing to a value") {
            cache.size shouldBe 0

            val (headKey, headValue) = cache.getHead()
            headKey shouldBe null
            headValue shouldBe null

            val (tailKey, tailValue) = cache.getTail()
            tailKey shouldBe null
            tailValue shouldBe null
        }

        When("We add a value to the cache") {
            cache[RespType.String("foo")] = RespType.String("bar")

            Then("The head should point to the newly added value") {
                cache.size shouldBe 1

                val (headKey, headValue) = cache.getHead()
                headKey.shouldBeInstanceOf<RespType.String>()
                headValue.shouldBeInstanceOf<RespType.String>()

                headKey.string shouldBe "foo"
                headValue.string shouldBe "bar"
            }
            Then("The tail should point to the newly added value") {
                val (tailKey, tailValue) = cache.getTail()
                tailKey.shouldBeInstanceOf<RespType.String>()
                tailValue.shouldBeInstanceOf<RespType.String>()

                tailKey.string shouldBe "foo"
                tailValue.string shouldBe "bar"
            }
        }

        When("We add a new value to the cache") {
            cache[RespType.String("foobar")] = RespType.String("bar")

            Then("The head should point to the newly added value") {
                cache.size shouldBe 2

                val (headKey, headValue) = cache.getHead()
                headKey.shouldBeInstanceOf<RespType.String>()
                headValue.shouldBeInstanceOf<RespType.String>()

                headKey.string shouldBe "foobar"
                headValue.string shouldBe "bar"
            }
            Then("The tail should point to the first value") {
                val (tailKey, tailValue) = cache.getTail()
                tailKey.shouldBeInstanceOf<RespType.String>()
                tailValue.shouldBeInstanceOf<RespType.String>()

                tailKey.string shouldBe "foo"
                tailValue.string shouldBe "bar"
            }
        }

        When("We add a value with an existing key") {
            cache[RespType.String("foo")] = RespType.String("baz")

            Then("The head should point to the updated value") {
                cache.size shouldBe 2

                val (headKey, headValue) = cache.getHead()
                headKey.shouldBeInstanceOf<RespType.String>()
                headValue.shouldBeInstanceOf<RespType.String>()

                headKey.string shouldBe "foo"
                headValue.string shouldBe "baz"
            }
            Then("The tail should point to the previous value") {
                val (tailKey, tailValue) = cache.getTail()
                tailKey.shouldBeInstanceOf<RespType.String>()
                tailValue.shouldBeInstanceOf<RespType.String>()

                tailKey.string shouldBe "foobar"
                tailValue.string shouldBe "bar"
            }
        }
    }
})