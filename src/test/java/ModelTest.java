import Model.Model;
import org.junit.Assert;
import org.junit.Test;

public class ModelTest {

    @Test
    public void putTest() {
        Model test = new Model();
        boolean actual = test.put(-1, 0, 1, true);
        boolean expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(0, -10, 1, true);
        expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(0, 0, 10, true);
        expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(0, 0, 10, false);
        expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(9, 0, 4, true);
        expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(0, 0, 4, true);
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(0, 0, 4, false);
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(5, 5, 3, true);
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.put(5, 5, 3, false);
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(5, 5, 1, true);
        actual = test.put(5, 5, 3, false);
        expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(5, 5, 2, false);
        actual = test.put(5, 6, 1, true);
        expected = false;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getTest() {
        Model test = new Model();
        char actual = test.get(-1, -10);
        char expected = '?';
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.get(0, 0);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(0, 0, 1, true);
        actual = test.get(0, 0);
        expected = '7';
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(9, 9, 1, true);
        test.put(0, 0, 1, true);
        actual = test.get(0, 0);
        expected = '8';
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void putOpponentFieldTest() {
        char exampleChar = 'X';
        Model test = new Model();
        boolean actual = test.putOpponentField(-1, 0, exampleChar);
        boolean expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.putOpponentField(0, -10, exampleChar);
        expected = false;
        Assert.assertEquals(expected, actual);


        test = new Model();
        actual = test.putOpponentField(0, 0, exampleChar);
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.putOpponentField(0, 0, exampleChar);
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.putOpponentField(5, 5, exampleChar);
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.putOpponentField(5, 5, exampleChar);
        expected = true;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getOpponentFieldTest() {
        Model test = new Model();
        char actual = test.getOpponentField(-1, -10);
        char expected = '?';
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.getOpponentField(0, 0);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.putOpponentField(0, 0, 'X');
        actual = test.getOpponentField(0, 0);
        expected = 'X';
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.putOpponentField(9, 9, 'X');
        test.putOpponentField(0, 0, 'X');
        actual = test.getOpponentField(0, 0);
        expected = 'X';
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void removeTest() {
        Model test = new Model();
        test.put(1, 1, 1, true);
        test.remove(1, 1);
        char actual = test.get(1, 1);
        char expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(1, 1, 4, true);
        test.remove(2, 1);
        actual = test.get(1, 1);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(1, 1, 4, true);
        test.remove(3, 1);
        actual = test.get(1, 1);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(1, 1, 4, true);
        test.remove(4, 1);
        actual = test.get(1, 1);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(1, 1, 4, true);
        test.remove(2, 1);
        actual = test.get(2, 1);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(1, 1, 4, true);
        test.remove(2, 1);
        actual = test.get(3, 1);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(1, 1, 4, true);
        test.remove(2, 1);
        actual = test.get(4, 1);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.remove(1, 1);
        actual = test.get(1, 1);
        expected = test.getEmpty();
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void shootTest() {
        Model test = new Model();
        test.put(0, 0, 4, true);
        boolean actual = test.shoot(0, 0);
        boolean expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        actual = test.shoot(-10, -10);
        expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(0, 0, 4, true);
        char actualChar = test.get(0, 0);
        char expectedChar = '1';
        Assert.assertEquals(expectedChar, actualChar);
        test.shoot(0, 0);
        actualChar = test.get(0, 0);
        expectedChar = 'A';
        Assert.assertEquals(expectedChar, actualChar);

    }

    @Test
    public void countDeadShips() {
        Model test = new Model();
        test.put(0, 0, 4, true);
        int actual = test.countDeadShips();
        int expected = 9;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void countAliveShips() {
        Model test = new Model();
        test.put(0, 0, 4, true);
        int actual = test.countAliveShips();
        int expected = 1;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void countEnemyDeadShipsTest() {
        Model test = new Model();
        test.putOpponentField(0, 0, '1');
        int actual = test.countEnemyDeadShips();
        int expected = 9;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void countEnemyAliveShipsTest() {
        Model test = new Model();
        test.putOpponentField(0, 0, '1');
        int actual = test.countAliveEnemyShips();
        int expected = 1;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void isFieldReadyCheckTest() {
        Model test = new Model();
        boolean actual = test.isFieldReady();
        boolean expected = false;
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(0, 0, 4, true);
        test.put(0, 1, 3, true);
        test.put(0, 2, 3, true);
        test.put(0, 3, 2, true);
        test.put(0, 4, 2, true);
        test.put(0, 5, 2, true);
        test.put(0, 6, 1, true);
        test.put(0, 7, 1, true);
        test.put(0, 8, 1, true);
        test.put(0, 9, 1, true);

        actual = test.isFieldReady();
        expected = true;
        Assert.assertEquals(expected, actual);

        test = new Model();
        test.put(0, 0, 4, true);
        test.put(0, 1, 3, true);
        test.put(0, 2, 3, true);
        test.put(0, 3, 2, true);
        test.put(0, 4, 2, true);
        test.put(0, 5, 2, true);
        test.put(0, 6, 1, true);
        test.put(0, 7, 1, true);
        test.put(0, 9, 1, true);

        actual = test.isFieldReady();
        expected = false;
        Assert.assertEquals(expected, actual);
    }

}
