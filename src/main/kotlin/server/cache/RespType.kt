package server.cache

sealed class RespType {
    data class String(val string: kotlin.String): RespType()

    data class Int(val int: kotlin.Int): RespType()

    data class Boolean(val boolean: kotlin.Boolean): RespType()

    data class BigInteger(val bigInteger: java.math.BigInteger): RespType()

    data class Double(val double: kotlin.Double): RespType()

    data class ByteArray(val byteArray: kotlin.ByteArray): RespType()
    
    data class Array(val array: kotlin.Array<RespType>): RespType()
    
    data class Map(val map: kotlin.collections.Map<RespType, RespType>): RespType()
    
    data class Set(val set: kotlin.collections.Set<RespType>): RespType()

    data class Null(val foo: kotlin.String): RespType()

    data class Error(val message: kotlin.String): RespType()
}

enum class Type(val symbol: Char) {
    STRING('+'),
    INT(':'),
    BOOLEAN('#'),
    BIGINTEGER('('),
    DOUBLE(','),
    BYTEARRAY('$'),
    ARRAY('*'),
    MAP('%'),
    SET('~'),
    NULL('_'),
    ERROR('-')
}
