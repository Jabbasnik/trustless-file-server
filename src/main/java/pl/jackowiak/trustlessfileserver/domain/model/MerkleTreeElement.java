package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Objects;

/**
 * The common Merkle tree element.
 */
public abstract class MerkleTreeElement {

    protected MerkleHash hash;

    protected MerkleTreeElement(MerkleHash hash) {
        this.hash = hash;
    }

    public MerkleHash hash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleTreeElement that = (MerkleTreeElement) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
