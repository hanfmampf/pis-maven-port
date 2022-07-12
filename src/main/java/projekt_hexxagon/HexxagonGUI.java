package projekt_hexxagon;

import processing.core.PApplet;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class HexxagonGUI extends PApplet {
    public static void main(String[] args) {
        String[] appArgs = {""};
        HexxagonGUI sketch = new HexxagonGUI();
        PApplet.runSketch(appArgs, sketch);
    }
    //variables
    HexxagonGame game;
    boolean ingame = true;
    boolean isPlayerTurn = true;
    int renderLoopCounter = 8;
    Hexxagon.fieldType playerColor;
    Map<int[], int[]> hexCoordinates;
    List<Move> currentMoves;

    public void settings() {
        size(800, 800);
    }


    public void setup() {
        setGame(4);
        frameRate(60);
        background(100);
        textSize(32);
        noStroke();
        draw();
    }
    public void draw(){
        //renderTileCount();
        if (renderLoopCounter != 0) renderLoopCounter--;
        List<Tile[]> cols = this.game.getBoard();
        for (int i = 0; i < cols.size(); i++){
            for (int j = 0; j < cols.get(i).length; j++){
                Tile t = cols.get(i)[j];
                pushMatrix();
                switch (cols.get(i)[j].type){
                    case EMPTY -> fill(255);
                    case RED -> fill(255, 0, 0);
                    case BLUE -> fill(0, 0, 255);
                    case GONE -> fill(0);
                }
                int offsetX = (i * 70) + 100;
                int offsetY = (j * 80) - cols.get(i).length * 40 + 400;
                for (Move m: currentMoves){
                    if (Arrays.equals(t.position, m.to) && t.type == Hexxagon.fieldType.EMPTY) {
                        if (m.isCopy) fill(0,255,0); else fill(255,255,0);
                    }
                }
                hexTile(offsetX, offsetY, 40);
                noFill();
                popMatrix();
            }
        }
        if(!isPlayerTurn && renderLoopCounter == 0){    //TODO nebenläufigkeit dies das
            assert currentMoves.isEmpty(): "hm";
            try {
                this.game = this.game.makeMove(this.game.aiMove());
                this.isPlayerTurn = true;
                renderLoopCounter = 8;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        renderMainMenu();
    }

    void renderTileCount(){
        if (ingame){
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

        }
    }

    void renderMainMenu(){
        ///option for difficulty
        //choose color ? maybe?
        //play button
//        if (!inGame){
//            fill(0,255,0);
//            rect(350, 700, 300, 70);  //rect(btn.px, btn.py, btn.w, btn.h);
//            fill(0);
//            text("PLAY", 770, 720);                                 //text(btn.action, btn.px+20, btn.py+20);
//            noFill();
//        }
    }

    private class mainMenu{
        int[] playBtn = new int[]{350, 700, 300, 70};


    }

    public void mouseClicked(){
        if (currentMoves.isEmpty()){
            hexCoordinates.forEach((key, value) -> {
                if (mouseOver(value)){
                    Tile t = this.game.getBoard().get(key[0])[key[1]];
                    if (t.type == playerColor){
                        currentMoves = this.game.getPossibleMoves(t);
                    } else {
                        currentMoves.clear();
                    }
                }
            });
        } else {
            for (Move currentMove : currentMoves) {
                int[] yolo = hexCoordinates.keySet().stream()
                        .filter(key -> Arrays.equals(key, currentMove.to))
                        .findFirst().get();
                if (hexCoordinates.containsKey(yolo)){
                    if (mouseOver(hexCoordinates.get(yolo))){
                        this.game = this.game.makeMove(currentMove);
                        this.isPlayerTurn = false;
                        renderLoopCounter = 8;
                        break;
                    }
                }
            }
            currentMoves.clear();
        }
    }

    boolean mouseOver(int[] xy){
        float mx = mouseX;
        float my = mouseY;
        return mx > xy[0] - 30 && mx < xy[0] + 30
                && my > xy[1] - 30 && my < xy[1] + 30;
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


    void setGame(int difficulty){
        List<Tile[]> columns = new ArrayList<>();
        Set<Tile> redTiles = new HashSet<>();
        Set<Tile> blueTiles = new HashSet<>();
        Map<Hexxagon.fieldType, Set<Tile>> boardTiles = new HashMap<>();
        boardTiles.put(Hexxagon.fieldType.RED, redTiles);
        boardTiles.put(Hexxagon.fieldType.BLUE, blueTiles);
        currentMoves = new ArrayList<>();

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

        this.playerColor = Hexxagon.fieldType.RED;
        this.game = Hexxagon.of(boardTiles, columns,
                difficulty, Hexxagon.fieldType.RED);

        hexCoordinates = new HashMap<>();
        List<Tile[]> cols = this.game.getBoard();
        for (int i = 0; i < cols.size(); i++){
            for (int j = 0; j < cols.get(i).length; j++){
                int offsetX = (i * 70) + 100;
                int offsetY = (j * 80) - cols.get(i).length * 40 + 400;
                hexCoordinates.put(new int[]{i,j}, new int[]{offsetX, offsetY});
            }
        }
    }
}
