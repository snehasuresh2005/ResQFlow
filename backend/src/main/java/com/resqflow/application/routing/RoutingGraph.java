package com.resqflow.application.routing;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class RoutingGraph {
    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final Map<String, List<GraphEdge>> adjacencyList = new HashMap<>();

    public void addNode(GraphNode node) {
        nodes.put(node.getKey(), node);
        adjacencyList.putIfAbsent(node.getKey(), new ArrayList<>());
    }

    public void addEdge(String fromKey, String toKey, double distance, double travelTime, String status, Long roadId) {
        GraphNode targetNode = nodes.get(toKey);
        if (targetNode == null) return;

        GraphEdge edge = GraphEdge.builder()
                .target(targetNode)
                .distance(distance)
                .travelTime(travelTime)
                .status(status)
                .roadId(roadId)
                .build();

        adjacencyList.get(fromKey).add(edge);
    }

    public GraphNode getNode(String key) {
        return nodes.get(key);
    }

    public List<GraphEdge> getEdges(String key) {
        return adjacencyList.getOrDefault(key, new ArrayList<>());
    }
}
