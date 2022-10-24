package pl.jackowiak.trustlessfileserver.domain.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * The Merkle node.
 */
public final class MerkleNode extends MerkleTreeElement {

    private final MerkleTreeElement left;

    private final MerkleTreeElement right;

    public MerkleNode(MerkleHash hash, MerkleTreeElement left, MerkleTreeElement right) {
        super(hash);
        this.left = left;
        this.right = right;
    }

    public MerkleTreeElement left() {
        return left;
    }

    public MerkleTreeElement right() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MerkleNode that = (MerkleNode) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), left, right);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MerkleNode.class.getSimpleName() + "[", "]")
                .add("left=" + left)
                .add("right=" + right)
                .add("hash=" + hash)
                .toString();
    }
}
