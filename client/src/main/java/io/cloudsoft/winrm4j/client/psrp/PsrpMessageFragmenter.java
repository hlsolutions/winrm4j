package io.cloudsoft.winrm4j.client.psrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsrpMessageFragmenter {

    private static final Logger LOG = LoggerFactory.getLogger(PsrpMessageFragmenter.class);

    public static final int DEFAULT_BLOB_LENGTH = 32_768;

    private final int blobLength;
    private long objectId;

    public PsrpMessageFragmenter withObjectId(long objectId) {
        this.objectId = objectId;
        return this;
    }

    public PsrpMessageFragmenter() {
        this(DEFAULT_BLOB_LENGTH);
    }

    public PsrpMessageFragmenter(final int blobLength) {
        this.blobLength = blobLength;
        this.objectId = 0;
    }

    public List<PsrpFragment> fragment(final PsrpMessage message) {

        LOG.trace("Fragmenting PSPR message: {}", message);

        this.objectId++;
        final var bytes = message.bytes();
        int fragmentedIdxStart = 0;
        int fragmentId = 0;
        final var result = new ArrayList<PsrpFragment>();
        while (fragmentedIdxStart < bytes.length) {
            final var fragmentIdxEnd = Math.min(fragmentedIdxStart + blobLength, bytes.length);
            result.add(new PsrpFragment(
                    objectId,
                    Arrays.copyOfRange(bytes, fragmentedIdxStart, fragmentIdxEnd),
                    fragmentId++,
                    fragmentedIdxStart == 0,
                    fragmentIdxEnd == bytes.length
            ));
            fragmentedIdxStart = fragmentIdxEnd;
        }
        LOG.trace("PSPR Fragments created: {}", result.size());
        return List.copyOf(result);
    }


}
