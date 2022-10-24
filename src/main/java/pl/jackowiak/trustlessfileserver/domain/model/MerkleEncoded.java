package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public final class MerkleEncoded {
    private final EncodingAlgo encodingAlgo;
    private final byte[] encodedContent;
    private final String hexString;

    public MerkleEncoded(EncodingAlgo encodingAlgo, byte[] content) {
        this.encodingAlgo = encodingAlgo;
        this.encodedContent = encodingAlgo.encode(content);
        this.hexString = encodingAlgo.toHex(encodedContent);
    }

    public EncodingAlgo getEncodingAlgo() {
        return encodingAlgo;
    }

    public byte[] getEncodedContent() {
        return encodedContent;
    }

    public String getHexString() {
        return hexString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleEncoded that = (MerkleEncoded) o;
        return encodingAlgo == that.encodingAlgo && Arrays.equals(encodedContent, that.encodedContent) && Objects.equals(hexString, that.hexString);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(encodingAlgo, hexString);
        result = 31 * result + Arrays.hashCode(encodedContent);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MerkleEncoded.class.getSimpleName() + "[", "]")
                .add("encodingAlgo=" + encodingAlgo)
                .add("encodedContent=" + Arrays.toString(encodedContent))
                .add("hexString='" + hexString + "'")
                .toString();
    }
}
