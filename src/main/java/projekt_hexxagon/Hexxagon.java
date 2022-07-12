package projekt_hexxagon;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

interface HexxagonGame {
    boolean isGameOver();
    List<Move> getPossibleMoves(Tile chosenTile);
    Hexxagon makeMove(Move move);
    Move aiMove() throws InterruptedException, ExecutionException;
    List<Tile[]> getBoard();
}

public class Hexxagon implements HexxagonGame{
    private final List<Tile[]> columns;
    private final Map<fieldType, Set<Tile>> boardTiles;
    private final int difficulty;
    private final fieldType playerColor;
    private final fieldType aiColor;

    // https://www.dataset.com/blog/maven-log4j2-project/
    private static final Logger logger = LogManager.getRootLogger();

    enum fieldType {
        EMPTY,
        RED,
        BLUE,
        GONE,
    }
    //logger.debug("");

    private Hexxagon(Map<fieldType, Set<Tile>> boardTiles, List<Tile[]> columns,
                     int difficulty, fieldType playerColor) {
        assert playerColor != fieldType.EMPTY
                && playerColor != fieldType.GONE: "Player Color needs to be RED or BLUE!";

        logger.debug("A new instance of Hexxagon has been created");

        if (playerColor == fieldType.RED){
            aiColor = fieldType.BLUE;
        } else aiColor = fieldType.RED;
        this.playerColor = playerColor;

        Map<fieldType, Set<Tile>> newMap = new HashMap<>();
        Set<Tile> red = new HashSet<>();
        Set<Tile> blue = new HashSet<>();

        boardTiles.get(fieldType.RED).forEach(t -> red.add(t.getCopy()));
        newMap.put(fieldType.RED, red);
        boardTiles.get(fieldType.BLUE).forEach(t -> blue.add(t.getCopy()));
        newMap.put(fieldType.BLUE, blue);

        List<Tile[]> newColumns = new ArrayList<>();
        for (Tile[] tArray: columns){
            Tile[] tmp = new Tile[tArray.length];
            IntStream.range(0, tArray.length).forEach(i -> tmp[i] = tArray[i].getCopy());
            newColumns.add(tmp);
        }
        this.boardTiles = newMap;
        this.columns = newColumns;
        this.difficulty = difficulty;
    }

    public static Hexxagon of (Map<fieldType, Set<Tile>> boardTiles
            ,List<Tile[]> columns, int difficulty, fieldType playerColor){
        logger.debug("Hexxagon.of() has been called");
        return new Hexxagon(boardTiles, columns, difficulty, playerColor);
    }

    public List<Tile[]> getBoard(){
        //return new ArrayList<>(this.columns);
        List<Tile[]> newColumns = new ArrayList<>();
        for (Tile[] tArray: columns){
            Tile[] tmp = new Tile[tArray.length];
            IntStream.range(0, tArray.length).forEach(i -> tmp[i] = tArray[i].getCopy());
            newColumns.add(tmp);
        }
        //logger.debug("Copy of board is being returned by getBoard()");
        return newColumns;
    }

    public boolean isGameOver(){
        boolean result = !movesLeft(fieldType.RED) || !movesLeft(fieldType.BLUE);
        logger.debug("isGameOver() is called will return {}", result);
        return result;
    }

    private boolean movesLeft(fieldType type){
        List<Move> m = new ArrayList<>();
        boardTiles.get(type).forEach(t -> m.addAll(getPossibleMoves(t)));
        logger.debug("movesLeft for {} is called and returns {}", type, m.size() != 0);
        return m.size() != 0;
    }

    public List<Move> getPossibleMoves(Tile chosenTile){
        if (chosenTile.type != fieldType.RED && chosenTile.type != fieldType.BLUE)
            return new ArrayList<>();
        Set<Tile> directNeighbors = new HashSet<>();
        Set<Tile> jumpNeighbors = new HashSet<>();
        for (int[] neighbor: chosenTile.neighbors){
            Tile copyTile = this.columns.get(neighbor[0])[neighbor[1]];
            if (copyTile.type == fieldType.EMPTY && chosenTile.position != copyTile.position) {
                directNeighbors.add(copyTile);
            }
        }

        assert directNeighbors.stream()
                .allMatch(t -> t.type == fieldType.EMPTY): "Not all direct neighbors are empty";

        for (int[] neighbor: chosenTile.neighbors){
            for (int[] outerNeighbor: this.columns.get(neighbor[0])[neighbor[1]].neighbors){
                Tile jumpTile = this.columns.get(outerNeighbor[0])[outerNeighbor[1]];
                if (!jumpTile.equals(chosenTile)    //filter this in stream?
                        && !directNeighbors.contains(jumpTile)
                        && !jumpNeighbors.contains(jumpTile)
                        && jumpTile.type == fieldType.EMPTY){
                    jumpNeighbors.add(jumpTile);
                }
            }
        }

        assert jumpNeighbors.stream()
                .allMatch(t -> t.type == fieldType.EMPTY): "Not all jumpNeighbors are empty";

        List<Move> moves = new ArrayList<>(directNeighbors.stream()
                .map(tile -> Move.of(chosenTile.type, true, chosenTile.position, tile.position))
                .toList());
        moves.addAll(jumpNeighbors.stream()
                .map(tile -> Move.of(chosenTile.type, false, chosenTile.position, tile.position))
                .toList());

        assert moves.stream()
                .allMatch(m -> this.columns.get(m.to[0])[m.to[1]].type == fieldType.EMPTY)
                : "Illegal Moves, that try to move to a non-empty field!";
        logger.debug("getPossible Moves is returning and has not produced illegal moves");
        return moves;
    }

    private List<Move> getAllPossibleMoves(fieldType type){
        List<Move> allMoves = new ArrayList<>();
        List<Move> waste = new ArrayList<>();
        boardTiles.get(type).forEach(t -> allMoves.addAll(getPossibleMoves(t)));
        logger.debug("getAllPossibleMoves has found {} moves", allMoves.size());
        for (Move m: allMoves){       //stream                  //useless jumps
            if (!m.isCopy){     //filter
                List<int[]> neighborsOfMovesTo = this.columns.get(m.to[0])[m.to[1]].neighbors;          //toList / stream
                if (neighborsOfMovesTo.stream()
                        .allMatch(mm -> this.columns.get(mm[0])[mm[1]].type == fieldType.EMPTY)){        //filter
                            waste.add(m);       //dunno
                }
            }
        }
        allMoves.removeAll(waste);
        waste.clear();
        for (Move m: allMoves.stream().filter(move -> !move.isCopy).toList()){    //all jumps
            List<Move> copies = allMoves.stream().filter(move -> move.isCopy).toList();    //all copies
            if (copies.stream().anyMatch(cpMove -> cpMove.to == m.to)){
                waste.add(m);
            }       //double copies
        }
        allMoves.removeAll(waste);
        logger.debug("getAllPossibleMoves returns {} moves after pruning bad ones"
                , allMoves.size());
        return allMoves;
    }

    private Move getRandomMove(fieldType color){
        Random rn = new Random();
        List<Move> allMoves = this.getAllPossibleMoves(color);
        assert allMoves.size() != 0: "";
        logger.debug("getRandomMove will successfully return a random move");
        return allMoves.get(rn.nextInt(0, allMoves.size()));
    }

    public Hexxagon makeMove(Move move){
        assert !this.isGameOver(): "The game has already finished";
        assert move.color == fieldType.BLUE || move.color == fieldType.RED: "Move does not have a color!";
        assert move.from[0] >= 0 && move.from[1]  >= 0: "Illegal indices in move.from";
        assert move.to[0] >= 0 && move.to[1]  >= 0: "Illegal indices in move.to";
        if (move.isCopy){
            assert move.from[0] - move.to[0] < 2 && move.from[1] - move.to[1] < 2: "Move went too far";
            assert move.from[0] - move.to[0] > -2 && move.from[1] - move.to[1] > -2: "Move went too far";
        } else {
            assert move.from[0] - move.to[0] < 3 && move.from[1] - move.to[1] < 3: "Move went too far";
            assert move.from[0] - move.to[0] > -3 && move.from[1] - move.to[1] > -3: "Move went too far";
        }

        Hexxagon newGame = Hexxagon.of(this.boardTiles, this.columns
                , this.difficulty, this.playerColor);
        Tile moveToTile = newGame.columns.get(move.to[0])[move.to[1]];
        Tile moveFromTile = newGame.columns.get(move.from[0])[move.from[1]];

        assert moveToTile.type == fieldType.EMPTY : "The tile the move is moving to is not empty";
        logger.debug("makeMove has passed all initial assertions");
        if (move.isCopy){
            moveToTile.setType(move.color);
            newGame.boardTiles.get(move.color).add(moveToTile);
        } else {    //is jump
            newGame.boardTiles.get(move.color).remove(moveFromTile);
            moveFromTile.setType(fieldType.EMPTY);
            newGame.boardTiles.get(move.color).add(moveToTile);
            moveToTile.setType(move.color);
        }
        logger.debug("Player pieces has successfully moved from {} to {}"
                , move.from, move.to);
        for (int[] neighbor: moveToTile.getNeighbors()){
            Tile neighborTile = newGame.columns.get(neighbor[0])[neighbor[1]];
            if (neighborTile.type == fieldType.RED || neighborTile.type == fieldType.BLUE){
                if (move.color == fieldType.BLUE){
                    newGame.boardTiles.get(fieldType.BLUE).add(neighborTile);
                    newGame.boardTiles.get(fieldType.RED).remove(neighborTile);
                } else {
                    newGame.boardTiles.get(fieldType.RED).add(neighborTile);
                    newGame.boardTiles.get(fieldType.BLUE).remove(neighborTile);
                }
                neighborTile.setType(move.color);
            }
        }
        logger.debug("makeMove has filled all neighboring pieces with color {}", move.color);
        if (!newGame.movesLeft(fieldType.RED)){ //fill all blue
            logger.debug("after makeMove, RED has no moves left," +
                    " so all remaining EMPTY tiles will be filled BLUE");
            newGame.columns.forEach(col -> Arrays.stream(col)
                    .filter(t -> t.type == fieldType.EMPTY)
                    .forEach(t -> {
                        t.setType(fieldType.BLUE);
                        newGame.boardTiles.get(fieldType.BLUE).add(t);
                    }));
        } else if (!newGame.movesLeft(fieldType.BLUE)){  //fill all red
            logger.debug("after makeMove, BLUE has no moves left," +
                    " so all remaining EMPTY tiles will be filled RED");
            newGame.columns.forEach(col -> Arrays.stream(col)
                    .filter(t -> t.type == fieldType.EMPTY)
                    .forEach(t -> {
                        t.setType(fieldType.RED);
                        newGame.boardTiles.get(fieldType.RED).add(t);
                    }));
        }
        return newGame;
    }

    public Move aiMove() throws InterruptedException, ExecutionException {
        // https://stackoverflow.com/questions/9664036/how-to-run-two-methods-simultaneously
        // https://stackoverflow.com/questions/25231149/can-i-use-callable-threads-without-executorservice
        // https://stackoverflow.com/questions/9418406/running-two-functions-with-threading
        // https://www.journaldev.com/1650/java-futuretask-example-program
        // https://www.baeldung.com/java-executor-service-tutorial
        assert !this.isGameOver(): "Game is over";
        List<Move> allMoves = getAllPossibleMoves(this.aiColor);
        long startTime = System.nanoTime();
        logger.debug("aiMove has started calculating");

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Callable<Map<Move, Float>>> testTasks = new ArrayList<>();
        List<Future<Map<Move, Float>>> results;

        if (this.difficulty > 2){
            allMoves.forEach(m -> testTasks.add(new minimaxTask(m
                    , Hexxagon.of(this.boardTiles, this.columns, this.difficulty, this.playerColor))));
        } else {
            allMoves.forEach(m -> testTasks.add(
                    new MonteCarloTask(
                            Hexxagon.of(this.boardTiles, this.columns, this.difficulty, this.playerColor),
                            m, this.difficulty*50)));
        }
        logger.debug("aiMove created the needed tasks for the executor service");
        logger.debug("aiMove difficulty setting is currently at {}", this.difficulty);
        results = executorService.invokeAll(testTasks);
        executorService.shutdown();
        assert results.stream().allMatch(Future::isDone):
                "aiMove crashed before finishing. Not all Futures are done";
        logger.debug("aiMoves executor service has successfully finished all tasks");
        Map<Move, Float> tmp = new HashMap<>();
        for (Future<Map<Move, Float>> res: results){
            tmp.putAll(res.get());
        }

        Move bestMove = Collections.max(tmp.entrySet(), Map.Entry.comparingByValue()).getKey();
        logger.debug("aiMove has determined: {} as the best move", bestMove);
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        logger.debug("aiMove needed {} seconds", (float)duration/1_000_000_000);
        return bestMove;
    }
    //logger.debug("");
    private static class minimaxTask implements Callable<Map<Move, Float>>{
        Move move;
        Hexxagon game;
        int depth;
        Map<Move, Float> resultMap = new HashMap<>();
        public minimaxTask(Move move, Hexxagon game) {
            this.move = move;
            this.game = game;
            logger.debug("new miniMax task has been created");
        }
        public Map<Move, Float> call(){
            assert game.difficulty > 2: "Illegal difficulty for Minimax";
            switch(game.difficulty){
                case 3 -> depth = 2;
                case 4,5 -> depth = 3;
            }

            float result = game.minimax(game.makeMove(move), -999, 999, depth, false);
            resultMap.put(this.move, result);
            logger.debug("miniMax task result: {} -> {}", move, result);
            return resultMap;
        }
    }

    private float minimax(Hexxagon game, float alpha, float beta, int depth, boolean isMax){
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
            List<Move> blueMoves = game.getAllPossibleMoves(game.aiColor);
            float value = -999;
            for (Move m: blueMoves){
                value = Math.max(value, minimax(game.makeMove(m), alpha, beta, depth - 1, false));
                if (value >= beta){
                    break;
                }
                alpha = Math.max(alpha, value);
            }
            return value;
        } else {
            List<Move> redMoves = game.getAllPossibleMoves(game.playerColor);
            float value = 999;
            for (Move m: redMoves){
                value = Math.min(value, minimax(game.makeMove(m), alpha, beta, depth - 1, true));
                if (value <= alpha){
                    break;
                }
                beta = Math.min(beta, value);
            }
            return value;
        }
    }

    private float monteCarlo(Hexxagon game, int amount){
        int winCounter = 0;
        for (int i = 0; i < amount; i++){
            Hexxagon newGame = Hexxagon.of(game.boardTiles, game.columns, game.difficulty, game.playerColor);
            boolean isAI = false;
            for (int j = 0; j < 5; j++){
                if (newGame.isGameOver()) break;
                Move move;
                move = newGame.getRandomMove(isAI ? game.aiColor : game.playerColor);
                newGame = newGame.makeMove(move);
                isAI = !isAI;
            }
            if (newGame.boardTiles.get(newGame.aiColor).size() > newGame.boardTiles.get(newGame.playerColor).size()){
                winCounter += 1;
            }
        }
        return winCounter / (float)amount;
    }

    private static class MonteCarloTask implements Callable<Map<Move, Float>> {
        Hexxagon game;
        Move move;
        int amount;
        Map<Move, Float> resultMap = new HashMap<>();

        public MonteCarloTask(Hexxagon game, Move move, int amount) {
            this.game = game;
            this.move = move;
            this.amount = amount;
            logger.debug("new monteCarlo task has been created");
        }

        @Override
        public Map<Move, Float> call() {
            float result = game.monteCarlo(game.makeMove(move), amount);
            resultMap.put(move, result);
            logger.debug("monteCarlo task result: {} -> {}", move, result);
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