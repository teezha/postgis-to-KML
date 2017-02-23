package postsqlsource;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.map.*;
import com.esri.toolkit.overlays.DrawingCompleteEvent;
import com.esri.toolkit.overlays.DrawingCompleteListener;
import com.esri.toolkit.overlays.DrawingOverlay;
import com.rob.arj.utils.PostGISGeometryEngine;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.postgis.PGgeometry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable, DrawingCompleteListener, MapEventListener {


    /**
     * ===========================================================
     * Hooks for the UI AKA hot injection of controls and methods
     * ===========================================================
     */
    @FXML
    Pane root;

    @FXML
    SwingNode swingNode;

    /**
     * ===========================================================
     * Common layers and overlays that will be added to the map
     * Required imports:
     * import com.esri.map.ArcGISTiledMapServiceLayer;
     * import com.esri.map.GraphicsLayer;
     * import com.esri.toolkit.overlays.DrawingOverlay;
     * ===========================================================
     */
    GraphicsLayer graphicLayer;
    DrawingOverlay drawingOverlay;
    ArcGISTiledMapServiceLayer baseLayer;
    static final String baseURL = "http://maps.gov.bc.ca/arcserver/rest/services/Province/albers_cache/MapServer";


    /**
     * ===========================================================
     * Basic Symbology for points line and polygons
     * import com.esri.core.symbol.MarkerSymbol;
     * import com.esri.core.symbol.SimpleFillSymbol;
     * import com.esri.core.symbol.SimpleLineSymbol;
     * import com.esri.core.symbol.SimpleMarkerSymbol;
     * ===========================================================
     */
    final SimpleLineSymbol symPolyLine = new SimpleLineSymbol(
            new Color(0, 0, 150), 3);
    final SimpleLineSymbol symPolyLineFree = new SimpleLineSymbol(
            new Color(200, 0, 0), 3);
    final SimpleLineSymbol symLine = new SimpleLineSymbol(new Color(0,
            0, 150), 3);
    final SimpleFillSymbol symRectangle = new SimpleFillSymbol(
            new Color(200, 0, 0, 60), new SimpleLineSymbol(
            new Color(200, 0, 0), 3));
    final SimpleLineSymbol symDottedLine = new SimpleLineSymbol(Color.BLACK, 2);
    final SimpleFillSymbol symPolygon = new SimpleFillSymbol(new Color(
            0, 0, 0, 80), symDottedLine);
    final SimpleMarkerSymbol pointSym = new SimpleMarkerSymbol(
            Color.RED, 15, SimpleMarkerSymbol.Style.DIAMOND);


    ConnectionInfo connectionInfo;

    /**
     * ===========================================================
     * Main map for the application
     * Required imports:
     * import com.esri.map.JMap;
     * ===========================================================
     */
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

    public void connect() throws Exception {
        ConnectionInfoDialog connectionInfoDialog = new ConnectionInfoDialog();
        connectionInfoDialog.showAndWait();

        if (connectionInfoDialog.getConnectionInfo().isValid()) {
            connectionInfo = connectionInfoDialog.getConnectionInfo();
        }

    }

    public void close() {
        System.exit(0);
    }


    public void load() {
        if (connectionInfo != null && connectionInfo.isValid()) {


            int id;
            String name, description;
            PGgeometry pgGeom;
            Geometry arjGeom;
            Graphic tmpGraphic;
            Map<String, Object> attributes;


            try {
                Statement sql = connectionInfo.getConnection().createStatement();
                ResultSet res = sql.executeQuery("SELECT id, name, description, geom from gist_8010_m04.points_of_interest");

                while (res.next()) {
                    id = res.getInt("id");
                    name = res.getString("name");
                    description = res.getString("description");
                    pgGeom = (PGgeometry) res.getObject("geom");
                    arjGeom = PostGISGeometryEngine.esriPointFromPostGISPoint((org.postgis.Point) pgGeom.getGeometry());

                    attributes = this.mapFromAttributes(id, name, description);

                    tmpGraphic = new Graphic(arjGeom, pointSym, attributes);
                    graphicLayer.addGraphic(tmpGraphic);

                }


            } catch (SQLException e) {
                e.printStackTrace();
            }


        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Connect to db first");
            alert.showAndWait();
        }

    }

    public void clear() {

    }

    public void point() {


        try {
            /** ===========================================================
             * import org.w3c.dom.Document;
             * import org.w3c.dom.Element;
             * =========================================================== */
            Document kmlDoc;
            kmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element kml, document;

            kml = kmlDoc.createElement("kml");
            kml.setAttribute("xmlns", "http:*www.opengis.net/kml/2.2");
            kmlDoc.appendChild(kml);

            document = kmlDoc.createElement("Document");
            kml.appendChild(document);

            Element placemark, name, description, kPoint, coordinates;



            SpatialReference albers = SpatialReference.create(3005);
            SpatialReference googleEarth = SpatialReference.create(4326);


            int[] graphicIDs = graphicLayer.getGraphicIDs();
            Graphic tmpGraphic;

            Point albersPoint;
            Point googlePoint;
            for (int i =0; i< graphicIDs.length; i++) {
                tmpGraphic = graphicLayer.getGraphic(graphicIDs[i]);

                if (tmpGraphic.getGeometry().getType() == Geometry.Type.POINT) {


                    placemark = kmlDoc.createElement("placemark");
                    name = kmlDoc.createElement("name");
                    description = kmlDoc.createElement("description");
                    kPoint = kmlDoc.createElement("kPoint");
                    coordinates = kmlDoc.createElement("coordinates");


                    albersPoint = (Point) tmpGraphic.getGeometry();
                    googlePoint = (Point) GeometryEngine.project(albersPoint,albers,googleEarth);

                    coordinates.setTextContent(
                            String.format("%s,%s", googlePoint.getX(), googlePoint.getY())
                    );

                    document.appendChild(placemark);
                    document.appendChild(name);
                    document.appendChild(description);
                    document.appendChild(kPoint);
                    kPoint.appendChild(coordinates);

                    name.setTextContent(tmpGraphic.getAttributeValue("name").toString());
                    description.setTextContent(tmpGraphic.getAttributeValue("description").toString());

                }


            }


            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("H:/var/gist/8010/"));
            File kmlFile = fileChooser.showSaveDialog(null);
            if (kmlFile != null) {

                saveDomAsXmlOnDisc(kmlDoc,kmlFile);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void line() {

    }

    public void polygon() {


    }

    /**
     * =================================================
     * Convert raw attribute values to a Java Map
     * ==================================================
     */

    public static Map<String, Object> mapFromAttributes(Integer pkey,
                                                        String name, String description) {
        Map<String, Object> list = new HashMap<String, Object>();
        list.put("pkey", pkey);
        list.put("name", name);
        list.put("description", description);
        return list;
    }

    /**
     * ===========================================================
     * import org.w3c.dom.Document;
     * import javax.xml.transform.OutputKeys;
     * import javax.xml.transform.Result;
     * import javax.xml.transform.Source;
     * import javax.xml.transform.Transformer;
     * import javax.xml.transform.TransformerFactory;
     * import javax.xml.transform.dom.DOMSource;
     * import javax.xml.transform.stream.StreamResult;
     * ===========================================================
     */

    public static void saveDomAsXmlOnDisc(Document doc, File file) {
        try {
            /** ===========================================================
             * set up a new source for transforming
             * =========================================================== */
            Source source = new DOMSource(doc);
            /** ===========================================================
             * set up a new target and in this case a file on disc
             * ===========================================================
             */
            Result result = new StreamResult(new PrintStream(file));
            /** ===========================================================
             * make a new transformer to go from DOM to DISC
             * ===========================================================
             */
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            /** ================================================================
             * indent the XML --> more human readable, parser ignore whitespace
             * ================================================================
             */
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty("{http:*xml.apache.org/xslt}indent-amount", "2");
            /** ===========================================================
             * write the DOM in memory to the file on disc
             * ===========================================================
             */
            xformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end of saveDomAsXmlOnDisc

}
