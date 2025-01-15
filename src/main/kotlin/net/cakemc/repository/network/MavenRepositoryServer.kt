package net.cakemc.repository.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler
import net.cakemc.repository.AbstractRepositoryServer
import net.cakemc.repository.credentials.RepositoryCredentials
import net.cakemc.repository.utils.GetRequestHandler
import net.cakemc.repository.utils.PublicationHandler
import java.nio.file.Path


class MavenRepositoryServer(
    private val host: String,
    private val port: Int,
    private val baseDirectory: Path,
    private val credentials: RepositoryCredentials,
    private val publicationHandler: PublicationHandler,
    private val getRequestHandler: GetRequestHandler
): AbstractRepositoryServer() {

    companion object {
        val EPOLL: Boolean = Epoll.isAvailable()
        val KQUEUE: Boolean = KQueue.isAvailable()
    }

    private var bossGroup: EventLoopGroup
    private var workerGroup: EventLoopGroup
    private val channel: Class<out ServerSocketChannel>

    init {
        val ioHandlerFactory = when {
            EPOLL && KQUEUE -> KQueueIoHandler.newFactory()
            EPOLL -> EpollIoHandler.newFactory()
            else -> NioIoHandler.newFactory()
        }

        bossGroup = MultiThreadIoEventLoopGroup(ioHandlerFactory)
        workerGroup = MultiThreadIoEventLoopGroup(ioHandlerFactory)

        channel = when {
            EPOLL && KQUEUE -> KQueueServerSocketChannel::class.java
            EPOLL -> EpollServerSocketChannel::class.java
            else -> NioServerSocketChannel::class.java
        }
    }

    override fun start() {
        try {
            val bootstrap = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(channel)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(HttpServerCodec())

                        pipeline.addLast(HttpObjectAggregator(100 * 1024 * 1024))
                        pipeline.addLast(ChunkedWriteHandler())

                        pipeline.addLast(
                            MavenRequestHandler(
                                baseDirectory, credentials,
                                publicationHandler, getRequestHandler
                            )
                        )
                    }

                })

            val serverChannel = bootstrap.bind(host, port).sync().channel()
            serverChannel.closeFuture().sync()
        } finally {
            this.stop()
        }
    }

    override fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }

}

