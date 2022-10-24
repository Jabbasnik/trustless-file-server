package pl.jackowiak.trustlessfileserver.domain.model;

import static java.lang.String.format;

public final class FailureMessages {
    public static final String PIECE_NOT_FOUND_IN_REPO = "Piece for given merkle hash <%s> and piece index <%d> not found in database";
    public static final String MERKLE_TREE_NOT_FOUND_REPO = "Merkle Tree with hash <%s> not found in database.";
    public static final String PIECE_NOT_FOUND_IN_MERKLE_TREE = "Piece with hash <%s> not present in selected merkle tree!";
    public static final String PIECE_CONTENT_NOT_FOUND_IN_REPO = "Piece content not found in database for piece with hash <%s>.";

    private FailureMessages() {
        throw new AssertionError(format("The class \"%s\" is not instantiable", this.getClass()));
    }
}
