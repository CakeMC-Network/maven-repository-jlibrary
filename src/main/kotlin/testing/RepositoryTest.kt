package testing

import net.cakemc.repository.MavenRepositoryServer
import net.cakemc.repository.utils.PublicationHandler
import java.io.File

object RepositoryTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val baseDirectory = File("maven-repository")

        val handler = object : PublicationHandler {

            override fun accept(name: String, file: ByteArray) {
                println("received file: $name - (${file.size})")
            }

        }

        val server = MavenRepositoryServer(
            8080, baseDirectory,
            "username", "password",
            handler
        )
        server.start()

    }

}