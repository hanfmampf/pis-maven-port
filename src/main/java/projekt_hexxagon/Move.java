package projekt_hexxagon;

import java.util.Arrays;
import java.util.Objects;

public record Move(Hexxagon.fieldType color, boolean isCopy, int[] from, int[] to) {

    public static Move of(Hexxagon.fieldType color, boolean isCopy, int[] from, int[] to) {
        return new Move(color, isCopy, from, to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return this.from[0] == move.from[0] && this.from[1] == move.from[1]
                && this.to[0] == move.to[0] && this.to[1] == move.to[1]
                && this.isCopy == move.isCopy && this.color == move.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.color, Arrays.hashCode(this.to)
                , Arrays.hashCode(this.from), isCopy);
    }

    @Override
    public String toString() {
        return "Move{" +
                "color=" + color +
                ", isCopy=" + isCopy +
                ", from=" + Arrays.toString(from) +
                ", to=" + Arrays.toString(to) +
                '}';
    }
}
