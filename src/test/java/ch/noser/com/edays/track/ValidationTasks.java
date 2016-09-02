package ch.noser.com.edays.track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by christoph.suter on 19.08.2016.
 */
public final class ValidationTasks {

    private static final int DOWNLOAD_DELAY = 200;
    private static final String WEBSITE_BODY = "<html><body>Content of Webpage</body></html>";

    private ValidationTasks() {
    }

    public static String download(String url) {
        try {
            Thread.sleep(DOWNLOAD_DELAY);
        } catch (InterruptedException e) {
            System.out.println("Download is interrupted: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return WEBSITE_BODY;
    }

    public static List<String> parse(String s) {
        String[] strings = s.replaceAll("\\<.*?>", "").split(" ");
        return new ArrayList<String>(Arrays.asList(strings));
    }

    public static List<String> validate(List<String> strings) {
        Optional<String> stringWithIllegalChar = strings.stream()
                .filter(s -> s.contains("<") || s.contains(">"))
                .findAny();

        if (stringWithIllegalChar.isPresent()) {
            throw new IllegalArgumentException("Illegal char found!");
        }

        return strings;
    }

    public static Integer calculateScore(List<String> strings) {
        return strings.size();
    }

    public static void log(Object v, Throwable e) {
        System.out.println("Value: " + v + ", exception: " + e);
    }
}
