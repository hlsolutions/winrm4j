package io.cloudsoft.winrm4j.client.psrp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * PowerShell Remoting Protocol Message
 *
 * @param runspacePoolId the RunspacePool ID (RPID)
 * @param pipelineId     the pipeline id (PID)
 * @param type           the MessageType
 * @param destination    the Destination
 * @param data           actual payload as string
 */
public record PsrpMessage(UUID runspacePoolId,
                          UUID pipelineId,
                          PsrpMessageType type,
                          PsrpDestination destination,
                          String data) {

    // 40 = 4 + 4 + 16 + 16
    public static final int HEADER_LENGTH = 40;

    static final byte[] BYTE_ORDER_MARK = {(byte) 239, (byte) 187, (byte) 191};

    public byte[] bytes() {

        final var blob = dataAsBytes();
        // 40 + bom.length + blob.length
        final var buffer = ByteBuffer.allocate(HEADER_LENGTH +blob.length);

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Destination (4 bytes)
        buffer.putInt(destination().value());
        // Type (4 bytes)
        buffer.putInt(type().value());

        buffer.order(ByteOrder.BIG_ENDIAN);

        // RPID (16 bytes)
        buffer.put(UuidTranslator.uuid2Guid(runspacePoolId()));

        // Pipeline ID (PID, 16 bytes)
        buffer.put(UuidTranslator.uuid2Guid(pipelineId()));

        // BOM (3 bytes), requires prepending a BOM marker
        //buffer.put(BYTE_ORDER_MARK);

        // The rest
        buffer.put(blob);

        return buffer.array();
    }

    public byte[] dataAsBytes() {
        return data().getBytes(StandardCharsets.UTF_8);
    }

    public static PsrpMessage read(final byte[] bytes) {
        return read(ByteBuffer.wrap(bytes));
    }

    public static PsrpMessage read(final ByteBuffer buffer) {

        final var b16 = new byte[16];
        final var b3 = new byte[3];

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Destination (4 bytes)
        final var destination = PsrpDestination.resolveByValue(buffer.getInt());
        // Type (4 bytes)
        final var type = PsrpMessageType.resolveByValue(buffer.getInt());

        buffer.order(ByteOrder.BIG_ENDIAN);

        // RPID (16 bytes)
        buffer.get(b16);
        final var runspacePoolId = UuidTranslator.guid2uuid(b16);

        // PID (16 bytes)
        buffer.get(b16);
        final var pipelineId = UuidTranslator.guid2uuid(b16);

        // Skip BOM (3 bytes) optional
        buffer.get(b3);
        if (!Arrays.equals(BYTE_ORDER_MARK, b3)) {
            // does not look like BOM, reset position
            buffer.position(buffer.position() - 3);
        }

        // The rest
        final var blob = new byte[buffer.remaining()];
        buffer.get(blob);

        return new PsrpMessage(
                runspacePoolId,
                pipelineId,
                type,
                destination,
                new String(blob, StandardCharsets.UTF_8)
        );
    }

    static byte[] uuid2GuidBytes2(final UUID id) {
        if (id == null) {
            return new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }

        final var idBuffer = ByteBuffer.allocate(16);
        idBuffer.order(ByteOrder.BIG_ENDIAN);
        idBuffer.putLong(id.getMostSignificantBits());
        idBuffer.putLong(id.getLeastSignificantBits());
        idBuffer.flip();

        // dword  4
        // word   2
        // word   2
        // byte[] 8

        final var remoteBuffer = ByteBuffer.allocate(16);
        remoteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        remoteBuffer.putInt(idBuffer.getInt());
        remoteBuffer.putShort(idBuffer.getShort());
        remoteBuffer.putShort(idBuffer.getShort());
        remoteBuffer.order(ByteOrder.BIG_ENDIAN);
        remoteBuffer.put(idBuffer);

        return remoteBuffer.array();
    }

    static UUID guidBytes2uuid1(final byte[] bytes) {
        final var remoteBuffer = ByteBuffer.wrap(bytes);

        final var idBuffer = ByteBuffer.allocate(16);

        remoteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        idBuffer.putInt(remoteBuffer.getInt());
        idBuffer.putShort(remoteBuffer.getShort());
        idBuffer.putShort(remoteBuffer.getShort());
        remoteBuffer.order(ByteOrder.BIG_ENDIAN);
        idBuffer.put(remoteBuffer);
        idBuffer.flip();

        final var mostSigBits = idBuffer.getLong();
        final var leastSigBits = idBuffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    static byte[] uuid2GuidBytes(final UUID id) {
        final var buffer = ByteBuffer.allocate(16);
        buffer.putLong(id.getMostSignificantBits());
        buffer.putLong(id.getLeastSignificantBits());
        return buffer.array();
    }

    static UUID guidBytes2uuid(final byte[] bytes) {
        final var buffer = ByteBuffer.wrap(bytes);
        final var mostSigBits = buffer.getLong();
        final var leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

}
