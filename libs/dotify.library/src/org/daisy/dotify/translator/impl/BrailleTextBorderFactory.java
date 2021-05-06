package org.daisy.dotify.translator.impl;

import org.daisy.dotify.api.graphics.BrailleGraphics;
import org.daisy.dotify.api.translator.Border;
import org.daisy.dotify.api.translator.Border.Builder.BuilderView;
import org.daisy.dotify.api.translator.BorderSpecification.Align;
import org.daisy.dotify.api.translator.BorderSpecification.Style;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.translator.TranslatorType;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class BrailleTextBorderFactory implements TextBorderFactory {
    private static final Logger logger = Logger.getLogger(BrailleTextBorderFactory.class.getCanonicalName());
    private static final String KEY_BORDER = "border";
    private static final String KEY_TOP = "top";
    private static final String KEY_LEFT = "left";
    private static final String KEY_RIGHT = "right";
    private static final String KEY_BOTTOM = "bottom";
    private static final String KEY_STYLE = "style";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_ALIGN = "align";
    private final Border.Builder builder;
    private Border border;

    private boolean useBorderBuilder = false;

    private final Map<String, Object> features;

    public BrailleTextBorderFactory() {
        this.features = new HashMap<>();
        this.builder = new Border.Builder();
        this.border = null;
    }

    @Override
    public void setFeature(String key, Object value) {
        if (KEY_BORDER.equals(key.toLowerCase()) && value instanceof Border) {
            border = (Border) value;
        } else if (key != null && key.toLowerCase().startsWith(KEY_BORDER)) {
            useBorderBuilder = true;
            HashSet<String> set = new HashSet<>();
            for (String s : key.toLowerCase().split("-")) {
                set.add(s);
            }
            set.remove(KEY_BORDER);
            BuilderView b = builder.getDefault();
            if (set.remove(KEY_TOP)) {
                b = builder.getTop();
            } else if (set.remove(KEY_LEFT)) {
                b = builder.getLeft();
            } else if (set.remove(KEY_RIGHT)) {
                b = builder.getRight();
            } else if (set.remove(KEY_BOTTOM)) {
                b = builder.getBottom();
            }
            if (set.size() == 1) {
                String s = set.iterator().next();
                set(b, s, value.toString());
            } else {
                //unknown
                logger.warning("Unknown feature: " + key);
                features.put(key, value);
            }
        } else {
            features.put(key, value);
        }
    }

    void set(BuilderView b, String key, String value) {
        key = key.toLowerCase();
        value = value.toUpperCase();
        if (key.equals(KEY_STYLE)) {
            b.style(Style.valueOf(value));
        } else if (key.equals(KEY_WIDTH)) {
            try {
                b.width(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                //ignore
                logger.warning("Ignoring unparsable value: " + value);
            }
        } else if (key.equals(KEY_ALIGN)) {
            b.align(Align.valueOf(value));
        } else {
            throw new IllegalArgumentException("Unkown value '" + value + "' for " + key);
        }
    }

    @Override
    public Object getFeature(String key) {
        return features.get(key);
    }

    @Override
    public TextBorderStyle newTextBorderStyle() throws TextBorderConfigurationException {

        String mode = "";
        try {
            mode = (String) getFeature(FEATURE_MODE);
        } catch (Exception e) {
        }

        if (!mode.equals(TranslatorType.BYPASS.toString())) {
            if (useBorderBuilder || border != null) {
                if (border == null) {
                    border = builder.build();
                } else if (useBorderBuilder) {
                    logger.warning("Bad combination of features. Some instructions were ignored.");
                }
                TextBorderStyle.Builder style = new TextBorderStyle.Builder();
                if (
                    border.getTop().getStyle() == Style.NONE &&
                    border.getBottom().getStyle() == Style.NONE &&
                    border.getLeft().getStyle() == Style.NONE &&
                    border.getRight().getStyle() == Style.NONE
                ) {
                    return style.build();
                }

                BufferedImage borderImage = renderBorderImage(border, false);
                BrailleGraphics bg = new BrailleGraphics(false);
                List<String> str = bg.renderGraphics(borderImage.getData());
                boolean t = border.getTop().getStyle() != Style.NONE;
                boolean b = border.getBottom().getStyle() != Style.NONE;
                boolean l = border.getLeft().getStyle() != Style.NONE;
                boolean r = border.getRight().getStyle() != Style.NONE;

                if (t) {
                    if (l) {
                        style.topLeftCorner("" + str.get(0).charAt(0));
                    }
                    style.topBorder("" + str.get(0).charAt(1));
                    if (r) {
                        style.topRightCorner("" + str.get(0).charAt(2));
                    }
                }
                if (l) {
                    style.leftBorder("" + str.get(1).charAt(0));
                }
                if (r) {
                    style.rightBorder("" + str.get(1).charAt(2));
                }
                if (b) {
                    if (l) {
                        style.bottomLeftCorner("" + str.get(2).charAt(0));
                    }
                    style.bottomBorder("" + str.get(2).charAt(1));
                    if (r) {
                        style.bottomRightCorner("" + str.get(2).charAt(2));
                    }
                }

                return style.build();
            }
        } else {
            if (useBorderBuilder || border != null) {
                if (border == null) {
                    border = builder.build();
                } else if (useBorderBuilder) {
                    logger.warning("Bad combination of features. Some instructions were ignored.");
                }
                boolean t = border.getTop().getStyle() != Style.NONE;
                boolean b = border.getBottom().getStyle() != Style.NONE;
                boolean l = border.getLeft().getStyle() != Style.NONE;
                boolean r = border.getRight().getStyle() != Style.NONE;
                TextBorderStyle.Builder style = new TextBorderStyle.Builder();
                if (t) {
                    style.topBorder("-");
                }
                if (b) {
                    style.bottomBorder("-");
                }
                if (l) {
                    style.leftBorder("|");
                }
                if (r) {
                    style.rightBorder("|");
                }
                style.topLeftCorner(selectCorner(t, l));
                style.topRightCorner(selectCorner(t, r));
                style.bottomLeftCorner(selectCorner(b, l));
                style.bottomRightCorner(selectCorner(b, r));
                return style.build();
            }
        }
        throw new BrailleTextBorderFactoryConfigurationException();
    }

    private static String selectCorner(boolean h, boolean v) {
        if (h && v) {
            return "+";
        } else if (h) {
            return "-";
        } else if (v) {
            return "|";
        } else {
            return "";
        }
    }

    /**
     * Creates a border image compatible with both 6 and 8-dot use.
     *
     * @return
     */
    private static BufferedImage renderBorderImage(
        Border border,
        boolean eightDot
    ) throws TextBorderConfigurationException {
        //cell dimensions in pixels
        final int cw = 2;
        final int ch = (eightDot ? 4 : 3);

        //border widths
        final int wt = border.getTop().getWidth();
        final int wb = border.getBottom().getWidth();
        final int wl = border.getLeft().getWidth();
        final int wr = border.getRight().getWidth();

        if (wt > 3) {
            throw new BrailleTextBorderFactoryConfigurationException(
                "Width of top border out of supported range [1," + ch + "]: " + wt
            );
        }
        if (wb > 3) {
            throw new BrailleTextBorderFactoryConfigurationException(
                "Width of bottom border out of supported range [1," + ch + "]: " + wb
            );
        }
        if (wl > 2) {
            throw new BrailleTextBorderFactoryConfigurationException(
                "Width of left border out of supported range [1," + cw + "]: " + wl
            );
        }
        if (wr > 2) {
            throw new BrailleTextBorderFactoryConfigurationException(
                "Width of right border out of supported range [1," + cw + "]: " + wr
            );
        }

        //cells required for borders (not fully implemented,
        // because multi-cell borders not supported by TextBorderStyle)
        final int cl = (int) Math.ceil(wl / (double) cw);
        final int cr = (int) Math.ceil(wr / (double) cw);
        final int ct = (int) Math.ceil(wt / (double) ch);
        final int cb = (int) Math.ceil(wb / (double) ch);

        //image dimensions
        final int w = cw * (cl + cr + 1);
        final int h = ch * (ct + cb + 1);

        //alignment
        final int at = border.getTop().getAlign().align(ch);
        final int ab = border.getBottom().getAlign().align(ch);
        final int al = border.getLeft().getAlign().align(cw);
        final int ar = border.getRight().getAlign().align(cw);

        //border coordinates
        final int x1 = 0 + Math.max(al - wl, 0);
        final int y1 = 0 + Math.max(at - wt, 0);
        final int x2 = w - (1 + Math.max(ar - wr, 0));
        final int y2 = h - (1 + Math.max(ab - wb, 0));

        BufferedImage borderImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g = (Graphics2D) borderImage.getGraphics();

        //stroke all widths manually, with multiple 1 pixel lines
        Stroke s = new BasicStroke(1);
        g.setStroke(s);

        //top
        if (border.getTop().getStyle() != Style.NONE) {
            for (int i = 0; i < wt; i++) {
                g.drawLine(x1, y1 + i, x2, y1 + i);
            }
        }
        //right
        if (border.getRight().getStyle() != Style.NONE) {
            for (int i = 0; i < wr; i++) {
                g.drawLine(x2 - i, y1, x2 - i, y2);
            }
        }
        //bottom
        if (border.getBottom().getStyle() != Style.NONE) {
            for (int i = 0; i < wb; i++) {
                g.drawLine(x2, y2 - i, x1, y2 - i);
            }
        }
        //left
        if (border.getLeft().getStyle() != Style.NONE) {
            for (int i = 0; i < wl; i++) {
                g.drawLine(x1 + i, y2, x1 + i, y1);
            }
        }

        return borderImage;
    }

    private static class BrailleTextBorderFactoryConfigurationException extends TextBorderConfigurationException {

        /**
         *
         */
        private static final long serialVersionUID = 2874595503401168992L;

        public BrailleTextBorderFactoryConfigurationException() {
            super();
        }

        public BrailleTextBorderFactoryConfigurationException(String message) {
            super(message);
        }

    }

}
