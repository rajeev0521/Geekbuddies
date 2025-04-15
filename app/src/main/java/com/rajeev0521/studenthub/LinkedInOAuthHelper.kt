package com.rajeev0521.studenthub

import com.github.scribejava.apis.LinkedInApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.oauth.OAuth20Service

object LinkedInOAuthHelper {
    private const val CLIENT_ID = "86mg2vhweewaw8"
    private const val CLIENT_SECRET = "WPL_AP1.t7kciRvMtfnq7nAn.T7DQfg=="
    private const val REDIRECT_URI = "https://student_hub.com/linkedin-callback"
    private const val SCOPES = "r_liteprofile r_emailaddress"

    fun getService(): OAuth20Service {
        return ServiceBuilder(CLIENT_ID)
            .apiSecret(CLIENT_SECRET)
            .defaultScope(SCOPES)
            .callback(REDIRECT_URI)
            .build(LinkedInApi20.instance())
    }
}