# Trustless File Server

## Requirements:
`Java 17`

---

## Running the app

Note that by default, if no path is specified application will run with default file (*pug-in-space.jpg*).

### 1. Quick run using gradle
```
./gradlew bootRun -Pargs="--filePath=FULL_PATH_TO_FILE"
```

### 2. Running using jar directly
```
1. Build the project and copying jar to ./build/application
    ./gradlew clean filesCopy 
    
2. Go to directory ./build/application/ and launch an app trustless-file-server.jar
    java -jar trustless-file-server.jar --filePath="RELATIVE_PATH_TO_FILE"
```

### 3. Running tests:
```
Run gradle tests:
    ./gradlew clean filesCopy
```

---

## Description
Following repository contains a proof of concept of trustless file server. 

### Main Goals
* application can store file pieces together with a proof of data integrity
* user is able to get available files and query for each one together with proof of inclusion
* application exposes endpoints for all possible actions user can take

### Architecture overview
Whole application is build using spring boot framework with spirit of **Hexagonal architecture**.
Spring framework enables easy inversion of control over handling actions related to serve as valid REST server.
The beauty of **Hexagonal architecture** enables an easy way of switching between clients (also called application layer) and also switching infrastructure layer, in most cases used as persistence layer, while keeping the precious domain layer untouched.

Having in mind the above, it's easy to switch between any other framework or plain java aplication layer as well as infrastructure layer, adding for example real database rather than keeping data in memory.

Files are being loaded on app startup using one of methods mentioned in **Running the app**. 
Files are split to chunks of *1024 Bytes*, encoded to BASE64 standard and then stored in repository.

Each file chunk is also included in **Merkle Tree** used for easy way of proving the inclusion of specific chunk to file.

Each piece proof of inclusion is calculated basing on construction of Merkle Proof Tree and then listed, on each piece query.

### Examples

*The example responses below are based on the file `pug-in-space.jpg`*

#### GET /hashes

This endpoint returns a json list of the merkle hashes and number of 1KB pieces of the files this server is serving. In our case this will be a singleton array.

Example:
```sh
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/hashes
```

```json
[{
  "hash": "3bbf3e0a2762bc092b329250638ac25bce4ef402cdf8a911ce5089304506d6c2",
  "pieces": 136
}]
```

#### GET /piece/:hashId/:pieceIndex

This endpoint returns a verifiable piece of the content.

| Parameter   | Description                                                                         |
|-------------|-------------------------------------------------------------------------------------|
| :hashId     | the merkle hash of the file we want to download                                     |
| :pieceIndex | the index of the piece we want to download (from zero to 'filesize divided by 1KB') |

The returned object will contain two fields:

| Field   | Description                                                                                                                                                                                                 |
|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| content | The binary content of the piece encoded in base64.                                                                                                                                                          |
| proof   | A list of hashes hex encoded to prove that the piece is legitimate. The first hash will be the hash of the sibling and the next will be the uncle's hash, the next the uncle of the uncle's hash and so on. |

Example:
```sh
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/piece/3bbf3e0a2762bc092b329250638ac25bce4ef402cdf8a911ce5089304506d6c2/0
```

```json
{
  "content": "/9j/4QC8RXhpZgAASUkqAAgAAAAGABIBAwABAAAAAQAAABoBBQABAAAAVgAAABsBBQABAAAAXgAAACgBAwABAAAAAgAAABMCAwABAAAAAQAAAGmHBAABAAAAZgAAAAAAAABJGQEA6AMAAEkZAQDoAwAABgAAkAcABAAAADAyMTABkQcABAAAAAECAwAAoAcABAAAADAxMDABoAMAAQAAAP//AAACoAQAAQAAAAAEAAADoAQAAQAAAEACAAAAAAAA/+IB2ElDQ19QUk9GSUxFAAEBAAAByGxjbXMCEAAAbW50clJHQiBYWVogB+IAAwAUAAkADgAdYWNzcE1TRlQAAAAAc2F3c2N0cmwAAAAAAAAAAAAAAAAAAPbWAAEAAAAA0y1oYW5knZEAPUCAsD1AdCyBnqUijgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJZGVzYwAAAPAAAABfY3BydAAAAQwAAAAMd3RwdAAAARgAAAAUclhZWgAAASwAAAAUZ1hZWgAAAUAAAAAUYlhZWgAAAVQAAAAUclRSQwAAAWgAAABgZ1RSQwAAAWgAAABgYlRSQwAAAWgAAABgZGVzYwAAAAAAAAAFdVJHQgAAAAAAAAAAAAAAAHRleHQAAAAAQ0MwAFhZWiAAAAAAAADzVAABAAAAARbJWFlaIAAAAAAAAG+gAAA48gAAA49YWVogAAAAAAAAYpYAALeJAAAY2lhZWiAAAAAAAAAkoAAAD4UAALbEY3VydgAAAAAAAAAqAAAAfAD4AZwCdQODBMkGTggSChgMYg70Ec8U9hhqHC4gQySsKWoufjPrObM/1kZXTTZUdlwXZB1shnVWfo2ILJI2nKunjLLbvpnKx9dl5Hfx+f///9sAQwAGBAUGBQQGBgUGBwcGCAoQCgoJCQoUDg8MEBcUGBgXFBYWGh0lHxobIxwWFiAsICMmJykqKRkfLTAtKDAlKCko/9sAQwEHBwcKCAoTCgoTKBoWGigoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgo/8IAEQgCQAQAAwERAAIRAQMRAf/EABsAAAIDAQEBAAAAAAAAAAAAAAIDAAEEBQYH/8QAGgEAAwEBAQEAAAAAAAAAAAAAAAECAwQFBv/aAAwDAQACEAMQAAAB+VMgRqwgRkCMgRkCxQIECMgRkCBbIEagQIFhQWECBAgQIECBAgRkCBAgQIECBAgQIECBAjLFAgQIECwjIECMgREZAgWlGQIFhGQVhGQIECxWyBYQCHAIIBDtOwgGnadMtBqiHQEnaDVHNWFBAgEqiCHaLTg7CIIZJ0ECIgQcR5CuCA==",
  "proofs": [
    "621accb08a549d314c2a55e7ad5917be24e2614fbe4666853227a1eb00e78c0e",
    "d5bcd1a55ebb50486874e00a69287e16e0d2e648bb1ad8355ea034c8df749e9b",
    "a7041c394a5d301cfa2c0f78642b8c1a54324d5e92e2ba70fbd25de2c951e753",
    "76f1e72be621ca9e7ab204538e4df1feca45ff0df94d87c09a69c6052b5a165c",
    "878fbf9448893d6a9e2c4676505c14d21b614a1f2a234678d8b9b08bdd9fda63",
    "e9fb30ef1eb1312c2120da7f06b3c51028d8b18491e6ecee1ab325965432f9d9",
    "189e01eb7f5c55f38a9cf82aceea1659ad3b1c0696392c1be9a3d44128d40fe3",
    "f8f0cc95b7770996e2ce314d2c729d32c56d8372c9fca01d96bca40634cd3415"
  ]
}
```

---

## DECISION LOG AND FUTURE GOALS
* ✔ Write whole app using hexagonal architecture with easy transition to CQRS app
* ✔ Provide base functionalities
* ✔ Enable easy hashing/encoding algorithm extension
* ✔ Serve proofs using Merkle Proof Tree
* ❌ Introduce way of starting the application with Docker
* ❌ Add E2E tests using Testcontainers
* ❌ Extend current way of handling hashing and encoding to let user choose specific hashing/encoding algorithm <- only application layer will be touched
* ❌ Rewrite app using RUST
* ❌ Add a way to dynamically add files and pieces - Merkelized Tree
