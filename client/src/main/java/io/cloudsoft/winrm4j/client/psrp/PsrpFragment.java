package io.cloudsoft.winrm4j.client.psrp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>PSRP Packet Fragment</b>
 * <p>
 * A WS-MAN packet can carry only a limited amount of data. Some PSRP messages do not fit into a single WS-MAN packet.
 * To overcome this, the PowerShell Remoting Protocol fragments messages before sending.
 * <p>
 * An individual fragment MUST be sent in a single WS-MAN packet; in other words, an individual fragment cannot be
 * broken down into smaller pieces and sent in separate WS-MAN packets.
 * <p>
 * A single WS-MAN packet, however, can contain multiple fragments. For instance, fragments belonging to a
 * <code>SESSION_CAPABILITY</code> message and a <code>INIT_RUNSPACEPOOL</code> message could be sent together in the
 * open content of a single <code>wxf:Create</code> WS-MAN packet.
 * <p>
 * Each message MUST be fragmented into one or more fragments with the fragment structure as
 * described in the following section. Each fragment MUST fit into the payload of a WS-MAN message.
 * <p>
 * <a href="https://winprotocoldoc.blob.core.windows.net/productionwindowsarchives/MS-PSRP/%5bMS-PSRP%5d.pdf">Source</a>
 *
 * @param objectId
 * @param blob
 * @param fragmentId
 * @param firstFragment
 * @param lastFragment
 */
public record PsrpFragment(long objectId,
                           byte[] blob,
                           long fragmentId,
                           boolean firstFragment,
                           boolean lastFragment) {

    // 21 = 8 + 8 + 1 + 4
    public static final int HEADER_LENGTH = 21;

    /**
     * Returns the encoded bytes of this fragment as specified by PSRP.
     *
     * @return byte array (see {@link #read(byte[])})
     */
    public byte[] bytes() {

        // 21 + blob.length
        final var buffer = ByteBuffer.allocate(HEADER_LENGTH + blob().length);
        final var nativeOrder = buffer.order();
        final var networkOrder = ByteOrder.BIG_ENDIAN;

        // ObjectId (8 bytes)
        buffer.order(networkOrder);
        buffer.putLong(objectId());
        buffer.order(nativeOrder);

        // FragmentId (8 bytes)
        buffer.order(networkOrder);
        buffer.putLong(fragmentId());
        buffer.order(nativeOrder);

        // Reserved + Fragment States (1 byte)
        buffer.put(fragmentByte());

        // Blob length (4 bytes)
        buffer.order(networkOrder);
        buffer.putInt(blob().length);
        buffer.order(nativeOrder);

        // The rest
        buffer.put(blob());

        return buffer.array();
    }

    public static List<PsrpFragment> readMulti(final byte[] bytes) {
        final var list = new ArrayList<PsrpFragment>();
        final var buffer = ByteBuffer.wrap(bytes);
        while (buffer.remaining() > 0) {
            list.add(read(buffer));
        }
        return list;
    }

    /**
     * Reads the encoded bytes into a fragment as specified by PSRP.
     *
     * @param bytes encoded bytes (see {@link #bytes()})
     * @return fragment
     */
    public static PsrpFragment read(final byte[] bytes) {
        return read(ByteBuffer.wrap(bytes));
    }

    /**
     * Reads the encoded bytes into a fragment as specified by PSRP.
     *
     * @param buffer encoded bytes (see {@link #bytes()})
     * @return fragment
     */
    public static PsrpFragment read(final ByteBuffer buffer) {

        // java uses network byte order / big endian as default

        // ObjectId (8 bytes)
        final var objectId = buffer.getLong();

        // FragmentId (8 bytes)
        final var fragmentId = buffer.getLong();

        // Reserved + Fragment States (1 byte = 6 bit + 1 + 1)
        final var fragmentByte = buffer.get();
        // 1 bit = is the last fragment (1 = yes, 0 = no)
        final var lastFragment = (fragmentByte & 0b10) == 0b10;
        // 1 bit = is the first fragment (1 = yes, 0 = no)
        final var firstFragment = (fragmentByte & 0b1) == 0b1;

        // Blob length (4 bytes)
        final var blobLength = buffer.getInt();

        // The rest
        final var blob = new byte[blobLength];
        buffer.get(blob);

        return new PsrpFragment(
                objectId,
                blob,
                fragmentId,
                firstFragment,
                lastFragment
        );
    }

    // 6 bit 0
    // 1 bit = is end fragment
    // 1 bit = is start fragment
    private byte fragmentByte() {
        // Reserved (6 bits)
        byte b = 0;
        // 1 bit = is the last fragment (1 = yes, 0 = no)
        if (lastFragment()) {
            b += 0b10;
        }
        // 1 bit = is the first fragment (1 = yes, 0 = no)
        if (firstFragment()) {
            b += 0b1;
        }
        return b;
    }

}
