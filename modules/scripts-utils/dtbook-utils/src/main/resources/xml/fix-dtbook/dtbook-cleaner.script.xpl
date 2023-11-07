<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:dtbook-cleaner.script"
                px:input-filesets="dtbook"
                px:output-filesets="dtbook"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook Cleaner</h1>
        <p px:role="desc">Apply cleanup routines and optionally tag sentences on a given DTBbook</p>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Nicolas Pavie</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:pavie.nicolas@gmail.com">pavie.nicolas@gmail.com</a></dd>
                <dt>Organisation:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be cleaned</p>
        </p:documentation>
    </p:input>
    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">Cleaned DTBooks</p>
        </p:documentation>
    </p:option>

    <p:option name="repair" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair the DTBook</h2>
            <div px:role="desc">
                <p>Apply repair routines on the DTBook</p>
                <ul>
                    <li>Removes levelx if it has descendant headings of x-1 (this simplifies later steps).
                        <aside><p>Note: Level normalizer cannot fix level1/level2/level1</p></aside></li>
                    <li>Splits a level into several levels on every additional heading on the same level</li>
                    <li>Add levels where needed.</li>
                    <li>Changes a hx into a p with @class="hx" if parent isn't levelx
                        <aside><p>Note: "Remove illegal headings" cannot handle hx in inline context.
                            Support for this could be added.</p></aside></li>
                    <li>Removes nested p</li>
                    <li>Adds an empty p-tag if hx is the last element</li>
                    <li>Apply fixes for lists: <ul>
                        <li>wraps a list in li when the parent of the list is another list</li>
                        <li>adds @type if missing (default value is "pl")</li>
                        <li>corrects @depth attribute</li>
                        <li>removes enum attribute if the list is not ordered</li>
                        <li>removes start attribute if the list is not ordered</li>
                    </ul></li>

                    <li>idref must be present on noteref and annoref. Add idref if missing or
                        change if empty.
                        <aside><p>The value of the idref must include a fragment identifier.
                            Add a hash mark in the beginning of all idref attributes that don't
                            contain a hash mark.</p></aside></li>
                    <li>Removes
                        <ul>
                            <li>empty/whitespace p except when
                                <ul>
                                    <li>receded by hx or no preceding element and parent is a level</li>
                                    <li>and followed only by other empty p</li>
                                </ul>
                            </li>
                            <li>empty/whitespace em, strong, sub, sup</li>
                            <li>empty/whitespace elements that must have children.</li>
                        </ul>
                    </li>
                    <li>Update the @page attribute to make it match the contents of the pagenum element.
                        <aside>
                            <p>If @page="normal" but the contents of the element doesn't match "normal"
                                content, the @page attribute is changed to:</p>
                            <ul>
                                <li>@page="front" if the contents is roman numerals and the pagenum element
                                    is located in the frontmatter of the book</li>
                                <li>@page="special" otherwise</li>
                            </ul>
                            <p>If @page="front" but the contents of the element doesn't match "front"
                                content (neither roman nor arabic numerals), the @page attribute is changed to "special"</p>
                        </aside>
                    </li>
                    <li>Fix metadata case errors <ul>
                        <li>remove unknown dc-metadata</li>
                        <li>add dtb:uid (if missing) from dc:Identifier</li>
                        <li>add dc:Title (if missing) from doctitle</li>
                        <li>add auto-generated dtb:uid if missing (or if it has empty contents)</li>
                    </ul>
                    </li>
                </ul>
            </div>
        </p:documentation>
    </p:option>
    <p:option name="fixCharset" select="'false'" px:type="boolean" px:hidden="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair - Fix charset</h2>
            <p px:role="desc">Fix the document charset (To be implemented)</p>
        </p:documentation>
    </p:option>
    <p:option name="tidy" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy up the DTBook</h2>
            <div px:role="desc">
                <p>Apply tidying routines on the DTBook</p>
                <ul>
                    <li>Removes
                        <ul>
                            <li>empty/whitespace p except when
                                <ul>
                                    <li>receded by hx or no preceding element and parent is a level</li>
                                    <li>and followed only by other empty p</li>
                                </ul>
                            </li>
                            <li>empty/whitespace em, strong, sub, sup</li>
                        </ul>
                    </li>
                    <li>Moves
                        <ul>
                            <li>pagenum inside h[x] before h[x]</li>
                            <li>pagenum inside a word after the word</li>
                        </ul>
                    </li>
                    <li>Update the @page attribute to make it match the contents of the pagenum
                        element.
                        <ul>
                            <li>If @page="front" but the contents of the element is an arabic number,
                                the @page attribute is changed to "normal"
                                (note:  arabic numbers are theoretically allowed from @page="front", but
                                are not considered standard practice by many)</li>
                            <li>If @page="special" but the element has no content, adds a dummy content
                                ("page break").</li>
                        </ul>
                    </li>
                    <li>Removes otherwise empty p or li around pagenum (except p in td)</li>
                    <li>Inserts docauthor and doctitle if a frontmatter exists without those elements</li>
                    <li>Removes existing whitespace nodes and indents output to aid debugging.
                        <ul>
                            <li>Does not remove whitespace or apply indentation in inline context</li>
                            <li>Does not apply indentation when number of children is 1</li>
                        </ul>
                    </li>
                </ul>
            </div>
        </p:documentation>
    </p:option>
    <p:option name="simplifyHeadingLayout" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Simplify headings layout</h2>
            <p px:role="desc">Redundant level structure is sometimes used to mimic the original layout,
                but can pose a problem in some circumstances. "Level cleaner" simplifies
                the level structure by removing redundant levels (subordinate levels will
                be moved upwards). Note that the headings of the affected levels will
                also change, which will alter the appearance of the layout.</p>
        </p:documentation>
    </p:option>
    <p:option name="externalizeWhitespace" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Externalize whitespaces</h2>
            <p px:role="desc">Externalizes leading and trailing whitespace from em, strong, sub, sup, pagenum, noteref.</p>
        </p:documentation>
    </p:option>
    <p:option name="documentLanguage" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Document language</h2>
            <p px:role="desc">Set a document language</p>
        </p:documentation>
    </p:option>
    <p:option name="narrator" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Prepare DTBook for pipeline 1 narrator</h2>
            <div px:role="desc">
                <p>Apply Pipeline 1 "narrator" cleaning routines on the document:</p>
                <ul>
                    <li>Adds dc:Language, dc:Date and dc:Publisher to dtbook, if not present in input,
                        or given but with null/whitespace only content values</li>
                    <li>Removes dc:description and dc:subject if not valued</li>
                    <li>Removes dc:Format (will be added by the fileset generator)</li>
                    <li>Prepare a dtbook to the Narrator schematron rules:<ul>
                        <li>Rule 14: Don't allow &lt;h x+1&gt; in &lt;level x+1&gt; unless &lt;h x&gt; in &lt;li x&gt; is present
                            <aside><p>This fix assumes headings are not empty (e.g. empty headings were
                                removed by a previous fix)</p></aside></li>
                        <li>Rule 100: Every document needs at least one heading on level 1
                            <aside><p>This fix assumes headings are not empty (e.g. empty headings were
                                removed by a previous fix)</p></aside></li>
                        <li>Rule 07: No &lt;list&gt; or &lt;dl&gt; inside &lt;p&gt; :<ul>
                            <li>Breaks the parent paragraph into a sequence of paragraphs, list and dl</li>
                            <li>Each newly created paragraph has the same attributes as the original one </li>
                            <li>New paragraph IDs are created if necessary</li>
                            <li>The original paragraph ID is conserved for the first paragraph created</li>
                        </ul></li>
                    </ul></li>
                    <li>Adds the dc:Title meta element and the &lt;doctitle&gt; frontmatter element, if not present in input,
                        or given but with null/whitespace only content values.
                        <aside><ul>Title value is taken:
                            <li>from the 'dc:Title' metadata if it is present</li>
                            <li>or else from the first 'doctitle' element in the bodymatter</li>
                            <li>or else from the first heading 1.</li>
                        </ul></aside>
                    </li>
                </ul>
            </div>
        </p:documentation>
    </p:option>
    <p:option name="publisher" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Narrator - Publisher</h2>
            <p px:role="desc">Publisher metadata (dc:Publisher) to be added if none is defined in the DTBook</p>
        </p:documentation>
    </p:option>
    <p:option name="ApplySentenceDetection" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Apply sentences detection</h2>
            <p px:role="desc">Encapsulate sentences within the document</p>
        </p:documentation>
    </p:option>
    <p:option name="WithDoctype" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include the doctype in resulting DTBook(s)</h2>
            <p px:role="desc">The resulting DTBook will have a standard DTBook 2005-3 doctype, optionally with MathML declaration if MathML is present in the document.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl">
        <p:documentation>
            px:dtbook-break-detect
            px:dtbook-unwrap-words
        </p:documentation>
    </p:import>
    <p:import href="../upgrade-dtbook/upgrade-dtbook.xpl">
        <p:documentation>
            px:dtbook-upgrade
        </p:documentation>
    </p:import>
    <p:import href="fix-dtbook.xpl">
        <p:documentation>
            pxi:dtbook-fix
        </p:documentation>
    </p:import>
    <p:import href="doctyping.xpl">
        <p:documentation>
            pxi:dtbook-doctyping
        </p:documentation>
    </p:import>

    <p:for-each px:message="Cleaning DTBook(s)">
        <p:variable name="output-name" select="concat(replace(replace(base-uri(.),'^.*/([^/]+)$','$1'),'\.[^\.]*$',''),'.xml')"/>
        <!-- Update the DTBook -->
        <px:dtbook-upgrade/>
        <!-- Apply routines -->
        <pxi:dtbook-fix>
            <p:with-option name="repair" select="$repair='true'"/>
            <p:with-option name="fixCharset" select="$fixCharset='true'"/>
            <p:with-option name="tidy" select="$tidy='true'"/>
            <p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout='true'"/>
            <p:with-option name="externalizeWhitespace" select="$externalizeWhitespace='true'"/>
            <p:with-option name="documentLanguage" select="$documentLanguage='true'"/>
            <p:with-option name="narrator" select="$narrator='true'"/>
            <p:with-option name="publisher" select="$publisher='true'"/>
        </pxi:dtbook-fix>

        <p:choose>
            <p:when test="$ApplySentenceDetection='true'">
                <px:dtbook-break-detect/>
                <px:dtbook-unwrap-words/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <!--
            FIXME: this should be handled with px:fileset-store
        -->
        <p:choose>
            <p:when test="$WithDoctype='true'">
                <!-- DTBook with doctype (result is serialized) -->
                <pxi:dtbook-doctyping/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <!-- Store on disk -->
        <p:store>
            <p:with-option name="href" select="concat(resolve-uri($result),$output-name)"/>
        </p:store>
    </p:for-each>

</p:declare-step>
