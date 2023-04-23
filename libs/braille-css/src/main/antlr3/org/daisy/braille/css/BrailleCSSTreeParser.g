tree grammar BrailleCSSTreeParser;

options {
    tokenVocab=BrailleCSSLexer;
    ASTLabelType=CommonTree;
}

import CSSTreeParser;

@header {
package org.daisy.braille.css;
import java.util.Arrays;
import java.util.Map;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.MediaQueryList;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleList;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.ElementName;
import cz.vutbr.web.csskit.RuleArrayList;
import cz.vutbr.web.csskit.antlr.SimplePreparator;
}

@members {
    private Preparator preparator;
    
    public void init(Preparator preparator, BrailleCSSRuleFactory ruleFactory, Map<String,String> namespaces) {
        gCSSTreeParser.init(preparator, namespaces);
        gCSSTreeParser.rf = ruleFactory;
        this.preparator = preparator;
    }
    
    public RuleList getRules() {
        return gCSSTreeParser.getRules();
    }
    
    public List<MediaQueryList> getImportMedia() {
        return gCSSTreeParser.getImportMedia();
    }
    
    public List<String> getImportPaths() {
        return gCSSTreeParser.getImportPaths();
    }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        gCSSTreeParser.displayRecognitionError(tokenNames, e);
    }
}

// @Override
// Added volume, text_transform_def, hyphenation_resource_def and counter_style_def
unknown_atrule returns [RuleBlock<?> stmnt]
@init { $stmnt = null; }
    : (v=volume) { $stmnt = v; }
    | (tt=text_transform_def) { $stmnt = tt; }
    | (hr=hyphenation_resource_def) { $stmnt = hr; }
    | (cs=counter_style_def) { $stmnt = cs; }
    | (aar=any_atrule) { $stmnt = aar; }
    | INVALID_ATSTATEMENT { gCSSTreeParser.debug("Skipping invalid at statement"); }
    ;

volume returns [RuleVolume stmnt]
@init {
    String pseudo = null;
    String pseudoFuncArg = null;
    CommonTree pos = null;
}
    : ^(VOLUME
        ( ^(PSEUDOCLASS i=IDENT)
           { pos = i; pseudo = i.getText(); }
        | ^(PSEUDOCLASS f=FUNCTION n=NUMBER)
           { pos = f;
             pseudo = f.getText();
             pseudoFuncArg = n.getText(); }
        )?
        decl=declarations
        areas=volume_areas
      )
      {
        try {
            $stmnt = preparator.prepareRuleVolume(decl, areas, pseudo, pseudoFuncArg);
        } catch (IllegalArgumentException e) {
            gCSSTreeParser.error(pos, e.getMessage());
        }
      }
    ;

volume_areas returns [List<RuleVolumeArea> list]
@init {
    $list = new ArrayList<RuleVolumeArea>();
}
    : ^(SET
        ( a=volume_area {
            if (a!=null) {
              list.add(a);
              gCSSTreeParser.debug("Inserted volume area rule #{} into @volume", $list.size()+1);
            }
          }
        )*
      )
    ;

volume_area returns [RuleVolumeArea area]
@init {
    List<RulePage> pages = null;
}
    : ^( a=VOLUME_AREA
         decl=declarations
         // nested anonymous page rules allowed in case of inline style
         (p=page {
             if (pages == null) pages = new ArrayList<RulePage>();
             pages.add(p);
         })*)
      {
        $area = preparator.prepareRuleVolumeArea(a.getText().substring(1), decl, pages);
      }
    ;

text_transform_def returns [RuleTextTransform def]
    : ^( TEXT_TRANSFORM n=IDENT decl=declarations ) {
          $def = preparator.prepareRuleTextTransform(n.getText(), decl);
      }
    | ^( TEXT_TRANSFORM decl=declarations ) {
          $def = preparator.prepareRuleTextTransform(decl);
      }
    ;

hyphenation_resource_def returns [RuleHyphenationResource def]
    : ^( HYPHENATION_RESOURCE l=LANG decl=declarations ) {
          String[] languageRanges = l.getText().replaceAll("^lang\\(\\s*", "").replaceAll("\\s*\\)$", "").split("\\s*,\\s*");
          if (languageRanges.length > 1) {
              gCSSTreeParser.debug(
                  "Comma-separated list of language ranges (CSS Level 4) not supported: " + l.getText());
              $def = null;
          } else {
              String tag = (languageRanges[0].startsWith("\"") || languageRanges[0].startsWith("\""))
                  ? org.unbescape.css.CssEscape.unescapeCss(languageRanges[0])
                  : languageRanges[0].replace("\\*", "*");
              if (tag.contains("*")) {
                  gCSSTreeParser.debug("Wildcard language matching (CSS Level 4) not supported: " + tag);
                  $def = null;
              } else {
                  try {
                      LanguageRange range = new LanguageTag(tag);
                      if ((decl == null || decl.isEmpty())) {
                          gCSSTreeParser.debug("Empty @hyphenation-resource rule was omited");
                          $def = null;
                      } else {
                          $def = new RuleHyphenationResource(range);
                          $def.replaceAll(decl);
                          gCSSTreeParser.debug("Create @hyphenation-resource with:\n{}", $def);
                      }
                  } catch (IllegalArgumentException e) {
                      gCSSTreeParser.debug("Not a valid language tag: " + tag);
                      $def = null;
                  }
              }
          }
      }
    ;

counter_style_def returns [RuleCounterStyle def]
    : ^( COUNTER_STYLE n=IDENT decl=declarations ) {
          $def = preparator.prepareRuleCounterStyle(n.getText(), decl);
      }
    ;

any_atrule returns [AnyAtRule stmnt]
@init {
    List<AnyAtRule> childrules = new ArrayList<AnyAtRule>();
}
    : ^(  at=(VENDOR_ATRULE|ATKEYWORD)
          decl=declarations
          ^( SET ( r=any_atrule { childrules.add(r); } )* )
        ) {
          String name = at.getText().substring(1);
          AnyAtRule aar = new AnyAtRule(name);
          if (decl != null) aar.addAll(decl);
          if (childrules != null) aar.addAll(childrules);
          if (aar.isEmpty())
              gCSSTreeParser.debug("Empty AnyAtRule was omited");
          else {
              gCSSTreeParser.debug("Create @" + name + " as with:\n{}", aar);
              $stmnt = aar;
          }
        }
    ;

// @Override
// Added :not() and :has()
pseudo returns [Selector.PseudoPage pseudoPage]
    : ^(PSEUDOCLASS m=MINUS? i=IDENT) {
          String name = i.getText();
          try {
              if (m != null) {
                  name = "-" + name;
                  // custom pseudo class is implemented via pseudo element
                  $pseudoPage = gCSSTreeParser.rf.createPseudoElement(":" + name);
              } else {
                  try {
                      $pseudoPage = gCSSTreeParser.rf.createPseudoClass(name);
                  } catch (Exception e) {
                      // maybe a single colon was used for a pseudo element
                      $pseudoPage = gCSSTreeParser.rf.createPseudoElement(":" + name);
                  }
              }
          } catch (Exception e) {
              gCSSTreeParser.error(i, "invalid pseudo declaration :{}", name);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOCLASS NOT sl=selector_list) {
          $pseudoPage = sl == null ? null : new SelectorImpl.NegationPseudoClassImpl(sl);
      }
    | ^(PSEUDOCLASS HAS rsl=relative_selector_list) {
          $pseudoPage = rsl == null ? null : new SelectorImpl.RelationalPseudoClassImpl(rsl);
      }
    | ^(PSEUDOCLASS l=LANG) {
          gCSSTreeParser.error(l, "unsupported pseudo declaration :{}", l);
          $pseudoPage = null;
      }
    | ^(PSEUDOCLASS m=MINUS? f=FUNCTION i=IDENT) {
          String func = f.getText();
          String arg = i.getText();
          try {
              if (m != null) {
                  func = "-" + func;
                  // custom pseudo class is implemented via pseudo element
                  $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(":" + func, arg);
              } else {
                  $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(func, arg);
              }
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration :{}({})", func, arg);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOCLASS m1=MINUS? f=FUNCTION m2=MINUS? n=NUMBER) {
          String func = f.getText();
          String arg = n.getText();
          if (m2 != null) arg = "-" + arg;
          try {
              if (m1 != null) {
                  func = "-" + func;
                  // custom pseudo class is implemented via pseudo element
                  $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(":" + func, arg);
              } else {
                  $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(func, arg);
              }
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration :{}({})", func, arg);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOCLASS m1=MINUS? f=FUNCTION m2=MINUS? n=INDEX) {
          String func = f.getText();
          String arg = n.getText();
          if (m2 != null) arg = "-" + arg;
          try {
              if (m1 != null) {
                  func = "-" + func;
                  // custom pseudo class is implemented via pseudo element
                  $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(":-" + func, arg);
              } else {
                  $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(func, arg);
              }
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration :{}({})", func, arg);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOELEM m=MINUS? i=IDENT) {
          String name = i.getText();
          if (m != null) name = "-" + name;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElement(name);
          } catch (Exception e) {
              gCSSTreeParser.error(i, "invalid pseudo declaration ::{}", name);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOELEM m=MINUS? f=FUNCTION i=IDENT) {
          String func = f.getText();
          if (m != null) func = "-" + func;
          String arg = i.getText();
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(func, arg);
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, arg);
          }
      }
    | ^(PSEUDOELEM m1=MINUS? f=FUNCTION m2=MINUS? n=NUMBER) {
          String func = f.getText();
          String arg = n.getText();
          if (m1 != null) func = "-" + func;
          if (m2 != null) arg = "-" + arg;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(func, arg);
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, arg);
          }
      }
    | ^(PSEUDOELEM m1=MINUS? f=FUNCTION m2=MINUS? n=INDEX) {
          String func = f.getText();
          String arg = n.getText();
          if (m1 != null) func = "-" + func;
          if (m2 != null) arg = "-" + arg;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(f.getText(), arg);
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, arg);
          }
      }
    ;

/*
 * Selector list
 * (https://drafts.csswg.org/selectors-4/#selector-list), used in
 * negation pseudo-class
 * (https://drafts.csswg.org/selectors-4/#negation-pseudo)
 */
selector_list returns [List<Selector> list]
@init {
    $list = new ArrayList<Selector>();
    boolean invalid = false;
}
@after {
    if (invalid) $list = null;
}
    : (s=selector {
          if (s == null)
              invalid = true;
          else
              list.add(s);
      })+
    ;

relative_selector_list returns [List<CombinedSelector> list]
@init {
    $list = new ArrayList<CombinedSelector>();
    boolean invalid = false;
}
@after {
    if (invalid) $list = null;
}
    : (s=relative_selector {
          if (s == null)
              invalid = true;
          else
              list.add(s);
      })+
    ;

/*
 * Relative selector
 * (https://drafts.csswg.org/selectors-4/#relative-selector), used in
 * relational pseudo-class
 * (https://drafts.csswg.org/selectors-4/#relational)
 */
relative_selector returns [CombinedSelector combinedSelector]
@init {
    $combinedSelector = (CombinedSelector)gCSSTreeParser.rf.createCombinedSelector().unlock();
    boolean invalid = false;
}
@after {
    if (invalid) $combinedSelector = null;
}
    : ASTERISK (c=combinator s=selector {
          if (s == null)
              invalid = true;
          else
              combinedSelector.add(s.setCombinator(c));
      })+
    ;

/*
 * Rule with selector relative to a certain element. An ampersand indicates that the relative
 * selector should be "chained" onto the element selector (cfr. the "parent reference" in SASS).
 */
relative_rule returns [RuleBlock<? extends Rule<?>> rb]
@init {
    boolean attach = false;
    List<Selector> sel = new ArrayList<Selector>();
    boolean invalid = false;
    List<RulePage> pages = null;
}
    : ^(RULE
        ((s=selector) {
            attach = true;
            // may not start with a type selector
            if (s == null || (s.size() > 0 && s.get(0) instanceof ElementName)) {
                invalid = true;
            }
            sel.add(s);
         }
         | (c=combinator s=selector) {
            sel.add(s.setCombinator(c));
         }
        )
        (c=combinator s=selector {
            sel.add(s.setCombinator(c));
        })*
        decl=declarations
        (p=page {
            if (pages == null) pages = new ArrayList<RulePage>();
            pages.add(p);
        })*
      ) {
          if (!invalid) {
              if (pages != null) {
                  // Anonymous pages present.
                  // We can't create a RuleSet, so this style won't be picked up by the DOM Analyzer.
                  // Anonymous pages inside relative rules are only supported when a single style element is parsed
                  // (when the BrailleCSSParserFactory.parseInlineStyle() function is called).
                  InlineStyle.RuleRelativeBlock rrb = new InlineStyle.RuleRelativeBlock(sel);
                  rrb.unlock();
                  rrb.addAll(decl);
                  rrb.addAll(pages);
                  $rb = rrb;
              } else {
                  CombinedSelector cs = (CombinedSelector)gCSSTreeParser.rf.createCombinedSelector().unlock();
                  Selector first = (Selector)gCSSTreeParser.rf.createSelector().unlock();
                  first.add(gCSSTreeParser.rf.createElementDOM(((SimplePreparator)preparator).elem, false)); // inlinePriority does not matter
                  if (attach) {
                      first.addAll(sel.get(0));
                      sel.remove(0);
                  }
                  cs.add(first);
                  cs.addAll(sel);
                  RuleSet rs = gCSSTreeParser.rf.createSet();
                  rs.replaceAll(decl);
                  rs.setSelectors(Arrays.asList(cs));
                  $rb = rs;
              }
          }
      }
    ;

/*
 * Simple list of declarations.
 */
simple_inlinestyle returns [List<Declaration> style]
    : ^(INLINESTYLE decl=declarations) {
          $style = decl;
      }
    ;

/*
 * Syntax of style attributes according to http://braillespecs.github.io/braille-css/#style-attribute.
 */
// @Override
inlinestyle returns [RuleList rules]
@init {
    $rules = gCSSTreeParser.rules = new RuleArrayList();
}
    : ^(INLINESTYLE (ib=inlineblock {
          // TODO: check that there is at most one block of simple
          // declarations, that all page at-rules have a different
          // pseudo-class, etc.
          if (ib != null) {
              $rules.add(ib);
          }
      })+ )
    ;

inlineblock returns [RuleBlock<?> b]
    : ^(RULE decl=declarations) {
          $b = preparator.prepareInlineRuleSet(decl, null);
      }
    | tt=text_transform_def { $b = tt; }
    | hr=hyphenation_resource_def { $b = hr; }
    | cs=counter_style_def { $b = cs; }
    | p=page { $b = p; }
    | v=volume { $b = v; }
    | pm=margin { $b = pm; }
    | va=volume_area { $b = va; }
    | aa=any_atrule { $b = aa; }
    | ^(AMPERSAND
         (rr=relative_rule { $b = rr; }
         |p=page { $b = new InlineStyle.RuleRelativePage(p); } // relative @page pseudo rule
         |v=volume { $b = new InlineStyle.RuleRelativeVolume(v); } // relative @volume pseudo rule
         |hr=hyphenation_resource_def { $b = new InlineStyle.RuleRelativeHyphenationResource(hr); } // relative @hyphenation-resource pseudo rule
         ))
    ;

// TODO: move to CSSTreeParser.g
page returns [RulePage stmnt]
@init {
    List<RuleSet> rules = null;
    List<RuleMargin> margins = null;
    String pseudo = null;
}
    : ^(PAGE
          (^(PSEUDOCLASS i=IDENT) {
              pseudo = i.getText();
          })?
          decl=declarations
          ^(SET
              (m=margin {
                  if (m != null) {
                      if (margins == null) margins = new ArrayList<RuleMargin>();
                      margins.add(m);
                  }
              })*
          )
      ) {
          $stmnt = (RulePage)preparator.prepareRulePage(decl, margins, null, pseudo);
      }
    ;
