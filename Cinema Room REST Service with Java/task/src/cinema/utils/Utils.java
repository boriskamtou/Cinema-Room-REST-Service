package cinema.utils;

public class Utils {
    public static int settingPrice(int row) {
        if (row <= 4) {
            return 10;
        } else {
            return 8;
        }
    }
}
