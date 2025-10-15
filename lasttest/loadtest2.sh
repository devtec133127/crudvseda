#!/bin/bash

# Konfiguration
CREATE_URL="http://localhost:8083/lending/books"
LOAN_URL="http://localhost:8083/lending/loans/request"
TOTAL_REQUESTS=1000
THREADS=50
REQUESTS_PER_THREAD=$((TOTAL_REQUESTS / THREADS))
USER_ID="123e4567-e89b-12d3-a456-426614174000"  # Feste UUID für alle Loans (ein User leiht alle)

SUCCESS=0
FAIL=0
START_TIME=$(date +%s)

echo "Starte Lasttest: Erstelle $TOTAL_REQUESTS Bücher, dann $TOTAL_REQUESTS Loan-Requests in $THREADS Threads an $LOAN_URL..."

# Phase 1: Sequentiell 1000 Bücher erstellen (Setup, um Verfügbarkeit zu gewährleisten)
echo "Phase 1: Bücher erstellen..."
create_success=0
create_fail=0
for i in $(seq 1 $TOTAL_REQUESTS); do
    TITLE="Book-$i"
    ISBN="ISBN-1234567890-$i"  # Unique pro Buch
    RESPONSE=$(curl -s -w "%{http_code}" -X POST $CREATE_URL \
        -H "Content-Type: application/json" \
        -d "{\"title\": \"$TITLE\", \"isbn\": \"$ISBN\"}" \
        -o /dev/null 2>/dev/null)

    if [ "$RESPONSE" = "200" ]; then
        ((create_success++))
    else
        ((create_fail++))
    fi

    echo -ne "\rBücher erstellt: $i/$TOTAL_REQUESTS (Success: $create_success, Fail: $create_fail)" >&2
done
echo -e "\nBücher-Setup abgeschlossen: $create_success Erfolge, $create_fail Fehler."

# Phase 2: Parallele Loan-Requests (50 Threads, je 20 Requests)
echo "Phase 2: Starte $THREADS Threads für Loan-Requests..."
for thread in $(seq 1 $THREADS); do
    (
        thread_success=0
        thread_fail=0
        start_book=$(((thread - 1) * REQUESTS_PER_THREAD + 1))
        end_book=$((start_book + REQUESTS_PER_THREAD - 1))

        for j in $(seq $start_book $end_book); do
            BOOK_TITLE="Book-$j"
            RESPONSE=$(curl -s -w "%{http_code}" -X POST $LOAN_URL \
                -H "Content-Type: application/json" \
                -d "{\"bookTitle\": \"$BOOK_TITLE\", \"userId\": \"$USER_ID\"}" \
                -o /dev/null 2>/dev/null)

            if [ "$RESPONSE" = "200" ]; then
                ((thread_success++))
            else
                ((thread_fail++))
            fi

            echo -ne "\rThread $thread: Buch $j (Success: $thread_success, Fail: $thread_fail)" >&2
        done

        # Thread-Ergebnisse global sammeln (via Datei oder Pipe; hier einfach echo für Log)
        echo "Thread $thread: $thread_success Success, $thread_fail Fail" >&2

        # Globale Zähler inkrementieren (mit lock-simuliertem Update)
        echo "$thread_success $thread_fail" >> /tmp/thread_results.tmp
    ) &
done

wait  # Warte auf alle Threads

# Ergebnisse aus Threads sammeln
if [ -f /tmp/thread_results.tmp ]; then
    while read line; do
        thread_success=$(echo $line | cut -d' ' -f1)
        thread_fail=$(echo $line | cut -d' ' -f2)
        SUCCESS=$((SUCCESS + thread_success))
        FAIL=$((FAIL + thread_fail))
    done < /tmp/thread_results.tmp
    rm /tmp/thread_results.tmp
fi

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
AVG_TIME=$(echo "scale=2; if($TOTAL_TIME > 0) $TOTAL_TIME / $TOTAL_REQUESTS else 0" | bc -l)

echo -e "\n\nTest abgeschlossen!"
echo "Gesamtzeit (inkl. Setup): ${TOTAL_TIME} Sekunden"
echo "Durchschnitt pro Loan-Request: ${AVG_TIME} Sekunden"
echo "Loan-Erfolge: $SUCCESS, Fehler: $FAIL"
echo "Throughput (Loans): $(echo "scale=2; if($TOTAL_TIME > 0) $TOTAL_REQUESTS / $TOTAL_TIME else 0" | bc -l) Requests/Sekunde"