package projekt_hexxagon;

import processing.core.PApplet;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class Tester extends PApplet {
    public static void main(String[] args) {
        String[] appArgs = {""};
        Tester sketch = new Tester();
        PApplet.runSketch(appArgs, sketch);
    }

    HexxagonGame playField;
    HexxagonGame playerOne;
    HexxagonGame playerTwo;
    int p1wins = 0;
    int p2wins = 0;
    int ties = 0;
    int counter = 0;

    void setPlayers(){
        playField = setGame(1, Hexxagon.fieldType.RED);
        playerOne = setGame(4, Hexxagon.fieldType.BLUE); //THIS IS RED
        playerTwo = setGame(4, Hexxagon.fieldType.RED); //THIS IS BLUE
    }
    //difficulty settings =>
    //1 -> monte carlo x 50
    //2 -> monte carlo x 100
    //3 -> minimax depth 2
    //4 -> minimax depth 3
    //5 -> minimax depth 3 + monte carlo

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        setPlayers();
        frameRate(60);
        background(100);
        textSize(32);
        noStroke();
        draw();
    }

    public void draw(){
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        List<Tile[]> cols = this.playField.getBoard();
        for (int i = 0; i < cols.size(); i++){
            for (int j = 0; j < cols.get(i).length; j++){
                pushMatrix();
                switch (cols.get(i)[j].type){
                    case EMPTY -> fill(255);
                    case RED -> fill(255, 0, 0);
                    case BLUE -> fill(0, 0, 255);
                    case GONE -> fill(0);
                }
                int offsetX = (i * 70) + 100;
                int offsetY = (j * 80) - cols.get(i).length * 40 + 400;
                hexTile(offsetX, offsetY, 40);
                noFill();
                popMatrix();
            }
        }
        System.out.printf("Scores %1s ; Total pieces: %2s\n",
                Arrays.toString(getTileCount(playField)),
                Arrays.stream(getTileCount(playField)).sum());
        if (!playField.isGameOver()) {
            Move m = null;
            try {
                if (counter % 2 == 0){
                    m = playerOne.aiMove();
                } else {
                    m = playerTwo.aiMove();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            counter++;
            assert m != null;
            playField = playField.makeMove(m);
            playerOne = playerOne.makeMove(m);
            playerTwo = playerTwo.makeMove(m);
        } else {
            counter = 0;
            int[] scores = getTileCount(playField);
            if (scores[0] > scores[1]){
                p1wins++;
            } else if (scores[0] < scores[1]){
                p2wins++;
            } else {
                ties++;
            }
            System.out.printf("Player RED won %1s times\n", p1wins);
            System.out.printf("Player BLUE won %1s times\n", p2wins);
            System.out.printf("Game was tied %1s times\n", ties);
            setPlayers();
        }
    }

    void hexTile(float x, float y, float radius) {
        float angle = TWO_PI / 6;
        beginShape();
        for (float a = 0; a < TWO_PI; a += angle) {
            float sx = x + cos(a) * radius;
            float sy = y + sin(a) * radius;
            vertex(sx, sy);
        }
        endShape(CLOSE);
    }

    public int[] getTileCount(HexxagonGame game){
        int r = game.getBoard().stream()
                .mapToInt(tArr -> (int) Arrays.stream(tArr)
                        .filter(t -> t.getType() == Hexxagon.fieldType.RED)
                        .count())
                .sum();
        int b = game.getBoard().stream()
                .mapToInt(tArr -> (int) Arrays.stream(tArr)
                        .filter(t -> t.getType() == Hexxagon.fieldType.BLUE)
                        .count())
                .sum();
        return new int[]{r,b};
    }

    public HexxagonGame setGame(int difficulty, Hexxagon.fieldType playerColor){
        List<Tile[]> columns = new ArrayList<>();
        Set<Tile> redTiles = new HashSet<>();
        Set<Tile> blueTiles = new HashSet<>();
        Map<Hexxagon.fieldType, Set<Tile>> boardTiles = new HashMap<>();
        boardTiles.put(Hexxagon.fieldType.RED, redTiles);
        boardTiles.put(Hexxagon.fieldType.BLUE, blueTiles);

        for (int i = 5; i <= 9; i++){     //TODO this whole block is for setting the game up
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
                assert t.getNeighbors().size() > 2 && t.getNeighbors().size() <= 6: "too many neighbors or none at all";
            }
        }

        columns.get(0)[0].setType(Hexxagon.fieldType.RED);
        boardTiles.get(Hexxagon.fieldType.RED).add(columns.get(0)[0]);

        columns.get(0)[4].setType(Hexxagon.fieldType.BLUE);
        boardTiles.get(Hexxagon.fieldType.BLUE).add(columns.get(0)[4]);

        columns.get(4)[8].setType(Hexxagon.fieldType.RED);
        boardTiles.get(Hexxagon.fieldType.RED).add(columns.get(4)[8]);

        columns.get(4)[0].setType(Hexxagon.fieldType.BLUE);
        boardTiles.get(Hexxagon.fieldType.BLUE).add(columns.get(4)[0]);

        columns.get(8)[0].setType(Hexxagon.fieldType.RED);
        boardTiles.get(Hexxagon.fieldType.RED).add(columns.get(8)[0]);

        columns.get(8)[4].setType(Hexxagon.fieldType.BLUE);
        boardTiles.get(Hexxagon.fieldType.BLUE).add(columns.get(8)[4]);

        columns.get(4)[3].setType(Hexxagon.fieldType.GONE);
        columns.get(3)[4].setType(Hexxagon.fieldType.GONE);
        columns.get(5)[4].setType(Hexxagon.fieldType.GONE);

        return Hexxagon.of(boardTiles, columns,
                difficulty, playerColor);
    }
}
