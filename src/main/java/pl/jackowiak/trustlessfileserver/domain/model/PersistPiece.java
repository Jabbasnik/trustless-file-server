package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Objects;
import java.util.StringJoiner;

import static pl.jackowiak.trustlessfileserver.domain.model.EncodingAlgo.parseEncodingAlgo;
import static pl.jackowiak.trustlessfileserver.domain.model.HashingAlgo.parseHashingAlgo;

public final class PersistPiece {

    private final MerkleHash merkleHash;
    private final MerkleEncoded merkleEncoded;

    public PersistPiece(byte[] pieceContent, String hashingAlgoName, String encodingAlgoName) {
        var hashingAlgo = determineHashingAlgo(hashingAlgoName);
        var encodingAlgo = determineEncodingAlgo(encodingAlgoName);
        this.merkleEncoded = new MerkleEncoded(encodingAlgo, pieceContent);
        this.merkleHash = new MerkleHash(pieceContent, hashingAlgo);
    }

    private static HashingAlgo determineHashingAlgo(String hashingAlgo) {
        return parseHashingAlgo(hashingAlgo)
                .orElseThrow(
                        () -> new IllegalArgumentException("Unable to hash piece. Reason: No hashing algorithm present in hashing algorithms registry"));
    }

    private static EncodingAlgo determineEncodingAlgo(String encodingAlgo) {
        return parseEncodingAlgo(encodingAlgo)
                .orElseThrow(
                        () -> new IllegalArgumentException("Unable to hash piece. Reason: No hashing algorithm present in hashing algorithms registry"));
    }

    public MerkleHash getMerkleHash() {
        return merkleHash;
    }

    public MerkleEncoded getMerkleEncoded() {
        return merkleEncoded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersistPiece that = (PersistPiece) o;
        return Objects.equals(merkleHash, that.merkleHash) && Objects.equals(merkleEncoded, that.merkleEncoded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merkleHash, merkleEncoded);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PersistPiece.class.getSimpleName() + "[", "]")
                .add("merkleHash=" + merkleHash)
                .add("merkleEncoded=" + merkleEncoded)
                .toString();
    }
}
