package projekt_hexxagon;

import java.util.*;
//TODO Immutable
public class Tile {
    Hexxagon.fieldType type;
    int[] position;
    List<int[]> neighbors = new ArrayList<>();

    public List<int[]> getNeighbors() {
        return neighbors;
    }
    public Hexxagon.fieldType getType(){
        return this.type;
    }
    public void setType(Hexxagon.fieldType type){
        this.type = type;
    }
    public Tile(Hexxagon.fieldType type, int[] position){
        assert position.length == 2: "Tile has more than two positional values";
        this.position = position;
        this.type = type;
    }

    public Tile getCopy(){
        Tile x = new Tile(this.type, this.position.clone());
        this.neighbors.forEach(n -> x.neighbors.add(n.clone()));
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
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