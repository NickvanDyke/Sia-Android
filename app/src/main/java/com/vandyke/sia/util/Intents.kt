package com.vandyke.sia.util

import android.content.Intent
import android.net.Uri

object Intents {
    val emailMe = Intent(Intent.ACTION_SENDTO)
            .setType("text/plain")
            .setData(Uri.parse("mailto:siamobiledev@gmail.com"))!!

    val playStore = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.vandyke.sia"))
            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)!!
}