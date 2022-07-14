package projekt_hexxagon;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class HexxagonGameTest {
    enum settings {
        DEFAULT,
        EMPTY,
        ONEPLAYER,
        FULL,
        AILOST,
        CENTER
    }
    static HexxagonGame defaultGame;
    static HexxagonGame fullGame;
    static HexxagonGame onePlayerGame;
    static HexxagonGame emptyGame;
    static HexxagonGame aiLostGame;
    static HexxagonGame centerGame;

    @BeforeAll
    static void setup() {
        defaultGame = getGameWithSettings(settings.DEFAULT);
        fullGame = getGameWithSettings(settings.FULL);
        onePlayerGame = getGameWithSettings(settings.ONEPLAYER);
        emptyGame = getGameWithSettings(settings.EMPTY);
        aiLostGame = getGameWithSettings(settings.AILOST);
        centerGame = getGameWithSettings(settings.CENTER);
    }

    @AfterAll
    static void tearDown() {
    }

    /** Test if isGameOver returns
     * the correct boolean value for the given games
     * **/
    @Test
    void isGameOver() {
        Assertions.assertFalse(defaultGame.isGameOver());
        Assertions.assertFalse(centerGame.isGameOver());
        Assertions.assertTrue(emptyGame.isGameOver());
        Assertions.assertTrue(onePlayerGame.isGameOver());
        Assertions.assertTrue(fullGame.isGameOver());
        Assertions.assertTrue(aiLostGame.isGameOver());
    }

    /** Test if getPossibleMoves returns
     *  the correct Lists in the given games
     * **/
    @Test
    void getPossibleMoves() {
        Tile emptyTile = emptyGame.getBoard().get(0)[0];
        Tile noMoveTile = aiLostGame.getBoard().get(0)[0];
        Tile redTile = defaultGame.getBoard().get(0)[0];
        Tile centerTile = fullGame.getBoard().get(4)[4];
        Tile centerTile2 = centerGame.getBoard().get(4)[4];

        Assertions.assertTrue(emptyGame.getPossibleMoves(emptyTile).isEmpty());
        Assertions.assertTrue(aiLostGame.getPossibleMoves(noMoveTile).isEmpty());
        Assertions.assertEquals(8, defaultGame.getPossibleMoves(redTile).size());
        Assertions.assertTrue(fullGame.getPossibleMoves(centerTile).isEmpty());
        Assertions.assertEquals(15, centerGame.getPossibleMoves(centerTile2).size());
    }

    /** Test if makeMove throws the correct
     * assertion errors when a bad move is passed in
     * **/
    @Test
    void makeMove() {
        Move tooFar = Move.of(Hexxagon.fieldType.RED, false, new int[]{0,0}, new int[]{7,5});
        AssertionError e = Assertions.assertThrows(AssertionError.class, ()-> {
            defaultGame.makeMove(tooFar);
        });
        Assertions.assertEquals("Move went too far", e.getMessage());

        Move badTarget = Move.of(Hexxagon.fieldType.RED, false, new int[]{0,0}, new int[]{-1,-1});
        e = Assertions.assertThrows(AssertionError.class, ()-> {
            defaultGame.makeMove(badTarget);
        });
        Assertions.assertEquals("Illegal indices in move.to", e.getMessage());
        Move badTarget2 = Move.of(Hexxagon.fieldType.RED, false, new int[]{-1,-1}, new int[]{1,1});
        e = Assertions.assertThrows(AssertionError.class, ()-> {
            defaultGame.makeMove(badTarget2);
        });
        Assertions.assertEquals("Illegal indices in move.from", e.getMessage());
    }

    /** Test if aiMove returns a legit moves
     *  and throws the correct assertion errors
     * **/
    @Test
    void aiMove() throws ExecutionException, InterruptedException {
        AssertionError e = Assertions.assertThrows(AssertionError.class, ()-> {
            fullGame.aiMove();
        });
        Assertions.assertEquals("Game is over", e.getMessage());

        e = Assertions.assertThrows(AssertionError.class, ()-> {
            aiLostGame.aiMove();
        });
        Assertions.assertEquals("Game is over", e.getMessage());

        e = Assertions.assertThrows(AssertionError.class, ()-> {
            emptyGame.aiMove();
        });
        Assertions.assertEquals("Game is over", e.getMessage());

        Move move = centerGame.aiMove();
        List<Tile[]> board = centerGame.getBoard();
        Tile moveFromTile = board.get(move.from()[0])[move.from()[1]];
        Tile moveToTile = board.get(move.to()[0])[move.to()[1]];
        assertSame(moveToTile.type, Hexxagon.fieldType.EMPTY);
        assertSame(moveFromTile.type, move.color());
    }

    /** Test if getBoard() causes reference leaks
     * **/
    @Test
    void getBoard() throws ExecutionException, InterruptedException {
        List<Tile[]> board = defaultGame.getBoard();
        HexxagonGame newGame = defaultGame.makeMove(defaultGame.aiMove());
        Assertions.assertNotEquals(board.hashCode(), newGame.getBoard().hashCode());
        Assertions.assertNotEquals(getTileCountBoard(newGame.getBoard()), getTileCountBoard(defaultGame.getBoard()));

        board.get(0)[0].setType(Hexxagon.fieldType.GONE);
        assertNotEquals(board.get(0)[0].getType(), defaultGame.getBoard().get(0)[0].getType());

    }

    public int[] getTileCountBoard(List<Tile[]> board){
        int r = board.stream()
                .mapToInt(tArr -> (int) Arrays.stream(tArr)
                        .filter(t -> t.getType() == Hexxagon.fieldType.RED)
                        .count())
                .sum();
        int b = board.stream()
                .mapToInt(tArr -> (int) Arrays.stream(tArr)
                        .filter(t -> t.getType() == Hexxagon.fieldType.BLUE)
                        .count())
                .sum();
        return new int[]{r,b};
    }

    static HexxagonGame getGameWithSettings(settings setting) {
        Set<Tile> redTiles = new HashSet<>();
        Set<Tile> blueTiles = new HashSet<>();
        Map<Hexxagon.fieldType, Set<Tile>> boardTiles = new HashMap<>();
        boardTiles.put(Hexxagon.fieldType.RED, redTiles);
        boardTiles.put(Hexxagon.fieldType.BLUE, blueTiles);
        List<Tile[]> columns = setupBoard();

        Random rn = new Random();
        switch(setting) {
            case EMPTY -> {
                return Hexxagon.of(boardTiles, columns,
                        3, Hexxagon.fieldType.RED);
            }
            case DEFAULT -> {
                columns.get(0)[0].setType(Hexxagon.fieldType.RED);
                columns.get(0)[4].setType(Hexxagon.fieldType.BLUE);
                columns.get(4)[8].setType(Hexxagon.fieldType.RED);
                columns.get(4)[0].setType(Hexxagon.fieldType.BLUE);
                columns.get(8)[0].setType(Hexxagon.fieldType.RED);
                columns.get(8)[4].setType(Hexxagon.fieldType.BLUE);
                columns.get(4)[3].setType(Hexxagon.fieldType.GONE);
                columns.get(3)[4].setType(Hexxagon.fieldType.GONE);
                columns.get(5)[4].setType(Hexxagon.fieldType.GONE);
            }
            case ONEPLAYER -> {
                columns.get(0)[0].setType(Hexxagon.fieldType.RED);
                columns.get(4)[8].setType(Hexxagon.fieldType.RED);
                columns.get(8)[0].setType(Hexxagon.fieldType.RED);
            }
            case FULL -> {
                columns.forEach(tArray -> Arrays.stream(tArray)
                        .forEach(t -> t.setType(rn.nextBoolean()
                                ? Hexxagon.fieldType.RED
                                : Hexxagon.fieldType.BLUE)));
            }
            case AILOST -> {
                columns.get(0)[0].setType(Hexxagon.fieldType.BLUE);
                columns.get(0)[1].setType(Hexxagon.fieldType.RED);
                columns.get(0)[2].setType(Hexxagon.fieldType.RED);

                columns.get(1)[0].setType(Hexxagon.fieldType.RED);
                columns.get(1)[1].setType(Hexxagon.fieldType.RED);
                columns.get(1)[2].setType(Hexxagon.fieldType.RED);

                columns.get(2)[0].setType(Hexxagon.fieldType.RED);
                columns.get(2)[1].setType(Hexxagon.fieldType.RED);
                columns.get(2)[2].setType(Hexxagon.fieldType.RED);

            }
            case CENTER -> {
                columns.get(0)[0].setType(Hexxagon.fieldType.RED);
                columns.get(0)[4].setType(Hexxagon.fieldType.BLUE);
                columns.get(4)[8].setType(Hexxagon.fieldType.RED);

                columns.get(4)[4].setType(Hexxagon.fieldType.RED);

                columns.get(4)[0].setType(Hexxagon.fieldType.BLUE);
                columns.get(8)[0].setType(Hexxagon.fieldType.RED);
                columns.get(8)[4].setType(Hexxagon.fieldType.BLUE);

                columns.get(4)[3].setType(Hexxagon.fieldType.GONE);
                columns.get(3)[4].setType(Hexxagon.fieldType.GONE);
                columns.get(5)[4].setType(Hexxagon.fieldType.GONE);
            }

            default -> throw
                    new RuntimeException("Unexpected enum value occurred getGameWithSettings");
        }


        columns.forEach(tArray -> Arrays.stream(tArray)
                .filter(t -> t.type != Hexxagon.fieldType.EMPTY
                        && t.type != Hexxagon.fieldType.GONE)
                .forEach(t -> boardTiles.get(t.type).add(t)));

        return Hexxagon.of(boardTiles, columns,
                3, Hexxagon.fieldType.RED);
    }

    static List<Tile[]> setupBoard() {
        List<Tile[]> columns = new ArrayList<>();
        for (int i = 5; i <= 9; i++){
            columns.add(new Tile[i]);
        }
        for (int i = 8; i >= 5; i--){
            columns.add(new Tile[i]);
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
                                = new Tile(Hexxagon.fieldType.EMPTY, new int[]{i,j})));

        for (int i = 0; i < columns.size(); i++){
            for (int j = 0; j < columns.get(i).length; j++){
                int colLength = columns.get(i).length -1;
                int[][] toBeUsed = new int[][]{};
                Tile t = columns.get(i)[j];
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