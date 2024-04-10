package io.cloudsoft.winrm4j.client.psrp;

import static org.testng.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.UUID;

import org.testng.annotations.Test;

public class UuidTranslatorTest {

    static final UUID UUID1 = UUID.fromString("2833cf0a-eb5c-4ff8-8509-3633150d0a5f");
    static final byte[] UUID_BYTES = {
            40, 51, -49, 10,
            -21, 92,
            79, -8,
            -123, 9, 54, 51, 21, 13, 10, 95
    };
    static final byte[] GUID1_BYTES = {
            // 4 DWORD LE
            10, -49, 51, 40,
            // WORD LE
            92, -21,
            // WORD LE
            -8, 79,
            // 8 BYTES
            -123, 9, 54, 51, 21, 13, 10, 95
    };

    @Test
    public void testUuidBytes() {
        assertEquals(
                UuidTranslator.uuidAsBytes(UUID1),
                UUID_BYTES,
                "invalid result"
        );
    }

    @Test
    public void testUuid2Guid() {
        assertEquals(
                UuidTranslator.uuid2Guid(UUID1),
                GUID1_BYTES,
                "invalid result"
        );
    }

    @Test
    public void testGuid2uuid() {
        assertEquals(
                UuidTranslator.guid2uuid(GUID1_BYTES),
                UUID1,
                "invalid result"
        );
    }

    static String bytes2Str(final byte[] bytes){
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

    static void printUuidViaBufferChar(ByteBuffer buffer){
        final var b16 = new byte[16];
        buffer.get(b16);
        buffer.flip();
        System.out.println(bytes2Str(b16));
        //buffer.flip();
    }

    public static void y(UUID uuid) {
        System.out.println("UUID: " + uuid.toString().toUpperCase());

        // Step 2: Convert the UUID to its string representation
        String uuidString = uuid.toString();

        // Step 3: Modify the string representation to match the format of a Windows GUID
        // Remove hyphens
        String windowsGuid = uuidString.replaceAll("-", "");

        // Add curly braces and insert dashes at specific positions
        windowsGuid = "{" + windowsGuid.substring(0, 8) + "-" + windowsGuid.substring(8, 12) + "-" +
                windowsGuid.substring(12, 16) + "-" + windowsGuid.substring(16, 20) + "-" +
                windowsGuid.substring(20) + "}";

        System.out.println("Windows GUID: " + windowsGuid);
    }

}