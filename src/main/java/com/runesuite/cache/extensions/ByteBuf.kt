package com.runesuite.cache.extensions

import com.runesuite.general.RuneScape
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.ByteBufUtil
import org.bouncycastle.jcajce.provider.digest.Whirlpool
import java.nio.IntBuffer
import java.nio.ShortBuffer
import java.nio.charset.Charset

fun ByteBuf.readableArray(): ByteArray {
    return getArray(readerIndex(), readableBytes())
}

fun ByteBuf.getArray(index: Int, length: Int): ByteArray {
    return ByteBufUtil.getBytes(this, index, length)
}

fun ByteBuf.readArray(length: Int): ByteArray {
    val a = getArray(readerIndex(), length)
    skipBytes(length)
    return a
}

fun ByteBuf.inputStream(): ByteBufInputStream {
    return ByteBufInputStream(this)
}

fun ByteBuf.outputStream(): ByteBufOutputStream {
    return ByteBufOutputStream(this)
}

fun ByteBuf.readSliceMax(maxLength: Int): ByteBuf {
    return readSlice(Math.min(maxLength, readableBytes()))
}

fun ByteBuf.readSliceAsInts(length: Int): IntBuffer {
    val byteLength = length * Integer.BYTES
    val b = nioBuffer(readerIndex(), byteLength).asIntBuffer()
    skipBytes(byteLength)
    return b
}

fun ByteBuf.readSliceAsShorts(length: Int): ShortBuffer {
    val byteLength = length * java.lang.Short.BYTES
    val b = nioBuffer(readerIndex(), byteLength).asShortBuffer()
    skipBytes(byteLength)
    return b
}

inline fun ByteBuf.forEach(crossinline action: (Byte) -> Unit) {
    forEachByte {
        action(it)
        true
    }
}

private val whirlpoolDigest by lazy { Whirlpool.Digest() }

@Synchronized
fun ByteBuf.whirlpool(): ByteArray {
    // org.bouncycastle.crypto.digests.WhirlPoolDigest.update(byte[] in, int inOff, int len)
    forEach {
        whirlpoolDigest.update(it)
    }
    val hash = whirlpoolDigest.digest()
    check(hash.size == 64)
    return hash
}

fun ByteBuf.readString(charset: Charset = RuneScape.CHARSET): String {
    val length = bytesBefore(0)
    check(length != -1)
    val s = toString(readerIndex(), length, charset)
    skipBytes(length + 1)
    return s
}