Beispiel Anfragen 

Buch erstellen:
curl -X POST http://localhost:8083/lending/books -H "Content-Type: application/json" -d '{"title": "Testbuch", "isbn": "123456"}'

Ausleihe anfragen:
curl -X POST http://localhost:8083/lending/loans/request -H "Content-Type: application/json" -d '{"bookTitle": "Testbuch", "userId": "123e4567-e89b-12d3-a456-426614174000"}'

Verlängerung (Ersetze <bookId> durch die ID aus der Response):
curl -X PUT http://localhost:8083/lending/loans/<bookId>/extend?userId=123e4567-e89b-12d3-a456-426614174000

Bestand abfragen:
curl -X GET http://localhost:8083/lending/inventory/123e4567-e89b-12d3-a456-426614174000

Verfügbare Bücher:
curl -X GET http://localhost:8083/lending/inventory/available


########## Requests against extern services ############

### Open library - GET Request
https://openlibrary.org/search.json?title=Java

### JSONPlaceholder API - POST Request
curl -X POST https://jsonplaceholder.typicode.com/posts \
-H "Content-Type: application/json" \
-d '{
"title": "Payment for Order: ORD1234",
"body": "Amount: 101.0 EUR",
"userId": 1
}'


########## Lasttest #############
Vor jedem Lasttest alles neu initialisieren

docker compose down --volumes --rmi local
docker compose up -d --build

Dann den Lasttest starten 
./loadtest.sh