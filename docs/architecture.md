# Architecture

The confirmation secret is shown once and stored as a hash. The confirm step can succeed only while that hash is still on the row.

## Boundaries

```text
POST /api/confirmations
  SecureRandom 32 bytes
  return the token in the response
  store SHA-256(token) on the row
  status PENDING

POST /api/confirmations/confirm
  hash the token the caller sent
  look up token_hash
        |
        +--> no row     404
        +--> row found  status CONFIRMED, token_hash cleared
```

The response never includes the hash. After confirm, the raw token no longer matches any row.

## Request path

1. Issue normalizes the email and refuses a second row for the same email with `409`.
2. The token is 32 random bytes, encoded for the response.
3. Confirm hashes the presented token and loads the row by that hash.
4. The row moves to `CONFIRMED` and the hash is set to null in the same transaction.

An unknown token and a token that was already used both miss the lookup. Both return `404` with the same message.

## Failure

There is no second success for the same token. Clearing the hash is what makes the token one-time. Leaving the hash in place would let the same secret confirm again.

This sample does not send the token. The outbox sample is the place a send would be recorded in the transaction that stores the hash. The email would carry the raw token. The database would still store the hash.

## What this sample leaves out

No expiry and no resend. Adding either would create a new random token, store the new hash, and clear the previous one. The confirm lookup would stay a hash comparison.
