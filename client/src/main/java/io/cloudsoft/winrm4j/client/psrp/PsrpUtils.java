package io.cloudsoft.winrm4j.client.psrp;

import java.util.Base64;
import java.util.UUID;

public class PsrpUtils {

    public static byte[] buildOpenShellPayload(final UUID runspacePoolId,
                                               final PsrpMessageFragmenter fragmenter) {
        return PsrpMessageFactory.combine(
                fragmenter,
                PsrpMessageFactory.sessionCapability(runspacePoolId),
                PsrpMessageFactory.initRunspacePool(runspacePoolId)
        );
    }

    public static String base64Encode(final byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

}
