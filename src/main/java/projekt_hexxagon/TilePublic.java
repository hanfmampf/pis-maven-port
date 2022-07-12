package projekt_hexxagon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TilePublic {
    public HexxagonPublic.fieldType2 type;
    int[] position;
    List<int[]> neighbors = new ArrayList<>();

    public List<int[]> getNeighbors() {
        return neighbors;
    }
    public HexxagonPublic.fieldType2 getType(){
        return this.type;
    }
    public void setType(HexxagonPublic.fieldType2 type){
        this.type = type;
    }
    public TilePublic(HexxagonPublic.fieldType2 type, int[] position){
        assert position.length == 2: "Tile has more than two positional values";
        this.position = position;
        this.type = type;
    }

    public TilePublic getCopy(){
        HexxagonPublic.fieldType2 t = null;
        switch(this.type){
            case RED -> t = HexxagonPublic.fieldType2.RED;
            case GONE -> t = HexxagonPublic.fieldType2.GONE;
            case EMPTY -> t = HexxagonPublic.fieldType2.EMPTY;
            case BLUE -> t = HexxagonPublic.fieldType2.BLUE;
        }
        assert t != null: "Tile Copy error occurred: fieldType t is null";
        TilePublic x = new TilePublic(t, this.position.clone());
        this.neighbors.forEach(n -> x.neighbors.add(n.clone()));
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TilePublic tile = (TilePublic) o;
        return this.position[0] == tile.position[0] && this.position[1] == tile.position[1];
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.type, Arrays.hashCode(this.position));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Tile: ");
        s.append("type=").append(type);
        s.append(", position=").append(Arrays.toString(position));
        s.append(", neighbors=");
        for (int[] n: neighbors) s.append(Arrays.toString(n)).append(", ");
        return s.toString();
    }
}