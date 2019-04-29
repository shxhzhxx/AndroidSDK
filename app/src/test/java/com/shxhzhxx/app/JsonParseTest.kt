package com.shxhzhxx.app

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.shxhzhxx.sdk.network.CODE_UNEXPECTED_RESPONSE
import com.shxhzhxx.sdk.network.mapper
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

val raw = JSONObject()
        .put("errno", 0)
        .put("msg", "success")
        .put("data", JSONObject()
                .put("str", "str")
                .put("int", 10)
                .put("boolean", true)
                .put("double", 0.5)
                .put("nest",
                        JSONObject().put("name", "shxhzhxx").put("age", 15))
                .put("list",
                        JSONArray()
                                .put(JSONObject().put("name", "shxhzhxx").put("age", 15))
                                .put(JSONObject().put("name", "shxhzhxx").put("age", 15))
                                .put(JSONObject().put("name", "shxhzhxx")/*.put("age", 15)*/)
                )
        )
        .toString()

const val errnoFailure = 100
val failure1 = JSONObject()
        .put("errno", errnoFailure)
        .put("msg", "failure")
        .toString()
val failure2 = JSONObject()
        .put("errno", errnoFailure)
        .put("msg", "failure")
        .put("data", "")
        .toString()
val failure3 = JSONObject()
        .put("errno", errnoFailure)
        .put("msg", "failure")
        .put("data", JSONObject())
        .toString()
val failure4 = JSONObject()
        .put("errno", errnoFailure)
        .put("msg", "failure")
        .put("data", JSONArray())
        .toString()
val failure5 = JSONObject()
        .put("errno", errnoFailure)
        .put("msg", "failure")
        .put("data", JSONObject.NULL)
        .toString()


class JsonParseTest {
    @Test
    fun main() {
        wrapResolve<PrimaryTypes>(raw).apply {
            assert(str == "str" && int == 10 && boolean && double == 0.5)
        }
        wrapResolve<NullableString>(raw).apply {
            assert(nullableString == null)
        }
        wrapResolve<NullableInt>(raw).apply {
            assert(nullableInt == null)
        }
        wrapResolve<NullableBoolean>(raw).apply {
            assert(nullableBoolean == null)
        }
        try {
            wrapResolve<NullString>(raw)
            assert(false)
        } catch (e: ResolveException) {
            assert(e.errno == CODE_UNEXPECTED_RESPONSE)
        }
        try {
            wrapResolve<NullInt>(raw)
            assert(false)
        } catch (e: ResolveException) {
            assert(e.errno == CODE_UNEXPECTED_RESPONSE)
        }
        try {
            wrapResolve<NullBoolean>(raw)
            assert(false)
        } catch (e: ResolveException) {
            assert(e.errno == CODE_UNEXPECTED_RESPONSE)
        }

        listOf(failure1, failure2, failure3, failure4, failure5).forEach { failure ->
            try {
                wrapResolve<PrimaryTypes>(failure)
                assert(false)
            } catch (e: ResolveException) {
                assert(e.errno == errnoFailure)
            }
        }

        nestResolve<Student>(raw).apply {
            assert(name == "shxhzhxx" && age == 15)
        }

        try {
            wrapResolve<StudentSex>(raw)
            assert(false)
        } catch (e: ResolveException) {
            assert(e.errno == CODE_UNEXPECTED_RESPONSE)
        }

        nestResolve<StudentNullableSex>(raw).apply {
            assert(name == "shxhzhxx" && age == 15 && sex == null)
        }

        listResolve<Student>(raw).apply {
            assert(size == 3)
            forEach { System.out.println(it) }
        }
    }
}

class ResolveException(val errno: Int) : RuntimeException()
data class Wrapper<T>(
        val errno: Int,
        val msg: String,
        val data: T?
) {
    val isSuccessful get() = errno == 0
}

inline fun <reified T> listResolve(raw: String) =
        wrapResolve<List<T>>(raw, TypeFactory.defaultInstance().constructParametricType(List::class.java, T::class.java)).list
                ?: throw ResolveException(CODE_UNEXPECTED_RESPONSE)

inline fun <reified T> nestResolve(raw: String) = wrapResolve<Nest<T>>(raw, TypeFactory.defaultInstance().constructParametricType(Nest::class.java, T::class.java)).nest
        ?: throw ResolveException(CODE_UNEXPECTED_RESPONSE)

inline fun <reified T> wrapResolve(raw: String, type: JavaType = TypeFactory.defaultInstance().constructType(T::class.java)): T =
        resolve<Wrapper<T>>(raw, TypeFactory.defaultInstance().constructParametricType(Wrapper::class.java, type)).let {
            return@let when {
                !it.isSuccessful -> throw ResolveException(it.errno)
                it.data !is T -> throw ResolveException(CODE_UNEXPECTED_RESPONSE)
                else -> it.data
            }
        }

inline fun <reified T> resolve(raw: String, type: JavaType = TypeFactory.defaultInstance().constructType(T::class.java)): T = mapper.readValue(raw, type)


data class PrimaryTypes(
        val str: String,
        val int: Int,
        val boolean: Boolean,
        val double: Double
)

data class NullableString(val nullableString: String?)
data class NullableInt(val nullableInt: Int?)
data class NullableBoolean(val nullableBoolean: Boolean?)

data class NullString(val nullString: String)
data class NullInt(val nullInt: Int)
data class NullBoolean(val nullBoolean: Boolean)

data class Nest<T>(val nest: T?)
data class Student(val name: String, val age: Int)
data class StudentSex(val name: String, val sex: String, val age: Int)
data class StudentNullableSex(val name: String, val sex: String?, val age: Int)

data class List<T>(val list: kotlin.collections.List<T?>)
