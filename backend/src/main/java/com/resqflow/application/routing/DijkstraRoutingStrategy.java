package com.resqflow.application.routing;

import org.springframework.stereotype.Component;

import java.util.*;

@Component("DIJKSTRA")
public class DijkstraRoutingStrategy implements RoutingStrategy {

    @Override
    public RouteResult calculateRoute(String startKey, String endKey, RoutingGraph graph) {
        if (!graph.getNodes().containsKey(startKey) || !graph.getNodes().containsKey(endKey)) {
            return RouteResult.builder().success(false).build();
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, Double> travelTimes = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Map<String, Long> predRoads = new HashMap<>();
        
        // PriorityQueue ordered by travel time (weight of edge)
        PriorityQueue<NodeTime> pq = new PriorityQueue<>(Comparator.comparingDouble(nt -> nt.time));

        for (String nodeKey : graph.getNodes().keySet()) {
            distances.put(nodeKey, Double.MAX_VALUE);
            travelTimes.put(nodeKey, Double.MAX_VALUE);
        }

        distances.put(startKey, 0.0);
        travelTimes.put(startKey, 0.0);
        pq.add(new NodeTime(startKey, 0.0));

        while (!pq.isEmpty()) {
            NodeTime current = pq.poll();
            String u = current.key;

            if (u.equals(endKey)) break;

            if (current.time > travelTimes.get(u)) continue;

            for (GraphEdge edge : graph.getEdges(u)) {
                // Ignore blocked roads
                if ("BLOCKED".equalsIgnoreCase(edge.getStatus())) {
                    continue;
                }

                String v = edge.getTarget().getKey();
                double newTime = travelTimes.get(u) + edge.getTravelTime();
                
                if (newTime < travelTimes.get(v)) {
                    travelTimes.put(v, newTime);
                    distances.put(v, distances.get(u) + edge.getDistance());
                    predecessors.put(v, u);
                    predRoads.put(v, edge.getRoadId());
                    pq.add(new NodeTime(v, newTime));
                }
            }
        }

        if (travelTimes.get(endKey) == Double.MAX_VALUE) {
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
                .totalTravelTime(travelTimes.get(endKey))
                .success(true)
                .build();
    }

    private static class NodeTime {
        final String key;
        final double time;

        NodeTime(String key, double time) {
            this.key = key;
            this.time = time;
        }
    }
}
