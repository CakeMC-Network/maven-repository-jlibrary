# maven-repository-jlibrary
Small library for cakemc-cluster-deployment-system

### Creating a publishing task and publishing our first file
publishing to the "custom" repository: (add this to the project you want to publish)
```kotlin
publishing {
    publications {
        create<MavenPublication>("deployment") {
            from(components["kotlin"])

            // Include a source JAR (optional)
            artifact(tasks.getByName("jar")) {
                classifier = "sources"
            }
        }
    }

    repositories {
        maven {
            name = "cluster-deployment"
            url = uri("http://localhost:8080/")
            isAllowInsecureProtocol = true
            credentials {
                username = "your-user-name"
                password = "your-password"
            }
        }
    }
}
```

### Using the library and starting your first Maven-Server
````kotlin
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
````