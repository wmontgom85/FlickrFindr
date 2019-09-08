package com.wmontgom85.flickrfindr

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wmontgom85.flickrfindr.repo.DBHelper
import com.wmontgom85.flickrfindr.repo.dao.FlickrImageDao
import com.wmontgom85.flickrfindr.repo.model.FlickrImage
import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
class DBOperationUnitTest : CoroutineScope {
    private var job : Job? = null

    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    private lateinit var imageDao: FlickrImageDao
    private lateinit var db: DBHelper

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DBHelper::class.java).build()
        imageDao = db.flickrImageDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun db_can_read_write() {
        val randomId = "unittest_${Random().nextInt()}"
        val flickrImage = createImage(randomId)

        launch {
            imageDao.insert(flickrImage)

            // check that it was inserted
            val insertedFlickrImage = imageDao.getImage(randomId)

            assertThat(insertedFlickrImage?.id, equalTo(randomId))
            assertThat(insertedFlickrImage?.owner, equalTo("owner_$randomId"))
            assertThat(insertedFlickrImage?.secret, equalTo("secret_$randomId"))
            assertThat(insertedFlickrImage?.server, equalTo(1001))
            assertThat(insertedFlickrImage?.farm, equalTo(2002))
            assertThat(insertedFlickrImage?.title, equalTo("title_$randomId"))
            assertThat(insertedFlickrImage?.isPublic, equalTo(1))
            assertThat(insertedFlickrImage?.isFriend, equalTo(1))
            assertThat(insertedFlickrImage?.isFamily, equalTo(1))

            // check deletion
            imageDao.delete(insertedFlickrImage!!)

            val deletedImage: FlickrImage? = imageDao.getImage(randomId)
            assertThat(deletedImage, nullValue())

            // check for deletion by string identifier
            imageDao.insert(flickrImage)
            val anotherFlickrImage = imageDao.getImage(randomId)

            assertThat(anotherFlickrImage?.id, equalTo(randomId))

            imageDao.delete(anotherFlickrImage!!.id)

            val anotherDeletedImage: FlickrImage? = imageDao.getImage(randomId)
            assertThat(anotherDeletedImage, nullValue())
        }
    }

    fun createImage(randomId: String) : FlickrImage =
        FlickrImage(
            randomId,
            "owner_$randomId",
            "secret_$randomId",
            1001,
            2002,
            "title_$randomId",
            1,
            1,
            1
        )
}