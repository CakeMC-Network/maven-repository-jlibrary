package net.cakemc.repository.utils

fun interface GetRequestHandler {

    fun requestReceived(path: String)

}