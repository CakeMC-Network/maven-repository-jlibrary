package net.cakemc.repository.credentials

class RepositoryCredentials(
    val username: String,
    val password: String,
) {

    fun isValid(credentials: List<String>): Boolean {
        return credentials[0] == username && credentials[1] == password
    }

}