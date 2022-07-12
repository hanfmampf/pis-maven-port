package projekt_hexxagon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class HexxagonPublicTest {

    enum settings {
        DEFAULT,
        EMPTY,
        ONEPLAYER,
        FULL,
        AILOST,
        CENTER
    }
    static HexxagonPublic defaultGame;
    static HexxagonPublic fullGame;
    static HexxagonPublic onePlayerGame;
    static HexxagonPublic emptyGame;
    static HexxagonPublic aiLostGame;
    static HexxagonPublic centerGame;

    @BeforeAll
    static void setup() {
        defaultGame = getGameWithSettings(HexxagonPublicTest.settings.DEFAULT);
        fullGame = getGameWithSettings(HexxagonPublicTest.settings.FULL);
        onePlayerGame = getGameWithSettings(HexxagonPublicTest.settings.ONEPLAYER);
        emptyGame = getGameWithSettings(HexxagonPublicTest.settings.EMPTY);
        aiLostGame = getGameWithSettings(HexxagonPublicTest.settings.AILOST);
        centerGame = getGameWithSettings(HexxagonPublicTest.settings.CENTER);
    }

    @Test
    void of() {
    }

    @Test
    void movesLeft() {
        Assertions.assertAll(
                ()-> assertTrue(defaultGame.movesLeft(HexxagonPublic.fieldType2.RED)),
                ()-> assertTrue(defaultGame.movesLeft(HexxagonPublic.fieldType2.BLUE)),
                ()-> assertFalse(fullGame.movesLeft(HexxagonPublic.fieldType2.RED)),
                ()-> assertFalse(fullGame.movesLeft(HexxagonPublic.fieldType2.BLUE)),
                ()-> assertTrue(onePlayerGame.movesLeft(HexxagonPublic.fieldType2.RED)),
                ()-> assertFalse(onePlayerGame.movesLeft(HexxagonPublic.fieldType2.BLUE)),
                ()-> assertFalse(emptyGame.movesLeft(HexxagonPublic.fieldType2.RED)),
                ()-> assertFalse(emptyGame.movesLeft(HexxagonPublic.fieldType2.BLUE)),
                ()-> assertTrue(centerGame.movesLeft(HexxagonPublic.fieldType2.RED)),
                ()-> assertTrue(centerGame.movesLeft(HexxagonPublic.fieldType2.BLUE))
                );
    }

    @Test
    void getAllPossibleMoves() {

    }

    @Test
    void getRandomMove() {
    }

    @Test
    void minimax() {

        //assert isgameover bei lost games
    }

    @Test
    void monteCarlo() {
        //assert game over bei lost games
    }

    static HexxagonPublic getGameWithSettings(HexxagonPublicTest.settings setting) {
        Set<TilePublic> redTiles = new HashSet<>();
        Set<TilePublic> blueTiles = new HashSet<>();
        Map<HexxagonPublic.fieldType2, Set<TilePublic>> boardTiles = new HashMap<>();
        boardTiles.put(HexxagonPublic.fieldType2.RED, redTiles);
        boardTiles.put(HexxagonPublic.fieldType2.BLUE, blueTiles);
        List<TilePublic[]> columns = setupBoard();

        Random rn = new Random();
        switch(setting) {
            case EMPTY -> {
                return HexxagonPublic.of(boardTiles, columns,
                        3, HexxagonPublic.fieldType2.RED);
            }
            case DEFAULT -> {
                columns.get(0)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(0)[4].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(4)[8].setType(HexxagonPublic.fieldType2.RED);
                columns.get(4)[0].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(8)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(8)[4].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(4)[3].setType(HexxagonPublic.fieldType2.GONE);
                columns.get(3)[4].setType(HexxagonPublic.fieldType2.GONE);
                columns.get(5)[4].setType(HexxagonPublic.fieldType2.GONE);
            }
            case ONEPLAYER -> {
                columns.get(0)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(4)[8].setType(HexxagonPublic.fieldType2.RED);
                columns.get(8)[0].setType(HexxagonPublic.fieldType2.RED);
            }
            case FULL -> {
                columns.forEach(tArray -> Arrays.stream(tArray)
                        .forEach(t -> t.setType(rn.nextBoolean()
                                ? HexxagonPublic.fieldType2.RED
                                : HexxagonPublic.fieldType2.BLUE)));
            }
            case AILOST -> {
                columns.get(0)[0].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(0)[1].setType(HexxagonPublic.fieldType2.RED);
                columns.get(0)[2].setType(HexxagonPublic.fieldType2.RED);

                columns.get(1)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(1)[1].setType(HexxagonPublic.fieldType2.RED);
                columns.get(1)[2].setType(HexxagonPublic.fieldType2.RED);

                columns.get(2)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(2)[1].setType(HexxagonPublic.fieldType2.RED);
                columns.get(2)[2].setType(HexxagonPublic.fieldType2.RED);

            }
            case CENTER -> {
                columns.get(0)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(0)[4].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(4)[8].setType(HexxagonPublic.fieldType2.RED);

                columns.get(4)[4].setType(HexxagonPublic.fieldType2.RED);

                columns.get(4)[0].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(8)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(8)[4].setType(HexxagonPublic.fieldType2.BLUE);

                columns.get(4)[3].setType(HexxagonPublic.fieldType2.GONE);
                columns.get(3)[4].setType(HexxagonPublic.fieldType2.GONE);
                columns.get(5)[4].setType(HexxagonPublic.fieldType2.GONE);
            }

            default -> throw
                    new RuntimeException("Unexpected enum value occurred getGameWithSettings");
        }


        columns.forEach(tArray -> Arrays.stream(tArray)
                .filter(t -> t.type != HexxagonPublic.fieldType2.EMPTY
                        && t.type != HexxagonPublic.fieldType2.GONE)
                .forEach(t -> boardTiles.get(t.type).add(t)));

        return HexxagonPublic.of(boardTiles, columns,
                3, HexxagonPublic.fieldType2.RED);
    }

    static List<TilePublic[]> setupBoard() {
        List<TilePublic[]> columns = new ArrayList<>();
        for (int i = 5; i <= 9; i++){
            columns.add(new TilePublic[i]);
        }
        for (int i = 8; i >= 5; i--){
            columns.add(new TilePublic[i]);
        }
        // https://www.redblobgames.com/grids/hexagons/
        // TODO try to find a common pattern and extract it
        int[][] topLeft = new int[][]{   //possible neighbors for top left tiles
                {1, 0},{1, 1},
                {0, 1},{-1, 0}
        };
        int[][] bottomLeft = new int[][]{
                {-1, -1},{0, -1},
                {1, 0}, {1, 1}
        };
        int[][] topRight = new int[][]{
                {1, 0},{0, 1},
                {-1, 1}, {-1, 0}
        };
        int[][] bottomRight = new int[][]{
                {-1, 1},{-1, 0},
                {0, -1}, {1, -1}
        };
        int[][] allLeft = new int[][]{   //all tiles that are not on the edges
                {0, -1},{1, 0},{1, 1},
                {0, 1},{-1, 0},{-1, -1}
        };
        int[][] allRight = new int[][]{   //all tiles that are not on the edges
                {0, -1},{1, -1},{1, 0},
                {0, 1},{-1, 1},{-1, 0}
        };
        int[][] allCenter = new int[][]{   //all tiles that are not on the edges
                {0, -1},{1, -1},{1, 0},
                {0, 1},{-1, 0},{-1, -1}
        };

        IntStream.range(0, columns.size())
                .forEach(i -> IntStream.range(0,columns.get(i).length)
                        .forEach(j ->  columns.get(i)[j]
                                = new TilePublic(HexxagonPublic.fieldType2.EMPTY, new int[]{i,j})));

        for (int i = 0; i < columns.size(); i++){
            for (int j = 0; j < columns.get(i).length; j++){
                int colLength = columns.get(i).length -1;
                int[][] toBeUsed = new int[][]{};
                TilePublic t = columns.get(i)[j];
                if (i < 4 && j == 0){
                    //top left
                    toBeUsed = topLeft;
                } else if (i < 4 && j == colLength){
                    //bottom left
                    toBeUsed = bottomLeft;
                } else if (i > 4 && j == 0){
                    //top right
                    toBeUsed = topRight;
                } else if (i > 4 && j == colLength) {
                    //bottom right
                    toBeUsed = bottomRight;
                } else if (i == 4 && j == 0) {
                    //center top
                    toBeUsed = new int[][]{{-1, 0}, {1, 0}, {0, 1}};
                } else if (i == 4 && j == colLength){
                    toBeUsed = new int[][]{{-1, -1}, {1, -1}, {0, -1}};
                } else if (i < 4){
                    //any other tile
                    toBeUsed = allLeft;
                } else if (i > 4){
                    toBeUsed = allRight;
                } else {
                    toBeUsed = allCenter;
                }
                for (int[] n: toBeUsed){
                    int x = i + n[0];
                    int y = j + n[1];
                    if (x >= 0 && x <= 8 && y >= 0 && y < columns.get(x).length){
                        t.getNeighbors().add(new int[]{x,y});
                    }
                }
                assert t.getNeighbors().size()
                        > 2 && t.getNeighbors().size() <= 6: "too many neighbors or none at all";
            }
        }
        return columns;
    }
}