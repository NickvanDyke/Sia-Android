package vandyke.siamobile.backend.models

data class SeedsModel(val primaryseed: String = "",
                      val addressesremaining: Int = 0,
                      val allseeds: ArrayList<String> = ArrayList())