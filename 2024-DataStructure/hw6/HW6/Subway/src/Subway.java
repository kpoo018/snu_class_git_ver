import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Subway {
    private static Map<String, List<Station>> stationMap = new HashMap<>();
    private static Map<String, Station> stationIDMap = new HashMap<>();
    private static Map<String, List<Pair>> travelTimeMap = new HashMap<>();
    private static Map<String, Integer> transferTimeMap = new HashMap<>();

    public static void main(String[] args) {

        String dataFilePath = args[0];

        try {
            File dataFile = new File(dataFilePath);
            Scanner scanner = new Scanner(dataFile);
            loadData(scanner);
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("파일이 존재하지 않습니다");
        }

        InputStream inputStream = System.in;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));


        try {
            String line;
            while (true) {
                line = reader.readLine();
                if ("QUIT".equalsIgnoreCase(line.trim())) {
                    break;
                }

                String[] stations = line.split(" ");
                if (stations.length < 2) continue;
                String start = stations[0];
                String end = stations[1];

                // 최단 경로 찾기
                findShortestPath(start, end);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void findShortestPath(String startName, String endName) {

        if (!stationMap.containsKey(startName) || !stationMap.containsKey(endName)) {
            System.out.println("입력한 역이 존재하지 않습니다.");
            return;
        }

        List<Station> startStations = stationMap.get(startName);
        List<Station> endStations = stationMap.get(endName);

        String startID = new String();
        String endID = new String();

        String resultEndID = new String();
        Map<String, Pair> resultPreviousStations = new HashMap<>();
        Integer resultTotalTime = Integer.MAX_VALUE;

        for(Station startStation : startStations){
            for(Station endStation : endStations){

                startID=startStation.id;
                endID=endStation.id;

                // 다익스트라 알고리즘을 위한 데이터 구조 준비
                Map<String, Integer> distances = new HashMap<>();
                Map<String, Pair> previousStations = new HashMap<>();
                PriorityQueue<Pair> pq = new PriorityQueue<>(Comparator.comparingInt(pair -> pair.time));

                // 초기화
                for (String stationID : travelTimeMap.keySet()) {
                    distances.put(stationID, Integer.MAX_VALUE);
                    previousStations.put(stationID, null);
                }
                distances.put(startID, 0);
                pq.add(new Pair(startID, 0));

                // 다익스트라 알고리즘 실행
                while (!pq.isEmpty()) {
                    Pair current = pq.poll();
                    String currentID = current.station;

                    // 도착역에 도달한 경우
                    if (currentID.equals(endID)) break;

                    // 인접한 역들 탐색 (travelTimeMap의 key는 역ID임)
                    for(Station other : stationMap.get(stationIDMap.get(currentID).name)) {
                        if (travelTimeMap.containsKey(currentID)) {
                            for (Pair neighbor : travelTimeMap.get(other.id)) {
                                int newDist = distances.get(currentID) + neighbor.time;
                                // 환승 시간 처리
                                if (!currentID.equals(other.id)) {
                                    newDist += transferTimeMap.getOrDefault(stationIDMap.get(currentID).name, 5);
                                }

                                if (newDist < distances.get(neighbor.station)) {
                                    distances.put(neighbor.station, newDist);
                                    previousStations.put(neighbor.station, new Pair(currentID, !currentID.equals(other.id) ? 1 : 0));
                                    pq.add(new Pair(neighbor.station, newDist));
                                }
                            }
                        }
                    }
                }

                if(distances.get(endID) < resultTotalTime){
                    resultEndID=endID;
                    resultTotalTime=distances.get(endID);
                    resultPreviousStations=previousStations;
                }
            }
        }

        // 최단 경로 출력
        printPath(startID, resultEndID, resultPreviousStations, resultTotalTime);
    }
    private static void printPath(String start, String end, Map<String, Pair> previousStations, int totalTime) {
        Pair endPair = new Pair(end,0);
        List<Pair> path = new ArrayList<>();
        for (Pair station = endPair; station != null; station = previousStations.get(station.station)) {
            path.add(station);
        }
        Collections.reverse(path); // 경로 역순 정리

        // 환승역을 []로 감싸기
        StringBuilder resultPath = new StringBuilder();
        for (Pair station : path) {
            if (station.time==1) {
                resultPath.append("[").append(stationIDMap.get(station.station).name).append("] ");
            } else {
                resultPath.append(stationIDMap.get(station.station).name).append(" ");
            }
        }

        System.out.println(resultPath.toString().trim()); // 경로 출력
        System.out.println(totalTime); // 소요 시간 출력
    }


    private static void loadData(Scanner scanner) {

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) break; // 빈 줄 처리
            String[] parts = line.split(" ");
            String id = parts[0]; // 고유번호
            String name = parts[1]; // 역 이름
            String lineNumber = parts[2]; // 노선 번호


            if(stationMap.containsKey(name)){
                transferTimeMap.put(name,5);
            }else{
                stationMap.put(name, new ArrayList<>());
            }
            stationMap.get(name).add(new Station(id, name, lineNumber));

            stationIDMap.put(id, new Station(id, name, lineNumber));

        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) break; // 빈 줄 처리
            String[] parts = line.split(" ");
            String fromStation = parts[0];
            String toStation = parts[1];
            int travelTime = Integer.parseInt(parts[2]); // 소요 시간

            if(!travelTimeMap.containsKey(fromStation)){
                travelTimeMap.put(fromStation, new ArrayList<>());
            }

            travelTimeMap.get(fromStation).add(new Pair(toStation, travelTime));

        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");
            String name = parts[0];
            int transferTime = Integer.parseInt(parts[1]); // 소요 시간

            transferTimeMap.put(name,transferTime);

        }

    }


    static class Station {
        String id;
        String name;
        String lineNumber;

        Station(String id, String name, String lineNumber) {
            this.id = id;
            this.name = name;
            this.lineNumber = lineNumber;
        }
    }

    static class Pair {
        String station;
        int time;

        Pair(String station, int time) {
            this.station = station;
            this.time = time;

        }
    }

}
