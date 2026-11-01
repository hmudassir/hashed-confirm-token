# Hashed confirmation token

A signup returns a random token **once**. The database stores only the SHA-256 hash of that token. Confirming hashes the token the caller sent and looks up that hash.

After a successful confirm the hash is cleared, so the same token cannot be used again. An unknown token and a used token both return `404`. The response never contains the hash.

## Run

Java 21.

```sh
./mvnw spring-boot:run
```

```sh
curl -sS -H 'Content-Type: application/json' \
  -d '{"email":"ada@example.com"}' \
  http://127.0.0.1:8080/api/confirmations

curl -sS -X POST http://127.0.0.1:8080/api/confirmations/confirm \
  -H 'Content-Type: application/json' \
  -d '{"token":"PASTE_THE_TOKEN"}'
```

## Tests

```sh
./mvnw test
```

## What this is

A portfolio proof of concept. It does not send the token by email. The outbox sample shows how that send would be stored in the same transaction.
