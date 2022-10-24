package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static pl.jackowiak.trustlessfileserver.domain.model.HashingAlgo.SHA_256;

public final class MerkleHash {
    private final HashingAlgo hashingAlgo;
    private final byte[] hash;
    private final String hexString;

    public MerkleHash(byte[] content, HashingAlgo hashingAlgo) {
        this.hashingAlgo = hashingAlgo;
        this.hash = hashingAlgo.hash(content);
        this.hexString = hashingAlgo.toHex(hash);
    }

    private MerkleHash(byte[] content) {
        this.hashingAlgo = SHA_256;
        this.hash = content;
        this.hexString = hashingAlgo.toHex(hash);
    }

    public static MerkleHash rawHash(byte[] content) {
        return new MerkleHash(content);
    }

    public HashingAlgo getHashingAlgo() {
        return hashingAlgo;
    }

    public byte[] getHash() {
        return hash;
    }

    public String getHexString() {
        return hexString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleHash that = (MerkleHash) o;
        return hashingAlgo == that.hashingAlgo && Arrays.equals(hash, that.hash) && Objects.equals(hexString, that.hexString);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(hashingAlgo, hexString);
        result = 31 * result + Arrays.hashCode(hash);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MerkleHash.class.getSimpleName() + "[", "]")
                .add("hashingAlgo=" + hashingAlgo)
                .add("hash=" + Arrays.toString(hash))
                .add("hexString='" + hexString + "'")
                .toString();
    }
}
