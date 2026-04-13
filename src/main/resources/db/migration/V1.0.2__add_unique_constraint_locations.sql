ALTER TABLE IF EXISTS weather.locations
ADD CONSTRAINT unique_user_location
UNIQUE (name, user_id, latitude, longitude)