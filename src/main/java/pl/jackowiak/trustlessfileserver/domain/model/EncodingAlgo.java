package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Optional;

import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Arrays.stream;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

public enum EncodingAlgo {
    BASE_64("BASE_64") {
        @Override
        byte[] encode(byte[] data) {
            return encodeBase64(data);
        }

        @Override
        String toHex(byte[] data) {
            return new String(decodeBase64(data));
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

    abstract String toHex(byte[] data);
}
