package net.cakemc.repository.network

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedFile
import io.netty.util.CharsetUtil
import net.cakemc.repository.credentials.RepositoryCredentials
import net.cakemc.repository.utils.GetRequestHandler
import net.cakemc.repository.utils.PublicationHandler
import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.*

class MavenRequestHandler(
    private val baseDirectory: Path,
    private val credentials: RepositoryCredentials,
    private val publicationHandler: PublicationHandler,
    private val getRequestHandler: GetRequestHandler
) : SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        when (request.method()) {
            HttpMethod.PUT -> handlePutRequest(ctx, request)
            HttpMethod.GET -> handleGetRequest(ctx, request)
            else -> sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED)
        }
    }

    private fun handlePutRequest(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val authHeader = request.headers().get("Authorization")
        if (authHeader == null || !isValidAuth(authHeader)) {
            sendError(ctx, HttpResponseStatus.UNAUTHORIZED)
            return
        }

        val uri = request.uri().removePrefix("/")
        val targetPath = baseDirectory.resolve(uri).normalize()

        if (!targetPath.startsWith(baseDirectory)) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN)
            return
        }

        try {
            val parentPath = targetPath.parent
            if (parentPath != null && !parentPath.toFile().exists()) {
                parentPath.toFile().mkdirs()
            }

            val byteBuf = request.content()
            val bytes = ByteArray(byteBuf.readableBytes())
            byteBuf.readBytes(bytes)

            publicationHandler.accept(uri, bytes)

            targetPath.toFile().writeBytes(bytes)

            sendResponse(ctx, HttpResponseStatus.CREATED)
        } catch (e: Exception) {
            e.printStackTrace()
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun handleGetRequest(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val uri = request.uri().removePrefix("/")
        val targetPath = baseDirectory.resolve(uri).normalize()

        if (!targetPath.startsWith(baseDirectory) || !targetPath.toFile().exists() || !targetPath.toFile().isFile) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND)
            return
        }

        this.getRequestHandler.requestReceived(uri)

        try {
            val randomAccessFile = RandomAccessFile(targetPath.toFile(), "r")
            val fileLength = randomAccessFile.length()

            val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
            response.headers()[HttpHeaderNames.CONTENT_LENGTH] = fileLength
            response.headers()[HttpHeaderNames.CONTENT_TYPE] = "application/octet-stream"
            ctx.write(response)

            ctx.write(ChunkedFile(randomAccessFile))

            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE)
        } catch (e: Exception) {
            e.printStackTrace()
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun isValidAuth(authHeader: String): Boolean {
        if (authHeader.startsWith("Basic ")) {
            val base64Credentials = authHeader.substring(6)
            val decodedCredentials = String(Base64.getDecoder().decode(base64Credentials), CharsetUtil.UTF_8)
            val split = decodedCredentials.split(":")

            if (split.size == 2 && credentials.isValid(split)) {
                return true
            }
        }
        return false
    }

    private fun sendResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus) {
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status)
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }

    private fun sendError(ctx: ChannelHandlerContext, status: HttpResponseStatus) {
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status)
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }
}
