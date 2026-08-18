-- Create Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Depots (Warehouses) Table
CREATE TABLE depots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    capacity DOUBLE PRECISION NOT NULL
);

-- Create Emergency Zones Table
CREATE TABLE emergency_zones (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    severity VARCHAR(20) NOT NULL,
    population_affected INT NOT NULL
);

-- Create Shelters Table
CREATE TABLE shelters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    capacity INT NOT NULL,
    current_occupancy INT DEFAULT 0
);

-- Create Resources Table (Single Table Inheritance)
CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL, -- FOOD, WATER, MEDICAL, SHELTER, EQUIPMENT
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20) NOT NULL,
    depot_id BIGINT REFERENCES depots(id),
    expiry_date DATE,
    priority INT DEFAULT 0,
    weight_per_unit DOUBLE PRECISION NOT NULL,
    volume_per_unit DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT DEFAULT 0, -- For optimistic locking
    -- Specific resource properties
    temp_constraint DOUBLE PRECISION, -- Medical
    storage_requirement VARCHAR(100), -- Food
    is_reusable BOOLEAN DEFAULT FALSE -- Equipment
);

-- Create Vehicles Table (Single Table Inheritance)
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    registration_number VARCHAR(50) UNIQUE NOT NULL,
    capacity_weight DOUBLE PRECISION NOT NULL,
    capacity_volume DOUBLE PRECISION NOT NULL,
    fuel_level DOUBLE PRECISION NOT NULL,
    current_latitude DOUBLE PRECISION NOT NULL,
    current_longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL, -- TRUCK, VAN, AMBULANCE, BOAT
    version BIGINT DEFAULT 0 -- For optimistic locking
);

-- Create Drivers Table
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    vehicle_id BIGINT REFERENCES vehicles(id),
    status VARCHAR(20) NOT NULL
);

-- Create Volunteers Table
CREATE TABLE volunteers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    assigned_task VARCHAR(255)
);

-- Create Emergency Requests Table
CREATE TABLE emergency_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(50) UNIQUE NOT NULL,
    emergency_zone_id BIGINT REFERENCES emergency_zones(id) NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    number_affected INT NOT NULL,
    deadline TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Resource Requirements Table
CREATE TABLE resource_requirements (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT REFERENCES emergency_requests(id) ON DELETE CASCADE,
    resource_type VARCHAR(50) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20) NOT NULL
);

-- Create Roads Table (Graph Edges)
CREATE TABLE roads (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_node_type VARCHAR(20) NOT NULL, -- DEPOT, ZONE, SHELTER, INTERSECTION
    start_node_id BIGINT NOT NULL,
    end_node_type VARCHAR(20) NOT NULL,
    end_node_id BIGINT NOT NULL,
    distance DOUBLE PRECISION NOT NULL,
    travel_time DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL -- OPEN, BLOCKED, RESTRICTED
);

-- Create Routes Table
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    total_distance DOUBLE PRECISION NOT NULL,
    total_time DOUBLE PRECISION NOT NULL
);

-- Create Route Nodes Table (Ordered sequence of nodes for a route)
CREATE TABLE route_nodes (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT REFERENCES routes(id) ON DELETE CASCADE,
    sequence_order INT NOT NULL,
    node_type VARCHAR(20) NOT NULL,
    node_id BIGINT NOT NULL
);

-- Create Missions Table
CREATE TABLE missions (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT REFERENCES emergency_requests(id),
    vehicle_id BIGINT REFERENCES vehicles(id),
    driver_id BIGINT REFERENCES drivers(id),
    route_id BIGINT REFERENCES routes(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Allocations Table
CREATE TABLE allocations (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT REFERENCES emergency_requests(id) NOT NULL,
    resource_id BIGINT REFERENCES resources(id) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Reservations Table (Concurrency locks)
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT REFERENCES emergency_requests(id) NOT NULL,
    resource_id BIGINT REFERENCES resources(id) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- Create Audit Logs Table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Disaster Simulations Table
CREATE TABLE disaster_simulations (
    id BIGSERIAL PRIMARY KEY,
    scenario VARCHAR(50) NOT NULL,
    requests_processed INT NOT NULL,
    requests_fulfilled INT NOT NULL,
    critical_fulfillment_rate DOUBLE PRECISION NOT NULL,
    avg_response_time DOUBLE PRECISION NOT NULL,
    vehicle_utilization DOUBLE PRECISION NOT NULL,
    resource_wastage DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Outbox Events Table (For Transactional Outbox Pattern)
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE
);
