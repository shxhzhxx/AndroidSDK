package com.shxhzhxx.app

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.modules.serializersModuleOf
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

/**
 * 理想的解析过程：
 *      是否希望直接返回String----> 是，直接返回
 *                                  否，是否符合标准JSON格式---->否，报错
 *                                                               是，开始解析------->失败，报错
 *                                                                                   成功，返回结果
 *
 * kotlinx.serialization 已知的问题：
 * 1、不支持别名，服务器返回格式不统一的情况下，用起来很困难
 * 2、KSerializer内不携带类型信息，无法提前判断泛型类型，返回非json格式的字符串。
 * 3、嵌套在内层的自定义解析需要在模型里声明。 @Serializable(with=MyJavaDateSerializer::class)
 * */

@Serializer(forClass = JSONObject::class)
object MyJSONSerializer : KSerializer<JSONObject> {
    override val descriptor: SerialDescriptor
        get() = StringDescriptor.withName("MyJSONSerializer")

    override fun deserialize(decoder: Decoder): JSONObject {
        return JSONObject((decoder as JsonInput).decodeJson().toString())
    }
}

@Serializer(forClass = String::class)
object MyStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor
        get() = StringDescriptor.withName("MyStringSerializer")

    override fun deserialize(decoder: Decoder): String {
        println("MyStringSerializer")
        return (decoder as JsonInput).decodeJson().toString()
    }
}

class JsonDeserializeTest {
    private val jsonDeserializer = Json(JsonConfiguration.Stable.copy(strictMode = false), context = serializersModuleOf(String::class, MyStringSerializer))
    private fun <T> deserialize(json: String, serializer: KSerializer<T>): T {
        return jsonDeserializer.parse(serializer, json)
    }

    @Test
    fun test() {
        deserialize(JSONObject()
                .put("str", "str")
                .put("int", 1)
                .toString(), Model.serializer()).apply {
            assert(str == "str")
            assert(int == 1)
            assert(plus10 == 11)
            assert(plus20 == 21)
        }
        deserialize(JSONObject()
                .put("str", "str")
                .toString(), Model.serializer()).apply {
            assert(str == "str")
            assert(int == 5)
        }
        deserialize(JSONObject()
                .put("str", "str1")
                .put("data", JSONObject()
                        .put("str", "str")
                        .put("int", 15)
                ).put("list", JSONArray().put("a").put("b").put("c"))
                .toString(), Wrapper.serializer(MyStringSerializer, String.serializer())).apply {
            println(data)
            println(str)
            assert(str == "str1")
//            assert(data.str == "str")
//            assert(data.int == 15)
            assert(list.size == 3)
            assert(list[0] == "a")
            assert(list[1] == "b")
            assert(list[2] == "c")
        }

        deserialize("{a:1}", MyJSONSerializer).apply {
            print(this)
        }
    }
}


@Serializable
data class Model(val str: String, val int: Int = 5) {
    val plus10 = int + 10
    val plus20 get() = int + 20
}

@Serializable
data class Wrapper<T, V>(val str: String, val data: T, val list: List<V>)