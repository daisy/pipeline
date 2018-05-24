# Induction of section structures in HTML

This is achieved in one XSLT calling `induce-sections.xsl`. This subdirectory also contains an earlier two-step implementation, which was subsequently repackaged.

Any sequence of HTML elements leading with a header (h1-h6) is to be wrapped as a section. Within each section, the header plus its (block level) elements are followed by sections for contiguous (subsequent) lower level sections.

I.e. h1 h2 h2 h3 h1 h2 h3 becomes section (h1 section (h2) section (h2 section (h3) ) ) section (h1 section (h2 (section h3) ) ).
 
In
```
h1
h2
h3
h2
h3
h1
h2
h2
h3
```

Out
```
section
  h1
  section
    h2
    section
      h3
  section  
    h2
    section
      h3
section
  h1
  section
    h2
  section
    h2
    section
    h3

```

Notes:
* Paragraphs and all other elements travel with the immediately preceding header
* Paragraphs and blocks preceding the first header, appear without a section wrapper
  (before the first section)
* Hence sequences with no headers, are unchanged
* The logic should also apply to 'section' elements as well as wrapper elements
    * Hence, a properly sectioned HTML is returned unchanged, but one whose headers do not lead sections is "repaired".
* When sections are skipped (e.g. h4 appearing before h3), the extra section wrappers should *not* appear. So such a section comes wrapped as if it were at a higher level - although its header still indicates its 'presentation' level.

(Examples:
correct, leading with h1;
correct, leading with para contents then h1;
correct, leading with h3;
correct, leading with para contents then h3;
skipping levels at the front;
skipping levels inside)

