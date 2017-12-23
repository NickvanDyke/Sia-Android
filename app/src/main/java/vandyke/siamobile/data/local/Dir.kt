package vandyke.siamobile.data.local

import android.arch.persistence.room.Entity

@Entity(tableName = "dirs")
class Dir(path: String) : Node(path)