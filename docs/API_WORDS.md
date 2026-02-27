# TEPS Words API (Current)

Base URL: `http://localhost:8080`

## Random word

### GET `/api/words/random`
Query:
- `type` (optional): `concepts` | `regular` (default: `concepts`)
- `partOfSpeech` (optional): only applied when `type=concepts`

Response (200):
```json
{
  "seq": 381,
  "word": "scholars",
  "partOfSpeech": "n.",
  "meaning": "장학생"
}
```

## Progress APIs

### POST `/api/words/bookmarks`
Body:
```json
{
  "wordType": "concepts",
  "seq": 381,
  "word": "scholars",
  "partOfSpeech": "n.",
  "meaning": "장학생"
}
```

### DELETE `/api/words/bookmarks`
Query: `wordType, seq, word, partOfSpeech, meaning`

### POST `/api/words/wrongs`
Body: same as bookmark payload

### GET `/api/words/progress`
Query: `wordType, seq, word, partOfSpeech, meaning`

Response:
```json
{ "bookmarked": true, "wrongCount": 2 }
```

### GET `/api/words/bookmarks`
Recent bookmarks list (max 100)

### GET `/api/words/wrongs`
Recent wrong answers list (max 100)

## Notes
- `type=regular` currently ignores `partOfSpeech`.
- Frontend expects `/api` proxy to backend 8080.
