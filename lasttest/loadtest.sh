#!/bin/bash

URL="http://localhost:8083/lending/books"
REQUESTS=1000
SUCCESS=0
FAIL=0
START_TIME=$(date +%s)

echo "Starte Lasttest mit $REQUESTS Requests an $URL..."

for i in $(seq 1 $REQUESTS); do
    TITLE="Testbuch-$i"
    TIMESTAMP=$(date +%s)
    RANDOM_NUM=$((RANDOM % 1000))
    ISBN="1234567890-${TIMESTAMP}-${RANDOM_NUM}-${i}"

    RESPONSE=$(curl -s -w "%{http_code}" -X POST $URL \
        -H "Content-Type: application/json" \
        -d "{\"title\": \"$TITLE\", \"isbn\": \"$ISBN\"}" \
        -o /dev/null 2>/dev/null)

    if [ "$RESPONSE" = "200" ]; then
        ((SUCCESS++))
    else
        ((FAIL++))
    fi

    echo -ne "\rFortschritt: $i/$REQUESTS (Success: $SUCCESS, Fail: $FAIL)" >&2
done

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
AVG_TIME=$(echo "scale=2; $TOTAL_TIME / $REQUESTS" | bc)

echo -e "\n\nTest abgeschlossen!"
echo "Gesamtzeit: ${TOTAL_TIME} Sekunden"
echo "Durchschnitt pro Request: ${AVG_TIME} Sekunden"
echo "Erfolge: $SUCCESS, Fehler: $FAIL"
echo "Throughput: $(echo "scale=2; $REQUESTS / $TOTAL_TIME" | bc) Requests/Sekunde"