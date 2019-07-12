package com.shxhzhxx.sdk.network

import com.shxhzhxx.sdk.network.PushClient.State.*
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue


class PushClient(
        private val onReceive: (ByteArray) -> Unit,
        private val onStateChange: (State) -> Unit
) {
    enum class State {
        INIT, CONNECTING, CONNECTED, BINDING, BOUND, CLOSED;
    }

    private val channel by lazy { SocketChannel.open().apply { configureBlocking(true) } }
    private val pushQueue = LinkedBlockingQueue<ByteArray>()
    private val receiveCallbacks = ConcurrentHashMap<ByteArrayKey, (ByteArray) -> Unit>()
    var maxBufferSize = 0
        private set
    var state = INIT
        private set(value) {
            if (field == value) return
            field = value
            onStateChange(value)
            if (value == CLOSED) pushThread.interrupt()
        }
    private val pushThread by lazy {
        Thread {
            while (state != CLOSED) {
                val data = try {
                    pushQueue.take()
                } catch (e: InterruptedException) {
                    return@Thread
                }
                if (data.size <= maxBufferSize) channel.write(ByteBuffer.wrap(data))
            }
        }
    }

    fun close() {
        channel.close()
        state = CLOSED
    }

    fun bind(id: Int) {
        if (id <= 0) return
        synchronized(this) {
            if (state != CONNECTED && state != CONNECTING) return
            state = BINDING
        }
        val echo1 = UUID.randomUUID().toString().toByteArray()
        val echo2 = UUID.randomUUID().toString().toByteArray()
        receiveCallbacks[ByteArrayKey(echo1)] = {
            if (!(it contentEquals echo2)) throw IOException() //server error
            state = BOUND
        }
        ByteBuffer.allocate(9 + echo1.size + 5 + echo2.size + 5)
                .putInt(echo1.size + 5).put(0).put(echo1)
                .putInt(9).put(1).putInt(id)
                .putInt(echo2.size + 5).put(0).put(echo2)
                .offer()
    }

    fun push(id: Int, data: ByteArray, offset: Int = 0, length: Int = data.size) {
        if (data.size < offset + length) return
        val len = length + 9
        ByteBuffer.allocate(len).putInt(len).put(2).putInt(id).put(data, offset, length).offer()
    }

    fun multiPush(ids: IntArray, data: ByteArray, offset: Int = 0, length: Int = data.size) {
        if (data.size < offset + length) return
        if (ids.size == 1) {
            return push(ids[0], data, offset, length)
        }
        val len = 9 + ids.size * 4 + length
        ByteBuffer.allocate(len).putInt(len).put(3).putInt(ids.size).also { for (id in ids) it.putInt(id) }
                .put(data, offset, length).offer()
    }

    fun broadcast(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        if (data.size < offset + length) return
        val len = length + 5
        ByteBuffer.allocate(len).putInt(len).put(6).put(data, offset, length).offer()
    }

    fun connect(host: String, port: Int) {
        synchronized(this) {
            if (state != INIT) return
            state = CONNECTING
            pushQueue.clear()
        }
        Thread {
            try {
                channel.socket().connect(InetSocketAddress(host, port), 3000)
                maxBufferSize = ByteBuffer.allocate(8).let { byteBuffer ->
                    byteBuffer.putInt(5).put(4).limit(5).position(0)
                    channel.write(byteBuffer)
                    byteBuffer.limit(8).position(0)
                    while (byteBuffer.position() < 8) {
                        channel.read(byteBuffer)
                    }
                    if (byteBuffer.getInt(0) != 8)
                        throw IOException()
                    byteBuffer.getInt(4)
                }
                val buffer = ByteBuffer.allocate(maxBufferSize)
                synchronized(this) {
                    state = if (state == BINDING) BINDING else CONNECTED
                    pushThread.start()
                }
                var receiver = onReceive
                while (true) {
                    buffer.limit(4).position(0)
                    while (buffer.position() < 4) {
                        if (channel.read(buffer) < 0) throw IOException()
                    }
                    val len = buffer.getInt(0)
                    if (len > maxBufferSize) throw IOException()
                    buffer.limit(len).position(4)
                    while (buffer.position() < len) {
                        if (channel.read(buffer) < 0) throw IOException()
                    }
                    buffer.position(4)
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)
                    receiver = receiveCallbacks.remove(ByteArrayKey(data)) ?: run { receiver(data);onReceive }
                }
            } catch (e: Throwable) {
                channel.close()
                state = CLOSED
            }
        }.start()
    }

    private fun ByteBuffer.offer() {
        pushQueue.offer(ByteArray(limit()).also { position(0);get(it) })
    }
}

class ByteArrayKey(private val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean =
            this === other || other is ByteArrayKey && this.bytes contentEquals other.bytes

    override fun hashCode(): Int = bytes.contentHashCode()
}