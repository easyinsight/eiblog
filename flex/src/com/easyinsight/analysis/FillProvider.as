package com.easyinsight.analysis {
import mx.graphics.GradientEntry;
import mx.graphics.IFill;
import mx.graphics.LinearGradient;
import mx.graphics.RadialGradient;
import mx.graphics.SolidColor;

public class FillProvider {

    public static const linearGradients:String = "Linear Gradients";
    public static const radialGradients:String = "Radial Gradients";
    public static const solidColors:String = "Solid Colors";
    public static const highContrast:String = "High Contrast";
    public static const ocean:String = "Ocean";

    public static const defaultFill:String = linearGradients;

    public function FillProvider() {
    }

    public static function getColor(scheme:String, defaults:Array, index:int):IFill {
        var array:Array;
        if (scheme == FillProvider.highContrast) {
            array = FillProvider.createSAPColors();
        } else if (scheme == FillProvider.linearGradients) {
            array = FillProvider.createLinearGradients();
        } else if (scheme == FillProvider.radialGradients) {
            array = FillProvider.createRadialGradients();
        } else {
            if (defaults == null) {
                array = FillProvider.createLinearGradients();
            } else {
                array = defaults;
            }
        }
        if (array.length == 1) {
            return array[0];
        }
        return array[index % array.length];
    }

    public static function createSAPColors():Array {
        return [ new SolidColor(0xF0B400), new SolidColor(0x1E6C0B),
        new SolidColor(0x00488C), new SolidColor(0x332600), new SolidColor(0xD84000),
        new SolidColor(0x434C43), new SolidColor(0xB00023), new SolidColor(0xF3C01C),
        new SolidColor(0xF8D753), new SolidColor(0xFAE16B), new SolidColor(0xFFF8A3)
        ];
    }

    public static function createLinearGradients():Array {
        return [ lg(0xf9fbf5, 0xa6bc59), lg(0xf1f3f7, 0x597197), lg(0xf9f3e0, 0xd6ab2a),
        lg(0xfcf3ef, 0xd86068), lg(0xe9f1e5, 0x5d9942), lg(0xd4c5cd, 0x7a4c6c),

        lg(0xffffff, 0xF0B400), lg(0xffffff, 0x1E6C0B), lg(0xffffff, 0x00488C),
        lg(0xffffff, 0x332600), lg(0xffffff, 0xD84000), lg(0xffffff, 0x846D74)];
    }

    public static function createRadialGradients():Array {
        return [ cg(0xf9fbf5, 0xa6bc59), cg(0xf1f3f7, 0x597197), cg(0xf9f3e0, 0xd6ab2a),
        cg(0xfcf3ef, 0xd86068), cg(0xe9f1e5, 0x5d9942), cg(0xd4c5cd, 0x7a4c6c),

        cg(0xffffff, 0xF0B400), cg(0xffffff, 0x1E6C0B), cg(0xffffff, 0x00488C),
        cg(0xffffff, 0x332600), cg(0xffffff, 0xD84000), cg(0xffffff, 0x846D74)];
    }

    public static function createSolidColors():Array {
        return [ new SolidColor(0xa6bc59), new SolidColor(0x597197),
        new SolidColor(0xd6ab2a), new SolidColor(0xd86068), new SolidColor(0x5d9942),
        new SolidColor(0x7a4c6c), new SolidColor(0xF0B400), new SolidColor(0x1E6C0B),
        new SolidColor(0x00488C), new SolidColor(0x332600), new SolidColor(0xD84000)
        ];
    }

    private static function lg(startColor:uint, endColor:uint):LinearGradient {
        var gradient:LinearGradient = new LinearGradient();
        var entry1:GradientEntry = new GradientEntry(endColor, 0.0);
        var entry2:GradientEntry = new GradientEntry(startColor, 0.15);
        var entry3:GradientEntry = new GradientEntry(endColor, .5);
        var entry4:GradientEntry = new GradientEntry(endColor, .9);
        var entry5:GradientEntry = new GradientEntry(startColor, 1);
        gradient.entries = [ entry1, entry2, entry3, entry4, entry5 ];
        return gradient;
    }

    private static function cg(startColor:uint, endColor:uint):IFill {
        var gradient:RadialGradient = new RadialGradient();
        var entry1:GradientEntry = new GradientEntry(startColor, 0.0);
        var entry2:GradientEntry = new GradientEntry(endColor, .3);
        var entry3:GradientEntry = new GradientEntry(endColor, .7);
        var entry4:GradientEntry = new GradientEntry(startColor, .9);
        var entry5:GradientEntry = new GradientEntry(endColor, 1);
        gradient.entries = [ entry1, entry2, entry3, entry4, entry5 ];
        return gradient;
    }
}
}