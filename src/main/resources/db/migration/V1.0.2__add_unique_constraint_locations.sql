ALTER TABLE IF EXISTS locations
ADD CONSTRAINT unique_user_location
UNIQUE (name, user_id, latitude, longitude)