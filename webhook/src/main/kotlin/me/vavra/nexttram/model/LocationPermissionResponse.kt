package me.vavra.nexttram.model

/**
 * Response to API.AI which asks for user's location.
 */
class LocationPermissionResponse {
    val data = Data()
    val speech = "Location"
    val contextOut = listOf("requesting_permission")
    class Data {
        val google = Google()
        class Google {
            val permissions_request = PermissionsRequest()
            class PermissionsRequest {
                val opt_context = "To find nearby tram stops"
                val permissions = listOf("DEVICE_PRECISE_LOCATION")
            }
        }
    }
}