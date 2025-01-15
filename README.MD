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

import net.cakemc.repository.RepositorySystem
import net.cakemc.repository.credentials.RepositoryCredentials
import net.cakemc.repository.utils.GetRequestHandler
import net.cakemc.repository.utils.PublicationHandler
import java.nio.file.Path

object RepositoryTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val credentials = RepositoryCredentials("username", "password")

        val publicationHandler = object : PublicationHandler {
            override fun accept(name: String, file: ByteArray) {
                println("received file $name with size of ${file.size}")
            }
        }

        val getRequestHandler = object : GetRequestHandler {
            override fun requestReceived(path: String) {
                println("received get for file: $path")
            }
        }

        val repositorySystem = RepositorySystem.Builder()
            .credentials(credentials)
            .host("0.0.0.0")
            .port(8080)
            .directory(Path.of("./maven-repository"))
            .publicationHandler(publicationHandler)
            .getRequestHandler(getRequestHandler)
            .build()

        repositorySystem.bind()

    }

}
````