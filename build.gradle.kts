plugins {
    kotlin("jvm") version "2.0.0"
}

group = "net.cakemc.library"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

@Suppress("unchecked_cast")
fun <V> prop(value: String): V {
    return properties.getValue(value) as V
}


dependencies {
    implementation(
        group = "io.netty",
        name = "netty-common",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-buffer",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-codec",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-codec-dns",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-resolver",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-resolver-dns",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-resolver-dns-classes-macos",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-resolver-dns-native-macos",
        version = prop("dep-netty"),
        classifier = "osx-aarch_64"
    )

    implementation(
        group = "io.netty",
        name = "netty-resolver-dns-native-macos",
        version = prop("dep-netty"),
        classifier = "osx-x86_64"
    )

    implementation(
        group = "io.netty",
        name = "netty-codec",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-transport",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-classes-epoll",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-classes-kqueue",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-native-epoll",
        version = prop("dep-netty"),
        classifier = "linux-aarch_64"
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-native-epoll",
        version = prop("dep-netty"),
        classifier = "linux-riscv64"
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-native-epoll",
        version = prop("dep-netty"),
        classifier = "linux-x86_64"
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-native-kqueue",
        version = prop("dep-netty"),
        classifier = "osx-aarch_64"
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-native-kqueue",
        version = prop("dep-netty"),
        classifier = "osx-x86_64"
    )

    implementation(
        group = "io.netty",
        name = "netty-codec-http",
        version = prop("dep-netty"),
    )

    implementation(
        group = "io.netty",
        name = "netty-transport-native-unix-common",
        version = prop("dep-netty"),
    )

    implementation(
        group = "io.netty",
        name = "netty-handler",
        version = prop("dep-netty")
    )

    implementation(
        group = "io.netty",
        name = "netty-handler-proxy",
        version = prop("dep-netty")
    )


}

kotlin {
    jvmToolchain(21)
}