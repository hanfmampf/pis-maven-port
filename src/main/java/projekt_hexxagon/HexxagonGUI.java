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
    boolean ingame = false;
    boolean isPlayerTurn = true;
    int renderLoopCounter = 8;
    Hexxagon.fieldType playerColor = Hexxagon.fieldType.RED;
    difficultySettings chosenDiff = difficultySettings.EASY;
    Map<int[], int[]> hexCoordinates;
    List<Move> currentMoves;
    enum difficultySettings {
        EASY,
        NORMAL,
        HARD,
        VERY_HARD,
        UNBEATABLE
    }
    difficultySettings[] diffValues = difficultySettings.values();


    public void settings() {
        size(800, 800);
    }

    public void setup() {
        frameRate(60);
        background(100);
        textSize(32);
        noStroke();
        textAlign(CENTER);
        draw();
    }
    public void draw(){
        if (ingame){
            background(87,8,97);
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
            if(!isPlayerTurn && renderLoopCounter == 0 && !game.isGameOver()){    //TODO nebenläufigkeit dies das
                assert currentMoves.isEmpty(): "hm";
                try {
                    this.game = this.game.makeMove(this.game.aiMove());
                    this.isPlayerTurn = true;
                    renderLoopCounter = 8;
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            if (game.isGameOver()){
                renderEndgameScreen();
            }
            renderTileCount();
        } else {
            background(100);
            renderMainMenu();
        }
    }

    void renderTileCount(){
        int[] rb = getTileCount();
        fill(0);
        text("RED: " + rb[0], 650, 700);
        text("BLUE: " + rb[1], 650, 750);
}

    void renderMainMenu(){
        ///option for difficulty
        //choose color ? maybe?
        //play button
        fill(0);
        text("CHOOSE YOUR DIFFICULTY!", width/2, height - 700);
        fill(87,8,97);
        rect(width-550, height - 650, 300, 70);
        fill(0);
        text(chosenDiff.toString(), width/2, height - 605);


        switch (playerColor){
            case RED -> {
                fill(200, 0,0);
                hexTile(width - 600, height -350, 80);
            }
            case BLUE -> {
                fill(0, 0,200);
                hexTile(width - 200, height -350, 80);
            }
        }

        fill(0);
        text("CHOOSE YOUR COLOR!", width/2, height - 500);
        fill(255, 0,0);
        hexTile(width - 600, height -350, 60);

        fill(0, 0,255);
        hexTile(width - 200, height -350, 60);

        fill(87,8,97);
        rect(width-550, height - 200, 300, 70);  //rect(btn.px, btn.py, btn.w, btn.h);
        fill(0);
        text("PLAY", width/2, height - 155);
        noFill();
    }

    void renderEndgameScreen(){
        String endgameText;
        int[] tileCount = getTileCount();
        if (tileCount[0] > tileCount[1]){
            endgameText = "RED WON!";
        } else if (tileCount[0] < tileCount[1]){
            endgameText = "BLUE WON!";
        } else {
            endgameText = "THE GAME IS TIED!";
        }
        fill(255);
        textSize(150);
        text(endgameText, width/2, height - 400);
        textSize(32);

        fill(255, 95);
        rect(width-550, height - 200, 300, 70);  //rect(btn.px, btn.py, btn.w, btn.h);
        fill(0);
        text("PLAY AGAIN?", width/2, height - 155);
        noFill();
    }

    public void mouseClicked(){
        if (ingame){
            if (game.isGameOver()) {
                if (mouseOverRect(width-550, height - 200, 300, 70)){
                    ingame = false;
                }
            } else {
                if (currentMoves.isEmpty()){
                    hexCoordinates.forEach((key, value) -> {
                        if (mouseOver(value, 40)){
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
                            if (mouseOver(hexCoordinates.get(yolo), 40)){
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
        } else {    //main menu
            if (mouseOverRect(width-550, height - 650, 300, 70)) {  //difficulty
                int next;
                if (chosenDiff == difficultySettings.UNBEATABLE) next = 0; else next = chosenDiff.ordinal()+1;
                chosenDiff = diffValues[next];
            }
            if (mouseOver(new int[]{width - 600, height -350}, 60)){
                playerColor = Hexxagon.fieldType.RED;
            }
            if (mouseOver(new int[]{width - 200, height -350}, 60)){
                playerColor = Hexxagon.fieldType.BLUE;
            }

            if (mouseOverRect(width-550, height - 200, 300, 70)){   //start playing
                setGame(chosenDiff.ordinal() + 1);
                ingame = true;
            }
        }
    }

    boolean mouseOver(int[] xy, int radius){
        int rad = radius - 4;
        float mx = mouseX;
        float my = mouseY;
        return mx > xy[0] - rad && mx < xy[0] + rad
                && my > xy[1] - rad && my < xy[1] + rad;
    }

    boolean mouseOverRect(int x, int y, int w, int h){
        float mx = mouseX;
        float my = mouseY;
        return mx >= x && mx <= x + w &&
                my >= y && my <= y + h;
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

    int[] getTileCount(){
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

    void setGame(int difficulty){
        List<Tile[]> columns = new ArrayList<>();
        Set<Tile> redTiles = new HashSet<>();
        Set<Tile> blueTiles = new HashSet<>();
        Map<Hexxagon.fieldType, Set<Tile>> boardTiles = new HashMap<>();
        boardTiles.put(Hexxagon.fieldType.RED, redTiles);
        boardTiles.put(Hexxagon.fieldType.BLUE, blueTiles);
        currentMoves = new ArrayList<>();

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

        columns.get(0)[0].setType(Hexxagon.fieldType.RED);
        columns.get(0)[4].setType(Hexxagon.fieldType.BLUE);
        columns.get(4)[8].setType(Hexxagon.fieldType.RED);
        columns.get(4)[0].setType(Hexxagon.fieldType.BLUE);
        columns.get(8)[0].setType(Hexxagon.fieldType.RED);
        columns.get(8)[4].setType(Hexxagon.fieldType.BLUE);
        columns.get(4)[3].setType(Hexxagon.fieldType.GONE);
        columns.get(3)[4].setType(Hexxagon.fieldType.GONE);
        columns.get(5)[4].setType(Hexxagon.fieldType.GONE);

        columns.forEach(tArray -> Arrays.stream(tArray)
                .filter(t -> t.type != Hexxagon.fieldType.EMPTY
                        && t.type != Hexxagon.fieldType.GONE)
                .forEach(t -> boardTiles.get(t.type).add(t)));

        this.game = Hexxagon.of(boardTiles, columns,
                difficulty, playerColor);

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
