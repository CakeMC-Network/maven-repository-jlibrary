package net.cakemc.repository

import net.cakemc.repository.credentials.RepositoryCredentials
import net.cakemc.repository.network.MavenRepositoryServer
import net.cakemc.repository.utils.GetRequestHandler
import net.cakemc.repository.utils.PublicationHandler
import java.nio.file.Path

class RepositorySystem(
    credentials: RepositoryCredentials,
    host: String,
    port: Int,
    directory: Path,
    publicationHandler: PublicationHandler,
    getRequestHandler: GetRequestHandler
) {

    private var repositoryServer: AbstractRepositoryServer

    init {
         repositoryServer = MavenRepositoryServer(
            host, port, directory,
            credentials, publicationHandler, getRequestHandler
        )
    }

    fun bind() {
        repositoryServer.start()
    }

    fun close() {
        repositoryServer.stop()
    }

    class Builder {
        private var credentials: RepositoryCredentials? = null
        private var host: String? = null
        private var port: Int = 80 // Default to HTTP port
        private var directory: Path? = null
        private var publicationHandler: PublicationHandler? = null
        private var getRequestHandler: GetRequestHandler? = null

        fun credentials(credentials: RepositoryCredentials) = apply { this.credentials = credentials }
        fun host(host: String) = apply { this.host = host }
        fun port(port: Int) = apply { this.port = port }
        fun directory(directory: Path) = apply { this.directory = directory }
        fun publicationHandler(publicationHandler: PublicationHandler) = apply { this.publicationHandler = publicationHandler }
        fun getRequestHandler(getRequestHandler: GetRequestHandler) = apply { this.getRequestHandler = getRequestHandler }

        fun build(): RepositorySystem {
            val credentials = this.credentials ?: throw IllegalArgumentException("Repository credentials must be set.")
            val host = this.host ?: throw IllegalArgumentException("Host must be set.")
            val directory = this.directory ?: throw IllegalArgumentException("Directory must be set.")
            val publicationHandler = this.publicationHandler ?: throw IllegalArgumentException("Publication handler must be set.")
            val getRequestHandler = this.getRequestHandler ?: throw IllegalArgumentException("Get request handler must be set.")

            return RepositorySystem(
                credentials,
                host,
                port,
                directory,
                publicationHandler,
                getRequestHandler
            )
        }
    }

}