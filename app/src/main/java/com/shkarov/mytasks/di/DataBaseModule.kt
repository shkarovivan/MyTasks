package com.shkarov.mytasks.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shkarov.mytasks.data_base.TaskDataBaseContract
import com.shkarov.mytasks.data_base.TasksDataBase
import com.shkarov.mytasks.data_base.TasksDbDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataBaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): TasksDataBase {
        val db = Room.databaseBuilder(
            context,
            TasksDataBase::class.java,
            TasksDataBase.DB_NAME
        )
            // Schemas v1 and v2 are structurally identical, so the migration is a no-op,
            // but without it the app crashes on update for existing users.
            .addMigrations(object : Migration(1, 2) {})
            // v3: sync fields; NOT NULL columns need defaults for existing rows,
            // and the defaultValue must match the @ColumnInfo of the entity.
            .addMigrations(object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE ${TasksDataBase.DB_NAME} " +
                            "ADD COLUMN ${TaskDataBaseContract.Columns.UPDATED_AT} INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        "ALTER TABLE ${TasksDataBase.DB_NAME} " +
                            "ADD COLUMN ${TaskDataBaseContract.Columns.DELETED} INTEGER NOT NULL DEFAULT 0"
                    )
                }
            })
            .build()

        Timber.d("✅ DataBaseModule: TasksDataBase создан: $db")
        return  db
    }

    @Provides
    fun providesTasksDbDao(db: TasksDataBase): TasksDbDao {
        val taskDbDao = db.taskDbDao()
        Timber.d("✅ TasksDbDao создан")
        return taskDbDao
    }
}

