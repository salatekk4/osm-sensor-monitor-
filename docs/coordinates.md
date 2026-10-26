# Geographic coordinate contract (first migration stage)

Geographic data uses WGS84 decimal degrees. Legacy argument and CSV column order is x=longitude, y=latitude. GeoPosition constructor order is explicitly latitude, longitude. Radius is in metres. Prefer GeoPosition at new geographic boundaries.

SensorValidator keeps its existing signatures and error keys X/Y. It now validates longitude [-180,180], latitude [-90,90], finite numeric fields and nonnegative radius. createSensor validates input itself. ID zero remains allowed for compatibility. GeoPosition is immutable and validates its constructor.

Latitude at the poles is valid WGS84 data, but not supported by the usual Web Mercator tile map. GeoPosition.isWebMercatorSupported exposes this separately; the view must reject unsupported locations or report them, not silently change stored coordinates.

## Not yet migrated

Sensor constructors/setters, SensorStorage and CSV loading still retain their existing behavior and can bypass SensorValidator. This commit does NOT establish validation on every write path. Use the validator or GeoPosition explicitly at geographic boundaries until the follow-up migration.

Existing Sensor x/y APIs, CSV header and old demo data are not rewritten. Old planar coordinates must NOT be automatically relabelled as real-world locations, even when numbers happen to fall within valid ranges. Keep backups and supply explicitly verified geographic data. Old UI labels should be updated to Longitude / Latitude / Radius (m).

MapViewport remains a PLANAR lab component. Do NOT pass longitude/latitude degrees and a radius in metres to its fitTo/toRenderData methods: it adds radius directly to coordinate values. Geographic rendering requires a separate adapter sharing the base map's projection, zoom and viewport. This commit does not implement tile positioning or geographic coverage circles.

## Manual checks after approval and push

Compile using the project's usual Java setup:

```sh
javac -d target/classes $(find src -name '*.java')
java -cp target/classes ru.miet.osmsensors.model.GeoCheck
java -cp target/classes ru.miet.osmsensors.model.ConcurrencyCheck
```

GeoCheck throws AssertionError on failure. These checks have not been run as part of the GitHub edit. Also test the existing form and CSV scenarios: invalid geographic input previously accepted by the form is now rejected deliberately.
