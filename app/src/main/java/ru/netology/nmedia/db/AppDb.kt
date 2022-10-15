package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.ReadedPostsEntity
import ru.netology.nmedia.dao.PostDao

@Database(entities = [PostEntity::class, ReadedPostsEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {

        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {

            return instance ?: synchronized( this) {

                instance ?: buildDataBase(context).also { instance = it }

            }

        }

        private fun buildDataBase (context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "roomappnmedia.db")
                //.allowMainThreadQueries()
                .build()

    }


}