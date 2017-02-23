package postsqlsource;

import com.esri.map.*;
import com.esri.toolkit.overlays.DrawingCompleteEvent;
import com.esri.toolkit.overlays.DrawingCompleteListener;
import com.esri.toolkit.overlays.DrawingOverlay;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, DrawingCompleteListener, MapEventListener {


    /** ===========================================================
     * Hooks for the UI AKA hot injection of controls and methods
     * =========================================================== */
    @FXML
    Pane root;

    @FXML
    SwingNode swingNode;

    /** ===========================================================
     * Common layers and overlays that will be added to the map
     * Required imports:
     * import com.esri.map.ArcGISTiledMapServiceLayer;
     * import com.esri.map.GraphicsLayer;
     * import com.esri.toolkit.overlays.DrawingOverlay;
     * =========================================================== */
    GraphicsLayer graphicLayer;
    DrawingOverlay drawingOverlay;
    ArcGISTiledMapServiceLayer baseLayer;
    static final String baseURL = "http://maps.gov.bc.ca/arcserver/rest/services/Province/albers_cache/MapServer";



    /** ===========================================================
     * Main map for the application
     * Required imports:
     *  import com.esri.map.JMap;
     * =========================================================== */
    JMap map;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        /** ===============================================================
         * Make a SWING JMap and a SwingNode then add the map to the node
         * =============================================================== */
        map = new JMap();
        swingNode.setContent(map);


        /** ===============================================================
         * Make a tiled layer and add it to the map
         * =============================================================== */
        baseLayer = new ArcGISTiledMapServiceLayer(baseURL);
        map.getLayers().add(baseLayer);

        /** ===============================================================
         * Make a graphics layer and add it to the map
         * =============================================================== */
        graphicLayer = new GraphicsLayer();
        map.getLayers().add(graphicLayer);

        /** ===============================================================
         * Make a drawing overlay and add it to the map
         * =============================================================== */
        drawingOverlay = new DrawingOverlay();
        map.addMapOverlay(drawingOverlay);
        drawingOverlay.addDrawingCompleteListener(this);
        map.addMapEventListener(this);
    }

    @Override
    public void drawingCompleted(DrawingCompleteEvent event) {

    }

    @Override
    public void mapReady(MapEvent mapEvent) {


        double lat, lon;
        lat = 49.060369722746124;
        lon = -122.87796020507814;
        int zoomLevel = 10;

        /** =======================================
         * tell the map to zoom to a lat lon
         * ======================================= */
        map.zoomTo(lat, lon, zoomLevel);
    }

    @Override
    public void mapExtentChanged(MapEvent mapEvent) {

    }

    @Override
    public void mapDispose(MapEvent mapEvent) {

    }
}
