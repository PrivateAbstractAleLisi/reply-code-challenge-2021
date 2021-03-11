package com.polimi.ingsw;

import javax.lang.model.element.ModuleElement;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class Main {

    //case parameters
    public static int WIDTH, HEIGTH, N_BUILDINGS, M_AVA_ANTENNAS;
    public static int REWARD = 0;
    public static ArrayList<Antenna> piazzabili = new ArrayList<>();
    public int placedAntennas = 0; //++ every time we place an antenna
    public static ArrayList<SkyScraper> buildings = new ArrayList<>();

    //====================
//=========MODEL======
//====================
    static class Cell {

        public Cell() {
            this.skyscr = null;
            this.antenna = null;
        }

        boolean isFree() {
            return (antenna == null);
        }

        SkyScraper skyscr;
        Antenna antenna;
    }


    static class SkyScraper {
        //• Latency weight: how important is the latency for the building.
        public int latencyWeight;

        // • Connection speed weight: how important is the connection speed for
        //the building.
        public int speedWeight;

        //???????TODO DUPLICATO NECESSARIO??? //
        public final int X_COORD, Y_COORD;

        public SkyScraper(int latencyWeight, int speedWeight, int x_COORD, int y_COORD) {
            this.latencyWeight = latencyWeight; //BL
            this.speedWeight = speedWeight; //BC
            X_COORD = x_COORD;
            Y_COORD = y_COORD;
            coveredBy = null;
        }

        public Antenna coveredBy;
    }

    static class Antenna implements Comparable<Antenna> {

        //Range: the distance that the antenna signal can reach.
        public int range; //AR
        //• Connection speed: the connection speed provided to the building connected to it.
        public int connectionSpeed; //AC

        public int X_COORD, Y_COORD;

        public Antenna(int range, int connectionSpeed) {
            this.range = range;
            this.connectionSpeed = connectionSpeed;
        }

        public void placeAntenna(int x, int y) {
            X_COORD = x;
            Y_COORD = y;
            placedAntennas++;
        }

        //a negative integer, zero, or a positive integer as this object is less than,
        // equal to, or greater than the specified object.
        @Override
        public int compareTo(Antenna o) {
            if (this.connectionSpeed > o.connectionSpeed)
                return -1;
            else if (this.connectionSpeed == o.connectionSpeed)
                return 0;
            else
                return +1;

        }
    }

    ////// MAP /////
    public static Cell[][] grid;


    ///// SCORING FUNCTIONS //////

    //s(a,b)
    public static int scoreAntennaBuilding(Antenna a, SkyScraper b) {
        return (b.speedWeight * a.connectionSpeed - b.latencyWeight * distManhattan(a, b));
    }

    //c(b) antenna that maximizes s(a,b) for b
    //just assign b.coveredBy if new antenna is greater than prev

    //s(b) score of building
    public static int scoreBuilding(SkyScraper b) {
        return scoreAntennaBuilding(b.coveredBy, b);
    }

    public static int getReward() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if(grid[row][col].skyscr.coveredBy == null)
                    return 0;
            }
        }
        return REWARD;
    }

    public static int totalScore() {

    }

    //r(b)
    public static List<Antenna> reachableFromB(SkyScraper b) {

        List<Antenna> result = new LinkedList<>();
        for (Cell[] cells : grid) {
            for (Cell cell : cells) {
                //if it has a placed antenna
                Antenna a = cell.antenna;
                if (a != null) {
                    if (distManhattan(a, b) <= a.range) result.add(a);
                }
            }
        }

        return result;
    }

    public static int distManhattan(Antenna a, SkyScraper b) {
        //|x1 - x2| + |y1 - y2|
        int distance = Math.abs(a.X_COORD - b.X_COORD) + Math.abs(a.Y_COORD - b.Y_COORD);
        return distance;
    }

    public static void sortAntennas(ArrayList<Antenna> list) {
        list.sort((o1, o2) -> o1.compareTo(o2));
    }


//=============================================================//
// ===================== INPUT PARSING ========================//
// ============================================================//


    private static class InputParsing {
        public InputParsing(String filename) {
            try {
                Scanner stream = new Scanner(new File(filename));

                //GET H and W
                if (stream.hasNext())
                    WIDTH = stream.nextInt();
                if (stream.hasNext())
                    HEIGTH = stream.nextInt();
                Integer i = 0;
                grid = new Cell[WIDTH][HEIGTH];
                for (int j = 0; j < WIDTH; j++) {
                    for (int k = 0; k < HEIGTH; k++) {
                        grid[j][k] = new Cell();
                    }
                }


                N_BUILDINGS = stream.nextInt();
                System.out.println(N_BUILDINGS);
                M_AVA_ANTENNAS = stream.nextInt();
                System.out.println(M_AVA_ANTENNAS);
                REWARD = stream.nextInt();
                System.out.println(REWARD);

                //CREATE BUILDINGS
                for (int j = 0; j < N_BUILDINGS; j++) {
                    Integer x, y, l, c;
                    x = stream.nextInt();
                    y = stream.nextInt();
                    l = stream.nextInt();
                    c = stream.nextInt();
                    grid[x][y].skyscr = new SkyScraper(l, c, x, y);
                }
                for (int j = 0; j < M_AVA_ANTENNAS; j++) {
                    Integer r, c;
                    r = stream.nextInt();
                    c = stream.nextInt();
                    piazzabili.add(new Antenna(r, c));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

//=============================================================//
// ===================OUTPUT===================================//
// ============================================================//

    //writeFile("./test.txt")
    private static Charset UTF8 = Charset.forName("UTF-8");

    public static void writeFile(String path) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), UTF8);
        writer.write(placedAntennas);
        writer.write("\n");

    }



    static int computeHeuristic(Position pos, Antenna a) {
        return 0;
    }

    static class Position {
        public int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static Map<Position, Integer> heuristicValues;
    static List<Position> sortedPositions = new SortedList<>();

    public static void main(String[] args) {

        InputParsing parse = new InputParsing("data_scenarios_a_example.in");
        System.out.println(grid[0][0]);
        System.out.println(piazzabili);
        sortAntennas(piazzabili);

        for (Antenna a : piazzabili) {


            for (int j = 0; j < WIDTH; j++) {
                for (int k = 0; k < HEIGTH; k++) {


                    if (grid[j][k].isFree()) {
                        Position currentPosition = new Position(j, k);
                        int score = computeHeuristic(currentPosition, a);
                        heuristicValues.put(currentPosition, score);
                    }


                }
            }




        }

    }
}




