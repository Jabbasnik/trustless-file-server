package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Optional;

import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Arrays.stream;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.codec.digest.DigestUtils.sha1;
import static org.apache.commons.codec.digest.DigestUtils.sha256;

public enum HashingAlgo {
    SHA_256("SHA-256") {
        @Override
        byte[] hash(byte[] data) {
            return sha256(data);
        }

        @Override
        String toHex(byte[] data) {
            return encodeHexString(data);
        }
    },
    SHA_1("SHA-1") {
        @Override
        byte[] hash(byte[] data) {
            return sha1(data);
        }

        @Override
        String toHex(byte[] data) {
            return encodeHexString(data);
        }
    };

    private final String algoName;

    HashingAlgo(String algoName) {
        this.algoName = algoName;
    }

    public static Optional<HashingAlgo> parseHashingAlgo(String algoName) {
        return stream(values())
                .filter(value -> value.algoName.equals(algoName))
                .collect(toOptional());
    }

    abstract byte[] hash(byte[] data);

    abstract String toHex(byte[] data);
}
