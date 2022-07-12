package projekt_hexxagon;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

interface HexxagonPublicInterface {
    boolean isGameOver();
    List<MovePublic> getPossibleMoves(TilePublic chosenTile);
    HexxagonPublic makeMove(MovePublic move);
    MovePublic aiMove() throws InterruptedException, ExecutionException;
    List<TilePublic[]> getBoard();
}

public class HexxagonPublic implements HexxagonPublicInterface {
    public final List<TilePublic[]> columns;
    public final Map<fieldType2, Set<TilePublic>> boardTiles;
    public final int difficulty;
    public final fieldType2 playerColor;
    public final fieldType2 aiColor;

    public enum fieldType2 {
        EMPTY,
        RED,
        BLUE,
        GONE,
    }

    public HexxagonPublic(Map<fieldType2, Set<TilePublic>> boardTiles, List<TilePublic[]> columns,
                          int difficulty, fieldType2 playerColor) {
        assert playerColor != fieldType2.EMPTY && playerColor != fieldType2.GONE: "Player Color needs to be RED or BLUE!";
        if (playerColor == fieldType2.RED){
            aiColor = fieldType2.BLUE;
        } else aiColor = fieldType2.RED;
        this.playerColor = playerColor;

        Map<fieldType2, Set<TilePublic>> newMap = new HashMap<>();
        Set<TilePublic> red = new HashSet<>();
        Set<TilePublic> blue = new HashSet<>();

        boardTiles.get(fieldType2.RED).forEach(t -> red.add(t.getCopy()));
        newMap.put(fieldType2.RED, red);
        boardTiles.get(fieldType2.BLUE).forEach(t -> blue.add(t.getCopy()));
        newMap.put(fieldType2.BLUE, blue);

        List<TilePublic[]> newColumns = new ArrayList<>();
        for (TilePublic[] tArray: columns){
            TilePublic[] tmp = new TilePublic[tArray.length];
            IntStream.range(0, tArray.length).forEach(i -> tmp[i] = tArray[i].getCopy());
            newColumns.add(tmp);
        }
        this.boardTiles = newMap;
        this.columns = newColumns;
        this.difficulty = difficulty;
    }

    public static HexxagonPublic of (Map<fieldType2, Set<TilePublic>> boardTiles
            , List<TilePublic[]> columns, int difficulty, fieldType2 playerColor){
        return new HexxagonPublic(boardTiles, columns, difficulty, playerColor);
    }

    public List<TilePublic[]> getBoard(){
        return new ArrayList<>(this.columns);
    }

    public boolean isGameOver(){
        return !movesLeft(fieldType2.RED) || !movesLeft(fieldType2.BLUE);
    }

    public boolean movesLeft(fieldType2 type){
        List<MovePublic> m = new ArrayList<>();
        boardTiles.get(type).forEach(t -> m.addAll(getPossibleMoves(t)));
        return m.size() != 0;
    }

    public List<MovePublic> getPossibleMoves(TilePublic chosenTile){
        if (chosenTile.type != fieldType2.RED && chosenTile.type != fieldType2.BLUE)
            return new ArrayList<>();
        Set<TilePublic> directNeighbors = new HashSet<>();
        Set<TilePublic> jumpNeighbors = new HashSet<>();
        for (int[] neighbor: chosenTile.neighbors){
            TilePublic copyTile = this.columns.get(neighbor[0])[neighbor[1]];
            if (copyTile.type == fieldType2.EMPTY && chosenTile.position != copyTile.position) {
                directNeighbors.add(copyTile);
            }
        }

        assert directNeighbors.stream()
                .allMatch(t -> t.type == fieldType2.EMPTY): "Not all direct neighbors are empty";

        for (int[] neighbor: chosenTile.neighbors){
            for (int[] outerNeighbor: this.columns.get(neighbor[0])[neighbor[1]].neighbors){
                TilePublic jumpTile = this.columns.get(outerNeighbor[0])[outerNeighbor[1]];
                if (!jumpTile.equals(chosenTile)    //filter this in stream?
                        && !directNeighbors.contains(jumpTile)
                        && !jumpNeighbors.contains(jumpTile)
                        && jumpTile.type == fieldType2.EMPTY){
                    jumpNeighbors.add(jumpTile);
                }
            }
        }

        assert jumpNeighbors.stream()
                .allMatch(t -> t.type == fieldType2.EMPTY): "Not all jumpNeighbors are empty";

        List<MovePublic> moves = new ArrayList<>(directNeighbors.stream()
                .map(tile -> MovePublic.of(chosenTile.type, true, chosenTile.position, tile.position))
                .toList());
        moves.addAll(jumpNeighbors.stream()
                .map(tile -> MovePublic.of(chosenTile.type, false, chosenTile.position, tile.position))
                .toList());

        assert moves.stream()
                .allMatch(m -> this.columns.get(m.to[0])[m.to[1]].type == fieldType2.EMPTY)
                : "Illegal Moves, that try to move to a non-empty field!";
        return moves;
    }

    public List<MovePublic> getAllPossibleMoves(fieldType2 type){
        List<MovePublic> allMoves = new ArrayList<>();
        List<MovePublic> waste = new ArrayList<>();
        boardTiles.get(type).forEach(t -> allMoves.addAll(getPossibleMoves(t)));
        for (MovePublic m: allMoves){       //stream
            if (!m.isCopy){     //filter
                List<int[]> neighborsOfMovesTo = this.columns.get(m.to[0])[m.to[1]].neighbors;          //toList / stream
                if (neighborsOfMovesTo.stream().allMatch(mm -> this.columns.get(mm[0])[mm[1]].type == fieldType2.EMPTY)){        //filter
                    waste.add(m);       //dunno
                }
            }
        }
        allMoves.removeAll(waste);
        waste.clear();
        for (MovePublic m: allMoves.stream().filter(move -> !move.isCopy).toList()){    //all jumps
            List<MovePublic> copies = allMoves.stream().filter(move -> move.isCopy).toList();    //all copies
            if (copies.stream().anyMatch(cpMove -> cpMove.to == m.to)){
                waste.add(m);
            }
        }
        allMoves.removeAll(waste);
        return allMoves;
    }

    public MovePublic getRandomMove(fieldType2 color){
        Random rn = new Random();
        List<MovePublic> allMoves = this.getAllPossibleMoves(color);
        assert allMoves.size() != 0: "";
        return allMoves.get(rn.nextInt(0, allMoves.size()));
    }

    public HexxagonPublic makeMove(MovePublic move){
        assert !this.isGameOver(): "The game has already finished";
        assert move.color == fieldType2.BLUE || move.color == fieldType2.RED: "Move does not have a color!";
        assert move.from[0] >= 0 && move.from[1]  >= 0: "Illegal indices in move.from";
        assert move.to[0] >= 0 && move.to[1]  >= 0: "Illegal indices in move.to";
        if (move.isCopy){
            assert move.from[0] - move.to[0] < 2 && move.from[1] - move.to[1] < 2: "Move went too far";
            assert move.from[0] - move.to[0] > -2 && move.from[1] - move.to[1] > -2: "Move went too far";
        } else {
            assert move.from[0] - move.to[0] < 3 && move.from[1] - move.to[1] < 3: "Move went too far";
            assert move.from[0] - move.to[0] > -3 && move.from[1] - move.to[1] > -3: "Move went too far";
        }

        HexxagonPublic newGame = HexxagonPublic.of(this.boardTiles, this.columns
                , this.difficulty, this.playerColor);
        TilePublic moveToTile = newGame.columns.get(move.to[0])[move.to[1]];
        TilePublic moveFromTile = newGame.columns.get(move.from[0])[move.from[1]];

        assert moveToTile.type == fieldType2.EMPTY : "The tile the move is moving to is not empty";
        if (move.isCopy){
            moveToTile.setType(move.color);
            newGame.boardTiles.get(move.color).add(moveToTile);
        } else {    //is jump
            newGame.boardTiles.get(move.color).remove(moveFromTile);
            moveFromTile.setType(fieldType2.EMPTY);
            newGame.boardTiles.get(move.color).add(moveToTile);
            moveToTile.setType(move.color);
        }
        for (int[] neighbor: moveToTile.getNeighbors()){
            TilePublic neighborTile = newGame.columns.get(neighbor[0])[neighbor[1]];
            if (neighborTile.type == fieldType2.RED || neighborTile.type == fieldType2.BLUE){
                if (move.color == fieldType2.BLUE){
                    newGame.boardTiles.get(fieldType2.BLUE).add(neighborTile);
                    newGame.boardTiles.get(fieldType2.RED).remove(neighborTile);
                } else {
                    newGame.boardTiles.get(fieldType2.RED).add(neighborTile);
                    newGame.boardTiles.get(fieldType2.BLUE).remove(neighborTile);
                }
                neighborTile.setType(move.color);
            }
        }
        if (!newGame.movesLeft(fieldType2.RED)){ //fill all blue
            newGame.columns.forEach(col -> Arrays.stream(col)
                    .filter(t -> t.type == fieldType2.EMPTY)
                    .forEach(t -> {
                        t.setType(fieldType2.BLUE);
                        newGame.boardTiles.get(fieldType2.BLUE).add(t);
                    }));
        } else if (!newGame.movesLeft(fieldType2.BLUE)){  //fill all red
            newGame.columns.forEach(col -> Arrays.stream(col)
                    .filter(t -> t.type == fieldType2.EMPTY)
                    .forEach(t -> {
                        t.setType(fieldType2.RED);
                        newGame.boardTiles.get(fieldType2.RED).add(t);
                    }));
        }
        return newGame;
    }

    public MovePublic aiMove() throws InterruptedException, ExecutionException {
        // https://stackoverflow.com/questions/9664036/how-to-run-two-methods-simultaneously
        // https://stackoverflow.com/questions/25231149/can-i-use-callable-threads-without-executorservice
        // https://stackoverflow.com/questions/9418406/running-two-functions-with-threading
        // https://www.journaldev.com/1650/java-futuretask-example-program
        // https://www.baeldung.com/java-executor-service-tutorial
        assert !this.isGameOver(): "Game is over";
        List<MovePublic> allMoves = getAllPossibleMoves(this.aiColor);
        long startTime = System.nanoTime();
        System.out.println("started calculating best move");

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Callable<Map<MovePublic, Float>>> testTasks = new ArrayList<>();
        List<Future<Map<MovePublic, Float>>> results;

        if (this.difficulty > 2){
            allMoves.forEach(m -> testTasks.add(new testMax(m
                    , HexxagonPublic.of(this.boardTiles, this.columns, this.difficulty, this.playerColor))));
        } else {
            allMoves.forEach(m -> testTasks.add(
                    new MonteCarloSimulator(
                            HexxagonPublic.of(this.boardTiles, this.columns, this.difficulty, this.playerColor),
                            m, this.difficulty*50)));
        }
        results = executorService.invokeAll(testTasks);
        executorService.shutdown();
        assert results.stream().allMatch(Future::isDone):"AI move crashed before finishing. Not all Futures are done";

        Map<MovePublic, Float> tmp = new HashMap<>();
        for (Future<Map<MovePublic, Float>> res: results){
            tmp.putAll(res.get());
        }

        MovePublic bestMove = Collections.max(tmp.entrySet(), Map.Entry.comparingByValue()).getKey();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println((float)duration/1_000_000_000 + " Seconds elapsed");
        System.out.println("---------------------");
        return bestMove;
    }

    public static class testMax implements Callable<Map<MovePublic, Float>>{
        MovePublic move;
        HexxagonPublic game;
        int depth;
        Map<MovePublic, Float> resultMap = new HashMap<>();
        public testMax(MovePublic move, HexxagonPublic game) {
            this.move = move;
            this.game = game;
        }
        public Map<MovePublic, Float> call(){
            assert game.difficulty > 2: "Illegal difficulty for Minimax";
            switch(game.difficulty){
                case 3 -> depth = 2;
                case 4,5 -> depth = 3;
            }

            float result = game.minimax(game.makeMove(move), -999, 999, depth, false);
            resultMap.put(this.move, result);
            return resultMap;
        }
    }

    public float minimax(HexxagonPublic game, float alpha, float beta, int depth, boolean isMax){
        if (depth == 0 || game.isGameOver()) {
           // if (game.difficulty == 5) {
                //if (isMax) return (monteCarlo(game, 20));
              //      else return (monteCarlo(game, 20)) * -1;
            //} else {
            return game.boardTiles.get(game.aiColor).size() - game.boardTiles.get(game.playerColor).size();
            //}
            //red is minimizing, therefore is a high negative number good for red, same principle for blue
        }

        if (isMax) {
            List<MovePublic> blueMoves = game.getAllPossibleMoves(game.aiColor);
            float value = -999;
            for (MovePublic m: blueMoves){
                value = Math.max(value, minimax(game.makeMove(m), alpha, beta, depth - 1, false));
                if (value >= beta){
                    break;
                }
                alpha = Math.max(alpha, value);
            }
            return value;
        } else {
            List<MovePublic> redMoves = game.getAllPossibleMoves(game.playerColor);
            float value = 999;
            for (MovePublic m: redMoves){
                value = Math.min(value, minimax(game.makeMove(m), alpha, beta, depth - 1, true));
                if (value <= alpha){
                    break;
                }
                beta = Math.min(beta, value);
            }
            return value;
        }
    }

    public float monteCarlo(HexxagonPublic game, int amount){
        int winCounter = 0;
        for (int i = 0; i < amount; i++){
            HexxagonPublic newGame = HexxagonPublic.of(game.boardTiles, game.columns, game.difficulty, game.playerColor);
            boolean isAI = false;
            for (int j = 0; j < 5; j++){
                if (newGame.isGameOver()) break;
                MovePublic move;
                move = newGame.getRandomMove(isAI ? game.aiColor : game.playerColor); //TODO random move????
                newGame = newGame.makeMove(move);
                isAI = !isAI;
            }
            if (newGame.boardTiles.get(newGame.aiColor).size() > newGame.boardTiles.get(newGame.playerColor).size()){
                winCounter += 1;
            }
        }
        return winCounter / (float)amount;
    }

    public static class MonteCarloSimulator implements Callable<Map<MovePublic, Float>> {
        HexxagonPublic game;
        MovePublic move;
        int amount;
        Map<MovePublic, Float> resultMap = new HashMap<>();

        public MonteCarloSimulator(HexxagonPublic game, MovePublic move, int amount) {
            this.game = game;
            this.move = move;
            this.amount = amount;
        }

        @Override
        public Map<MovePublic, Float> call() {
            float result = game.monteCarlo(game.makeMove(move), amount);
            resultMap.put(move, result);
            return resultMap;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        this.columns.forEach(tArray -> Arrays.stream(tArray).forEach(t -> s.append(t.toString())));
        return s.toString();
    }
}