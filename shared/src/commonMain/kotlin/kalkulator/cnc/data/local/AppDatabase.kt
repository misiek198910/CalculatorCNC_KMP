package kalkulator.cnc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.ConstructedBy
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kalkulator.cnc.data.dao.CncToolDao
import kalkulator.cnc.data.dao.SubscriptionDao
import kalkulator.cnc.data.dao.ThreadDao
import kalkulator.cnc.data.dao.UserToolDao
import kalkulator.cnc.entity.CncToolEntity
import kalkulator.cnc.entity.SubscriptionEntity
import kalkulator.cnc.entity.UserToolEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        CncToolEntity::class,
        UserToolEntity::class,
        SubscriptionEntity::class,
        kalkulator.cnc.entity.ThreadEntity::class
    ],
    version = 9
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cncToolDao(): CncToolDao
    abstract fun userToolDao(): UserToolDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun threadDao(): ThreadDao
}
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {

        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_tools` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workpiece` TEXT, `name` TEXT NOT NULL, `f` TEXT, `s` TEXT, `notes` TEXT)")

        try {
            connection.execSQL(
                """
                INSERT INTO `user_tools` (`id`, `name`, `f`, `s`, `workpiece`, `notes`)
                SELECT 
                    `id`, 
                    IFNULL(`name`, 'Brak nazwy'), 
                    `f`, 
                    `s`, 
                    `workpiece`, 
                    `notes` 
                FROM `tools`
                """.trimIndent()
            )
        } catch (e: Exception) {

        }

        connection.execSQL("DROP TABLE IF EXISTS `tools`")
        connection.execSQL("DROP TABLE IF EXISTS `subscription_status`")
        connection.execSQL("DROP TABLE IF EXISTS `threads`")
        connection.execSQL("DROP TABLE IF EXISTS `cnc_tools`")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `subscription_status` (`id` INTEGER NOT NULL, `isPremium` INTEGER NOT NULL, `purchaseToken` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `threads` (`id` INTEGER NOT NULL, `category_id` INTEGER NOT NULL, `table_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `pitch` TEXT NOT NULL, `hole_min` TEXT NOT NULL, `hole_max` TEXT NOT NULL, `hole_size` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cnc_tools` (`articleNumber` TEXT NOT NULL, `toolCategory` TEXT NOT NULL, `toolModelName` TEXT NOT NULL, `materialGroup` TEXT NOT NULL, `diameter` REAL NOT NULL, `flutesCount` INTEGER NOT NULL, `vcMin` REAL NOT NULL, `vcMax` REAL NOT NULL, `feedStep` REAL NOT NULL, `category_code` TEXT NOT NULL, PRIMARY KEY(`articleNumber`, `materialGroup`))")
    }
}

fun createRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .addMigrations(MIGRATION_8_9)
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}