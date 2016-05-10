import java.util.List;

public class Video {
    private String name;
    private List<Integer> offsets;

    public Video(String n, List<Integer> o) {
        name = n;
        offsets = o;
    }

    public String getName() {
        return this.name;
    }

    public List<Integer> getTimes() {
        return this.offsets;
    }
}

