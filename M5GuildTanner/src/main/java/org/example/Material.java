package org.example;

public enum Material {
    Green("Green dragonhide", "Green dragon leather", 96),
    Blue("Blue dragonhide", "Blue dragon leather", 97),
    Red("Red dragonhide", "Red dragon leather", 98),
    Black("Black dragonhide", "Black dragon leather", 99);

    String hide;
    String leather;
    int comp;

    Material(String hide, String leather, int comp){
        this.hide    = hide;
        this.leather = leather;
        this.comp    = comp;
    }
}
