import java.io.BufferedWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author junqi lu
 * @date 2023/7/18 10:23
 */
public class Main {
    private static final Charset GB18030 = Charset.forName("GB18030");
    private static final Path ENC_OUT = Paths.get("out/enc_out");
    private static final Path DEC_OUT = Paths.get("out/dec_out");
    private static final int SURROGATES_CEILING = 0XDFFF;
    private static final int SURROGATES_FLOOR = 0XD800;

    public static void main(String[] args) throws Exception {

        // unicode code point -> GB
        HashMap<Integer, Integer> encMap = new HashMap<>(
                loadMap("resources/GB18030_2022_MappingTableBMP.txt", true)
        );
        encMap.putAll(
                loadMap("resources/GB18030_2022_MappingTableSMP.txt", true)
        );

        // GB -> unicode code point
        HashMap<Integer, Integer> decMap = new HashMap<>(
                loadMap("resources/GB18030_2022_MappingTableBMP.txt", false)
        );
        decMap.putAll(
                loadMap("resources/GB18030_2022_MappingTableSMP.txt", false)
        );

        // Encoding (Unicode -> GB18030) check
        List<String> output = IntStream.range(0, 0x110000)
                .mapToObj(c -> checkEnc(encMap, c))
                .filter(Objects::nonNull)
                // 按code point十六进制字符串顺序排
                .sorted(Comparator.comparing(a -> a.split("\\s+")[0]))
                .collect(Collectors.toList());

        Files.deleteIfExists(ENC_OUT);
        Files.createDirectories(ENC_OUT.getParent());

        BufferedWriter bw = Files.newBufferedWriter(ENC_OUT, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        bw.append("code point\t\tmapped gb\t\ttransferred gb");
        bw.newLine();
        for (String str : output) {
            bw.append(str);
            bw.newLine();
        }
        bw.close();

        // Decoding (GB18030 -> Unicode) check
        output = decMap.keySet()
                .stream()
                .map(b -> checkDec(decMap, b))
                .filter(Objects::nonNull)
                // 按code point十六进制字符串顺序排
                .sorted(Comparator.comparing(a -> a.split("\\s+")[1]))
                .collect(Collectors.toList());

        Files.deleteIfExists(DEC_OUT);
        Files.createDirectories(DEC_OUT.getParent());

        bw = Files.newBufferedWriter(DEC_OUT, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        bw.append("gbbytes\t\tmapped code point\t\ttransferred code point");
        bw.newLine();
        for (String str : output) {
            bw.append(str);
            bw.newLine();
        }
        bw.close();
    }

    static Map<Integer, Integer> loadMap(String mapFile, boolean isEncoding) throws Exception {
        return Files.readAllLines(Paths.get(mapFile)).stream()
                .map(line -> line.split("[ \t]+"))
                .collect(Collectors.toMap(
                        m -> Integer.parseUnsignedInt(m[isEncoding ? 0 : 1], 16),
                        m -> Integer.parseUnsignedInt(m[isEncoding ? 1 : 0], 16)
                ));
    }

    static String checkEnc(Map<Integer, Integer> mapping, Integer c) {
        // UTF-16 使用的代理, 不被赋予Unicode字符
        if (c >= SURROGATES_FLOOR && c <= SURROGATES_CEILING) {
            return null;
        }

        if (!mapping.containsKey(c)) {
            return "code point:\t" + Integer.toHexString(c) + "\t no mapping GB18030 code";
        }
        Integer gbMappingCode = mapping.get(c);

        String str = new String(new int[]{c}, 0, 1);
        byte[] gbBytes = str.getBytes(GB18030);
        String hexString = byteToHex(gbBytes);
        int gbTransferredCode = Integer.parseUnsignedInt(hexString, 16);

        return gbMappingCode.equals(gbTransferredCode) ?
                null :
                Integer.toUnsignedString(c, 16)
                        + "\t\t\t" + Integer.toUnsignedString(gbMappingCode, 16)
                        + "\t\t\t\t" + Integer.toUnsignedString(gbTransferredCode, 16);
    }

    static String checkDec(Map<Integer, Integer> mapping, int gbcode) {
        // gbCode为1,2,4字节编码方案
        int len = (gbcode & 0xffff0000) != 0 ? 4 : ((gbcode & 0xffffff00) != 0 ? 2 : 1);
        ByteBuffer bb = ByteBuffer.allocate(4);
        switch (len) {
            case 4:
                bb.putInt(gbcode);
                break;
            case 2:
                bb.putShort((short) gbcode);
                break;
            default:
                bb.put((byte) gbcode);
        }
        int gbTransferredCodePoint = new String(bb.array(), GB18030).codePointAt(0);
        int mappedCodePoint = mapping.get(gbcode);

        return gbTransferredCodePoint == mappedCodePoint ? null :
                Integer.toUnsignedString(gbcode, 16)
                        + "\t\t\t" + Integer.toUnsignedString(mappedCodePoint, 16)
                        + "\t\t\t\t" + Integer.toUnsignedString(gbTransferredCodePoint, 16);
    }


    static String byteToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            // byte在转成int类型时, 会扩展成32位, 这一步是为了将高24位置为0
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
