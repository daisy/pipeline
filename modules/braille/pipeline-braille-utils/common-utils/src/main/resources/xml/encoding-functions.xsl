<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:brl="http://www.daisy.org/ns/pipeline/braille"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:variable name="unicode-table" select="'⠀⠁⠂⠃⠄⠅⠆⠇⠈⠉⠊⠋⠌⠍⠎⠏⠐⠑⠒⠓⠔⠕⠖⠗⠘⠙⠚⠛⠜⠝⠞⠟⠠⠡⠢⠣⠤⠥⠦⠧⠨⠩⠪⠫⠬⠭⠮⠯⠰⠱⠲⠳⠴⠵⠶⠷⠸⠹⠺⠻⠼⠽⠾⠿'"/>
    <xsl:variable name="nabcc-table" select="' a1b''k2l`cif/msp&quot;e3h9o6r~djg>ntq,*5&lt;-u8v.%{$+x!&amp;;:4|0z7(_?w}#y)='"/>
    
    <xsl:function name="brl:unicode-braille-to-escape-sequence" as="xs:string">
        <xsl:param name="unicode-braille" as="xs:string"/>
        <xsl:sequence select="pxi:dec-to-hex(string-to-codepoints($unicode-braille))"/>
    </xsl:function>
    
    <xsl:function name="brl:unicode-braille-to-nabcc" as="xs:string">
        <xsl:param name="unicode-braille" as="xs:string"/>
        <xsl:sequence select="translate($unicode-braille, $unicode-table, $nabcc-table)"/>
    </xsl:function>
    
    <xsl:function name="brl:nabcc-to-unicode-braille" as="xs:string">
        <xsl:param name="nabcc" as="xs:string"/>
        <xsl:sequence select="translate($nabcc, $nabcc-table, $unicode-table)"/>
    </xsl:function>
    
    <!--
    <xsl:function name="brl:unicode-braille-to-nabcc" as="xs:string">
        <xsl:param name="unicode-braille" as="xs:string"/>
        <xsl:variable name="unicode-table" select="'&#x2800;&#x2801;&#x2802;&#x2803;&#x2804;&#x2805;&#x2806;&#x2807;&#x2808;&#x2809;&#x280A;&#x280B;&#x280C;&#x280D;&#x280E;&#x280F;&#x2810;&#x2811;&#x2812;&#x2813;&#x2814;&#x2815;&#x2816;&#x2817;&#x2818;&#x2819;&#x281A;&#x281B;&#x281C;&#x281D;&#x281E;&#x281F;&#x2820;&#x2821;&#x2822;&#x2823;&#x2824;&#x2825;&#x2826;&#x2827;&#x2828;&#x2829;&#x282A;&#x282B;&#x282C;&#x282D;&#x282E;&#x282F;&#x2830;&#x2831;&#x2832;&#x2833;&#x2834;&#x2835;&#x2836;&#x2837;&#x2838;&#x2839;&#x283A;&#x283B;&#x283C;&#x283D;&#x283E;&#x283F;&#x2840;&#x2841;&#x2842;&#x2843;&#x2844;&#x2845;&#x2846;&#x2847;&#x2848;&#x2849;&#x284A;&#x284B;&#x284C;&#x284D;&#x284E;&#x284F;&#x2850;&#x2851;&#x2852;&#x2853;&#x2854;&#x2855;&#x2856;&#x2857;&#x2858;&#x2859;&#x285A;&#x285B;&#x285C;&#x285D;&#x285E;&#x285F;&#x2860;&#x2861;&#x2862;&#x2863;&#x2864;&#x2865;&#x2866;&#x2867;&#x2868;&#x2869;&#x286A;&#x286B;&#x286C;&#x286D;&#x286E;&#x286F;&#x2870;&#x2871;&#x2872;&#x2873;&#x2874;&#x2875;&#x2876;&#x2877;&#x2878;&#x2879;&#x287A;&#x287B;&#x287C;&#x287D;&#x287E;&#x287F;&#x2880;&#x2881;&#x2882;&#x2883;&#x2884;&#x2885;&#x2886;&#x2887;&#x2888;&#x2889;&#x288A;&#x288B;&#x288C;&#x288D;&#x288E;&#x288F;&#x2890;&#x2891;&#x2892;&#x2893;&#x2894;&#x2895;&#x2896;&#x2897;&#x2898;&#x2899;&#x289A;&#x289B;&#x289C;&#x289D;&#x289E;&#x289F;&#x28A0;&#x28A1;&#x28A2;&#x28A3;&#x28A4;&#x28A5;&#x28A6;&#x28A7;&#x28A8;&#x28A9;&#x28AA;&#x28AB;&#x28AC;&#x28AD;&#x28AE;&#x28AF;&#x28B0;&#x28B1;&#x28B2;&#x28B3;&#x28B4;&#x28B5;&#x28B6;&#x28B7;&#x28B8;&#x28B9;&#x28BA;&#x28BB;&#x28BC;&#x28BD;&#x28BE;&#x28BF;&#x28C0;&#x28C1;&#x28C2;&#x28C3;&#x28C4;&#x28C5;&#x28C6;&#x28C7;&#x28C8;&#x28C9;&#x28CA;&#x28CB;&#x28CC;&#x28CD;&#x28CE;&#x28CF;&#x28D0;&#x28D1;&#x28D2;&#x28D3;&#x28D4;&#x28D5;&#x28D6;&#x28D7;&#x28D8;&#x28D9;&#x28DA;&#x28DB;&#x28DC;&#x28DD;&#x28DE;&#x28DF;&#x28E0;&#x28E1;&#x28E2;&#x28E3;&#x28E4;&#x28E5;&#x28E6;&#x28E7;&#x28E8;&#x28E9;&#x28EA;&#x28EB;&#x28EC;&#x28ED;&#x28EE;&#x28EF;&#x28F0;&#x28F1;&#x28F2;&#x28F3;&#x28F4;&#x28F5;&#x28F6;&#x28F7;&#x28F8;&#x28F9;&#x28FA;&#x28FB;&#x28FC;&#x28FD;&#x28FE;&#x28FF;'"/>
        <xsl:variable name="nabcc-table" select="'&#x20;&#x61;&#x31;&#x62;&#x27;&#x6B;&#x32;&#x6C;&#x60;&#x63;&#x69;&#x66;&#x2F;&#x6D;&#x73;&#x70;&#x22;&#x65;&#x33;&#x68;&#x39;&#x6F;&#x36;&#x72;&#x7E;&#x64;&#x6A;&#x67;&#x3E;&#x6E;&#x74;&#x71;&#x2C;&#x2A;&#x35;&#x3C;&#x2D;&#x75;&#x38;&#x76;&#x2E;&#x25;&#x7B;&#x24;&#x2B;&#x78;&#x21;&#x26;&#x3B;&#x3A;&#x34;&#x7C;&#x30;&#x7A;&#x37;&#x28;&#x5F;&#x3F;&#x77;&#x7D;&#x23;&#x79;&#x29;&#x3D;&#xBA;&#x41;&#xB9;&#x42;&#xB4;&#x4B;&#xB2;&#x4C;&#x40;&#x43;&#x49;&#x46;&#xF7;&#x4D;&#x53;&#x50;&#xA8;&#x45;&#xB3;&#x48;&#xA7;&#x4F;&#xB6;&#x52;&#x5E;&#x44;&#x4A;&#x47;&#xBB;&#x4E;&#x54;&#x51;&#xB8;&#xD7;&#xAF;&#xAB;&#xAD;&#x55;&#xAE;&#x56;&#xB7;&#xA4;&#x5B;&#xA2;&#xB1;&#x58;&#xA1;&#xA5;&#xB5;&#xA6;&#xAC;&#x5C;&#xB0;&#x5A;&#xA9;&#xBC;&#x7F;&#xBF;&#x57;&#x5D;&#xA3;&#x59;&#xBE;&#xBD;&#xAA;&#x81;&#xE2;&#x82;&#xE6;&#x8B;&#xEA;&#x8C;&#x80;&#x83;&#x89;&#x86;&#xF8;&#x8D;&#x93;&#x90;&#xE3;&#x85;&#xEE;&#x88;&#xF2;&#x8F;&#xE0;&#x92;&#x9E;&#x84;&#x8A;&#x87;&#xE5;&#x8E;&#x94;&#x91;&#xF0;&#xE1;&#xFB;&#xE9;&#xFE;&#x95;&#xEC;&#x96;&#xF1;&#xED;&#x9B;&#xFD;&#xE7;&#x98;&#xF6;&#xE4;&#xF5;&#xFA;&#xF4;&#x9C;&#xF9;&#x9A;&#xE8;&#xEF;&#x9F;&#xF3;&#x97;&#x9D;&#xFF;&#x99;&#xFC;&#xEB;&#xA0;&#x01;&#xC2;&#x02;&#xC6;&#x0B;&#xCA;&#x0C;&#x00;&#x03;&#x09;&#x06;&#xD8;&#x0D;&#x13;&#x10;&#xC3;&#x05;&#xCE;&#x08;&#xD2;&#x0F;&#xC0;&#x12;&#x1E;&#x04;&#x0A;&#x07;&#xC5;&#x0E;&#x14;&#x11;&#xD0;&#xC1;&#xDB;&#xC9;&#xDE;&#x15;&#xCC;&#x16;&#xD1;&#xCD;&#x1B;&#xDD;&#xC7;&#x18;&#xD6;&#xC4;&#xD5;&#xDA;&#xD4;&#x1C;&#xD9;&#x1A;&#xC8;&#xCF;&#x1F;&#xD3;&#x17;&#x1D;&#xDF;&#x19;&#xDC;&#xCB;'"/>
        <xsl:sequence select="translate($unicode-braille, $unicode-table, $nabcc-table)"/>
    </xsl:function>
    -->
    
    <xsl:function name="pxi:dec-to-hex" as="xs:string">
        <xsl:param name="dec" as="xs:integer"/>
        <xsl:sequence select="if ($dec = 0) then '0' else concat(if ($dec > 16) then pxi:dec-to-hex($dec idiv 16) else '',
            substring('0123456789ABCDEF', ($dec mod 16) + 1, 1))"/>
    </xsl:function>
    
</xsl:stylesheet>
