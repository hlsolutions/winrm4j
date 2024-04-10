package io.cloudsoft.winrm4j.client.psrp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * This contains translations for UUID <-> GUID
 * <p>
 * The Java implementation {@link UUID} stores the data in two
 * <code>long</code> values internally, i.e. <code>mostSigBits</code> and
 * <code>leastSigBits</code>. That is everything in <em>network byte order (big endian)</code>.
 * <p>
 * However, the <code>GUID</code> is stored different: DWORD (4 bytes), WORD (2 bytes),
 * WORD (2 bytes) and a BYE array of 8. Said that, this means the first half is stored
 * with <em>little endian</em>, but the byte array stored natual in the <em>big endian</em>.
 */
public class UuidTranslator {

    static ByteBuffer uuidAsBuffer(final UUID uuid) {
        final var buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        buffer.flip();
        return buffer;
    }

    public static byte[] uuidAsBytes(final UUID uuid) {
        return uuidAsBuffer(uuid).array();
    }

    public static byte[] uuid2Guid(final String uuid) {
        return uuid2Guid(UUID.fromString(uuid));
    }

    public static byte[] uuid2Guid(final byte[] uuid) {
        final var buffer = ByteBuffer.wrap(uuid);
        return uuid2Guid(new UUID(buffer.getLong(), buffer.getLong()));
    }

    static byte[] uuid2Guid(final UUID id) {
        if (id == null) {
            return new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }

        final var uuidBuffer = uuidAsBuffer(id);

        // dword  4 LE
        // word   2 LE
        // word   2 LE
        // byte[] 8 BE
        final var guidBuffer = ByteBuffer.allocate(16);
        guidBuffer.order(ByteOrder.LITTLE_ENDIAN);
        guidBuffer.putInt(uuidBuffer.getInt()); // 4
        guidBuffer.putShort(uuidBuffer.getShort()); // 2
        guidBuffer.putShort(uuidBuffer.getShort()); // 2
        guidBuffer.order(ByteOrder.BIG_ENDIAN);
        guidBuffer.put(uuidBuffer); // 8

        return guidBuffer.array();
    }

    public static UUID guid2uuid(final byte[] guidBytes) {
        return guid2uuid(ByteBuffer.wrap(guidBytes));
    }

    public static UUID guid2uuid(final ByteBuffer guidBuffer) {

        final var uuidBuffer = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(guidBuffer.getInt()) // 4
                .putShort(guidBuffer.getShort()) // 2
                .putShort(guidBuffer.getShort()) // 2
                .order(ByteOrder.BIG_ENDIAN)
                .put(guidBuffer)
                .flip();

        final var mostSigBits = uuidBuffer.getLong();
        final var leastSigBits = uuidBuffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

}
