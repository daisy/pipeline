package org.daisy.dotify.formatter.impl.page;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link PaginatorTools}.
 */
public class PaginatorToolsTest {

    @Test
    public void testDistributeEqualSpacingTruncate() throws Exception {
        List<String> fields = new ArrayList<>();
        fields.add("// ");
        fields.add("================");
        fields.add(" //");
        Assert.assertEquals(
            "// ==== //",
            PaginatorTools.distribute(
                fields,
                10,
                " ",
                PaginatorTools.DistributeMode.EQUAL_SPACING_TRUNCATE));
    }
}
