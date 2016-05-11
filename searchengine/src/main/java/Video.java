import java.util.List;

public class Video {
    private String name;
    private int offset;

    public Video(String n, int o) {
        name = n;
        offset = o;
    }

    public String getName() {
        return this.name;
    }

    public int getTime() {
        return this.offset;
    }
}

