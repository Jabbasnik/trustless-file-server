# Trustless File Server

## Requirements:
`Java 17`

## Running the app

Note that by default, if no path is specified application will run with default file (*icons_rgb_circle.png*).

### 1. Quick run using gradle
```
gradle bootRun -Pargs="--filePath=FULL_PATH_TO_FILE"
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

*The example responses below are based on the file `icons_rgb_circle.png` provided with the specification as a test case*

#### GET /hashes

This endpoint should return a json list of the merkle hashes and number of 1KB pieces of the files this server is serving. In our case this will be a singleton array.

Example:
```sh
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/hashes
```

```json
[{
  "hash": "9b39e1edb4858f7a3424d5a3d0c4579332640e58e101c29f99314a12329fc60b",
  "pieces": 17
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
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/piece/9b39e1edb4858f7a3424d5a3d0c4579332640e58e101c29f99314a12329fc60b/8
```

```json
{
  "content": "1wSDXYz+dPEXQP9oAYKE7Tz5ttGgCYkD3ile/OXpP4AAAPTqv+BlsRiHgknDtgQv/orRny7+AhAAgB7a+tKLxbYEp8bkJiY7bdm/L7n35ek/QN/NOQMAGYi+8c17X7AQLf8MUxOjP83+B+jzn71XLs+ZAgQZiKkxO7QCtffz27kjyYu/zP0HGAwtQJCJGA36zFtvWIgWSrWF646n/wACAFBzIfnqL7qTZGiXFC/+uvPpZ0Z/AvTfpW4AmL9yedpaQD5iKpDRoO0q/lMc/an9B2Ag5roBwDpAXuI8wLOnTlqItgSABEd/xsVf97/+xocLMCACAGQoxkkaDdqGz2m8e3YjNXc+1fsPMCDfLQ0As9YD8vLM735jNGjDpdj7Hxd/Gf0JMDAzSwOAUaCQoWdPn3QeoKHi4i+jPwGogxYgyPkPgOefK3YYDdpITyfYouXiL4CBe6AFyG3AkKl4ymw0aLPErkyK7T93P/+imL923QcMMDhagIAFMRo0Wk5ohij+Y1pTam5+OOXDBWgALUBAt9jcaTRoY6Q4oenu+S9d/AUweHNLA8C09YC8xbjJ7a93LMSARTuWi78AqMP8lcsPBACAYvuvj3VnzzM42xK8+CtGf9676KgZQFMsBoA5SwGEnSffNhp0QOJehrikLTV6/wEa4cIDAWBxOwAg2k/iUDD9t83oTwD68b/1S/71VcsBhK0vvZjkGMomi10XF38BUKO5lQKABk3gB8+89Ua3JYX+SPHpf7jj6T9AowMAwA9iNOgOrUACwEaK/08/M/oToDm+WykATFsXYKkYDRo7AdQr1Yu/tP8ANMrMSgEA4CHbXv2F0aB1B4AER3/euzhb3P/6Gx8uQHOsuAPgDACwomdPnzQatCYRrmKnJTV3PtX7D9Ak81cur7gD8J2lAVYS7SnPnjppIWqQYu9/XPxl9CdAc9kBAFYlLqhKdVLNoDz10590R66mRu8/QONcWDEAzF+5bAcAeKxnfvcbo0F76GkXfwEwAMsPAc9aEuBxYjSo8wAbF2uY4mVrdz//opi/dt0HDNAs00v/j83L/p92AViXexdniv+z76VsC7mRf/mnYuj557J4v3FgdcdbbxTX3nnPF38DUh39efPDKR8uQPM8UOMPPS4dAE8WTzu/f/NEVu95+PDLxdZDB3z4G5DieYq757908RdAM808LgDYAYB1iHnnNz86k9V73vn7t7uHWFm7CE8p7hg5/AvQWHOPCwAmAcE6RetDXH6Ui2hfifMArN22V9Mc/RmtgAA0z/yVy48NAHOWCNbv+jvvdaeg5CJGg25/veODX4OYohTrlmIABqCRHno6OfS4dACsTfQ/53Y4dvuvj3Vvs2V1thn9CQ==",
  "proof": [
    "6a10a0b8c1bd3651cba6e5604b31df595e965be137650d296c05afc1084cfe1f", // sibling hash
    "956bf86d100b2f49a8d057ebafa85b8db89a0f19d5627a1226fea1cb3e23d3f3", // uncle hash
    "04284ddea22b003e6098e7dd1a421a565380d11530a35f2e711a8dd2b9b5e7f8", // uncle's uncle hash
    "c66a821b749e0576e54b89dbac8f71211a508f7916e3d6235900372bed6c6c22", // etc.
    "a8bd48117723dee92524c25730f9e08e5d47e78c87d17edb344d4070389d049e"  // child of root
  ]
}
```

---

## DECISION LOG AND FUTURE GOALS
* ✔ Write whole app using hexagonal architecture
* ✔ Provide base functionalities
* ✔ Enable easy hashing/encoding algorithm extension
* ✔ Serve proofs using Merkle Proof Tree
* ❌ Introduce way of starting the application with Docker
* ❌ Add E2E tests using Testcontainers
* ❌ Extend current way of handling hashing and encoding to let user choose specific hashing/encoding algorithm <- only application layer will be touched
* ❌ Rewrite app using RUST
* ❌ Add a way to dynamically add files and pieces
