package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.StringJoiner;

/**
 * The Merkle leaf.
 */
public final class MerkleLeaf extends MerkleTreeElement {

    public MerkleLeaf(MerkleHash hash) {
        super(hash);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MerkleLeaf.class.getSimpleName() + "[", "]")
                .toString();
    }
}
