package de.demo.lending;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class PackageDependency {
    public static void main(String[] args) throws IOException {
        JavaClasses classes = new ClassFileImporter().importPackages("de.demo.lending");

        Set<String> edges = new HashSet<>();

        String basePackage = "de.demo.lending";
        int minLevel = 1;
        int maxLevel = 2;

        // nur bestimmte Packages anzeigen
        List<String> allowedPackages = List.of(
                "de.demo.lending.loan.application",
                "de.demo.lending.inventory.application",
                "de.demo.lending.procurement.application",
                "de.demo.lending.payment.application",
                "de.demo.lending.loan.domain",
                "de.demo.lending.inventory.domain",
                "de.demo.lending.procurement.domain",
                "de.demo.lending.payment.domain",
                "de.demo.lending.loan.adapter",
                "de.demo.lending.inventory.adapter",
                "de.demo.lending.procurement.adapter",
                "de.demo.lending.payment.adapter"

        );

        classes.forEach(origin -> {
            String originPkg = getPackageLevelInRange(origin.getPackageName(), minLevel, maxLevel, basePackage);
            if (originPkg == null || allowedPackages.stream().noneMatch(originPkg::startsWith)) return;

            origin.getDirectDependenciesFromSelf().stream()
                    .map(dep -> getPackageLevelInRange(dep.getTargetClass().getPackageName(), minLevel, maxLevel, basePackage))
                    .filter(pkg -> pkg != null)
                    .filter(pkg -> allowedPackages.stream().anyMatch(pkg::startsWith))
                    .forEach(targetPkg -> {
                        if (!originPkg.equals(targetPkg)) {
                            edges.add("  \"" + originPkg + "\" -> \"" + targetPkg + "\";");
                        }
                    });
        });

        // DOT-Datei schreiben
        StringBuilder dot = new StringBuilder();
        dot.append("digraph packages {\n");
        edges.forEach(dot::append);
        dot.append("}\n");

        try (FileWriter writer = new FileWriter("target/package-deps.dot")) {
            writer.write(dot.toString());
        }

        System.out.println("DOT-Datei erzeugt: target/package-deps.dot");
        System.out.println("Mit Graphviz rendern: dot -Tpng target/package-deps.dot -o target/package-deps.png");
    }

    private static String getPackageLevelInRange(String fullPackage, int minLevel, int maxLevel, String basePackage) {
        String remainder = fullPackage.startsWith(basePackage + ".")
                ? fullPackage.substring(basePackage.length() + 1)
                : fullPackage;

        String[] parts = remainder.split("\\.");
        if (parts.length < minLevel) return null;

        int level = Math.min(parts.length, maxLevel);
        StringBuilder sb = new StringBuilder(basePackage);
        for (int i = 0; i < level; i++) {
            sb.append(".").append(parts[i]);
        }
        return sb.toString();
    }
}
