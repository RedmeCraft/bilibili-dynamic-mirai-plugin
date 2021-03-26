package top.colter.mirai.plugin

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.delay
import top.colter.mirai.plugin.utils.httpGet
import top.colter.mirai.plugin.PluginConfig.BPI
import top.colter.mirai.plugin.bean.User
import top.colter.mirai.plugin.utils.generateImg

suspend fun init(){
    PluginMain.logger.info("初始化数据中...")
    PluginData.userData.forEach { user ->
        delay(2000)
        val rawDynamic = httpGet(BPI["dynamic"]+user.uid ,BPI["COOKIE"]!!).getJSONObject("data").getJSONArray("cards")

        val raw0 = rawDynamic.getJSONObject(0)
        val desc = raw0.getJSONObject("desc")
        user.dynamicId = desc.getBigInteger("dynamic_id").toString()
        user.liveStatus =
            try {
                raw0.getJSONObject("display").getJSONObject("live_info").getInteger("live_status")
            }catch (e:Exception){
                0
            }

        rawDynamic.forEach { item ->
            val dy = JSON.parseObject(item.toString())
            val desc = dy.getJSONObject("desc")
            PluginMain.historyDynamic.add(desc.getBigInteger("dynamic_id").toString())
        }
    }
    PluginMain.logger.info("初始化结束")
}

suspend fun initFollowInfo(uid:String, user: User, hex: String): String? {
    delay(1000)
    val res = httpGet(BPI["dynamic"]+uid,BPI["COOKIE"]!!).getJSONObject("data").getJSONArray("cards").getJSONObject(0)
    val userProfile = res.getJSONObject("desc").getJSONObject("user_profile")
    val name = userProfile.getJSONObject("info").getString("uname")
//    val user : User = User()
    user.uid = uid
    user.name = name
    user.dynamicId = res.getJSONObject("desc").getBigInteger("dynamic_id").toString()
    try {
        user.liveStatus = res.getJSONObject("display").getJSONObject("live_info").getInteger("live_status")
    }catch (e:Exception){
        user.liveStatus = 0
    }

    val face = userProfile.getJSONObject("info").getString("face")
    val pendant = userProfile.getJSONObject("pendant").getString("image")

    delay(1000)
    val liveRoom = httpGet(BPI["liveRoom"]+uid,BPI["COOKIE"]!!).getJSONObject("data").getBigInteger("roomid").toString()
    user.liveRoom = liveRoom

    return generateImg(uid,name,face,pendant,hex)
}