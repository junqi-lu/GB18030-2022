import java.util.stream.Collectors;

/**
 * @author junqi lu
 * @date 2023/7/19 14:26
 */
public class Test {
    public static void main(String[] args) {
        String s = "\uD801\uDC00";
        System.out.println(s);
        System.out.println("code point at 0: 0x" + Integer.toUnsignedString(s.codePointAt(0), 16));
        System.out.println("code point at 1: 0x" + Integer.toUnsignedString(s.codePointAt(1), 16));
        System.out.println("length: " + s.length());
        System.out.println("s.charAt(0): " + s.charAt(0) + "\ns.charAt(1): " + s.charAt(1));
        System.out.println("s.toUpperCase(): " + s.toUpperCase());
        System.out.println("s.toLowerCase(): " + s.toLowerCase());
        System.out.println("s.chars(): " + s.chars()
                .mapToObj(i -> Integer.toUnsignedString(i, 16))
                .collect(Collectors.toList())
        );
        System.out.println("Character.isHighSurrogate(s.charAt(0)): " + Character.isHighSurrogate(s.charAt(0)));
        System.out.println("Character.isLowSurrogate(s.charAt(0)): " + Character.isLowSurrogate(s.charAt(0)));
        System.out.println("Character.isLowSurrogate(s.codePointAt(0)): " + Character.charCount(s.codePointAt(0)));
    }
}
