package io.cloudsoft.winrm4j.client.psrp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsrpMessageDefragmenter {

    private static final Logger LOG = LoggerFactory.getLogger(PsrpMessageDefragmenter.class);

    private final Map<Long, List<PsrpFragment>> fragmentCache = Collections.synchronizedMap(new HashMap<>());

    public Optional<PsrpMessage> defragment(final PsrpFragment fragment) {

        fragmentCache.computeIfAbsent(fragment.objectId(), x -> new ArrayList<>())
                .add(fragment);

        if (messageCompleted(fragment)) {
            try (final var baos = new ByteArrayOutputStream()) {
                final var fragments = fragmentCache.get(fragment.objectId());
                for (final var f : fragments) {
                    baos.write(f.blob());
                }
                return Optional.of(PsrpMessage.read(baos.toByteArray()));
            } catch (final IOException e) {
                // actually unlikely to happen (ByteArrayOutStream is memory-based)
                throw new RuntimeException("Failed writing PSPR fragment to internal buffer", e);
            } finally {
                fragmentCache.remove(fragment.objectId());
            }
        }

        return Optional.empty();
    }

    private boolean messageCompleted(final PsrpFragment fragment) {
        final var fragments = fragmentCache.get(fragment.objectId());
        return fragments.stream()
                // last fragment must exist
                .filter(PsrpFragment::lastFragment)
                .findFirst()
                // fragmentId is incremented, started by 0
                .filter(lastFragment -> fragments.size() == fragment.fragmentId() + 1)
                .isPresent();
    }

    public boolean hasFragments() {
        return !fragmentCache.isEmpty();
    }

}
