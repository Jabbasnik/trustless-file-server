package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Base64;
import java.util.Optional;

import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Arrays.stream;
import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;

/**
 * Possible encoding algorithms.
 */
public enum EncodingAlgo {
    BASE_64("BASE_64") {
        @Override
        byte[] encode(byte[] data) {
            return getEncoder().encode(data);
        }

        @Override
        byte[] decode(byte[] data) {
            return getDecoder().decode(data);
        }

        @Override
        String toHex(byte[] data) {
            return new String(data);
        }
    };

    private final String algoName;

    EncodingAlgo(String algoName) {
        this.algoName = algoName;
    }

    public static Optional<EncodingAlgo> parseEncodingAlgo(String algoName) {
        return stream(values())
                .filter(value -> value.algoName.equals(algoName))
                .collect(toOptional());
    }

    abstract byte[] encode(byte[] data);

    abstract byte[] decode(byte[] data);

    abstract String toHex(byte[] data);
}
