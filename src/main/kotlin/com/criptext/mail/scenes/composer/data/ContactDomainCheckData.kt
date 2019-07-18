package com.criptext.mail.scenes.composer.data

import com.criptext.mail.db.models.Contact
import org.json.JSONArray

data class ContactDomainCheckData(val name: String, val isCriptextDomain: Boolean){
    companion object {
        val KNOWN_EXTERNAL_DOMAINS = listOf(
                /* Criptext main domain */
                ContactDomainCheckData(Contact.mainDomain, true),
                /* Default domains included */
                ContactDomainCheckData("aol.com", false),
                ContactDomainCheckData("att.net", false),
                ContactDomainCheckData("comcast.net", false),
                ContactDomainCheckData("facebook.com", false),
                ContactDomainCheckData("gmail.com", false),
                ContactDomainCheckData("gmx.com", false),
                ContactDomainCheckData("googlemail.com", false),
                ContactDomainCheckData("google.com", false),
                ContactDomainCheckData("hotmail.com", false),
                ContactDomainCheckData("hotmail.co.uk", false),
                ContactDomainCheckData("mac.com", false),
                ContactDomainCheckData("me.com", false),
                ContactDomainCheckData("mail.com", false),
                ContactDomainCheckData("msn.com", false),
                ContactDomainCheckData("live.com", false),
                ContactDomainCheckData("sbcglobal.net", false),
                ContactDomainCheckData("verizon.net", false),
                ContactDomainCheckData("yahoo.com", false),
                ContactDomainCheckData("yahoo.co.uk", false),

                /* Other global domains */
                ContactDomainCheckData("email.com", false),
                ContactDomainCheckData("fastmail.fm", false),
                ContactDomainCheckData("games.com", false),
                ContactDomainCheckData("gmx.net", false),
                ContactDomainCheckData("hush.com", false),
                ContactDomainCheckData("hushmail.com", false),
                ContactDomainCheckData("icloud.com", false),
                ContactDomainCheckData("iname.com", false),
                ContactDomainCheckData("inbox.com", false),
                ContactDomainCheckData("lavabit.com", false),
                ContactDomainCheckData("love.com", false),
                ContactDomainCheckData("outlook.com", false),
                ContactDomainCheckData("pobox.com", false),
                ContactDomainCheckData("protonmail.ch", false),
                ContactDomainCheckData("protonmail.com", false),
                ContactDomainCheckData("tutanota.de", false),
                ContactDomainCheckData("tutanota.com", false),
                ContactDomainCheckData("tutamail.com", false),
                ContactDomainCheckData("tuta.io", false),
                ContactDomainCheckData("keemail.me", false),
                ContactDomainCheckData("rocketmail.com", false),
                ContactDomainCheckData("safe-mail.net", false),
                ContactDomainCheckData("wow.com", false),
                ContactDomainCheckData("ygm.com", false),
                ContactDomainCheckData("ymail.com", false),
                ContactDomainCheckData("zoho.com", false),
                ContactDomainCheckData("yandex.com", false),

                /* United States ISP domains */
                ContactDomainCheckData("bellsouth.net", false),
                ContactDomainCheckData("charter.net", false),
                ContactDomainCheckData("cox.net", false),
                ContactDomainCheckData("earthlink.net", false),
                ContactDomainCheckData("juno.com", false),

                /* British ISP domains */
                ContactDomainCheckData("btinternet.com", false),
                ContactDomainCheckData("virginmedia.com", false),
                ContactDomainCheckData("blueyonder.co.uk", false),
                ContactDomainCheckData("freeserve.co.uk", false),
                ContactDomainCheckData("live.co.uk", false),
                ContactDomainCheckData("ntlworld.com", false),
                ContactDomainCheckData("o2.co.uk", false),
                ContactDomainCheckData("orange.net", false),
                ContactDomainCheckData("sky.com", false),
                ContactDomainCheckData("talktalk.co.uk", false),
                ContactDomainCheckData("tiscali.co.uk", false),
                ContactDomainCheckData("virgin.net", false),
                ContactDomainCheckData("wanadoo.co.uk", false),
                ContactDomainCheckData("bt.com", false),

                /* Domains used in Asia */
                ContactDomainCheckData("sina.com", false),
                ContactDomainCheckData("sina.cn", false),
                ContactDomainCheckData("qq.com", false),
                ContactDomainCheckData("naver.com", false),
                ContactDomainCheckData("hanmail.net", false),
                ContactDomainCheckData("daum.net", false),
                ContactDomainCheckData("nate.com", false),
                ContactDomainCheckData("yahoo.co.jp", false),
                ContactDomainCheckData("yahoo.co.kr", false),
                ContactDomainCheckData("yahoo.co.id", false),
                ContactDomainCheckData("yahoo.co.in", false),
                ContactDomainCheckData("yahoo.com.sg", false),
                ContactDomainCheckData("yahoo.com.ph", false),
                ContactDomainCheckData("163.com", false),
                ContactDomainCheckData("yeah.net", false),
                ContactDomainCheckData("126.com", false),
                ContactDomainCheckData("21cn.com", false),
                ContactDomainCheckData("aliyun.com", false),
                ContactDomainCheckData("foxmail.com", false),

                /* French ISP domains */
                ContactDomainCheckData("hotmail.fr", false),
                ContactDomainCheckData("live.fr", false),
                ContactDomainCheckData("laposte.net", false),
                ContactDomainCheckData("yahoo.fr", false),
                ContactDomainCheckData("wanadoo.fr", false),
                ContactDomainCheckData("orange.fr", false),
                ContactDomainCheckData("gmx.fr", false),
                ContactDomainCheckData("sfr.fr", false),
                ContactDomainCheckData("neuf.fr", false),
                ContactDomainCheckData("free.fr", false),

                /* German ISP domains */
                ContactDomainCheckData("gmx.de", false),
                ContactDomainCheckData("hotmail.de", false),
                ContactDomainCheckData("live.de", false),
                ContactDomainCheckData("online.de", false),
                ContactDomainCheckData("t-online.de", false),
                ContactDomainCheckData("web.de", false),
                ContactDomainCheckData("yahoo.de", false),

                /* Italian ISP domains */
                ContactDomainCheckData("libero.it", false),
                ContactDomainCheckData("virgilio.it", false),
                ContactDomainCheckData("hotmail.it", false),
                ContactDomainCheckData("aol.it", false),
                ContactDomainCheckData("tiscali.it", false),
                ContactDomainCheckData("alice.it", false),
                ContactDomainCheckData("live.it", false),
                ContactDomainCheckData("yahoo.it", false),
                ContactDomainCheckData("email.it", false),
                ContactDomainCheckData("tin.it", false),
                ContactDomainCheckData("poste.it", false),
                ContactDomainCheckData("teletu.it", false),

                /* Russian ISP domains */
                ContactDomainCheckData("mail.ru", false),
                ContactDomainCheckData("rambler.ru", false),
                ContactDomainCheckData("yandex.ru", false),
                ContactDomainCheckData("ya.ru", false),
                ContactDomainCheckData("list.ru", false),

                /* Belgian ISP domains */
                ContactDomainCheckData("hotmail.be", false),
                ContactDomainCheckData("live.be", false),
                ContactDomainCheckData("skynet.be", false),
                ContactDomainCheckData("voo.be", false),
                ContactDomainCheckData("tvcablenet.be", false),
                ContactDomainCheckData("telenet.be", false),

                /* Argentinian ISP domains */
                ContactDomainCheckData("hotmail.com.ar", false),
                ContactDomainCheckData("live.com.ar", false),
                ContactDomainCheckData("yahoo.com.ar", false),
                ContactDomainCheckData("fibertel.com.ar", false),
                ContactDomainCheckData("speedy.com.ar", false),
                ContactDomainCheckData("arnet.com.ar", false),

                /* Domains used in Mexico */
                ContactDomainCheckData("yahoo.com.mx", false),
                ContactDomainCheckData("live.com.mx", false),
                ContactDomainCheckData("hotmail.es", false),
                ContactDomainCheckData("hotmail.com.mx", false),
                ContactDomainCheckData("prodigy.net.mx", false),

                /* Domains used in Brazil */
                ContactDomainCheckData("yahoo.com.br", false),
                ContactDomainCheckData("hotmail.com.br", false),
                ContactDomainCheckData("outlook.com.br", false),
                ContactDomainCheckData("uol.com.br", false),
                ContactDomainCheckData("bol.com.br", false),
                ContactDomainCheckData("terra.com.br", false),
                ContactDomainCheckData("ig.com.br", false),
                ContactDomainCheckData("itelefonica.com.br", false),
                ContactDomainCheckData("r7.com", false),
                ContactDomainCheckData("zipmail.com.br", false),
                ContactDomainCheckData("globo.com", false),
                ContactDomainCheckData("globomail.com", false),
                ContactDomainCheckData("oi.com.br", false)
        )

        fun fromJSON(jsonString: String): List<ContactDomainCheckData> {
            val array = JSONArray(jsonString)
            val checkDataList = mutableListOf<ContactDomainCheckData>()
            for(i in 0 until array.length()){
                val json = array.getJSONObject(i)
                checkDataList.add(
                        ContactDomainCheckData(
                                name = json.getString("name"),
                                isCriptextDomain = json.getBoolean("isCriptextDomain")
                        )
                )
            }
            return checkDataList
        }
    }
}