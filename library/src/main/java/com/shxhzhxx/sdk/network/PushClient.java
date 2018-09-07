package com.shxhzhxx.sdk.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.UUID;

public class PushClient extends Thread {
    enum State {
        INIT,
        CONNECTED,
        BIND,
        ERROR
    }

    public interface Callback {
        void onStateChange(State state);

        void onReceiveData(byte[] data);
    }

    private final byte[] BIND_ECHO = UUID.randomUUID().toString().getBytes();
    private State state;
    private final Callback callback;
    private final String host;
    private final int port;
    private SocketChannel channel;
    private ByteBuffer sendBuffer;

    public PushClient(String host, int port, Callback callback) {
        state = State.INIT;
        this.callback = callback;
        this.host = host;
        this.port = port;
    }

    public State getPushState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
        callback.onStateChange(state);
    }

    public synchronized void getIp() {
        if (state != State.CONNECTED && state != State.BIND)
            return;
        sendBuffer.limit(5).position(0);
        sendBuffer.putInt(5);
        sendBuffer.put((byte) 5);
        sendBuffer.position(0);
        try {
            channel.write(sendBuffer);
        } catch (IOException ignore) {
        }
    }

    public synchronized void bind(int id) {
        if (state != State.CONNECTED || id <= 0)
            return;
        sendBuffer.limit(9).position(0);
        sendBuffer.putInt(9);
        sendBuffer.put((byte) 1);
        sendBuffer.putInt(id);
        sendBuffer.position(0);
        try {
            channel.write(sendBuffer);
        } catch (IOException ignore) {
            return;
        }

        //echo
        sendBuffer.limit(BIND_ECHO.length + 5).position(0);
        sendBuffer.putInt(BIND_ECHO.length + 5);
        sendBuffer.put((byte) 0);
        sendBuffer.put(BIND_ECHO);
        sendBuffer.position(0);
        try {
            channel.write(sendBuffer);
        } catch (IOException ignore) {
        }
    }

    public int send(int[] ids, byte[] data) {
        return send(ids, data, 0, data.length);
    }

    public synchronized int send(int[] ids, byte[] data, int offset, int length) {
        if (state != State.CONNECTED && state != State.BIND)
            return -1;
        if (ids.length == 1) {
            return send(ids[0], data, offset, length);
        }
        int len = 9 + ids.length * 4 + length;
        if (len > sendBuffer.capacity() || data.length < offset + length)
            return -1;
        sendBuffer.limit(len).position(0);
        sendBuffer.putInt(len);
        sendBuffer.put((byte) 3);
        sendBuffer.putInt(ids.length);
        for (int id : ids)
            sendBuffer.putInt(id);
        sendBuffer.put(data, offset, length);
        sendBuffer.position(0);
        try {
            return channel.write(sendBuffer);
        } catch (IOException e) {
            return -1;
        }
    }

    public int send(int id, byte[] data) {
        return send(id, data, 0, data.length);
    }

    public synchronized int send(int id, byte[] data, int offset, int length) {
        if (state != State.CONNECTED && state != State.BIND)
            return -1;
        int len = length + 9;
        if (len > sendBuffer.capacity() || data.length < offset + length)
            return -1;
        sendBuffer.limit(len).position(0);
        sendBuffer.putInt(len);
        sendBuffer.put((byte) 2);
        sendBuffer.putInt(id);
        sendBuffer.put(data, offset, length);
        sendBuffer.position(0);
        try {
            return channel.write(sendBuffer);
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public void run() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(host, port));
            int size = getBufferSize();
            ByteBuffer buffer = ByteBuffer.allocate(size);
            sendBuffer = ByteBuffer.allocate(size);
            setState(State.CONNECTED);
            for (; ; ) {
                buffer.limit(4).position(0);
                while (buffer.position() < 4) {
                    if (channel.read(buffer) < 0)
                        throw new IOException();
                }
                int len = buffer.getInt(0);
                if (len > size) {
                    throw new IOException();
                }
                buffer.limit(len).position(4);
                while (buffer.position() < len) {
                    if (channel.read(buffer) < 0)
                        throw new IOException();
                }
                buffer.position(4);
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                if (Arrays.equals(BIND_ECHO, data)) {
                    setState(State.BIND);
                } else {
                    callback.onReceiveData(data);
                }
            }
        } catch (IOException e) {
            setState(State.ERROR);
            try {
                channel.close();
            } catch (IOException ignore) {
            }
        }
    }

    private int getBufferSize() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(5);
        buffer.put((byte) 4);
        buffer.limit(5).position(0);
        channel.write(buffer);
        buffer.limit(8).position(0);
        while (buffer.position() < 8) {
            channel.read(buffer);
        }
        if (buffer.getInt(0) != 8)
            throw new IOException();
        return buffer.getInt(4);
    }
}
