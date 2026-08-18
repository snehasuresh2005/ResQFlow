package com.resqflow.application.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoutingTest {

    private RoutingGraph graph;
    private GraphNode n1;
    private GraphNode n2;
    private GraphNode n3;

    @BeforeEach
    public void setUp() {
        graph = new RoutingGraph();

        n1 = GraphNode.builder().key("DEPOT_1").type("DEPOT").id(1L).latitude(12.0).longitude(77.0).build();
        n2 = GraphNode.builder().key("ZONE_1").type("ZONE").id(1L).latitude(12.1).longitude(77.1).build();
        n3 = GraphNode.builder().key("SHELTER_1").type("SHELTER").id(1L).latitude(12.2).longitude(77.2).build();

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
    }

    @Test
    public void testDijkstraAndAStarRouting() {
        // Add two paths:
        // Path A: Depot_1 -> Zone_1 (distance 10, time 15 mins)
        // Path B: Zone_1 -> Shelter_1 (distance 15, time 20 mins)
        graph.addEdge("DEPOT_1", "ZONE_1", 10.0, 15.0, "OPEN", 1L);
        graph.addEdge("ZONE_1", "SHELTER_1", 15.0, 20.0, "OPEN", 2L);

        DijkstraRoutingStrategy dijkstra = new DijkstraRoutingStrategy();
        RouteResult resDijkstra = dijkstra.calculateRoute("DEPOT_1", "SHELTER_1", graph);

        assertTrue(resDijkstra.isSuccess());
        assertEquals(25.0, resDijkstra.getTotalDistance());
        assertEquals(35.0, resDijkstra.getTotalTravelTime());
        assertEquals(3, resDijkstra.getPath().size());

        AStarRoutingStrategy astar = new AStarRoutingStrategy();
        RouteResult resAStar = astar.calculateRoute("DEPOT_1", "SHELTER_1", graph);

        assertTrue(resAStar.isSuccess());
        assertEquals(25.0, resAStar.getTotalDistance());
        assertEquals(3, resAStar.getPath().size());
    }

    @Test
    public void testBlockedRoadsBypassed() {
        // Depot -> Zone path is BLOCKED
        graph.addEdge("DEPOT_1", "ZONE_1", 10.0, 15.0, "BLOCKED", 1L);
        
        // Alternative path: Depot -> Shelter (15km) -> Zone (5km)
        graph.addEdge("DEPOT_1", "SHELTER_1", 15.0, 20.0, "OPEN", 2L);
        graph.addEdge("SHELTER_1", "ZONE_1", 5.0, 8.0, "OPEN", 3L);

        DijkstraRoutingStrategy dijkstra = new DijkstraRoutingStrategy();
        RouteResult res = dijkstra.calculateRoute("DEPOT_1", "ZONE_1", graph);

        assertTrue(res.isSuccess());
        // Should take the longer open path instead of the shorter blocked highway
        assertEquals(20.0, res.getTotalDistance());
        assertEquals(28.0, res.getTotalTravelTime());
        assertEquals("SHELTER_1", res.getPath().get(1).getKey());
    }
}
