package com.polimi.ingsw;

import javax.lang.model.element.ModuleElement;
import javax.swing.text.Position;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class Main {

    //case parameters
    public static int WIDTH, HEIGTH, N_BUILDINGS, M_AVA_ANTENNAS;
    public static int N_REMAINING;
    public static int REWARD = 0;
    public static ArrayList<Antenna> piazzabili = new ArrayList<>();
    public static int placedAntennas = 0; //++ every time we place an antenna
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
        public int buildingScore;
    }

    static class Antenna implements Comparable<Antenna> {

        //Range: the distance that the antenna signal can reach.
        public int range; //AR
        //• Connection speed: the connection speed provided to the building connected to it.
        public int connectionSpeed; //AC

        public int X_COORD, Y_COORD;

        public int id;

        public Antenna(int range, int connectionSpeed, int id) {
            this.range = range;
            this.connectionSpeed = connectionSpeed;
            this.id = id;
        }

        public void placeAntenna(int x, int y) {
            X_COORD = x;
            Y_COORD = y;
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

    //
    public static void updateAndPlaceAntenna(Antenna a, Position pos) {
        a.placeAntenna(pos.x, pos.y);
        // placedAntennas++;
        grid[pos.x][pos.y].antenna = a;
        assert (grid[pos.x][pos.y].antenna != null);

    }

    ////// MAP /////
    public static Cell[][] grid;


    ///// SCORING FUNCTIONS //////

    //s(a,b), check reachability
    public static int scoreAntennaBuilding(Antenna a, SkyScraper b) {
        return (b.speedWeight * a.connectionSpeed - b.latencyWeight * distManhattan(a, b));
    }

    //c(b) antenna that maximizes s(a,b) for b
    //just assign b.coveredBy if new antenna is greater than prev

    //s(b) score of building
    public static int scoreBuilding(SkyScraper b) {
        if (b.coveredBy == null)
            return 0;
        return scoreAntennaBuilding(b.coveredBy, b);
    }

    public static int computeReward() {
        for (SkyScraper b : buildings) {
            if (b.coveredBy == null)
                return 0;
        }
        return REWARD;
    }

    public static int totalScore() {
        int result = 0;
        for (SkyScraper b : buildings) {
            result += scoreBuilding(b);
        }
        return result + computeReward();
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

    public static int distManhattan(Position x1, Position x2) {
        //|x1 - x2| + |y1 - y2|
        int distance = Math.abs(x1.x - x2.x) + Math.abs(x1.y - x2.y);
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
                N_REMAINING = N_BUILDINGS;
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
                    SkyScraper sky = new SkyScraper(l, c, x, y);
                    grid[x][y].skyscr = sky;
                    buildings.add(sky);
                }
                for (int j = 0; j < M_AVA_ANTENNAS; j++) {
                    Integer r, c;
                    r = stream.nextInt();
                    c = stream.nextInt();
                    piazzabili.add(new Antenna(r, c, j));

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

//=============================================================//
// ===================OUTPUT===================================//
// ============================================================//


    static ArrayList<SkyScraper> getBuildingsInRange(Position pos, int range) {

        ArrayList<SkyScraper> list = new ArrayList<>();
        int r_start = Math.max(0, pos.x - range + 1);
        int r_end = Math.min(WIDTH, pos.x + range + 1);
        int c_start = Math.max(0, pos.y - range + 1);
        int c_end = Math.min(HEIGTH, pos.y + range + 1);

        for(int r = r_start; r < r_end; r++) {
            for(int c = c_start; c < c_end; c++){
                if(grid[r][c].skyscr != null)
                    list.add(grid[r][c].skyscr);
            }
        }
        return list;
    }

    // Data la posizione pos dell'antenna a, calcolo il punteggio di quell'antenna rispetto a
    // tutti i building tali per cui lo score per la coppia (antenna,building) è migliore.
    static int computeHeuristic(Position pos, Antenna a) {

        List<SkyScraper> buildingsInRange = getBuildingsInRange(pos, a.range);

        int heuristicScore = -1;
        for (SkyScraper b : buildingsInRange) {
            if(b.coveredBy == null){
            int distance = distManhattan(new Position(b.X_COORD, b.Y_COORD), pos);
            if (distance > a.range)
                continue;
            Antenna tempAnt = new Antenna(a.range, a.connectionSpeed, a.id);
            tempAnt.placeAntenna(pos.x, pos.y);
            int score = scoreAntennaBuilding(tempAnt, b);
            if (b.coveredBy == null)
                heuristicScore = heuristicScore + scoreAntennaBuilding(a, b);
            else if (score > scoreAntennaBuilding(b.coveredBy, b))
                heuristicScore = heuristicScore + score; //better score
        }
            }
        return heuristicScore;
        //return new Random().nextInt()%256;
    }

    static class Position {
        public int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    static Map<Position, Integer> heuristicValues = new HashMap<>();

    public static Position pickBestPosition() {

        Position best = new Position(0, 0);
        Integer currentBestValue = -1;
        for (Position cur : heuristicValues.keySet()) {
            if (heuristicValues.get(cur) > currentBestValue) {
                currentBestValue = heuristicValues.get(cur);
                best = cur;
            }
        }

        return best;
    }


    public static void main(String[] args) {

        InputParsing parse = new InputParsing("data_scenarios_b_mumbai.in");

        System.out.println("lettura file ok");
        sortAntennas(piazzabili);
        System.out.println("antenne ordinate");
        List<Antenna> alreadyPlacedAntenna = new ArrayList<>();

        boolean solutionIsFound = false;
        int loop = 0;
        for (Antenna a : piazzabili) {
    
            if (solutionIsFound) break; //TODO se non si può piazzare nulla, attivare il flag
            System.out.println("antenna " + loop + " of " + piazzabili.size());
            loop++;
            for (int j = 0; j < WIDTH; j++) {
                for (int k = 0; k < HEIGTH; k++) {

                    if (grid[j][k].isFree()) { //free === return (cell.antenna != null)
                        Position currentPosition = new Position(j, k);
                        int score = computeHeuristic(currentPosition, a);
                        heuristicValues.put(currentPosition, score);

                    }
                }
            }

            Position bestOne = pickBestPosition();

            //POSIZIONA L'ANTENNA
            updateAndPlaceAntenna(a, bestOne);
            //da fare: updateCoveredBy(Position whereIllPlaceTheAntenna);

            for (SkyScraper b : buildings) {
                int distance = distManhattan(new Position(b.X_COORD, b.Y_COORD), bestOne);
                if (distance > a.range)
                    continue;
                if(b.coveredBy == null) {
                    b.coveredBy = a;
                    N_REMAINING--;
                }
                if(N_REMAINING < 1)
                    solutionIsFound = true;

            }

            //PULISCI e PREPARA PER NUOVA ITERAZIONE

            //ricordati che hai piazzato questa antenna
            alreadyPlacedAntenna.add(a);
            //reset heuristic values
            heuristicValues = new HashMap<>(); //garbage collector does the work
        loop++;
        }

        System.out.println("finito, non va un cazzo");
        //OUTPUT STAGE

        PrintWriter out = null;

        try {
            out = new PrintWriter("output-b.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        //numero antenne pia
        out.println(alreadyPlacedAntenna.size());
        out.flush();

        /*
        The output data has to be saved into a plain-text ASCII file.
    The first line of the output contains one integer number:
            • M0: the number of antennas placed in the grid
        The next M0
        lines contains two space-separated integer numbers:
        • idi: the id of the i
        th antenna to be placed
        • AX[idi]: the x coordinate of the ith antenna
    • AY [idi]: the y coordinate of the ith antenna
The antenna identifier is meant as the 0-based index of the antennas available
from the input data.
         */
        for (int i = 0; i < alreadyPlacedAntenna.size(); i++) {
            out.println(alreadyPlacedAntenna.get(i).id + "  " + alreadyPlacedAntenna.get(i).X_COORD + "  " + alreadyPlacedAntenna.get(i).Y_COORD);
            out.flush();
        }

    }
}




