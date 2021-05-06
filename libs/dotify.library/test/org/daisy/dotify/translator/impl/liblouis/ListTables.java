package org.daisy.dotify.translator.impl.liblouis;

import org.liblouis.CompilationException;
import org.liblouis.Louis;
import org.liblouis.Translator;

import java.util.Comparator;
import java.util.logging.Logger;

/**
 * TODO: write java doc.
 */
public class ListTables {
    private static final Logger logger = Logger.getLogger(ListTables.class.getCanonicalName());

    public static void main(String[] args) {
        Louis.listTables().stream()
            .map(TblInfo::new)
            .filter(v -> "literary".equals(v.type) && !"sv".equals(v.locale))
            .sorted(Comparator.comparing(o -> o.locale))
            .forEach(tii -> {
                String path = null;
                try {
                    path = tii.table.getTranslator().getTable();
                } catch (CompilationException e2) {
                    logger.severe("Comp failed");
                }
                if (path != null) {
                    try {
                        new Translator(path);
                        String prefix = "specs.put(";
                        String postfix = ", \"" + path + "\");";
                        String eightDot = tii.eightDot ? ".dotsPerCell(DotsPerCell.EIGHT)" : "";
                        if ("full".equalsIgnoreCase(tii.contraction)) {
                            logger.info(
                                String.format(
                                    "%snew TranslatorSpecification(\"%s\", " +
                                    "TranslatorMode.Builder.withType(TranslatorType.CONTRACTED)%s.build())%s",
                                    prefix,
                                    tii.locale,
                                    eightDot,
                                    postfix
                                )
                            );
                        } else if ("no".equalsIgnoreCase(tii.contraction)) {
                            logger.info(
                                String.format(
                                    "%snew TranslatorSpecification(\"%s\", " +
                                    "TranslatorMode.Builder.withType(TranslatorType.UNCONTRACTED)%s.build())%s",
                                    prefix,
                                    tii.locale,
                                    eightDot,
                                    postfix
                                )
                            );
                        }
                        if (tii.grade != null) {
                            try {
                                double grade = Double.parseDouble(tii.grade);
                                logger.info(String.format(
                                    "%snew TranslatorSpecification(\"%s\", " +
                                    "TranslatorMode.Builder.withGrade(%s)%s.build())%s",
                                    prefix,
                                    tii.locale,
                                    grade,
                                    eightDot,
                                    postfix
                                ));
                            } catch (NumberFormatException e) {
                                //Do nothing
                            }
                        }

                    } catch (CompilationException e1) {
                        logger.severe("Failed to read from table path: " + path);
                    }
                }
            });
    }
}
