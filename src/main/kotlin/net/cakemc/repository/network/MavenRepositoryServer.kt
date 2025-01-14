package net.cakemc.repository.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler
import net.cakemc.repository.utils.PublicationHandler
import java.io.File


class MavenRepositoryServer(
    private val port: Int,
    private val baseDirectory: File,
    private val username: String,
    private val password: String,
    private val handler: PublicationHandler
) {

    companion object {
        val EPOLL: Boolean = Epoll.isAvailable()
        val KQUEUE: Boolean = KQueue.isAvailable()
    }

    fun start() {

        val ioHandlerFactory = if (EPOLL) (if (KQUEUE) KQueueIoHandler.newFactory() else
            EpollIoHandler.newFactory()) else NioIoHandler.newFactory()
        val bossGroup = MultiThreadIoEventLoopGroup(ioHandlerFactory)
        val workerGroup = MultiThreadIoEventLoopGroup(ioHandlerFactory)

         val channel = if (EPOLL) (if (KQUEUE) KQueueServerSocketChannel::class.java else
             EpollServerSocketChannel::class.java) else NioServerSocketChannel::class.java

        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(channel)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(HttpServerCodec())

                        pipeline.addLast(HttpObjectAggregator(100 * 1024 * 1024))
                        pipeline.addLast(ChunkedWriteHandler())

                        pipeline.addLast(
                            MavenRequestHandler(
                            baseDirectory, username, password, handler
                        )
                        )
                    }

                })

            val serverChannel = bootstrap.bind(port).sync().channel()
            serverChannel.closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}

