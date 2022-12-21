package Model;

import ViewController.Info;

import java.util.Arrays;

public class Model {
    private char[][] field;
    private char[][] opponentField;
    private int[] shipCounters = new int[4];
    private int fieldSize = 10;
    private int[] shipMaxes = {4, 3, 2, 1};
    private char empty = Info.blocks[Info.blocks.length - 1];
    private char destroyed = 'X';
    private char missed = '*';

    public Model() {
        field = new char[fieldSize][fieldSize];
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                field[i][j] = empty;
            }
        }
        opponentField = new char[fieldSize][fieldSize];
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                opponentField[i][j] = empty;
            }
        }
    }

    public Model(int fieldSize) {
        if (fieldSize > 0) {
            this.fieldSize = fieldSize;
            field = new char[fieldSize][fieldSize];
            for (int i = 0; i < fieldSize; i++) {
                for (int j = 0; j < fieldSize; j++) {
                    field[i][j] = empty;
                }
            }
            opponentField = new char[fieldSize][fieldSize];
            for (int i = 0; i < fieldSize; i++) {
                for (int j = 0; j < fieldSize; j++) {
                    opponentField[i][j] = empty;
                }
            }
        }
    }

    public char get(int x, int y) {
        if (!((isCordAppropriate(x)) && (isCordAppropriate(y)))) return '?';
        return field[y][x];
    }

    public char getOpponentField(int x, int y) {
        if (!((isCordAppropriate(x)) && (isCordAppropriate(y)))) return '?';
        return opponentField[y][x];
    }

    public boolean putOpponentField(int x, int y, char c) {
        if (!((isCordAppropriate(x)) && (isCordAppropriate(y)))) return false;
        opponentField[y][x] = c;
        return true;
    }

    public boolean put(int x, int y, int shipSize, boolean axis) { // axis == true -> x (horizontal) ; axis == false -> y (vertical);
        if (!((isCordAppropriate(x)) && (isCordAppropriate(y)))) return false;
        if ((shipSize == 1) || (shipSize == 2) || (shipSize == 3) || (shipSize == 4)) {
            char shipChar = empty;

            if ((shipSize == 4) && (shipCounters[shipSize - 1] < shipMaxes[shipSize - 1])) shipChar = '1';

            if ((shipSize == 3) && (shipCounters[shipSize - 1] < shipMaxes[shipSize - 1])) {
                switch (shipCounters[shipSize - 1]) {
                    case 0:
                        shipChar = '2';
                        break;
                    case 1:
                        shipChar = '3';
                        break;
                }
            }

            if ((shipSize == 2) && (shipCounters[shipSize - 1] < shipMaxes[shipSize - 1])) {
                switch (shipCounters[shipSize - 1]) {
                    case 0:
                        shipChar = '4';
                        break;
                    case 1:
                        shipChar = '5';
                        break;
                    case 2:
                        shipChar = '6';
                        break;

                }
            }

            if ((shipSize == 1) && (shipCounters[shipSize - 1] < shipMaxes[shipSize - 1])) {
                switch (shipCounters[shipSize - 1]) {
                    case 0:
                        shipChar = '7';
                        break;
                    case 1:
                        shipChar = '8';
                        break;
                    case 2:
                        shipChar = '9';
                        break;
                    case 3:
                        shipChar = '0';
                        break;
                }
            }

            if (axis) {

                if (isThereAPlaceForShip(x, y, shipSize, axis)) {
                    shipCounters[shipSize - 1]++;
                    for (int i = 0; i < shipSize; i++) {
                        field[y][x + i] = shipChar;
                    }
                    return true;
                }

            } else {

                if (isThereAPlaceForShip(x, y, shipSize, axis)) {
                    shipCounters[shipSize - 1]++;
                    for (int i = 0; i < shipSize; i++) {
                        field[y + i][x] = shipChar;
                    }
                    return true;
                }

            }
        }
        return false;
    }

    public void remove(int x, int y) {
        if ((isCordAppropriate(x)) && (isCordAppropriate(y))) {
            char tmp = field[y][x];
            switch (tmp) {
                case '1':
                    shipCounters[3]--;
                    break;
                case '2', '3':
                    shipCounters[2]--;
                    break;
                case '4', '5', '6':
                    shipCounters[1]--;
                    break;
                case '7', '8', '9', '0':
                    shipCounters[0]--;
                    break;
            }
            for (int i = 0; i < fieldSize; i++) {
                for (int j = 0; j < fieldSize; j++) {
                    if (field[i][j] == tmp) field[i][j] = empty;
                }
            }
        }
    }

    public boolean shoot(int x, int y) {
        if ((isCordAppropriate(x)) && (isCordAppropriate(y))) {
            if (field[y][x] == empty) {
                field[y][x] = missed;
            } else {
                switch (field[y][x]) {
                    case '1':
                        field[y][x] = 'A';
                        break;
                    case '2':
                        field[y][x] = 'B';
                        break;
                    case '3':
                        field[y][x] = 'C';
                        break;
                    case '4':
                        field[y][x] = 'D';
                        break;
                    case '5':
                        field[y][x] = 'E';
                        break;
                    case '6':
                        field[y][x] = 'F';
                        break;
                    case '7':
                        field[y][x] = 'G';
                        break;
                    case '8':
                        field[y][x] = 'H';
                        break;
                    case '9':
                        field[y][x] = 'I';
                        break;
                    case '0':
                        field[y][x] = 'J';
                        break;
                }
            }
            findDestroyedShips();
            return true;
        }
        return false;
    }

    public void findDestroyedShips() {
        int[] counters = new int[10];
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                switch (field[i][j]) {
                    case 'A':
                        counters[0]++;
                        break;
                    case 'B':
                        counters[1]++;
                        break;
                    case 'C':
                        counters[2]++;
                        break;
                    case 'D':
                        counters[3]++;
                        break;
                    case 'E':
                        counters[4]++;
                        break;
                    case 'F':
                        counters[5]++;
                        break;
                    case 'G':
                        counters[6]++;
                        break;
                    case 'H':
                        counters[7]++;
                        break;
                    case 'I':
                        counters[8]++;
                        break;
                    case 'J':
                        counters[9]++;
                        break;
                }
            }
        }

        for (int i = 0; i < counters.length; i++) {
            switch (i) {
                case 0:
                    if (counters[i] == 4) markDead('A');
                    break;
                case 1:
                    if (counters[i] == 3) markDead('B');
                    break;
                case 2:
                    if (counters[i] == 3) markDead('C');
                    break;
                case 3:
                    if (counters[i] == 2) markDead('D');
                    break;
                case 4:
                    if (counters[i] == 2) markDead('E');
                    break;
                case 5:
                    if (counters[i] == 2) markDead('F');
                    break;
                case 6:
                    if (counters[i] == 1) markDead('G');
                    break;
                case 7:
                    if (counters[i] == 1) markDead('H');
                    break;
                case 8:
                    if (counters[i] == 1) markDead('I');
                    break;
                case 9:
                    if (counters[i] == 1) markDead('J');
                    break;
            }
        }
    }

    private void markDead(char index) {
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if (field[i][j] == index) field[i][j] = destroyed;
            }
        }
    }

    public void clear() {
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                field[i][j] = empty;
            }
        }
    }

    public boolean isSpotFree(int x, int y) {
        if ((isCordAppropriate(x)) && (isCordAppropriate(y))) {
            if (field[y][x] == empty) return true;
        }
        return false;

    }

    public boolean isThereAPlaceForShip(int x, int y, int shipSize, boolean axis) {
        if (!((isCordAppropriate(x)) && (isCordAppropriate(y)))) return false;
        boolean flag = false;

        if (axis) {

            for (int i = 0; i < shipSize; i++) {
                if (!isSpotFree(x + i, y)) flag = true;
            }
        } else {

            for (int i = 0; i < shipSize; i++) {
                if (!isSpotFree(x, y + i)) flag = true;
            }
        }
        return !flag;

    }

    public boolean amILost() {
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if ((field[i][j] != empty) && (field[i][j] != destroyed) && (field[i][j] != missed)) return false;
            }
        }
        return true;
    }

    public boolean isCordAppropriate(int x) {
        if ((x >= 0) && (x < fieldSize)) return true;
        return false;
    }

    public boolean isFieldReady() {
        int counters[] = new int[11];
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                switch (field[i][j]) {
                    case '1':
                        counters[0]++;
                        break;
                    case '2':
                        counters[1]++;
                        break;
                    case '3':
                        counters[2]++;
                        break;
                    case '4':
                        counters[3]++;
                        break;
                    case '5':
                        counters[4]++;
                        break;
                    case '6':
                        counters[5]++;
                        break;
                    case '7':
                        counters[6]++;
                        break;
                    case '8':
                        counters[7]++;
                        break;
                    case '9':
                        counters[8]++;
                        break;
                    case '0':
                        counters[9]++;
                        break;
                }
            }
        }

        for (int i = 0; i < counters.length; i++) {
            switch (i) {
                case 0:
                    if (counters[i] != 4) return false;
                    break;
                case 1, 2:
                    if (counters[i] != 3) return false;
                    break;
                case 3, 4, 5:
                    if (counters[i] != 2) return false;
                    break;
                case 6, 7, 8, 9:
                    if (counters[i] != 1) return false;
                    break;
            }
        }
        return true;
    }

    public char getEmpty() {
        return empty;
    }

    public char getDestroyed() {
        return destroyed;
    }

    public char getMissed() {
        return missed;
    }

    public char[][] getOpponentField() {
        return opponentField;
    }

    public int countDeadShips() {
        return (10 - countAliveShips());
    }

    public int countEnemyDeadShips() {
        return (10 - countAliveEnemyShips());
    }

    public int countAliveShips() {
        int counter = 0;
        boolean[] checks = new boolean[10];
        Arrays.fill(checks, true);
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if ((field[i][j] == '1') && (checks[1])) {
                    checks[1] = false;
                    counter++;
                }
                if ((field[i][j] == '2') && (checks[2])) {
                    checks[2] = false;
                    counter++;
                }

                if ((field[i][j] == '3') && (checks[3])) {
                    checks[3] = false;
                    counter++;
                }

                if ((field[i][j] == '4') && (checks[4])) {
                    checks[1] = false;
                    counter++;
                }

                if ((field[i][j] == '5') && (checks[5])) {
                    checks[5] = false;
                    counter++;
                }

                if ((field[i][j] == '6') && (checks[6])) {
                    checks[6] = false;
                    counter++;
                }

                if ((field[i][j] == '7') && (checks[7])) {
                    checks[7] = false;
                    counter++;
                }

                if ((field[i][j] == '8') && (checks[8])) {
                    checks[8] = false;
                    counter++;
                }

                if ((field[i][j] == '9') && (checks[9])) {
                    checks[9] = false;
                    counter++;
                }
                if ((field[i][j] == '0') && (checks[0])) {
                    checks[0] = false;
                    counter++;
                }
            }
        }
        return counter;
    }

    public int countAliveEnemyShips() {
        int counter = 0;
        boolean[] checks = new boolean[10];
        Arrays.fill(checks, true);
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if ((opponentField[i][j] == '1') && (checks[1])) {
                    checks[1] = false;
                    counter++;
                }
                if ((opponentField[i][j] == '2') && (checks[2])) {
                    checks[2] = false;
                    counter++;
                }

                if ((opponentField[i][j] == '3') && (checks[3])) {
                    checks[3] = false;
                    counter++;
                }

                if ((opponentField[i][j] == '4') && (checks[4])) {
                    checks[1] = false;
                    counter++;
                }

                if ((opponentField[i][j] == '5') && (checks[5])) {
                    checks[5] = false;
                    counter++;
                }

                if ((opponentField[i][j] == '6') && (checks[6])) {
                    checks[6] = false;
                    counter++;
                }

                if ((opponentField[i][j] == '7') && (checks[7])) {
                    checks[7] = false;
                    counter++;
                }

                if ((opponentField[i][j] == '8') && (checks[8])) {
                    checks[8] = false;
                    counter++;
                }

                if ((opponentField[i][j] == '9') && (checks[9])) {
                    checks[9] = false;
                    counter++;
                }
                if ((opponentField[i][j] == '0') && (checks[0])) {
                    checks[0] = false;
                    counter++;
                }
            }
        }
        return counter;
    }


    public void setOpponentField(char[][] opponentField) {
        this.opponentField = opponentField;
    }

    public String toStringOppponentsField() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < opponentField.length; i++) {
            for (int j = 0; j < opponentField.length; j++) {
                stringBuilder.append(opponentField[i][j]);
                //stringBuilder.append(' ');
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                stringBuilder.append(field[i][j]);
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    public int getFieldSize() {
        return fieldSize;
    }
}
