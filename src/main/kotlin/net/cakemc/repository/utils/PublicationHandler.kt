package net.cakemc.repository.utils

fun interface PublicationHandler {

    fun accept(name: String, file: ByteArray)

}