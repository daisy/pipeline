/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dedicon.pipeline.braille.model;

import java.util.List;

/**
 * A symbol from the symbols list
 * 
 * @author Paul Rambags
 */
public class Symbol {
    
    private String character;
    private String language;
    private List<Replace> replaces;

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<Replace> getReplaces() {
        return replaces;
    }

    public void setReplaces(List<Replace> replaces) {
        this.replaces = replaces;
    }
}
