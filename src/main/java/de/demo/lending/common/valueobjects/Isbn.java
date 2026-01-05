package de.demo.lending.common.valueobjects;

public record Isbn(String value) {
    /**
     * Kompakter Konstruktor mit Validierung
     */
    public Isbn {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ISBN darf nicht leer sein");
        }

        value = normalize(value);

        if (!isValidFormat(value)) {
            throw new IllegalArgumentException(
                    "Ungültiges ISBN-Format: " + value + ". Erwartet: ISBN-10 oder ISBN-13"
            );
        }
    }

    /**
     * Factory Method (optional, für klareren Intent)
     */
    public static Isbn of(String isbn) {
        return new Isbn(isbn);
    }

    private static String normalize(String isbn) {
        return isbn.replaceAll("[-\\s]", "").toUpperCase();
    }

    private static boolean isValidFormat(String isbn) {
        // ISBN-10: 10 Zeichen (9 Ziffern + Checkziffer X)
        if (isbn.length() == 10) {
            return isbn.substring(0, 9).matches("\\d{9}")
                    && (Character.isDigit(isbn.charAt(9)) || isbn.charAt(9) == 'X');
        }

        // ISBN-13: 13 Ziffern
        if (isbn.length() == 13) {
            return isbn.matches("\\d{13}");
        }

        return false;
    }

    /**
     * Formatiert ISBN mit Bindestrichen
     */
    public String formatted() {
        if (value.length() == 10) {
            return String.format("%s-%s-%s-%s",
                    value.substring(0, 1),
                    value.substring(1, 3),
                    value.substring(3, 9),
                    value.substring(9));
        } else { // ISBN-13
            return String.format("%s-%s-%s-%s-%s",
                    value.substring(0, 3),
                    value.substring(3, 4),
                    value.substring(4, 6),
                    value.substring(6, 12),
                    value.substring(12));
        }
    }

    @Override
    public String toString() {
        return value;
    }
}