((nil
  . ((compile-command . (format "cd %s && mvn clean install"
                                (locate-dominating-file buffer-file-name "pom.xml")))))
 (java-mode
  . ((indent-tabs-mode . t)
     (tab-width . 4)))
 (nxml-mode
  . ((indent-tabs-mode . nil)
     (nxml-child-indent . 2))))
