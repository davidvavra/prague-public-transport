package me.vavra.nexttram


import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.EntityNotFoundException
import com.google.appengine.api.datastore.KeyFactory

/**
 * Saving and reading data.
 */
object Datastore {

    val datastore = DatastoreServiceFactory.getDatastoreService()

    fun getUserLocation(userId: String): String? {
        val key = KeyFactory.createKey("UserInfo", userId)
        try {
            return datastore.get(key).getProperty("location") as String?
        } catch (e: EntityNotFoundException) {
            return null
        }
    }

    fun saveUserLocation(userId: String, location: String?) {
        val employee = Entity("UserInfo", userId)
        employee.setProperty("location", location)
        datastore.put(employee)
    }
}