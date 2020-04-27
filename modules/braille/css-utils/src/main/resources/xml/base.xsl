<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:import href="regex-utils.xsl"/>
    <xsl:import href="counters.xsl"/>
    
    <!-- ====== -->
    <!-- Syntax -->
    <!-- ====== -->
    
    <!--
        <color>
    -->
    <xsl:variable name="css:COLOR_RE" select="'#[0-9A-F]{6}'"/>
    <xsl:variable name="css:COLOR_RE_groups" select="0"/>
    
    <!--
        <braille-character>: http://braillespecs.github.io/braille-css/#dfn-braille-character
    -->
    <xsl:variable name="css:BRAILLE_CHAR_RE" select="'\p{IsBraillePatterns}'"/>
    <xsl:variable name="css:BRAILLE_CHAR_RE_groups" select="0"/>
    
    <!--
        <braille-string>: http://braillespecs.github.io/braille-css/#dfn-braille-string
    -->
    <xsl:variable name="css:BRAILLE_STRING_RE">'\p{IsBraillePatterns}*?'|"\p{IsBraillePatterns}*?"</xsl:variable>
    <xsl:variable name="css:BRAILLE_STRING_RE_groups" select="0"/>
    
    <!--
        <ident>
    -->
    <xsl:variable name="css:IDENT_RE" select="'(\p{L}|_)(\p{L}|[0-9]|_|-)*'"/>
    <xsl:variable name="css:IDENT_RE_groups" select="2"/>
    
    <xsl:variable name="css:IDENT_LIST_RE" select="re:space-separated($css:IDENT_RE)"/>
    <xsl:variable name="css:IDENT_LIST_RE_groups" select="re:space-separated-groups($css:IDENT_RE_groups)"/>
    
    <xsl:variable name="css:VENDOR_PRF_IDENT_RE" select="'-(\p{L}|_)+-(\p{L}|[0-9]|_)(\p{L}|[0-9]|_|-)*'"/>
    <xsl:variable name="css:VENDOR_PRF_IDENT_RE_groups" select="3"/>
    
    <!--
        <integer>
    -->
    <xsl:variable name="css:INTEGER_RE" select="'0|-?[1-9][0-9]*'"/>
    <xsl:variable name="css:INTEGER_RE_groups" select="0"/>
    
    <!--
        non-negative <integer>
    -->
    <xsl:variable name="css:NON_NEGATIVE_INTEGER_RE" select="'0|[1-9][0-9]*'"/>
    <xsl:variable name="css:NON_NEGATIVE_INTEGER_RE_groups" select="0"/>
    
    <!--
        positive <integer>
    -->
    <xsl:variable name="css:POSITIVE_INTEGER_RE" select="'[1-9][0-9]*'"/>
    <xsl:variable name="css:POSITIVE_INTEGER_RE_groups" select="0"/>
    
    <!--
        positive <number> (normalized)
    -->
    <xsl:variable name="css:POSITIVE_NUMBER_RE" select="'[1-9][0-9]*|(0|[1-9][0-9]*)\.[0-9]*?[1-9]'"/>
    <xsl:variable name="css:POSITIVE_NUMBER_RE_groups" select="1"/>
    
    <!--
        positive <percentage>
    -->
    <xsl:variable name="css:POSITIVE_PERCENTAGE_RE" select="concat('(',$css:POSITIVE_NUMBER_RE,')%')"/>
    <xsl:variable name="css:POSITIVE_PERCENTAGE_RE_number" select="1"/>
    <xsl:variable name="css:POSITIVE_PERCENTAGE_RE_groups" select="1 + $css:POSITIVE_NUMBER_RE_groups"/>

    <!--
        <string>
    -->
    <xsl:variable name="css:STRING_RE">'[^']*'|"[^"]*"</xsl:variable>
    <xsl:variable name="css:STRING_RE_groups" select="0"/>
    
    <!--
        content()
    -->
    <xsl:variable name="css:CONTENT_FN_RE" select="'content\(\)'"/>
    <xsl:variable name="css:CONTENT_FN_RE_groups" select="0"/>
    
    <!--
        attr(<name>)
    -->
    <xsl:variable name="css:ATTR_FN_RE" select="concat('attr\(\s*(',$css:IDENT_RE,')\s*\)')"/>
    <xsl:variable name="css:ATTR_FN_RE_name" select="1"/>
    <xsl:variable name="css:ATTR_FN_RE_groups" select="$css:ATTR_FN_RE_name + $css:IDENT_RE_groups"/>
    
    <!--
        url(<string>) | attr(<name> url)
    -->
    <xsl:variable name="css:URL_RE" select="concat('url\(\s*(',$css:STRING_RE,')\s*\)|attr\(\s*(',$css:IDENT_RE,')(\s+url)?\s*\)')"/>
    <xsl:variable name="css:URL_RE_string" select="1"/>
    <xsl:variable name="css:URL_RE_attr" select="$css:URL_RE_string + $css:STRING_RE_groups + 1"/>
    <xsl:variable name="css:URL_RE_groups" select="$css:URL_RE_attr + $css:IDENT_RE_groups + 1"/>
    
    <!--
        string(<ident>): http://braillespecs.github.io/braille-css/#dfn-string
    -->
    <xsl:variable name="css:STRING_FN_RE" select="concat('string\(\s*(',$css:IDENT_RE,')\s*(,\s*(',$css:IDENT_RE,')\s*)?\)')"/>
    <xsl:variable name="css:STRING_FN_RE_ident" select="1"/>
    <xsl:variable name="css:STRING_FN_RE_scope" select="$css:STRING_FN_RE_ident + $css:IDENT_RE_groups + 2"/>
    <xsl:variable name="css:STRING_FN_RE_groups" select="$css:STRING_FN_RE_scope + $css:IDENT_RE_groups"/>
    
    <!--
        counter(<ident>,<counter-style>?): http://braillespecs.github.io/braille-css/#dfn-counter
    -->
    <xsl:variable name="css:COUNTER_FN_RE" select="concat('counter\(\s*(',$css:IDENT_RE,')\s*(,\s*(',$css:COUNTER_STYLE_RE,')\s*)?\)')"/>
    <xsl:variable name="css:COUNTER_FN_RE_ident" select="1"/>
    <xsl:variable name="css:COUNTER_FN_RE_style" select="$css:COUNTER_FN_RE_ident + $css:IDENT_RE_groups + 2"/>
    <xsl:variable name="css:COUNTER_FN_RE_groups" select="$css:COUNTER_FN_RE_style + $css:COUNTER_STYLE_RE_groups"/>
    
    <!--
        target-text(<url>): http://braillespecs.github.io/braille-css/#dfn-target-text
    -->
    <xsl:variable name="css:TARGET_TEXT_FN_RE" select="concat('target-text\(\s*(',$css:URL_RE,')\s*\)')"/>
    <xsl:variable name="css:TARGET_TEXT_FN_RE_url" select="1"/>
    <xsl:variable name="css:TARGET_TEXT_FN_RE_url_string" select="$css:TARGET_TEXT_FN_RE_url + $css:URL_RE_string"/>
    <xsl:variable name="css:TARGET_TEXT_FN_RE_url_attr" select="$css:TARGET_TEXT_FN_RE_url + $css:URL_RE_attr"/>
    <xsl:variable name="css:TARGET_TEXT_FN_RE_groups" select="$css:TARGET_TEXT_FN_RE_url + $css:URL_RE_groups"/>
    
    <!--
        target-string(<url>,<ident>): http://braillespecs.github.io/braille-css/#dfn-target-string
    -->
    <xsl:variable name="css:TARGET_STRING_FN_RE" select="concat('target-string\(\s*(',$css:URL_RE,')\s*,\s*(',$css:IDENT_RE,')\s*\)')"/>
    <xsl:variable name="css:TARGET_STRING_FN_RE_url" select="1"/>
    <xsl:variable name="css:TARGET_STRING_FN_RE_url_string" select="$css:TARGET_STRING_FN_RE_url + $css:URL_RE_string"/>
    <xsl:variable name="css:TARGET_STRING_FN_RE_url_attr" select="$css:TARGET_STRING_FN_RE_url + $css:URL_RE_attr"/>
    <xsl:variable name="css:TARGET_STRING_FN_RE_ident" select="$css:TARGET_STRING_FN_RE_url + $css:URL_RE_groups + 1"/>
    <xsl:variable name="css:TARGET_STRING_FN_RE_groups" select="$css:TARGET_STRING_FN_RE_ident + $css:IDENT_RE_groups"/>
    
    <!--
        target-counter(<url>,<ident>,<counter-style>?): http://braillespecs.github.io/braille-css/#dfn-target-counter
    -->
    <xsl:variable name="css:TARGET_COUNTER_FN_RE" select="concat('target-counter\(\s*(',$css:URL_RE,')\s*,\s*(',$css:IDENT_RE,')\s*(,\s*(',$css:COUNTER_STYLE_RE,')\s*)?\)')"/>
    <xsl:variable name="css:TARGET_COUNTER_FN_RE_url" select="1"/>
    <xsl:variable name="css:TARGET_COUNTER_FN_RE_url_string" select="$css:TARGET_COUNTER_FN_RE_url + $css:URL_RE_string"/>
    <xsl:variable name="css:TARGET_COUNTER_FN_RE_url_attr" select="$css:TARGET_COUNTER_FN_RE_url + $css:URL_RE_attr"/>
    <xsl:variable name="css:TARGET_COUNTER_FN_RE_ident" select="$css:TARGET_COUNTER_FN_RE_url + $css:URL_RE_groups + 1"/>
    <xsl:variable name="css:TARGET_COUNTER_FN_RE_style" select="$css:TARGET_COUNTER_FN_RE_ident + $css:IDENT_RE_groups + 2"/>
    <xsl:variable name="css:TARGET_COUNTER_FN_RE_groups" select="$css:TARGET_COUNTER_FN_RE_style + $css:COUNTER_STYLE_RE_groups"/>
    
    <!--
        target-content(<url>)
    -->
    <xsl:variable name="css:TARGET_CONTENT_FN_RE" select="concat('target-content\(\s*(',$css:URL_RE,')\s*\)')"/>
    <xsl:variable name="css:TARGET_CONTENT_FN_RE_url" select="1"/>
    <xsl:variable name="css:TARGET_CONTENT_FN_RE_url_string" select="$css:TARGET_CONTENT_FN_RE_url + $css:URL_RE_string"/>
    <xsl:variable name="css:TARGET_CONTENT_FN_RE_url_attr" select="$css:TARGET_CONTENT_FN_RE_url + $css:URL_RE_attr"/>
    <xsl:variable name="css:TARGET_CONTENT_FN_RE_groups" select="$css:TARGET_CONTENT_FN_RE_url + $css:URL_RE_groups"/>
    
    <!--
        leader(<braille-string>[,[<integer>|<percentage>][,[left|center|right]]?]?): http://braillespecs.github.io/braille-css/#dfn-leader
    -->
    <xsl:variable name="css:LEADER_FN_RE" select="concat('leader\(\s*(',$css:BRAILLE_STRING_RE,')\s*(,\s*((',$css:POSITIVE_NUMBER_RE,')|(',$css:POSITIVE_PERCENTAGE_RE,'))\s*(,\s*(left|center|right))?)?\s*\)')"/>
    <xsl:variable name="css:LEADER_FN_RE_pattern" select="1"/>
    <xsl:variable name="css:LEADER_FN_RE_position" select="$css:LEADER_FN_RE_pattern + $css:BRAILLE_STRING_RE_groups + 2"/>
    <xsl:variable name="css:LEADER_FN_RE_position_abs" select="$css:LEADER_FN_RE_position + 1"/>
    <xsl:variable name="css:LEADER_FN_RE_position_rel" select="$css:LEADER_FN_RE_position_abs + $css:POSITIVE_NUMBER_RE_groups + 1"/>
    <xsl:variable name="css:LEADER_FN_RE_alignment" select="$css:LEADER_FN_RE_position_rel + $css:POSITIVE_PERCENTAGE_RE_groups + 2"/>
    <xsl:variable name="css:LEADER_FN_RE_groups" select="$css:LEADER_FN_RE_alignment"/>
    
    <!--
        flow(<ident>,<scope>?): http://braillespecs.github.io/braille-css/#dfn-flow-1
    -->
    <xsl:variable name="css:FLOW_FN_RE" select="concat('flow\(\s*(',$css:IDENT_RE,')\s*(,\s*(document|volume)\s*)?\)')"/>
    <xsl:variable name="css:FLOW_FN_RE_ident" select="1"/>
    <xsl:variable name="css:FLOW_FN_RE_scope" select="$css:FLOW_FN_RE_ident + $css:IDENT_RE_groups + 2"/>
    <xsl:variable name="css:FLOW_FN_RE_groups" select="$css:FLOW_FN_RE_scope"/>
    
    <!--
        -foo-bar([<ident>|<string>|<integer>][,[<ident>|<string>|<integer>]]*)
    -->
    <xsl:variable name="css:VENDOR_PRF_FN_ARG_RE" select="re:or(($css:IDENT_RE,$css:STRING_RE,$css:INTEGER_RE))"/>
    <xsl:variable name="css:VENDOR_PRF_FN_ARG_RE_ident" select="1"/>
    <xsl:variable name="css:VENDOR_PRF_FN_ARG_RE_string" select="$css:VENDOR_PRF_FN_ARG_RE_ident + $css:IDENT_RE_groups + 1"/>
    <xsl:variable name="css:VENDOR_PRF_FN_ARG_RE_integer" select="$css:VENDOR_PRF_FN_ARG_RE_string + $css:STRING_RE_groups + 1"/>
    <xsl:variable name="css:VENDOR_PRF_FN_ARG_RE_groups" select="$css:VENDOR_PRF_FN_ARG_RE_integer + $css:INTEGER_RE_groups"/>
    <xsl:variable name="css:VENDOR_PRF_FN_RE" select="concat('(',$css:VENDOR_PRF_IDENT_RE,')\(\s*(',re:comma-separated($css:VENDOR_PRF_FN_ARG_RE),')\s*\)')"/>
    <xsl:variable name="css:VENDOR_PRF_FN_RE_func" select="1"/>
    <xsl:variable name="css:VENDOR_PRF_FN_RE_args" select="$css:VENDOR_PRF_FN_RE_func + $css:VENDOR_PRF_IDENT_RE_groups + 1"/>
    <xsl:variable name="css:VENDOR_PRF_FN_RE_groups" select="$css:VENDOR_PRF_FN_RE_args + re:comma-separated-groups($css:VENDOR_PRF_FN_ARG_RE_groups)"/>
    
    <xsl:variable name="css:CONTENT_RE" select="concat('(',$css:STRING_RE,')|
                                                        (',$css:CONTENT_FN_RE,')|
                                                        (',$css:ATTR_FN_RE,')|
                                                        (',$css:STRING_FN_RE,')|
                                                        (',$css:COUNTER_FN_RE,')|
                                                        (',$css:TARGET_TEXT_FN_RE,')|
                                                        (',$css:TARGET_STRING_FN_RE,')|
                                                        (',$css:TARGET_COUNTER_FN_RE,')|
                                                        (',$css:TARGET_CONTENT_FN_RE,')|
                                                        (',$css:LEADER_FN_RE,')|
                                                        (',$css:FLOW_FN_RE,')|
                                                        (',$css:VENDOR_PRF_FN_RE,')')"/>
    <xsl:variable name="css:CONTENT_RE_string" select="1"/>
    <xsl:variable name="css:CONTENT_RE_content_fn" select="$css:CONTENT_RE_string + $css:STRING_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_attr_fn" select="$css:CONTENT_RE_content_fn + $css:CONTENT_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_attr_fn_name" select="$css:CONTENT_RE_attr_fn + $css:ATTR_FN_RE_name"/>
    <xsl:variable name="css:CONTENT_RE_string_fn" select="$css:CONTENT_RE_attr_fn + $css:ATTR_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_string_fn_ident" select="$css:CONTENT_RE_string_fn + $css:STRING_FN_RE_ident"/>
    <xsl:variable name="css:CONTENT_RE_string_fn_scope" select="$css:CONTENT_RE_string_fn + $css:STRING_FN_RE_scope"/>
    <xsl:variable name="css:CONTENT_RE_counter_fn" select="$css:CONTENT_RE_string_fn + $css:STRING_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_counter_fn_ident" select="$css:CONTENT_RE_counter_fn + $css:COUNTER_FN_RE_ident"/>
    <xsl:variable name="css:CONTENT_RE_counter_fn_style" select="$css:CONTENT_RE_counter_fn + $css:COUNTER_FN_RE_style"/>
    <xsl:variable name="css:CONTENT_RE_target_text_fn" select="$css:CONTENT_RE_counter_fn + $css:COUNTER_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_target_text_fn_url" select="$css:CONTENT_RE_target_text_fn + $css:TARGET_TEXT_FN_RE_url"/>
    <xsl:variable name="css:CONTENT_RE_target_text_fn_url_string" select="$css:CONTENT_RE_target_text_fn + $css:TARGET_TEXT_FN_RE_url_string"/>
    <xsl:variable name="css:CONTENT_RE_target_text_fn_url_attr" select="$css:CONTENT_RE_target_text_fn + $css:TARGET_TEXT_FN_RE_url_attr"/>
    <xsl:variable name="css:CONTENT_RE_target_string_fn" select="$css:CONTENT_RE_target_text_fn + $css:TARGET_TEXT_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_target_string_fn_url" select="$css:CONTENT_RE_target_string_fn + $css:TARGET_STRING_FN_RE_url"/>
    <xsl:variable name="css:CONTENT_RE_target_string_fn_url_string" select="$css:CONTENT_RE_target_string_fn + $css:TARGET_STRING_FN_RE_url_string"/>
    <xsl:variable name="css:CONTENT_RE_target_string_fn_url_attr" select="$css:CONTENT_RE_target_string_fn + $css:TARGET_STRING_FN_RE_url_attr"/>
    <xsl:variable name="css:CONTENT_RE_target_string_fn_ident" select="$css:CONTENT_RE_target_string_fn + $css:TARGET_STRING_FN_RE_ident"/>
    <xsl:variable name="css:CONTENT_RE_target_counter_fn" select="$css:CONTENT_RE_target_string_fn + $css:TARGET_STRING_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_target_counter_fn_url" select="$css:CONTENT_RE_target_counter_fn + $css:TARGET_COUNTER_FN_RE_url"/>
    <xsl:variable name="css:CONTENT_RE_target_counter_fn_url_string" select="$css:CONTENT_RE_target_counter_fn + $css:TARGET_COUNTER_FN_RE_url_string"/>
    <xsl:variable name="css:CONTENT_RE_target_counter_fn_url_attr" select="$css:CONTENT_RE_target_counter_fn + $css:TARGET_COUNTER_FN_RE_url_attr"/>
    <xsl:variable name="css:CONTENT_RE_target_counter_fn_ident" select="$css:CONTENT_RE_target_counter_fn + $css:TARGET_COUNTER_FN_RE_ident"/>
    <xsl:variable name="css:CONTENT_RE_target_counter_fn_style" select="$css:CONTENT_RE_target_counter_fn + $css:TARGET_COUNTER_FN_RE_style"/>
    <xsl:variable name="css:CONTENT_RE_target_content_fn" select="$css:CONTENT_RE_target_counter_fn + $css:TARGET_COUNTER_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_target_content_fn_url" select="$css:CONTENT_RE_target_content_fn + $css:TARGET_CONTENT_FN_RE_url"/>
    <xsl:variable name="css:CONTENT_RE_target_content_fn_url_string" select="$css:CONTENT_RE_target_content_fn + $css:TARGET_CONTENT_FN_RE_url_string"/>
    <xsl:variable name="css:CONTENT_RE_target_content_fn_url_attr" select="$css:CONTENT_RE_target_content_fn + $css:TARGET_CONTENT_FN_RE_url_attr"/>
    <xsl:variable name="css:CONTENT_RE_leader_fn" select="$css:CONTENT_RE_target_content_fn + $css:TARGET_CONTENT_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_leader_fn_pattern" select="$css:CONTENT_RE_leader_fn + $css:LEADER_FN_RE_pattern"/>
    <xsl:variable name="css:CONTENT_RE_leader_fn_position" select="$css:CONTENT_RE_leader_fn + $css:LEADER_FN_RE_position"/>
    <xsl:variable name="css:CONTENT_RE_leader_fn_position_abs" select="$css:CONTENT_RE_leader_fn + $css:LEADER_FN_RE_position_abs"/>
    <xsl:variable name="css:CONTENT_RE_leader_fn_position_rel" select="$css:CONTENT_RE_leader_fn + $css:LEADER_FN_RE_position_rel"/>
    <xsl:variable name="css:CONTENT_RE_leader_fn_alignment" select="$css:CONTENT_RE_leader_fn + $css:LEADER_FN_RE_alignment"/>
    <xsl:variable name="css:CONTENT_RE_flow_fn" select="$css:CONTENT_RE_leader_fn + $css:LEADER_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_flow_fn_ident" select="$css:CONTENT_RE_flow_fn + $css:FLOW_FN_RE_ident"/>
    <xsl:variable name="css:CONTENT_RE_flow_fn_scope" select="$css:CONTENT_RE_flow_fn + $css:FLOW_FN_RE_scope"/>
    <xsl:variable name="css:CONTENT_RE_vendor_prf_fn" select="$css:CONTENT_RE_flow_fn + $css:FLOW_FN_RE_groups + 1"/>
    <xsl:variable name="css:CONTENT_RE_vendor_prf_fn_func" select="$css:CONTENT_RE_vendor_prf_fn + $css:VENDOR_PRF_FN_RE_func"/>
    <xsl:variable name="css:CONTENT_RE_vendor_prf_fn_args" select="$css:CONTENT_RE_vendor_prf_fn + $css:VENDOR_PRF_FN_RE_args"/>
    <xsl:variable name="css:CONTENT_RE_groups" select="$css:CONTENT_RE_vendor_prf_fn + $css:VENDOR_PRF_FN_RE_groups"/>
    
    <xsl:variable name="css:CONTENT_LIST_RE" select="re:space-separated($css:CONTENT_RE)"/>
    <xsl:variable name="css:CONTENT_LIST_RE_groups" select="re:space-separated-groups($css:CONTENT_RE_groups)"/>
    
    <xsl:variable name="css:STRING_SET_PAIR_RE" select="concat('(',$css:IDENT_RE,')\s+(',$css:CONTENT_LIST_RE,')')"/>
    <xsl:variable name="css:STRING_SET_PAIR_RE_ident" select="1"/>
    <xsl:variable name="css:STRING_SET_PAIR_RE_list" select="$css:STRING_SET_PAIR_RE_ident + $css:IDENT_RE_groups + 1"/>
    
    <xsl:variable name="css:COUNTER_SET_PAIR_RE" select="concat('(',$css:IDENT_RE,')(\s+(',$css:INTEGER_RE,'))?')"/>
    <xsl:variable name="css:COUNTER_SET_PAIR_RE_ident" select="1"/>
    <xsl:variable name="css:COUNTER_SET_PAIR_RE_value" select="$css:COUNTER_SET_PAIR_RE_ident + $css:IDENT_RE_groups + 2"/>
    
    <xsl:variable name="css:PROPERTY_VALUE_RE">([^'"\{\}@&amp;;:]+|'[^']*'|"[^"]*")*</xsl:variable>
    <xsl:variable name="css:PROPERTY_VALUE_RE_groups" select="1"/>
    
    <xsl:variable name="css:DECLARATION_RE" select="concat('(',$css:IDENT_RE,'|',$css:VENDOR_PRF_IDENT_RE,')\s*:(',$css:PROPERTY_VALUE_RE,')')"/>
    <xsl:variable name="css:DECLARATION_RE_property" select="1"/>
    <xsl:variable name="css:DECLARATION_RE_value" select="$css:DECLARATION_RE_property + $css:IDENT_RE_groups + $css:VENDOR_PRF_IDENT_RE_groups + 1"/>
    <xsl:variable name="css:DECLARATION_RE_groups" select="$css:DECLARATION_RE_value + $css:PROPERTY_VALUE_RE_groups"/>
    
    <xsl:variable name="css:DECLARATION_LIST_RE" select="concat('\s*(', $css:DECLARATION_RE ,')?(;\s*(', $css:DECLARATION_RE ,')?)*')"/>
    <xsl:variable name="css:DECLARATION_LIST_RE_groups" select="1 + $css:DECLARATION_RE_groups + 2 + $css:DECLARATION_RE_groups"/>
    
    <xsl:variable name="css:ATKEYWORD_RE" select="concat('@((',$css:IDENT_RE,')|(',$css:VENDOR_PRF_IDENT_RE,'))')"/>
    <xsl:variable name="css:ATKEYWORD_RE_name" select="1"/>
    <xsl:variable name="css:ATKEYWORD_RE_groups" select="$css:ATKEYWORD_RE_name + 1 + $css:IDENT_RE_groups + 1 + $css:VENDOR_PRF_IDENT_RE_groups"/>
    
    <xsl:variable name="css:NESTED_NESTED_NESTED_RULE_RE" select="concat($css:ATKEYWORD_RE,'(',$css:PSEUDOCLASS_RE,')?\s+\{',$css:DECLARATION_LIST_RE,'\}')"/>
    <xsl:variable name="css:NESTED_NESTED_RULE_RE" select="concat($css:ATKEYWORD_RE,'(',$css:PSEUDOCLASS_RE,')?\s+\{((',$css:DECLARATION_LIST_RE,'|',$css:NESTED_NESTED_NESTED_RULE_RE,')*)\}')"/>
    <xsl:variable name="css:NESTED_RULE_RE" select="concat($css:ATKEYWORD_RE,'(',$css:PSEUDOCLASS_RE,')?\s+\{((',$css:DECLARATION_LIST_RE,'|',$css:NESTED_NESTED_RULE_RE,')*)\}')"/>
    
    <xsl:variable name="css:PSEUDOCLASS_RE" select="concat(':(',$css:IDENT_RE,'|',$css:VENDOR_PRF_IDENT_RE,')(\([1-9][0-9]*\))?')"/>
    <xsl:variable name="css:PSEUDOCLASS_RE_groups" select="1 + $css:IDENT_RE_groups + $css:VENDOR_PRF_IDENT_RE_groups + 1"/>
    
    <xsl:variable name="css:PSEUDOELEMENT_RE" select="concat('::(',$css:IDENT_RE,'|',$css:VENDOR_PRF_IDENT_RE,')(\(',$css:IDENT_RE,'\))?')"/>
    <xsl:variable name="css:PSEUDOELEMENT_RE_groups" select="1 + $css:IDENT_RE_groups + $css:VENDOR_PRF_IDENT_RE_groups + 1 + $css:IDENT_RE_groups"/>
    
    <xsl:variable name="css:RULE_RE" select="concat('((',$css:ATKEYWORD_RE,')(\s+(',$css:IDENT_RE,'))?(',$css:PSEUDOCLASS_RE,')?\s*|([^\s\{;@][^\{;@&amp;]*))\{((',$css:DECLARATION_LIST_RE,'|',$css:NESTED_RULE_RE,')*)\}')"/>
    <xsl:variable name="css:RULE_RE_selector" select="1"/>
    <xsl:variable name="css:RULE_RE_selector_atrule" select="$css:RULE_RE_selector + 1"/>
    <xsl:variable name="css:RULE_RE_selector_atrule_name" select="$css:RULE_RE_selector_atrule + $css:ATKEYWORD_RE_groups + 2"/>
    <xsl:variable name="css:RULE_RE_selector_atrule_pseudoclass" select="$css:RULE_RE_selector_atrule_name + $css:IDENT_RE_groups + 1"/>
    <xsl:variable name="css:RULE_RE_selector_relative" select="$css:RULE_RE_selector_atrule_pseudoclass + $css:PSEUDOCLASS_RE_groups + 1"/>
    <xsl:variable name="css:RULE_RE_value" select="$css:RULE_RE_selector_relative + 1"/>
    
    <!-- ======= -->
    <!-- Parsing -->
    <!-- ======= -->
    
    <xsl:function name="css:property">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="$value instance of xs:integer">
                <css:property name="{$name}" value="{format-number($value, '0')}"/>
            </xsl:when>
            <xsl:otherwise>
                <css:property name="{$name}" value="{$value}"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    <xsl:template match="@css:*" mode="css:attribute-as-property" as="element()">
        <css:property name="{replace(local-name(),'^_','-')}" value="{string()}"/>
    </xsl:template>
    
    <xsl:template match="css:property" mode="css:property-as-attribute" as="attribute()">
        <xsl:attribute name="css:{replace(@name,'^-','_')}" select="@value"/>
    </xsl:template>
    
    <!--
        css:parse-stylesheet implemented in Java
    -->
    <!--
    <xsl:function name="css:parse-stylesheet" as="css:rule*">
        <xsl:param name="stylesheet" as="xs:string?"/>
        <xsl:param name="deep" as="xs:boolean"/>
        <xsl:param name="context" as="xs:QName?"/>
    </xsl:function>
    -->
    
    <xsl:function name="css:parse-declaration-list" as="element()*"> <!-- css:property* -->
        <xsl:param name="declaration-list" as="xs:string?"/>
        <xsl:if test="$declaration-list">
            <xsl:analyze-string select="$declaration-list" regex="{$css:DECLARATION_RE}">
                <xsl:matching-substring>
                    <xsl:sequence select="css:property(
                                            regex-group($css:DECLARATION_RE_property),
                                            replace(regex-group($css:DECLARATION_RE_value), '(^\s+|\s+$)', '')
                                            )"/>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:if>
    </xsl:function>
    
    <xsl:template name="css:deep-parse-stylesheet" as="element(css:rule)*">
        <xsl:param name="stylesheet" required="yes"/>
        <xsl:param name="context" as="element()?" select="."/>
        <xsl:variable name="stylesheet" as="element(css:rule)*">
            <xsl:choose>
                <xsl:when test="not(exists($stylesheet))"/>
                <xsl:when test="$stylesheet instance of attribute()">
                    <xsl:sequence select="css:parse-stylesheet(
                                            $stylesheet,
                                            true(),
                                            if ($stylesheet/parent::*/ancestor-or-self::css:rule[@selector='@page'])
                                              then QName('','page')
                                              else if ($stylesheet/parent::*/ancestor-or-self::css:rule[@selector='@volume'])
                                                then QName('','volume')
                                                else if ($stylesheet/parent::*/ancestor-or-self::css:rule[matches(@selector,'^@-')])
                                                  then QName('','vendor-rule')
                                                  else ())"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="css:parse-stylesheet($stylesheet, true())"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates mode="css:deep-parse" select="$stylesheet">
            <xsl:with-param name="context" tunnel="yes" select="$context"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:function name="css:deep-parse-stylesheet" as="element(css:rule)*">
        <xsl:param name="stylesheet"/>
        <xsl:call-template name="css:deep-parse-stylesheet">
            <xsl:with-param name="stylesheet" select="$stylesheet"/>
            <xsl:with-param name="context" select="()"/>
        </xsl:call-template>
    </xsl:function>
    
    <xsl:function name="css:deep-parse-page-stylesheet" as="element()*"> <!-- css:rule* -->
        <xsl:param name="stylesheet" as="xs:string?"/>
        <xsl:variable name="stylesheet" as="element(css:rule)*"
                      select="css:parse-stylesheet($stylesheet, true(), QName('','page'))"/>
        <xsl:apply-templates mode="css:deep-parse" select="$stylesheet"/>
    </xsl:function>
    
    <xsl:function name="css:deep-parse-volume-stylesheet" as="element()*"> <!-- css:rule* -->
        <xsl:param name="stylesheet" as="xs:string?"/>
        <xsl:variable name="stylesheet" as="element(css:rule)*"
                      select="css:parse-stylesheet($stylesheet, true(), QName('','volume'))"/>
        <xsl:apply-templates mode="css:deep-parse" select="$stylesheet"/>
    </xsl:function>
    
    <xsl:function name="css:parse-string" as="element()?">
        <xsl:param name="string" as="xs:string"/>
        <xsl:if test="matches($string,re:exact($css:STRING_RE))">
            <css:string value="{replace(replace(replace(
                                  substring($string, 2, string-length($string)-2),
                                  '\\A\s?','&#xA;'),
                                  '\\27\s?',''''),
                                  '\\22\s?','&quot;')}"/>
        </xsl:if>
    </xsl:function>
    
    <xsl:template name="css:parse-content-list" as="element()*">
        <xsl:param name="content-list" as="xs:string?"/>
        <xsl:param name="context" as="element()?" select="."/>
        <xsl:if test="$content-list">
            <xsl:analyze-string select="$content-list" regex="{$css:CONTENT_RE}" flags="x">
                <xsl:matching-substring>
                    <xsl:choose>
                        <!--
                            <string>
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_string)!=''">
                            <xsl:sequence select="css:parse-string(regex-group($css:CONTENT_RE_string))"/>
                        </xsl:when>
                        <!--
                            content()
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_content_fn)!=''">
                            <css:content/>
                        </xsl:when>
                        <!--
                            attr(<name>)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_attr_fn)!=''">
                            <css:attr name="{regex-group($css:CONTENT_RE_attr_fn_name)}"/>
                        </xsl:when>
                        <!--
                            string(<ident>,<scope>?)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_string_fn)!=''">
                            <css:string name="{regex-group($css:CONTENT_RE_string_fn_ident)}">
                                <xsl:if test="regex-group($css:CONTENT_RE_string_fn_scope)!=''">
                                    <xsl:attribute name="scope" select="regex-group($css:CONTENT_RE_string_fn_scope)"/>
                                </xsl:if>
                            </css:string>
                        </xsl:when>
                        <!--
                            counter(<ident>,<counter-style>?)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_counter_fn)!=''">
                            <xsl:element name="css:counter">
                                <xsl:attribute name="name" select="regex-group($css:CONTENT_RE_counter_fn_ident)"/>
                                <xsl:if test="regex-group($css:CONTENT_RE_counter_fn_style)!=''">
                                    <xsl:attribute name="style" select="regex-group($css:CONTENT_RE_counter_fn_style)"/>
                                </xsl:if>
                            </xsl:element>
                        </xsl:when>
                        <!--
                            target-text(<url>)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_target_text_fn)!=''">
                            <xsl:element name="css:text">
                                <xsl:choose>
                                    <xsl:when test="regex-group($css:CONTENT_RE_target_text_fn_url_string)!=''">
                                        <xsl:attribute name="target"
                                                       select="substring(regex-group($css:CONTENT_RE_target_text_fn_url_string),
                                                                         2,
                                                                         string-length(regex-group($css:CONTENT_RE_target_text_fn_url_string))-2)"/>
                                    </xsl:when>
                                    <xsl:when test="exists($context)">
                                        <xsl:attribute name="target"
                                                       select="string($context/@*[name()=regex-group($css:CONTENT_RE_target_text_fn_url_attr)])"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="target-attribute" select="regex-group($css:CONTENT_RE_target_text_fn_url_attr)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:element>
                        </xsl:when>
                        <!--
                            target-string(<url>,<ident>)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_target_string_fn)!=''">
                            <xsl:element name="css:string">
                                <xsl:choose>
                                    <xsl:when test="regex-group($css:CONTENT_RE_target_string_fn_url_string)!=''">
                                        <xsl:attribute name="target"
                                                       select="substring(regex-group($css:CONTENT_RE_target_string_fn_url_string),
                                                                         2,
                                                                         string-length(regex-group($css:CONTENT_RE_target_string_fn_url_string))-2)"/>
                                    </xsl:when>
                                    <xsl:when test="exists($context)">
                                        <xsl:attribute name="target"
                                                       select="string($context/@*[name()=regex-group($css:CONTENT_RE_target_string_fn_url_attr)])"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="target-attribute" select="regex-group($css:CONTENT_RE_target_string_fn_url_attr)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:attribute name="name" select="regex-group($css:CONTENT_RE_target_string_fn_ident)"/>
                            </xsl:element>
                        </xsl:when>
                        <!--
                            target-counter(<url>,<ident>,<counter-style>?)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_target_counter_fn)!=''">
                            <xsl:element name="css:counter">
                                <xsl:choose>
                                    <xsl:when test="regex-group($css:CONTENT_RE_target_counter_fn_url_string)!=''">
                                        <xsl:attribute name="target"
                                                       select="substring(regex-group($css:CONTENT_RE_target_counter_fn_url_string),
                                                                         2,
                                                                         string-length(regex-group($css:CONTENT_RE_target_counter_fn_url_string))-2)"/>
                                    </xsl:when>
                                    <xsl:when test="exists($context)">
                                        <xsl:attribute name="target"
                                                       select="string($context/@*[name()=regex-group($css:CONTENT_RE_target_counter_fn_url_attr)])"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="target-attribute" select="regex-group($css:CONTENT_RE_target_counter_fn_url_attr)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:attribute name="name" select="regex-group($css:CONTENT_RE_target_counter_fn_ident)"/>
                                <xsl:if test="regex-group($css:CONTENT_RE_target_counter_fn_style)!=''">
                                    <xsl:attribute name="style" select="regex-group($css:CONTENT_RE_target_counter_fn_style)"/>
                                </xsl:if>
                            </xsl:element>
                        </xsl:when>
                        <!--
                            target-content(<url>)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_target_content_fn)!=''">
                            <xsl:element name="css:content">
                                <xsl:choose>
                                    <xsl:when test="regex-group($css:CONTENT_RE_target_content_fn_url_string)!=''">
                                        <xsl:attribute name="target"
                                                       select="substring(regex-group($css:CONTENT_RE_target_content_fn_url_string),
                                                                         2,
                                                                         string-length(regex-group($css:CONTENT_RE_target_content_fn_url_string))-2)"/>
                                    </xsl:when>
                                    <xsl:when test="exists($context)">
                                        <xsl:attribute name="target"
                                                       select="string($context/@*[name()=regex-group($css:CONTENT_RE_target_content_fn_url_attr)])"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="target-attribute" select="regex-group($css:CONTENT_RE_target_content_fn_url_attr)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:element>
                        </xsl:when>
                        <!--
                            leader(<braille-string>[,[<integer>|<percentage>][,[left|center|right]]?]?)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_leader_fn)!=''">
                            <css:leader pattern="{substring(regex-group($css:CONTENT_RE_leader_fn_pattern),
                                                            2, string-length(regex-group($css:CONTENT_RE_leader_fn_pattern))-2)}">
                                <xsl:if test="regex-group($css:CONTENT_RE_leader_fn_position)!=''">
                                    <xsl:attribute name="position" select="regex-group($css:CONTENT_RE_leader_fn_position)"/>
                                    <xsl:if test="regex-group($css:CONTENT_RE_leader_fn_alignment)!=''">
                                        <xsl:attribute name="alignment" select="regex-group($css:CONTENT_RE_leader_fn_alignment)"/>
                                    </xsl:if>
                                </xsl:if>
                            </css:leader>
                        </xsl:when>
                        <!--
                            flow(<ident>)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_flow_fn)!=''">
                            <css:flow from="{regex-group($css:CONTENT_RE_flow_fn_ident)}">
                                <xsl:if test="regex-group($css:CONTENT_RE_flow_fn_scope)!=''">
                                    <xsl:attribute name="scope" select="regex-group($css:CONTENT_RE_flow_fn_scope)"/>
                                </xsl:if>
                            </css:flow>
                        </xsl:when>
                        <!--
                            -foo-bar([<ident>|<string>|<integer>][,[<ident>|<string>|<integer>]]*)
                        -->
                        <xsl:when test="regex-group($css:CONTENT_RE_vendor_prf_fn)!=''">
                            <css:custom-func name="{regex-group($css:CONTENT_RE_vendor_prf_fn_func)}">
                                <xsl:analyze-string select="regex-group($css:CONTENT_RE_vendor_prf_fn_args)" regex="{$css:VENDOR_PRF_FN_ARG_RE}">
                                    <xsl:matching-substring>
                                        <xsl:attribute name="arg{(position()+1) idiv 2}" select="."/>
                                    </xsl:matching-substring>
                                </xsl:analyze-string>
                            </css:custom-func>
                        </xsl:when>
                    </xsl:choose>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:if>
    </xsl:template>
    
    <xsl:function name="css:parse-content-list" as="element()*">
        <xsl:param name="content-list" as="xs:string?"/>
        <xsl:param name="context" as="element()?"/>
        <xsl:call-template name="css:parse-content-list">
            <xsl:with-param name="content-list" select="$content-list"/>
            <xsl:with-param name="context" select="$context"/>
        </xsl:call-template>
    </xsl:function>
    
    <xsl:function name="css:parse-string-set" as="element()*">
        <xsl:param name="pairs" as="xs:string?"/>
        <!--
            force eager matching
        -->
        <xsl:variable name="regexp" select="concat($css:STRING_SET_PAIR_RE,'(\s*,|$)')"/>
        <xsl:if test="$pairs">
            <xsl:analyze-string select="$pairs" regex="{$regexp}" flags="x">
                <xsl:matching-substring>
                    <css:string-set name="{regex-group($css:STRING_SET_PAIR_RE_ident)}"
                                    value="{regex-group($css:STRING_SET_PAIR_RE_list)}"/>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:if>
    </xsl:function>
    
    <xsl:function name="css:parse-counter-set" as="element()*">
        <xsl:param name="pairs" as="xs:string?"/>
        <xsl:param name="initial" as="xs:integer"/>
        <xsl:if test="$pairs">
            <xsl:analyze-string select="$pairs" regex="{$css:COUNTER_SET_PAIR_RE}" flags="x">
                <xsl:matching-substring>
                    <css:counter-set name="{regex-group($css:COUNTER_SET_PAIR_RE_ident)}"
                                     value="{if (regex-group($css:COUNTER_SET_PAIR_RE_value)!='')
                                             then regex-group($css:COUNTER_SET_PAIR_RE_value)
                                             else format-number($initial,'0')}"/>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:if>
    </xsl:function>
    
    <xsl:template mode="css:deep-parse" match="css:property[@name='content' and @value]">
        <xsl:param name="context" as="element()?" tunnel="yes" select="()"/>
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@* except @value"/>
            <xsl:call-template name="css:parse-content-list">
                <xsl:with-param name="content-list" select="@value"/>
                <xsl:with-param name="context" select="$context"/>
            </xsl:call-template>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="css:deep-parse" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- ===================================== -->
    <!-- Validating, inheriting and defaulting -->
    <!-- ===================================== -->
    
    <xsl:template match="css:property" mode="css:validate">
        <xsl:if test="css:is-valid(.)">
            <xsl:sequence select="."/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="css:property" mode="css:inherit">
        <!-- true means input is valid and result should be valid too -->
        <xsl:param name="validate" as="xs:boolean"/>
        <xsl:param name="compute" as="xs:boolean" select="false()"/>
        <xsl:param name="context" as="element()"/>
        <xsl:choose>
            <xsl:when test="@value='inherit'">
                <xsl:call-template name="css:parent-property">
                    <xsl:with-param name="property" select="@name"/>
                    <xsl:with-param name="compute" select="$compute"/>
                    <xsl:with-param name="concretize-inherit" select="true()"/>
                    <xsl:with-param name="concretize-initial" select="false()"/>
                    <xsl:with-param name="validate" select="$validate"/>
                    <xsl:with-param name="context" select="$context"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:property" mode="css:default">
        <xsl:choose>
            <xsl:when test="@value='initial'">
                <xsl:sequence select="css:property(@name, css:initial-value(@name))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:property" mode="css:compute">
        <!--
            true means input does not have value 'inherit' and result should not have value
            'inherit' either. 'inherit' in the result means that the computed value is equal to the
            computed value of the parent, or the initial value if there is no parent.
        -->
        <xsl:param name="concretize-inherit" as="xs:boolean"/>
        <!--
            true means input does not have value 'initial' and result should not have value
            'initial' either. 'initial' in the result means that the computed value is equal to the
            initial value.
        -->
        <xsl:param name="concretize-initial" as="xs:boolean"/>
        <!--
            true means input is valid and result should be valid too
        -->
        <xsl:param name="validate" as="xs:boolean"/>
        <xsl:param name="context" as="element()"/>
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template name="css:parent-property" as="element()?">
        <xsl:param name="property" as="xs:string" required="yes"/>
        <xsl:param name="compute" as="xs:boolean" select="false()"/>
        <!--
            When compute is true, 'inherit' in the result means that the computed value is equal to
            the computed value of the parent (or the initial value if there is no parent).
        -->
        <xsl:param name="concretize-inherit" as="xs:boolean" select="true()"/>
        <xsl:param name="concretize-initial" as="xs:boolean" select="true()"/>
        <!--
            true means input is valid and result should be valid too
        -->
        <xsl:param name="validate" as="xs:boolean"/>
        <!--
            TODO: make into tunnel parameter? (meaning: to be passed to functions that make use of
            css:parent-property, and if css:parent-property is overridden it may have its own set of
            tunnel parameters)
        -->
        <xsl:param name="context" as="element()" select="."/>
        <xsl:variable name="parent" as="element()?" select="$context/ancestor::*[not(self::css:_)][1]"/>
        <xsl:choose>
            <xsl:when test="exists($parent) and $compute">
                <xsl:call-template name="css:computed-properties">
                    <xsl:with-param name="properties" select="($property)"/>
                    <xsl:with-param name="concretize-inherit" select="$concretize-inherit"/>
                    <xsl:with-param name="concretize-initial" select="$concretize-initial"/>
                    <xsl:with-param name="validate" select="$validate"/>
                    <xsl:with-param name="context" select="$parent"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="exists($parent)">
                <xsl:call-template name="css:specified-properties">
                    <xsl:with-param name="properties" select="($property)"/>
                    <xsl:with-param name="concretize-inherit" select="$concretize-inherit"/>
                    <xsl:with-param name="concretize-initial" select="$concretize-initial"/>
                    <xsl:with-param name="validate" select="$validate"/>
                    <xsl:with-param name="context" select="$parent"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$concretize-initial">
                <xsl:sequence select="css:property($property, css:initial-value($property))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="css:property($property, 'initial')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="css:cascaded-properties" as="element()*">
        <xsl:param name="properties" as="xs:string*" select="('#all')"/>
        <xsl:param name="validate" as="xs:boolean" select="false()"/>
        <xsl:param name="context" as="element()" select="."/>
        <xsl:variable name="declarations" as="element()*">
            <xsl:apply-templates select="$context/@css:*[replace(local-name(),'^_','-')=$properties]" mode="css:attribute-as-property"/>
        </xsl:variable>
        <xsl:variable name="declarations" as="element()*"
            select="(css:parse-declaration-list(css:parse-stylesheet($context/@style)
                       /self::css:rule[not(@selector)][last()]/@style),
                     $declarations)"/>
        <xsl:variable name="declarations" as="element()*"
            select="if ('#all'=$properties) then $declarations else $declarations[@name=$properties and not(@name='#all')]"/>
        <xsl:variable name="declarations" as="element()*">
            <xsl:choose>
                <xsl:when test="$validate">
                    <xsl:apply-templates select="$declarations" mode="css:validate"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$declarations"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="for $property in distinct-values($declarations/self::css:property/@name) return
                              ($declarations/self::css:property[@name=$property])[last()]"/>
    </xsl:template>
    
    <xsl:template name="css:specified-properties" as="element()*">
        <xsl:param name="properties" select="'#all'"/>
        <xsl:param name="concretize-inherit" as="xs:boolean" select="true()"/>
        <xsl:param name="concretize-initial" as="xs:boolean" select="true()"/>
        <xsl:param name="validate" as="xs:boolean" select="false()"/>
        <xsl:param name="context" as="element()" select="."/>
        <xsl:variable name="properties" as="xs:string*"
                      select="if ($properties instance of xs:string)
                              then tokenize(normalize-space($properties), ' ')
                              else $properties"/>
        <xsl:variable name="declarations" as="element()*">
            <xsl:call-template name="css:cascaded-properties">
                <xsl:with-param name="properties" select="$properties"/>
                <xsl:with-param name="validate" select="$validate"/>
                <xsl:with-param name="context" select="$context"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="properties" as="xs:string*" select="$properties[not(.='#all')]"/>
        <xsl:variable name="properties" as="xs:string*"
            select="if ($validate) then $properties[.=$css:properties] else $properties"/>
        <xsl:variable name="declarations" as="element()*"
            select="($declarations,
                     for $property in distinct-values($properties) return
                       if ($declarations/self::css:property[@name=$property]) then ()
                       else if (css:is-inherited($property)) then css:property($property, 'inherit')
                       else css:property($property, 'initial'))"/>
        <xsl:variable name="declarations" as="element()*">
            <xsl:choose>
                <xsl:when test="$concretize-inherit">
                    <xsl:apply-templates select="$declarations" mode="css:inherit">
                        <xsl:with-param name="validate" select="$validate"/>
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$declarations"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$concretize-initial">
                <xsl:apply-templates select="$declarations" mode="css:default"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$declarations"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="css:specified-properties" as="element()*">
        <xsl:param name="properties"/>
        <xsl:param name="concretize-inherit" as="xs:boolean"/>
        <xsl:param name="concretize-initial" as="xs:boolean"/>
        <xsl:param name="validate" as="xs:boolean"/>
        <xsl:param name="context" as="element()"/>
        <xsl:call-template name="css:specified-properties">
            <xsl:with-param name="properties" select="$properties"/>
            <xsl:with-param name="concretize-inherit" select="$concretize-inherit"/>
            <xsl:with-param name="concretize-initial" select="$concretize-initial"/>
            <xsl:with-param name="validate" select="$validate"/>
            <xsl:with-param name="context" select="$context"/>
        </xsl:call-template>
    </xsl:function>
    
    <xsl:template name="css:computed-properties" as="element()*">
        <xsl:param name="properties" select="'#all'"/>
        <xsl:param name="concretize-inherit" as="xs:boolean" select="true()"/>
        <xsl:param name="concretize-initial" as="xs:boolean" select="true()"/>
        <xsl:param name="validate" as="xs:boolean" select="false()"/>
        <xsl:param name="context" as="element()" select="."/>
        <xsl:variable name="declarations" as="element()*">
            <xsl:call-template name="css:specified-properties">
                <xsl:with-param name="properties" select="$properties"/>
                <xsl:with-param name="concretize-inherit" select="false()"/>
                <xsl:with-param name="concretize-initial" select="false()"/>
                <xsl:with-param name="validate" select="$validate"/>
                <xsl:with-param name="context" select="$context"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="declarations" as="element()*">
            <xsl:apply-templates select="$declarations" mode="css:compute">
                <xsl:with-param name="concretize-inherit" select="false()"/>
                <xsl:with-param name="concretize-initial" select="false()"/>
                <xsl:with-param name="validate" select="$validate"/>
                <xsl:with-param name="context" select="$context"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="declarations" as="element()*">
            <xsl:choose>
                <xsl:when test="$concretize-inherit">
                    <xsl:apply-templates select="$declarations" mode="css:inherit">
                        <xsl:with-param name="validate" select="$validate"/>
                        <xsl:with-param name="compute" select="true()"/>
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$declarations"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$concretize-initial">
                <xsl:apply-templates select="$declarations" mode="css:default"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$declarations"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="css:computed-properties" as="element()*">
        <xsl:param name="properties"/>
        <xsl:param name="concretize-inherit" as="xs:boolean"/>
        <xsl:param name="concretize-initial" as="xs:boolean"/>
        <xsl:param name="validate" as="xs:boolean"/>
        <xsl:param name="context" as="element()"/>
        <xsl:call-template name="css:computed-properties">
            <xsl:with-param name="properties" select="$properties"/>
            <xsl:with-param name="concretize-inherit" select="$concretize-inherit"/>
            <xsl:with-param name="concretize-initial" select="$concretize-initial"/>
            <xsl:with-param name="validate" select="$validate"/>
            <xsl:with-param name="context" select="$context"/>
        </xsl:call-template>
    </xsl:function>
    
    <xsl:function name="css:computed-properties" as="element()*">
        <xsl:param name="properties"/>
        <xsl:param name="validate" as="xs:boolean"/>
        <xsl:param name="context" as="element()"/>
        <xsl:sequence select="css:computed-properties($properties, true(), true(), $validate, $context)"/>
    </xsl:function>
    
    <!-- =========== -->
    <!-- Serializing -->
    <!-- =========== -->
    
    <xsl:template match="css:rule" mode="css:serialize" as="xs:string">
        <xsl:param name="base" as="xs:string*" select="()"/>
        <xsl:param name="level" as="xs:integer" select="1"/>
        <xsl:param name="indent" as="xs:string?" select="()"/>
        <xsl:variable name="newline" as="xs:string"
                      select="if (exists($indent)) then string-join(('&#xa;',for $i in 2 to $level return $indent),'')
                              else ' '"/>
        <xsl:choose>
            <xsl:when test="not(@selector) and exists($base)">
                <xsl:sequence select="if (@style)
                                      then string-join((
                                             string-join($base,', '),' {',$newline,$indent,
                                             string(@style),
                                             $newline,'}'),'')
                                      else css:serialize-stylesheet(*,$base,$level,$indent)"/>
            </xsl:when>
            <xsl:when test="not(@selector)">
                <xsl:sequence select="if (@style)
                                      then string(@style)
                                      else css:serialize-stylesheet(*,(),$level,$indent)"/>
            </xsl:when>
            <xsl:when test="exists($base) and not(matches(@selector,'^&amp;:'))">
                <xsl:sequence select="string-join((
                                        string-join($base,', '),' {',$newline,$indent,
                                        css:serialize-stylesheet(
                                          if (@style)
                                            then css:deep-parse-stylesheet(@style)
                                            else *,
                                          @selector,
                                          $level+1,
                                          $indent),
                                        $newline,'}'),'')"/>
            </xsl:when>
            <xsl:otherwise> <!-- matches(@selector,'^&amp;:') -->
                <xsl:sequence select="css:serialize-stylesheet(
                                        if (@style)
                                          then css:deep-parse-stylesheet(@style)
                                          else *,
                                        if (exists($base))
                                          then for $s in @selector return
                                               for $b in $base return
                                                 for $bb in tokenize($b,'\s*,\s*') return
                                                   concat($bb,substring($s,2))
                                          else @selector,
                                        $level,
                                        $indent)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:property[@value]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat(@name,': ',@value)"/>
    </xsl:template>
    
    <xsl:template match="css:property[@name='content' and not(@value)]" mode="css:serialize" as="xs:string">
        <xsl:variable name="value" as="xs:string*">
            <xsl:apply-templates mode="#current" select="*"/>
        </xsl:variable>
        <xsl:sequence select="concat(@name,': ',if (exists($value)) then string-join($value,' ') else 'none')"/>
    </xsl:template>
    
    <xsl:template match="css:string-set" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat(@name,' ',@value)"/>
    </xsl:template>
    
    <xsl:template match="css:counter-set" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat(@name,' ',@value)"/>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('&quot;',
                                     replace(replace(
                                       @value,
                                       '\n','\\A '),
                                       '&quot;','\\22 '),
                                     '&quot;')"/>
    </xsl:template>
    
    <xsl:template match="css:content[not(@target|@target-attribute)]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="'content()'"/>
    </xsl:template>
    
    <xsl:template match="css:content[@target]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-content(url(&quot;',@target,'&quot;))')"/>
    </xsl:template>
    
    <xsl:template match="css:content[@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-content(attr(',@target-attribute,' url))')"/>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('attr(',@name,')')"/>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target|@target-attribute)]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('string(',@name,if (@scope) then concat(', ', @scope) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:counter" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('counter(',@name,if (@style) then concat(', ', @style) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:text[@target]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-text(url(&quot;',@target,'&quot;))')"/>
    </xsl:template>
    
    <xsl:template match="css:text[@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-text(attr(',@target-attribute,' url))')"/>
    </xsl:template>
    
    <xsl:template match="css:string[@name][@target]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-string(url(&quot;',@target,'&quot;), ',@name,')')"/>
    </xsl:template>
    
    <xsl:template match="css:string[@name][@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-string(attr(',@target-attribute,' url), ',@name,')')"/>
    </xsl:template>
    
    <xsl:template match="css:counter[@target]" mode="css:serialize" as="xs:string">
        <xsl:variable name="target" as="xs:string" select="(@original-target,@target)[1]"/>
        <xsl:variable name="target" as="xs:string" select="if (contains($target,'#')) then $target else concat('#',$target)"/>
        <xsl:sequence select="concat('target-counter(url(&quot;',$target,'&quot;), ',@name,if (@style) then concat(', ', @style) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:counter[@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-counter(attr(',@target-attribute,' url), ',@name,if (@style) then concat(', ', @style) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:leader" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('leader(',string-join((concat('&quot;',@pattern,'&quot;'),@position,@alignment),', '),')')"/>
    </xsl:template>
    
    <xsl:template match="css:flow[@from]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="string-join(('flow(',@from,if (@scope) then (', ',@scope) else (),')'),'')"/>
    </xsl:template>
    
    <xsl:template match="css:custom-func" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat(
                                @name,
                                '(',
                                string-join(for $i in 1 to 10 return @*[name()=concat('arg',$i)]/string(),', '),
                                ')')"/>
    </xsl:template>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:sequence select="css:serialize-stylesheet($rules,())"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:param name="base" as="xs:string*"/>
        <xsl:sequence select="css:serialize-stylesheet($rules,$base,1)"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:param name="base" as="xs:string*"/>
        <xsl:param name="level" as="xs:integer"/>
        <xsl:sequence select="css:serialize-stylesheet($rules,$base,$level,())"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:param name="base" as="xs:string*"/>
        <xsl:param name="level" as="xs:integer"/>
        <xsl:param name="indent" as="xs:string?"/>
        <xsl:variable name="newline" as="xs:string"
                      select="if (exists($indent)) then string-join(('&#xa;',for $i in 2 to $level return $indent),'') else ' '"/>
        <xsl:variable name="serialized-pseudo-rules" as="xs:string*">
            <xsl:apply-templates select="$rules[self::css:rule and @selector[matches(.,'^&amp;:')]]" mode="css:serialize">
                <xsl:with-param name="base" select="$base"/>
                <xsl:with-param name="level" select="$level"/>
                <xsl:with-param name="indent" select="$indent"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="serialized-at-rules" as="xs:string*">
            <xsl:apply-templates select="$rules[self::css:rule and @selector[not(matches(.,'^&amp;:'))]]" mode="css:serialize">
                <xsl:with-param name="level" select="if (exists($base)) then $level+1 else $level"/>
                <xsl:with-param name="indent" select="$indent"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="serialized-declarations" as="xs:string*">
            <xsl:apply-templates mode="css:serialize"
                                 select="$rules[(self::css:rule and not(@selector)) or self::css:property]">
                <xsl:with-param name="level" select="if (exists($base)) then $level+1 else $level"/>
                <xsl:with-param name="indent" select="$indent"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="serialized-rules" as="xs:string*">
            <xsl:choose>
                <xsl:when test="exists($base)">
                    <xsl:variable name="serialized-inner-rules" as="xs:string*">
                        <xsl:if test="exists($serialized-declarations)">
                            <xsl:sequence select="string-join($serialized-declarations,string-join((';',$newline,$indent),''))"/>
                        </xsl:if>
                        <xsl:sequence select="$serialized-at-rules"/>
                    </xsl:variable>
                    <xsl:if test="exists($serialized-inner-rules)">
                        <xsl:sequence select="string-join((
                                                string-join($base,', '),' {',$newline,$indent,
                                                string-join($serialized-inner-rules,string-join(($newline,$indent),'')),
                                                $newline,'}'),'')"/>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="exists($serialized-declarations)">
                        <xsl:sequence select="string-join($serialized-declarations,string-join((';',$newline),''))"/>
                    </xsl:if>
                    <xsl:sequence select="$serialized-at-rules"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:sequence select="$serialized-pseudo-rules"/>
        </xsl:variable>
        <xsl:sequence select="string-join($serialized-rules,$newline)"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-declaration-list" as="xs:string">
        <xsl:param name="declarations" as="element()*"/>
        <xsl:variable name="serialized-declarations" as="xs:string*">
            <xsl:apply-templates select="$declarations" mode="css:serialize"/>
        </xsl:variable>
        <xsl:sequence select="string-join($serialized-declarations, '; ')"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-content-list" as="xs:string">
        <xsl:param name="components" as="element()*"/>
        <xsl:variable name="serialized-components" as="xs:string*">
            <xsl:apply-templates select="$components" mode="css:serialize"/>
        </xsl:variable>
        <xsl:sequence select="string-join($serialized-components, ' ')"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-string-set" as="xs:string">
        <xsl:param name="pairs" as="element()*"/>
        <xsl:variable name="serialized-pairs" as="xs:string*">
            <xsl:apply-templates select="$pairs" mode="css:serialize"/>
        </xsl:variable>
        <xsl:sequence select="string-join($serialized-pairs, ', ')"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-counter-set" as="xs:string">
        <xsl:param name="pairs" as="element()*"/>
        <xsl:variable name="serialized-pairs" as="xs:string*">
            <xsl:apply-templates select="$pairs" mode="css:serialize"/>
        </xsl:variable>
        <xsl:sequence select="string-join($serialized-pairs, ' ')"/>
    </xsl:function>
    
    <xsl:function name="css:style-attribute" as="attribute()?">
        <xsl:param name="style" as="xs:string?"/>
        <xsl:if test="$style and $style!=''">
            <xsl:attribute name="style" select="$style"/>
        </xsl:if>
    </xsl:function>
    
    <!-- ========== -->
    <!-- Evaluating -->
    <!-- ========== -->
    
    <xsl:template match="css:string[@value]" mode="css:eval" as="xs:string">
        <xsl:sequence select="string(@value)"/>
    </xsl:template>
    
    <xsl:template match="css:content" mode="css:eval">
        <xsl:param name="context" as="element()?" select="()"/>
        <xsl:if test="$context">
            <xsl:sequence select="$context/child::node()"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="css:eval" as="xs:string?">
        <xsl:param name="context" as="element()?" select="()"/>
        <xsl:if test="$context">
            <xsl:variable name="name" select="string(@name)"/>
            <xsl:sequence select="string($context/@*[name()=$name])"/>
        </xsl:if>
    </xsl:template>
    
    <!-- ======= -->
    <!-- Strings -->
    <!-- ======= -->
    
    <xsl:function name="css:string" as="element()*">
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="last-set" as="element()?"
                      select="$context/(self::*|preceding::*|ancestor::*)
                              [contains(@css:string-set,$name) or contains(@css:string-entry,$name)]
                              [last()]"/>
        <xsl:choose>
            <xsl:when test="$context/ancestor::*/@css:flow[not(.='normal')]">
                <xsl:choose>
                    <xsl:when test="$last-set
                                    intersect $context/ancestor::*[@css:anchor][1]/descendant-or-self::*">
                        <xsl:variable name="value" as="xs:string?"
                                      select="(css:parse-string-set($last-set/@css:string-entry),
                                               css:parse-string-set($last-set/@css:string-set))
                                              [@name=$name][last()]/@value"/>
                        <xsl:choose>
                            <xsl:when test="$value">
                                <xsl:sequence select="css:parse-content-list($value, $context)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="context" as="element()?"
                                              select="$last-set/(preceding::*|ancestor::*)[last()]
                                                      intersect $context/ancestor::*[@css:anchor][1]/descendant-or-self::*"/>
                                <xsl:if test="$context">
                                    <xsl:sequence select="css:string($name, $context)"/>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="anchor" as="xs:string" select="$context/ancestor::*/@css:anchor"/>
                        <xsl:variable name="context" as="element()?" select="collection()//*[@css:id=$anchor][1]"/>
                        <xsl:if test="$context">
                            <xsl:sequence select="css:string($name, $context)"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$last-set">
                <xsl:variable name="value" as="xs:string?"
                              select="(css:parse-string-set($last-set/@css:string-entry),
                                       css:parse-string-set($last-set/@css:string-set))
                                      [@name=$name][last()]/@value"/>
                <xsl:choose>
                    <xsl:when test="$value">
                        <xsl:sequence select="css:parse-content-list($value, $context)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="context" as="element()?" select="$last-set/(preceding::*|ancestor::*)[last()]"/>
                        <xsl:if test="$context">
                            <xsl:sequence select="css:string($name, $context)"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:function>
    
</xsl:stylesheet>
