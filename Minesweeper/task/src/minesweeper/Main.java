package minesweeper;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        UserOutput.printMinesAMountsMessage();
        final int mines = UserInput.readMinesAmount();
        Board board = new Board(mines);
        board.print();
        GameState state = GameState.FOW;

        while (state != GameState.WINS && state != GameState.LOSE) {
            UserOutput.printSetMarkMessage();
            Transition transition = UserInput.readTransition();

            state = state.next(transition, board);
            board.print();
        }

        if (state == GameState.WINS) {
            UserOutput.printWinMessage();
        } else {
            UserOutput.printLoseMessage();
        }

    }
}

class Coordinates {
    private int x;
    private int y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

enum Command {
    FREE {
        @Override
        public GameState handle(Coordinates coordinates, Board board) {
            GameState state = GameState.EXPLORE;
            //TODO: check if there is a bomb return loses
            if (board.isBomb(coordinates)) {
                state = GameState.LOSE;
            } else {
                board.explore(coordinates);
                if (board.checkVictory()) {
                    state = GameState.WINS;
                }
            }

            return state;
        }
    }, MINE {
        @Override
        public GameState handle(Coordinates coordinates, Board board) {
            GameState state;
            if (board.mark(coordinates)) {
                state = GameState.MARK;
            } else {
                state = GameState.FOW;
            }
            if (board.checkVictory()) {
                state = GameState.WINS;
            }

            return state;
        }
    };

    public abstract GameState handle(Coordinates coordinates, Board board);
}

class Transition {
    private Coordinates coordinates;
    private Command command;

    public Transition(Coordinates coordinates, Command command) {
        this.coordinates = coordinates;
        this.command = command;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Command getCommand() {
        return command;
    }

    public GameState handle(Board board) {
        return getCommand().handle(getCoordinates(), board);
    }
}

enum GameState {
    FOW, EXPLORE, MARK, LOSE, WINS;

    public GameState next(Transition transition, Board board) {
        return transition.handle(board);
    }
}

enum CellValue {
    BOMB('x'), EMPTY('/'), MINE_MARK('*'), FOW('.'), ONE('1'), TWO('2'), THREE('3'), FOUR('4'), FIVE('5'), SIX('6'), SEVEN('7'),
    EIGHT('8') {
        @Override
        public CellValue next() {
            throw new NoSuchElementException();
        }
    };

    final private char value;

    CellValue(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    public CellValue next() {
        return CellValue.values()[ordinal() + 1];
    }
}

class Board {
    private final static int BOMB_BOUND = 6;
    private final static int SIZE = 9;

    final private CellValue[][] field = new CellValue[SIZE][SIZE];;
    final private CellValue[][] userField = new CellValue[SIZE][SIZE];;
    private final int minesAmount;

    public Board(int mines) {
        this.minesAmount = mines;
        flood();
        fillGuide();
        initializeUserField();
    }

    public int getRows() {
        return field.length;
    }

    public int getCols() {
        return field[0].length;
    }

    public int getMinesAmount() {
        return minesAmount;
    }

    private void initialize() {
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                field[i][j] = CellValue.EMPTY;
            }
        }
    }

    private void initializeUserField() {

        for (int i = 0; i < getRows(); i++) {
            this.userField[i] = Arrays.copyOf(this.field[i], SIZE);
            for (int j = 0; j < getCols(); j++) {
                    userField[i][j] = CellValue.FOW;
            }
        }
    }

    private void flood() {

        int bound = BOMB_BOUND;
        int mines = getMinesAmount();

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                Random random = new Random();
                int x = random.nextInt(10);
                if (x > bound && mines != 0) {
                    field[i][j] = CellValue.BOMB;
                   // fillGuide(i, j);
                    bound--;
                    mines--;
                } else {
                    field[i][j] = CellValue.EMPTY;
                }
            }
        }
    }

    private void fillGuide() {
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                if (field[i][j] == CellValue.BOMB) {
                    fillGuide(i, j);
                }
            }
        }
    }

    private void fillGuide(int row, int col) {
        int lowestRow   = row == 0 ? 0 : row - 1;
        int upperRow    = row == (getRows() - 1) ? getRows() : row + 2;//The row is not the length but the index
        int lowestCol   = col == 0 ? 0 : col - 1;
        int upperCol    = col == (getCols() - 1) ? getCols() : col + 2;

        for (int i = lowestRow; i < upperRow; i++) {
            for (int j = lowestCol; j < upperCol; j++) {
                if(field[i][j] != CellValue.BOMB) {
                    if (field[i][j] == CellValue.EMPTY) {
                        field[i][j] = CellValue.ONE;
                    } else {
                        field[i][j] = field[i][j].next();
                    }
                }
            }
        }

    }


    /**
     * Set the proper mark in the user field
     * @param coordinates coordinates to mark
     * @return actual state of the board for that coordinate
     */
    public boolean mark(Coordinates coordinates) {
        CellValue marked = userField[coordinates.getY()][coordinates.getX()];
        boolean result = false;

        switch (marked) {
            case FOW:
                userField[coordinates.getY()][coordinates.getX()] = CellValue.MINE_MARK;
                result = true;
                break;
            case MINE_MARK:
                userField[coordinates.getY()][coordinates.getX()] = CellValue.FOW;
                break;
        }
        return result;
    }

    public boolean checkVictory() {
        boolean isVictory = true;

        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                if (field[i][j] == CellValue.BOMB) {
                    isVictory &= ((userField[i][j] == CellValue.MINE_MARK || userField[i][j] == CellValue.FOW));
                } else {
                    isVictory &= (userField[i][j] == field[i][j]);
                }
            }
        }

        return isVictory;
    }

    public void print() {
        UserOutput.printUserField(userField);
    }

    public boolean isBomb(Coordinates coordinates) {
        return this.field[coordinates.getY()][coordinates.getX()] == CellValue.BOMB;
    }

    public void explore(Coordinates coordinates) {

        Deque<Coordinates> stack = new ArrayDeque<>();
        Deque<Coordinates> processedStack = new ArrayDeque<>();

        stack.offerLast(coordinates);
        processedStack.offerLast(coordinates);

        while(!stack.isEmpty()) {
            Coordinates current = stack.pollLast();

            if (isExplorable(current)) {
                cleanCell(current);
                //Explore Up
                Coordinates nextCoordinate = new Coordinates(current.getX(), current.getY() - 1);
                if (!processedStack.contains(nextCoordinate) && isExplorable(current)) {
                    stack.offerLast(nextCoordinate);
                    processedStack.offerLast(nextCoordinate);
                }

                //Explore Down
                nextCoordinate = new Coordinates(current.getX(), current.getY() + 1);
                if (!processedStack.contains(nextCoordinate) && isExplorable(current)) {
                    stack.offerLast(nextCoordinate);
                    processedStack.offerLast(nextCoordinate);
                }

                //Explore Left
                nextCoordinate = new Coordinates(current.getX() - 1, current.getY());
                if (!processedStack.contains(nextCoordinate) && isExplorable(current)) {
                    stack.offerLast(nextCoordinate);
                    processedStack.offerLast(nextCoordinate);
                }

                //Explore Right
                nextCoordinate = new Coordinates(current.getX() + 1, current.getY());
                if (!processedStack.contains(nextCoordinate) && isExplorable(current)) {
                    stack.offerLast(nextCoordinate);
                    processedStack.offerLast(nextCoordinate);
                }
            }
        }
    }

    private boolean isExplorable(Coordinates coordinates) {
        boolean result = false;

        if(!isOutOfBoundary(coordinates)) {

            CellValue cellValue = field[coordinates.getY()][coordinates.getX()];
            result = (cellValue != CellValue.BOMB);
        }

        return result;
    }

    private boolean isOutOfBoundary(Coordinates coordinates) {
        return 0 > coordinates.getY() || coordinates.getY() >= getRows()
                    || 0 > coordinates.getX() || coordinates.getX() >= getCols();
    }

    private void cleanCell(Coordinates coordinates) {
        userField[coordinates.getY()][coordinates.getX()] = field[coordinates.getY()][coordinates.getX()];
    }
}

class UserInput {

    public static int readMinesAmount() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    public static Transition readTransition() {
        Scanner scanner = new Scanner(System.in);

        Coordinates coordinates = readCoordinates(scanner);
        Command command = readCommand(scanner);

        return new Transition(coordinates, command);
    }

    public static Command readCommand(Scanner scanner) {
        String command = scanner.nextLine();

        return Command.valueOf(command.toUpperCase().trim());
    }

    public static Coordinates readCoordinates(Scanner scanner) {

        int x = scanner.nextInt();
        int y = scanner.nextInt();

        Coordinates coordinates = new Coordinates(x - 1, y -1);
        return coordinates;
    }
}

class UserOutput {

    private final static String MINES_AMOUNT_MESSAGE = "How many mines do you want on the field?";
    private final static String INPUT_COORDINATES_MESSAGE = "Set/unset mines marks or claim a cell as free:";
    private final static String NUMBER_MESSAGE = "There is a number here!";
    private final static String WINS_MESSAGE = "Congratulations! You found all the mines!";
    private final static String LOSES_MESSAGE = "You stepped on a mine and failed!";

    public static void printMinesAMountsMessage() {
        System.out.print(MINES_AMOUNT_MESSAGE);
    }
    public static void printUserField(CellValue[][] userField) {
        System.out.println("-|123456789|");
        System.out.println("-|---------|");
        for (int i = 0; i < userField.length; i++) {
            System.out.printf("%d|", i + 1);
            for (int j = 0; j < userField[i].length; j++) {
                System.out.printf("%s", userField[i][j].getValue());
            }
            System.out.println("|");
        }
        System.out.println("-|---------|");
        System.out.println();
    }

    public static void printNumberMessage() {
        System.out.println(NUMBER_MESSAGE);
    }
    public static void printSetMarkMessage() {
        System.out.print(INPUT_COORDINATES_MESSAGE);
    }
    public static void printWinMessage() {
        System.out.println(WINS_MESSAGE);
    }

    public static void printLoseMessage() {
        System.out.println(LOSES_MESSAGE);
    }
}
