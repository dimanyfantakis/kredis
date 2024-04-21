import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.math.BigInteger
import server.cache
import server.cache.LRUCache
import server.cache.RespType
import server.cache.Type
import server.constructResponse
import server.executeCommand
import server.getBasicType

private const val OK = "+OK\\r\\n"

class ServerTest : BehaviorSpec ({



     Given("A get request") {
//         val cache = LRUCache
         val key = RespType.String(string = "foo")
         cache[key] = RespType.String(string = "bar")
         When("We execute the command") {
             And("The key exists in the database") {
                 val request = "GET +foo\\r\\n"
                 val response = executeCommand(request.split(" "))
                 Then("We should get back the correct value") {
                     response shouldBe "+bar\\r\\n"
                 }
             }
             And("The key doesn't exist in the database") {
                 val request = "GET +not foo\\r\\n"
                 val response = executeCommand(request.split(" "))
                 Then("The response from the server should be null") {
                     response shouldBe "_\\r\\n"
                 }
             }
         }
     }

     Given("A RespType object") {
//        val cache = LRUCache
        When("We get the value from the database") {
            And("We construct the response of the server") {
                Then("The response should follow the Redis protocol") {
                    val key = RespType.String(string = "foo")
                    cache[key] = RespType.String(string = "bar")
                    val response = constructResponse(key)
                    response shouldBe "+bar"
                }
            }
        }
    }

     Given("A key of the database and a Type") {
         When("We call getBasicType with that Type and the key") {
             And("The Type is a String") {
                 val type = Type.STRING
                 val key = "foo"
                 val s = getBasicType(type, key)
                 Then("We should return a String RespType with the given key") {
                     s.shouldBeInstanceOf<RespType.String>()
                     s.string shouldBe key
                 }
             }
             And("The Type is a Int") {
                 val type = Type.INT
                 val key = "1"
                 val i = getBasicType(type, key)
                 Then("We should return a String RespType with the given key") {
                     i.shouldBeInstanceOf<RespType.Int>()
                     i.int shouldBe 1
                 }
             }
             And("The Type is a Boolean") {
                 val type = Type.BOOLEAN
                 val key = "true"
                 val b = getBasicType(type, key)
                 Then("We should return a String RespType with the given key") {
                     b.shouldBeInstanceOf<RespType.Boolean>()
                     b.boolean shouldBe true
                 }
             }
             And("The Type is a BigInteger") {
                 val type = Type.BIGINTEGER
                 val key = "123456789"
                 val b = getBasicType(type, key)
                 Then("We should return a String RespType with the given key") {
                     b.shouldBeInstanceOf<RespType.BigInteger>()
                     b.bigInteger shouldBe BigInteger.valueOf(123456789)
                 }
             }
         }
     }

     Given("A set request") {
         When("We execute the command") {
             And("The key doesn't exist in the database") {
                 val request = "SET +foo +bar\\r\\n"
                 val response = executeCommand(request.split(" "))
                 Then("We should get back an OK value") {
                     response shouldBe OK
                 }

                 Then("foo") {
                     cache.size shouldBe 1
                 }
             }
             And("The key exists in the database") {
                 val request = "SET +foo baz\\r\\n"
                 val response = executeCommand(request.split(" "))
                 Then("We should get back an OK value") {
                     response shouldBe OK
                 }
             }
         }
     }
})
