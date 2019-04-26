package com.shxhzhxx.app

import com.shxhzhxx.sdk.network.*
import org.json.JSONObject
import org.junit.Test

class JsonParseTest {
    @Test
    fun main() {
        val raw1 = JSONObject()
                .put("errno", 0)
                .put("tips", "success")
                .put("data", JSONObject().put("data", JSONObject()
                        .put("serviceIMNumber", "kefuchannelimid_996035")
                        .put("xh_config_id", "kefuchannelimid_996035")
                        .put("hx_contact_group", "kefuchannelimid_996035")
                        .put("service_IM_number_for_teacher", "kefuchannelimid_996035")
                        .put("xh_config_id_for_teacher", "kefuchannelimid_996035")
                        .put("hx_contact_group_for_teacher", "kefuchannelimid_996035")
                )
                )
                .toString()
//        val ret1 = mapper.readValue<Response<Config>>(raw1, object : TypeReference<Response<Config>>() {}).data
//        val ret1 = resolve<Response<Config>>(raw1).data
        val ret1 = wwrapResolve<Config>(raw1)
        assert(ret1?.serviceIMNumber == "kefuchannelimid_996035")
    }
}


data class Config(
        val serviceIMNumber: String,
        val xh_config_id: String,
        val hx_contact_group: String,
        val service_IM_number_for_teacher: String,
        val xh_config_id_for_teacher: String,
        val hx_contact_group_for_teacher: String
)