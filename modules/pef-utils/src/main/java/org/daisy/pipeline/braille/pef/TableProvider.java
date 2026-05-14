package org.daisy.pipeline.braille.pef;

import org.daisy.dotify.api.table.Table;
import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.Query;

public interface TableProvider extends Provider<Query,Table>, org.daisy.dotify.api.table.TableProvider {}
