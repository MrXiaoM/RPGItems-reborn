package think.rpgitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static think.rpgitems.RPGItems.adv;

public class ColorHelper {
    private static final Pattern startWithColor = Pattern.compile("^(&[LMNKOlmnko])+");
    private static final Pattern gradientPattern = Pattern.compile("\\{(#[ABCDEFabcdef0123456789]{6}):(#[ABCDEFabcdef0123456789]{6}):(.*)}");
    private static final Pattern hexPattern = Pattern.compile("&(#[ABCDEFabcdef0123456789]{6})");
    private static final Pattern translatePattern = Pattern.compile("<translate:(.*?)>");
    private static final MiniMessage miniMessage = MiniMessage.builder().postProcessor((it) -> it.decoration(TextDecoration.ITALIC, false)).build();

    public static Component miniMessage(String s) {
        return miniMessage.deserialize(legacyToMiniMessage(s));
    }

    public static void send(Player player, String s) {
        adv().player(player).sendMessage(miniMessage(s));
    }

    public static void parseAndSend(CommandSender sender, String s) {
        List<BaseComponent> builder = split(translatePattern, parseColor(s), regexResult -> {
            if (!regexResult.isMatched) {
                return new TextComponent(TextComponent.fromLegacyText(regexResult.text));
            } else {
                return new TranslatableComponent(regexResult.result.group(1));
            }
        });
        BaseComponent[] components = builder.toArray(BaseComponent[]::new);
        sender.spigot().sendMessage(components);
    }

    public static BaseComponent bungee(String s) {
        return new TextComponent(TextComponent.fromLegacyText(parseColor(s)));
    }

    public static String parseColor(String s) {
        String fin = s;
        fin = String.join("", split(hexPattern, fin, regexResult -> {
            if (!regexResult.isMatched) return regexResult.text;
            String hex = regexResult.text.substring(1);
            return parseHex(hex);
        }));
        fin = String.join("", split(gradientPattern, fin, regexResult -> {
            if (!regexResult.isMatched) return regexResult.text;
            String[] args = regexResult.text.substring(1, regexResult.text.length() - 1).split(":", 3);
            String extra = "";
            Matcher m = startWithColor.matcher(args[2]);
            if (m.find()) {
                extra = ChatColor.translateAlternateColorCodes('&', m.group());
            }
            return parseGradient(m.replaceAll(""), extra, args[0], args[1]);
        }));
        return ChatColor.translateAlternateColorCodes('&', fin);
    }
    /**
     * 生成 Minecraft 1.16+ 渐变颜色文字
     * @param s 字符串
     * @param extraFormat 额外样式
     * @param startHex 开始颜色 (#XXXXXX)
     * @param endHex 结束颜色 (#XXXXXX)
     * @return 渐变文字
     */
    public static String parseGradient(String s, String extraFormat, String startHex, String endHex) {
        int color1 = hex(startHex);
        int color2 = hex(endHex);
        int[] colors = createGradient(color1, color2, s.length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < colors.length; i++) {
            result.append(hexToMc(colors[i])).append(extraFormat).append(s.charAt(i));
        }
        return result.toString();
    }

    /**
     * 生成 Minecraft 1.16+ 16进制颜色代码
     * @param hex 16进制颜色 (#XXXXXX)
     * @return 颜色代码
     */
    public static String parseHex(String hex) {
        StringBuilder result = new StringBuilder("§x");
        for (char c : hex.substring(1, hex.length() - 1).toLowerCase().toCharArray()) {
            result.append('§').append(c);
        }
        result.append("§F");
        return result.toString();
    }
    public static int[] createGradient(int startHex, int endHex, int step) {
        if (step == 1) return new int[] { startHex };

        int[] colors = new int[step];
        int[] start = hexToRGB(startHex);
        int[] end = hexToRGB(endHex);

        int stepR = (end[0] - start[0]) / (step - 1);
        int stepG = (end[1] - start[1]) / (step - 1);
        int stepB = (end[2] - start[2]) / (step - 1);

        for (int i = 0; i < step; i++) {
            colors[i] = rgbToHex(
                    start[0] + stepR * i,
                    start[1] + stepG * i,
                    start[2] + stepB * i
            );
        }
        return colors;
    }
    public static String hexToMc(int hex) {
        return parseHex(hex(hex));
    }
    public static int hex(String hex) {
        return Integer.parseInt(hex.substring(1), 16);
    }
    public static String hex(int hex) {
        return "#" + String.format("%06x", hex);
    }
    public static int[] hexToRGB(int hex) {
        return new int[] {
                (hex >> 16) & 0xff,
                (hex >> 8) & 0xff,
                hex & 0xff
        };
    }
    public static int rgbToHex(int r, int g, int b) {
        return (r << 16) + (g << 8) + b;
    }

    public static void split(Pattern regex, String s, Consumer<RegexResult> consumer) {
        int index = 0;
        Matcher m = regex.matcher(s);
        while (m.find()) {
            int first = m.start();
            int last = m.end();
            if (first > index) {
                consumer.accept(new RegexResult(null, s.substring(index, first)));
            }
            consumer.accept(new RegexResult(m.toMatchResult(), s.substring(first, last)));
            index = last;
        }
        if (index < s.length()) {
            consumer.accept(new RegexResult(null, s.substring(index)));
        }
    }

    public static <T> List<T> split(Pattern regex, String s, Function<RegexResult, T> transform) {
        List<T> list = new ArrayList<>();
        int index = 0;
        Matcher m = regex.matcher(s);
        while (m.find()) {
            int first = m.start();
            int last = m.end();
            if (first > index) {
                T value = transform.apply(new RegexResult(null, s.substring(index, first)));
                if (value != null) list.add(value);
            }
            T value = transform.apply(new RegexResult(m.toMatchResult(), s.substring(first, last)));
            if (value != null) list.add(value);
            index = last;
        }
        if (index < s.length()) {
            T value = transform.apply(new RegexResult(null, s.substring(index)));
            if (value != null) list.add(value);
        }
        return list;
    }
    public static class RegexResult {
        public final MatchResult result;
        public final boolean isMatched;
        public final String text;

        public RegexResult(MatchResult result, String text) {
            this.result = result;
            this.isMatched = result != null;
            this.text = text;
        }
    }


    public static String legacyToMiniMessage(String legacy) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isColorCode(chars[i])) {
                stringBuilder.append(chars[i]);
                continue;
            }
            if (i + 1 >= chars.length) {
                stringBuilder.append(chars[i]);
                continue;
            }
            switch (chars[i+1]) {
                case '0': stringBuilder.append("<black>"); break;
                case '1': stringBuilder.append("<dark_blue>"); break;
                case '2': stringBuilder.append("<dark_green>"); break;
                case '3': stringBuilder.append("<dark_aqua>"); break;
                case '4': stringBuilder.append("<dark_red>"); break;
                case '5': stringBuilder.append("<dark_purple>"); break;
                case '6': stringBuilder.append("<gold>"); break;
                case '7': stringBuilder.append("<gray>"); break;
                case '8': stringBuilder.append("<dark_gray>"); break;
                case '9': stringBuilder.append("<blue>"); break;
                case 'a': stringBuilder.append("<green>"); break;
                case 'b': stringBuilder.append("<aqua>"); break;
                case 'c': stringBuilder.append("<red>"); break;
                case 'd': stringBuilder.append("<light_purple>"); break;
                case 'e': stringBuilder.append("<yellow>"); break;
                case 'f': stringBuilder.append("<white>"); break;
                case 'r': stringBuilder.append("<reset><!i>"); break;
                case 'l': stringBuilder.append("<b>"); break;
                case 'm': stringBuilder.append("<st>"); break;
                case 'o': stringBuilder.append("<i>"); break;
                case 'n': stringBuilder.append("<u>"); break;
                case 'k': stringBuilder.append("<o>"); break;
                case 'x': {
                    if (i + 13 >= chars.length
                            || !isColorCode(chars[i+2])
                            || !isColorCode(chars[i+4])
                            || !isColorCode(chars[i+6])
                            || !isColorCode(chars[i+8])
                            || !isColorCode(chars[i+10])
                            || !isColorCode(chars[i+12])) {
                        stringBuilder.append(chars[i]);
                        continue;
                    }
                    stringBuilder
                            .append("<#")
                            .append(chars[i+3])
                            .append(chars[i+5])
                            .append(chars[i+7])
                            .append(chars[i+9])
                            .append(chars[i+11])
                            .append(chars[i+13])
                            .append(">");
                    i += 12;
                    break;
                }
                default: {
                    stringBuilder.append(chars[i]);
                    continue;
                }
            }
            i++;
        }
        return stringBuilder.toString();
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }
}
