package vandyke.siamobile.data.local

import android.arch.persistence.room.Entity

@Entity(tableName = "files")
class File(path: String) : Node(path)