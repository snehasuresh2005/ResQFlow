-- Indexes for Emergency Requests
CREATE INDEX idx_emergency_requests_priority ON emergency_requests(priority);
CREATE INDEX idx_emergency_requests_status ON emergency_requests(status);
CREATE INDEX idx_emergency_requests_created_at ON emergency_requests(created_at);

-- Indexes for Resources
CREATE INDEX idx_resources_type ON resources(resource_type);
CREATE INDEX idx_resources_depot_id ON resources(depot_id);
CREATE INDEX idx_resources_expiry_date ON resources(expiry_date);

-- Indexes for Vehicles
CREATE INDEX idx_vehicles_status ON vehicles(status);

-- Indexes for Missions
CREATE INDEX idx_missions_status ON missions(status);

-- Index for Outbox processing
CREATE INDEX idx_outbox_processed ON outbox_events(processed) WHERE processed = FALSE;
