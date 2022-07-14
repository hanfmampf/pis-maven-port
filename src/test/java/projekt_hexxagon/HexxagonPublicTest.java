package projekt_hexxagon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class HexxagonPublicTest {

    enum settings {
        DEFAULT,
        EMPTY,
        ONEPLAYER,
        FULL,
        AILOST,
        CENTER,
        OBVWIN
    }
    static HexxagonPublic defaultGame;
    static HexxagonPublic fullGame;
    static HexxagonPublic onePlayerGame;
    static HexxagonPublic emptyGame;
    static HexxagonPublic aiLostGame;
    static HexxagonPublic centerGame;
    static HexxagonPublic obvBestMoveGame;

    @BeforeAll
    static void setup() {
        defaultGame = getGameWithSettings(HexxagonPublicTest.settings.DEFAULT);
        fullGame = getGameWithSettings(HexxagonPublicTest.settings.FULL);
        onePlayerGame = getGameWithSettings(HexxagonPublicTest.settings.ONEPLAYER);
        emptyGame = getGameWithSettings(HexxagonPublicTest.settings.EMPTY);
        aiLostGame = getGameWithSettings(HexxagonPublicTest.settings.AILOST);
        centerGame = getGameWithSettings(HexxagonPublicTest.settings.CENTER);
        obvBestMoveGame = getGameWithSettings(HexxagonPublicTest.settings.OBVWIN);
    }

    /** Test if the movesLeft Method returns correct Values
     *  for all players in the given games
     * **/
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

    /** Test if getAllPossibleMoves returns
     * the expected number of moves in a given game
     * **/
    @Test
    void getAllPossibleMoves() {
        //assertequal size
        List<MovePublic> allMoves = centerGame.getAllPossibleMoves(HexxagonPublic.fieldType2.RED);
        assertNotEquals(39, allMoves.size());
        assertEquals(21, allMoves.size());
    }


    /** Test if getRandomMove produces
     *  a legit move in a given game
     * **/
    @Test
    void getRandomMove() {
        //test if move is valid
        MovePublic move = centerGame.getRandomMove(HexxagonPublic.fieldType2.RED);
        List<TilePublic[]> board = centerGame.getBoard();

        TilePublic moveFromTile = board.get(move.from()[0])[move.from()[1]];
        TilePublic moveToTile = board.get(move.to()[0])[move.to()[1]];
        assertSame(moveToTile.type, HexxagonPublic.fieldType2.EMPTY);
        assertSame(moveFromTile.type, move.color());

    }

    /** Test if minimax chooses the best move in a set scenario
     * **/
    @RepeatedTest(10)
    void minimax() {
        List<MovePublic> allMoves = obvBestMoveGame.getAllPossibleMoves(HexxagonPublic.fieldType2.BLUE);
        List<Callable<Map<MovePublic, Float>>> aiTasks = new ArrayList<>();
        allMoves.forEach(move -> aiTasks.add(new HexxagonPublic.aiTask(obvBestMoveGame, move
                , 150, HexxagonPublic.aiMode.MINMAX, 3)));
        Map<MovePublic, Float> tmp = new HashMap<>();
        aiTasks.forEach(t -> {
            try {
                tmp.putAll(t.call());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        MovePublic bestMove = Collections.max(tmp.entrySet(), Map.Entry.comparingByValue()).getKey();
        assertArrayEquals(bestMove.to(), new int[]{2, 2});
    }

    /** Test if monteCarlo chooses the best move in a set scenario
     * **/
    @RepeatedTest(10)
    void monteCarlo() {
        int difficulty = 2;
        List<MovePublic> allMoves = obvBestMoveGame.getAllPossibleMoves(HexxagonPublic.fieldType2.BLUE);
        List<Callable<Map<MovePublic, Float>>> aiTasks = new ArrayList<>();
        allMoves.forEach(move -> aiTasks.add(new HexxagonPublic.aiTask(obvBestMoveGame, move
                , 100, HexxagonPublic.aiMode.MCS, difficulty - 1)));
        Map<MovePublic, Float> tmp = new HashMap<>();
        aiTasks.forEach(t -> {
            try {
                tmp.putAll(t.call());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        MovePublic bestMove = Collections.max(tmp.entrySet(), Map.Entry.comparingByValue()).getKey();

        assertArrayEquals(bestMove.to(), new int[]{2, 2});
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
            case OBVWIN -> {
                columns.get(1)[0].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(1)[1].setType(HexxagonPublic.fieldType2.RED);
                columns.get(1)[2].setType(HexxagonPublic.fieldType2.RED);
                columns.get(2)[1].setType(HexxagonPublic.fieldType2.RED);
                columns.get(2)[3].setType(HexxagonPublic.fieldType2.RED);
                columns.get(3)[2].setType(HexxagonPublic.fieldType2.RED);
                columns.get(3)[3].setType(HexxagonPublic.fieldType2.RED);

                columns.get(5)[0].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(5)[1].setType(HexxagonPublic.fieldType2.RED);
                columns.get(5)[2].setType(HexxagonPublic.fieldType2.RED);
                columns.get(6)[0].setType(HexxagonPublic.fieldType2.RED);
                columns.get(6)[2].setType(HexxagonPublic.fieldType2.RED);
                columns.get(7)[0].setType(HexxagonPublic.fieldType2.RED);

                columns.get(4)[5].setType(HexxagonPublic.fieldType2.BLUE);
                columns.get(4)[6].setType(HexxagonPublic.fieldType2.RED);
                columns.get(3)[7].setType(HexxagonPublic.fieldType2.RED);
                columns.get(4)[8].setType(HexxagonPublic.fieldType2.RED);
                columns.get(5)[7].setType(HexxagonPublic.fieldType2.RED);
            }
            default -> throw
                    new RuntimeException("Unexpected enum value occurred getGameWithSettings");
        }


        columns.forEach(tArray -> Arrays.stream(tArray)
                .filter(t -> t.type != HexxagonPublic.fieldType2.EMPTY
                        && t.type != HexxagonPublic.fieldType2.GONE)
                .forEach(t -> boardTiles.get(t.type).add(t)));

        return HexxagonPublic.of(boardTiles, columns,
                4, HexxagonPublic.fieldType2.RED);
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