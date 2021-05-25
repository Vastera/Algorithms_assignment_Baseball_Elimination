/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashMap;

public class BaseballElimination {
    private final int teamNumber;
    private final String[] teamNames;
    private final int[] winTimes;
    private final int[] loseTimes;
    private final int[] remainingTimes;
    private final int[][] games;
    private HashMap<String, Integer> teamID;
    private String lastTeam;
    private SET<String> R;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(
            String filename) {
        if (filename == null)
            throw new IllegalArgumentException("input filename is null~");
        In in = new In(filename);
        teamNumber = Integer.parseInt(in.readLine());
        teamNames = new String[teamNumber];
        winTimes = new int[teamNumber];
        loseTimes = new int[teamNumber];
        remainingTimes = new int[teamNumber];
        games = new int[teamNumber][teamNumber];
        teamID = new HashMap<String, Integer>();

        int i = 0;
        String[] content;
        while (in.hasNextLine()) {
            content = in.readLine().split(" ");
            int k = 0;
            while (content[k].equals(""))
                k++;
            teamNames[i] = content[k++];
            teamID.put(teamNames[i], i);
            while (content[k].equals(""))
                k++;
            winTimes[i] = Integer.parseInt(content[k++]);
            while (content[k].equals(""))
                k++;
            loseTimes[i] = Integer.parseInt(content[k++]);
            while (content[k].equals(""))
                k++;
            remainingTimes[i] = Integer.parseInt(content[k++]);
            for (int j = 0; j < teamNumber; j++) {
                while (content[k].equals(""))
                    k++;
                games[i][j] = Integer.parseInt(content[k++]);
            }
            i++;
        }
    }


    // encode the vertex for games between i-th team and j-team
    private int encode(int i, int j) {
        return i * teamNumber + j + teamNumber;
    }

    private int[] decode(int v) {
        int i, j;
        i = (v - teamNumber) / teamNumber;
        j = (v - teamNumber) - i * teamNumber;
        return new int[] { i, j };
    }


    // number of teams
    public int numberOfTeams() {
        return teamNumber;
    }

    // all teams
    public Iterable<String> teams() {
        Queue<String> allTeams = new Queue<String>();
        for (int i = 0; i < teamNumber; i++)
            allTeams.enqueue(teamNames[i]);
        return allTeams;
    }

    // number of wins for given team
    public int wins(String team) {
        if (!teamID.containsKey(team))
            throw new IllegalArgumentException("no team named: " + team);
        int i = teamID.get(team);
        return winTimes[i];
    }

    // number of losses for given team
    public int losses(String team) {
        if (!teamID.containsKey(team))
            throw new IllegalArgumentException("no team named: " + team);
        int i = teamID.get(team);
        return loseTimes[i];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (!teamID.containsKey(team))
            throw new IllegalArgumentException("no team named: " + team);
        int i = teamID.get(team);
        return remainingTimes[i];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (!teamID.containsKey(team1))
            throw new IllegalArgumentException("no team named: " + team1);
        int i = teamID.get(team1);
        if (!teamID.containsKey(team2))
            throw new IllegalArgumentException("no team named: " + team2);
        int j = teamID.get(team2);
        return games[i][j];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (!teamID.containsKey(team))
            throw new IllegalArgumentException("no team named: " + team);
        if (team.equals(lastTeam))
            return R.size() != 0;
        else
            R = new SET<String>();
        lastTeam = team;
        int x = teamID.get(team);
        FlowNetwork flowNetwork = new FlowNetwork(teamNumber * teamNumber + teamNumber + 2);
        int s = teamNumber * teamNumber + teamNumber;
        int t = teamNumber * teamNumber + teamNumber + 1;
        int teamLeftWins = winTimes[x] + remainingTimes[x];
        for (int i = 0; i < teamNumber; i++) {
            if (x != i) {
                if (teamLeftWins < winTimes[i]) {
                    R.add(teamNames[i]);
                    return R.size() != 0;
                }
                else {
                    flowNetwork.addEdge(new FlowEdge(i, t, teamLeftWins - winTimes[i]));
                    for (int j = i + 1; j < teamNumber; j++) {
                        if (x != j && games[i][j] != 0) {
                            flowNetwork.addEdge(new FlowEdge(s, encode(i, j), games[i][j]));
                            flowNetwork.addEdge(new FlowEdge(encode(i, j), i, Integer.MAX_VALUE));
                            flowNetwork.addEdge(new FlowEdge(encode(i, j), j, Integer.MAX_VALUE));
                        }
                    }
                }
            }
        }
        // System.out.println(flowNetwork.toString());
        FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, s, t);
        int[] ij;
        int i;
        for (int v = teamNumber; v < teamNumber * teamNumber; v++) {
            if (fordFulkerson.inCut(v)) {
                ij = decode(v);
                R.add(teamNames[ij[0]]);
                R.add(teamNames[ij[1]]);
            }
        }
        return R.size() != 0;
    }

    // subset R of teams that eliminates given team;null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (isEliminated(team))
            return R;
        else
            return null;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }

    }
}
