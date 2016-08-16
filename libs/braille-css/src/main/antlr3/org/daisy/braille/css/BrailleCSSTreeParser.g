tree grammar BrailleCSSTreeParser;

options {
    tokenVocab=BrailleCSSLexer;
    ASTLabelType=CommonTree;
}

import CSSTreeParser;

@header {
package org.daisy.braille.css;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleList;
import cz.vutbr.web.css.Selector;
}

@members {
    private Preparator preparator;
    
    public void init(Preparator preparator, List<MediaQuery> wrapMedia, RuleFactory ruleFactory) {
        gCSSTreeParser.init(preparator, wrapMedia);
        gCSSTreeParser.rf = ruleFactory;
        this.preparator = preparator;
    }
    
    public RuleList getRules() {
        return gCSSTreeParser.getRules();
    }
    
    public List<List<MediaQuery>> getImportMedia() {
        return gCSSTreeParser.getImportMedia();
    }
    
    public List<String> getImportPaths() {
        return gCSSTreeParser.getImportPaths();
    }
}

// @Override
// Added volume
unknown_atrule returns [RuleBlock<?> stmnt]
@init { $stmnt = null; }
    : (v=volume) { $stmnt = v; }
    | INVALID_ATSTATEMENT { gCSSTreeParser.debug("Skipping invalid at statement"); }
    ;

volume returns [RuleVolume stmnt]
@init {
    String pseudo = null;
    String pseudoFuncArg = null;
}
    : ^(VOLUME
        ( ^(PSEUDOCLASS i=IDENT)
           { pseudo = i.getText(); }
        | ^(PSEUDOCLASS f=FUNCTION n=NUMBER)
           { pseudo = f.getText();
             pseudoFuncArg = n.getText(); }
        )?
        decl=declarations
        areas=volume_areas
      )
      {
        $stmnt = preparator.prepareRuleVolume(decl, areas, pseudo, pseudoFuncArg);
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
    : ^( a=VOLUME_AREA
         decl=declarations )
      {
        $area = preparator.prepareRuleVolumeArea(a.getText().substring(1), decl);
      }
    ;

// @Override
// Added :not() and :has()
pseudo returns [Selector.PseudoPage pseudoPage]
    : ^(PSEUDOCLASS m=MINUS? i=IDENT) {
          String name = i.getText();
          if (m != null) name = "-" + name;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoClass(name);
          } catch (Exception e1) {
              // maybe a single colon was used for a pseudo element
              try {
                  $pseudoPage = gCSSTreeParser.rf.createPseudoElement(name);
                  gCSSTreeParser.warn(i, "Use a double colon for pseudo element ::" + name); }
              catch (Exception e2) {
                  gCSSTreeParser.error(i, "invalid pseudo declaration :" + name);
                  $pseudoPage = null;
              }
          }
      }
    | ^(PSEUDOCLASS NOT sl=selector_list) {
          $pseudoPage = new SelectorImpl.NegationPseudoClassImpl(sl);
      }
    | ^(PSEUDOCLASS HAS rsl=relative_selector_list) {
          $pseudoPage = new SelectorImpl.RelationalPseudoClassImpl(rsl);
      }
    | ^(PSEUDOCLASS f=FUNCTION i=IDENT) {
          $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(f.getText(), i.getText());
      }
    | ^(PSEUDOCLASS f=FUNCTION m=MINUS? n=NUMBER) {
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
              $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(f.getText(), exp);
      }
    | ^(PSEUDOCLASS f=FUNCTION m=MINUS? n=INDEX) {
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(f.getText(), exp);
      }
    | ^(PSEUDOELEM m=MINUS? i=IDENT) {
          String name = i.getText();
          if (m != null) name = "-" + name;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElement(name);
          } catch (Exception e) {
              gCSSTreeParser.error(i, "invalid pseudo declaration ::" + name);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOELEM f=FUNCTION i=IDENT) {
          String func = f.getText();
          String arg = i.getText();
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(func, arg);
          } catch (Exception e) {
            gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, arg);
          }
      }
    | ^(PSEUDOELEM f=FUNCTION m=MINUS? n=NUMBER) {
          String func = f.getText();
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(func, exp);
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, exp);
          }
      }
    | ^(PSEUDOELEM f=FUNCTION m=MINUS? n=INDEX) {
          String func = f.getText();
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(f.getText(), exp);
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, exp);
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
}
    : (s=selector { list.add(s); })+
    ;

relative_selector_list returns [List<CombinedSelector> list]
@init {
    $list = new ArrayList<CombinedSelector>();
}
    : (s=relative_selector { list.add(s); })+
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
}
    : ASTERISK (c=combinator s=selector { combinedSelector.add(s.setCombinator(c)); })+
    ;

/*
 * Simple list of declarations.
 */
simple_inlinestyle returns [List<Declaration> style]
    : ^(INLINESTYLE decl=declarations) {
          $style = decl;
      }
    ;
