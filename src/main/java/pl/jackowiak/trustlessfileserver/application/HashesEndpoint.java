package pl.jackowiak.trustlessfileserver.application;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import pl.jackowiak.trustlessfileserver.application.model.Hashes;
import pl.jackowiak.trustlessfileserver.application.model.PieceData;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleEncoded;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleHash;
import pl.jackowiak.trustlessfileserver.domain.model.MerkleTreeElement;
import pl.jackowiak.trustlessfileserver.domain.model.PieceProof;
import pl.jackowiak.trustlessfileserver.domain.ports.in.ServerFiles;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;

/**
 * Controller serving file server endpoints.
 */
@RestController
@RequestMapping("/")
class HashesEndpoint {

    private final ServerFiles serverFiles;

    HashesEndpoint(ServerFiles serverFiles) {
        this.serverFiles = serverFiles;
    }

    @GetMapping(value = "/hashes", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<?> hashes() {
        var availableFiles = serverFiles.availableFiles();
        var response = availableFiles.entrySet().stream()
                .map(this::createHashesResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private Hashes createHashesResponse(Map.Entry<MerkleHash, Integer> entry) {
        var key = entry.getKey();
        return new Hashes(key.getHexString(), entry.getValue());
    }

    @GetMapping(value = "/piece/{hashId}/{pieceIndex}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<?> getAppRoot(@PathVariable String hashId, @PathVariable Integer pieceIndex) throws DecoderException {
        var merkleHash = MerkleHash.rawHash(Hex.decodeHex(hashId));
        return serverFiles.getProofForPiece(merkleHash, pieceIndex)
                .map(this::mapToPieceData)
                .fold(domainFailure -> badRequest().body(domainFailure.reason()), ResponseEntity::ok);
    }

    private PieceData mapToPieceData(PieceProof proofForPiece) {
        var stringProofs = proofForPiece.proofs().stream()
                .map(MerkleTreeElement::hash)
                .map(MerkleHash::getHexString)
                .toList();
        var content = proofForPiece.content();
        return new PieceData(content.getHexString(), stringProofs);
    }
}
