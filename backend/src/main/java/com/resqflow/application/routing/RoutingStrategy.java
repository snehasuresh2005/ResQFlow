package com.resqflow.application.routing;

public interface RoutingStrategy {
    RouteResult calculateRoute(String startKey, String endKey, RoutingGraph graph);
}
