package com.resqflow.application.routing;

import com.resqflow.common.utils.DistanceUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("ASTAR")
public class AStarRoutingStrategy implements RoutingStrategy {

    // Assumed average vehicle speed: 50 km/h. Heuristic converts distance to minutes.
    private static final double AVERAGE_SPEED_KMH = 50.0;

    private double calculateHeuristic(GraphNode node, GraphNode target) {
        double distanceKm = DistanceUtils.calculateDistance(
                node.getLatitude(), node.getLongitude(),
                target.getLatitude(), target.getLongitude()
        );
        // Time in minutes = (distanceKm / speedKmh) * 60
        return (distanceKm / AVERAGE_SPEED_KMH) * 60.0;
    }

    @Override
    public RouteResult calculateRoute(String startKey, String endKey, RoutingGraph graph) {
        if (!graph.getNodes().containsKey(startKey) || !graph.getNodes().containsKey(endKey)) {
            return RouteResult.builder().success(false).build();
        }

        GraphNode targetNode = graph.getNode(endKey);

        Map<String, Double> gScores = new HashMap<>(); // actual travel time from start
        Map<String, Double> fScores = new HashMap<>(); // estimated total travel time
        Map<String, Double> distances = new HashMap<>(); // actual distance from start
        Map<String, String> predecessors = new HashMap<>();
        Map<String, Long> predRoads = new HashMap<>();

        PriorityQueue<NodeFScore> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));

        for (String nodeKey : graph.getNodes().keySet()) {
            gScores.put(nodeKey, Double.MAX_VALUE);
            fScores.put(nodeKey, Double.MAX_VALUE);
        }

        gScores.put(startKey, 0.0);
        distances.put(startKey, 0.0);
        double initialH = calculateHeuristic(graph.getNode(startKey), targetNode);
        fScores.put(startKey, initialH);
        
        pq.add(new NodeFScore(startKey, initialH));

        while (!pq.isEmpty()) {
            NodeFScore current = pq.poll();
            String u = current.key;

            if (u.equals(endKey)) break;

            if (current.fScore > fScores.get(u)) continue;

            for (GraphEdge edge : graph.getEdges(u)) {
                if ("BLOCKED".equalsIgnoreCase(edge.getStatus())) {
                    continue;
                }

                String v = edge.getTarget().getKey();
                double tentativeG = gScores.get(u) + edge.getTravelTime();

                if (tentativeG < gScores.get(v)) {
                    gScores.put(v, tentativeG);
                    distances.put(v, distances.get(u) + edge.getDistance());
                    predecessors.put(v, u);
                    predRoads.put(v, edge.getRoadId());

                    double h = calculateHeuristic(edge.getTarget(), targetNode);
                    double f = tentativeG + h;
                    fScores.put(v, f);

                    pq.add(new NodeFScore(v, f));
                }
            }
        }

        if (gScores.get(endKey) == Double.MAX_VALUE) {
            return RouteResult.builder().success(false).build();
        }

        // Reconstruct path
        List<GraphNode> path = new ArrayList<>();
        List<Long> roadIds = new ArrayList<>();
        String curr = endKey;

        while (curr != null) {
            path.add(0, graph.getNode(curr));
            String pred = predecessors.get(curr);
            if (pred != null) {
                roadIds.add(0, predRoads.get(curr));
            }
            curr = pred;
        }

        return RouteResult.builder()
                .path(path)
                .roadIds(roadIds)
                .totalDistance(distances.get(endKey))
                .totalTravelTime(gScores.get(endKey))
                .success(true)
                .build();
    }

    private static class NodeFScore {
        final String key;
        final double fScore;

        NodeFScore(String key, double fScore) {
            this.key = key;
            this.fScore = fScore;
        }
    }
}
