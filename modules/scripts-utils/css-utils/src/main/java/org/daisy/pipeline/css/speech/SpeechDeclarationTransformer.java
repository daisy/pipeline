package org.daisy.pipeline.css.speech;

import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.Cue;
import cz.vutbr.web.css.CSSProperty.SpeechRate;
import cz.vutbr.web.css.CSSProperty.VoiceFamily;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.css.TermTime;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.domassign.DeclarationTransformer;

public class SpeechDeclarationTransformer extends DeclarationTransformer {

	@Override
	public boolean parseDeclaration(Declaration d, Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		String propertyName = d.getProperty().toLowerCase();
		if ("cue-before".equals(propertyName) || "cue-after".equals(propertyName)) {
			return genericTerm(TermURI.class, d.get(0), d.getProperty(), Cue.uri, false,
			                   properties, values);
		} else if (propertyName.startsWith("pause")) {
			// jStyleParser doesn't accept pauses with "ms" or "s" at the end
			Term<?> term = d.get(0);
			if (!(term instanceof TermTime))
				return super.parseDeclaration(d, properties, values);
			properties.put(propertyName, null);
			values.put(propertyName, term);
			return true;
		} else if ("voice-family".equals(propertyName)) {
			if (d.size() == 0)
				return false;
			TermList list = tf.createList();
			for (Term<?> t : d) {
				list.add(t);
			}
			properties.put(d.getProperty(), VoiceFamily.list_values);
			values.put(d.getProperty(), list);
			return true;
		} else if ("speech-rate".equals(propertyName)) {
			if (d.size() == 1 && d.get(0) instanceof TermPercent) {
				TermPercent p = (TermPercent)d.get(0);
				if (p.getValue() <= 0) {
					return false; // must be non-negative percentage
				} else {
					properties.put(d.getProperty(), SpeechRate.number);
					values.put(d.getProperty(), p);
					return true;
				}
			}
			return super.parseDeclaration(d, properties, values);
		} else {
			return super.parseDeclaration(d, properties, values);
		}
	}
}
